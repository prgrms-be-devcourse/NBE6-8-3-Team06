"use client"
import { ImageWithFallback } from "@/components/ImageWithFallback";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Separator } from "@/components/ui/separator";
import { ArrowLeft, BookOpen, Building, Calendar, Globe, Heart, Plus, Star, ThumbsDown, ThumbsUp } from "lucide-react";
import { useRouter } from "next/navigation";
import { use, useEffect, useState } from "react";
import { BookDetailDto, fetchBookDetail, ReviewResponseDto, addToMyBooks, ReadState } from "@/types/book";
import { useReview, useReviewRecommend } from "@/app/_hooks/useReview";
import { useTheme } from "next-themes";
import { useAuth } from "@/app/_hooks/auth-context";
import { toast } from "sonner";

export default function page({params}:{params:Promise<{bookId:string}>}){
    const {bookId:bookIdStr} = use(params);
    const bookId = parseInt(bookIdStr);
    
    const [bookDetail, setBookDetail] = useState<BookDetailDto | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isInMyBooks, setIsInMyBooks] = useState(false);
    const router = useRouter();
    const reviewApi = useReview(bookId);
    const reviewRecommendApi = useReviewRecommend();
    const [tabState, setTabState] = useState("description");
    const [reviewPage, setReviewPage] = useState(0);
    const {theme} = useTheme();
    const { isLoggedIn } = useAuth();
    
    const loadBookDetail = async () => {
      try {
        setLoading(true);
        const detail = await fetchBookDetail(bookId);
        setBookDetail(detail);
      } catch (err) {
        setError(err instanceof Error ? err.message : '책 정보를 불러오는데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };

    const loadReviews = async (page: number = 0) => {
      try {
        const detail = await reviewApi.getReviews(page);
        setBookDetail({...bookDetail!, reviews: detail});
        setReviewPage(page);
      } catch (err) {
        console.error('리뷰 로드 실패:', err);
      } 
    }

    useEffect(() => {
      loadBookDetail();
    }, [bookId]);
    
    const onNavigate = (path: string) => {
      router.push(path);
    };
    
    const onAddToMyBooks = async (bookId: number) => {
      if (!isLoggedIn) {
        toast.error("로그인을 해 주세요.", {
          action:{
            label:"이동",
            onClick:()=>{onNavigate("/login")}
          }
        });
        return;
      }
      
      try {
        await addToMyBooks(bookId);
        setBookDetail(prev => prev ? { ...prev, readState: ReadState.WISH } : null);
        setIsInMyBooks(true);
      } catch (error) {
        console.error('내 목록에 추가 실패:', error);
      }
    };
    
    if (loading) {
      return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="text-center">
            <p>책 정보를 불러오는 중...</p>
          </div>
        </div>
      );
    }
    
    if (error) {
      return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="text-center">
            <p className="text-red-500 mb-4">{error}</p>
            <Button onClick={() => onNavigate('/books')} className="mt-4">
              책 목록으로 돌아가기
            </Button>
          </div>
        </div>
      );
    }
    
    if (!bookDetail) {
      return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="text-center">
            <p>책을 찾을 수 없습니다.</p>
            <Button onClick={() => onNavigate('/books')} className="mt-4">
              책 목록으로 돌아가기
            </Button>
          </div>
        </div>
      );
    }

  const renderStars = (rating: number) => {
    return [...Array(5)].map((_, i) => (
      <Star
        key={i}
        className={`h-4 w-4 ${
          i < Math.floor(rating) 
            ? 'fill-yellow-400 text-yellow-400' 
            : 'text-gray-300'
        }`}
      />
    ));
  };

  const handleAddToMyBooks = () => {
    onAddToMyBooks(bookDetail.id);
  };

  const handleRecommend = async(review:ReviewResponseDto, recommend:boolean)=>{
    if (!isLoggedIn){
      toast.error("로그인을 해주세요.", {
        action: {
          label:"이동",
          onClick: ()=>{onNavigate("/login")}
        }
      })
      return;
    }
    // 업데이트가 늦어질 경우 대비해서 프론트에서 먼저 적용
    const reviews = bookDetail.reviews.data.map((r)=>{
      if (r.id === review.id){
        let likeCount = r.likeCount;
        let dislikeCount = r.dislikeCount
        if (r.isRecommended === null){
          if (recommend){
            likeCount++;
          }else{
            dislikeCount++;
          }
          return {...r, isRecommended:recommend, likeCount:likeCount, dislikeCount:dislikeCount}
        }else if (r.isRecommended === recommend){
          if (recommend){
            likeCount--;
          }else{
            dislikeCount--;
          }
          return {...r, isRecommended:null, likeCount:likeCount, dislikeCount:dislikeCount}
        }else{
          if (recommend){
            likeCount++;
            dislikeCount--;
          }else{
            likeCount--;
            dislikeCount++;
          }
          return {...r, isRecommended:recommend, likeCount:likeCount, dislikeCount:dislikeCount}
        }
      }
      return r;
    });
    setBookDetail({...bookDetail, reviews: {...bookDetail.reviews, data:  reviews}});
    // 백엔드에서 업데이트
    if (review.isRecommended === null){
      await reviewRecommendApi.createReviewRecommend(review.id, recommend);
    }else if (review.isRecommended === recommend){
      await reviewRecommendApi.deleteReviewRecommend(review.id);
    }else{
      await reviewRecommendApi.modifyReviewRecomend(review.id, recommend);
    }
    await loadReviews(reviewPage);
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* 뒤로가기 버튼 */}
      <Button 
        variant="ghost" 
        onClick={() => onNavigate('/books')}
        className="mb-6"
      >
        <ArrowLeft className="h-4 w-4 mr-2" />
        책 목록으로 돌아가기
      </Button>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* 책 기본 정보 */}
        <div className="lg:col-span-1">
          <Card>
            <CardContent className="p-6">
              <div className="text-center mb-6">
                <ImageWithFallback
                  src={bookDetail.imageUrl || `https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=200&h=300&fit=crop&crop=center&sig=${bookDetail.id}`}
                  alt={bookDetail.title}
                  className="w-48 h-72 object-cover rounded mx-auto mb-4"
                />
                <h1 className="text-2xl mb-2">{bookDetail.title}</h1>
                <p className="text-lg text-muted-foreground mb-4">{bookDetail.authors.join(', ')}</p>
                
                <div className="flex items-center justify-center space-x-1 mb-2">
                  {renderStars(bookDetail.avgRate)}
                  <span className="text-lg ml-2">{bookDetail.avgRate.toFixed(1)}</span>
                </div>
                <p className="text-sm text-muted-foreground mb-6">
                  평균 평점
                </p>

                <Badge className="mb-4">{bookDetail.categoryName}</Badge>
              </div>

              <Separator className="mb-6" />

              <div className="space-y-4">
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">출간일</span>
                  <span className="text-sm flex items-center">
                    <Calendar className="h-4 w-4 mr-1" />
                    {new Date(bookDetail.publishedDate).toLocaleDateString('ko-KR')}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">페이지</span>
                  <span className="text-sm flex items-center">
                    <BookOpen className="h-4 w-4 mr-1" />
                    {bookDetail.totalPage}쪽
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">출판사</span>
                  <span className="text-sm flex items-center">
                    <Building className="h-4 w-4 mr-1" />
                    {bookDetail.publisher}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">ISBN</span>
                  <span className="text-sm">{bookDetail.isbn13}</span>
                </div>
              </div>

              <Separator className="my-6" />

              <div className="space-y-3">
                {bookDetail.readState === ReadState.WISH || bookDetail.readState === ReadState.READING || bookDetail.readState === ReadState.READ || isInMyBooks ? (
                  <Button className="w-full" disabled>
                    내 목록에 추가됨
                  </Button>
                ) : (
                  <Button className="w-full" onClick={handleAddToMyBooks}>
                    <Plus className="h-4 w-4 mr-2" />
                    내 목록에 추가
                  </Button>
                )}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 책 상세 정보 및 리뷰 */}
        <div className="lg:col-span-2">
          <Tabs defaultValue="description" className="w-full" value={tabState} onValueChange={(state)=>{setTabState(state)}}>
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger value="description">책 소개</TabsTrigger>
              <TabsTrigger value="reviews">리뷰 ({bookDetail.reviews.totalElements})</TabsTrigger>
            </TabsList>
            
            <TabsContent value="description" className="mt-6">
              <Card>
                <CardHeader>
                  <CardTitle>책 소개</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div>
                      <h3 className="font-medium mb-2">제목</h3>
                      <p className="text-muted-foreground">{bookDetail.title}</p>
                    </div>
                    <div>
                      <h3 className="font-medium mb-2">작가</h3>
                      <p className="text-muted-foreground">{bookDetail.authors.join(', ')}</p>
                    </div>
                    <div>
                      <h3 className="font-medium mb-2">카테고리</h3>
                      <p className="text-muted-foreground">{bookDetail.categoryName}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
            
            <TabsContent value="reviews" className="mt-6">
              <div className="space-y-6">
                {bookDetail.reviews.data.map((review) => (
                  <Card key={review.id}>
                    <CardContent className="p-6">
                      <div className="flex items-start space-x-4">
                        <Avatar>
                          <AvatarFallback>{review.memberName?.charAt(0) || 'U'}</AvatarFallback>
                        </Avatar>
                        <div className="flex-1">
                          <div className="flex items-center space-x-2 mb-2">
                            <span className="font-medium">{review.memberName || '익명 사용자'}</span>
                            <div className="flex items-center space-x-1">
                              {renderStars(review.rate)}
                            </div>
                            <span className="text-sm text-muted-foreground">
                              {new Date(review.createdDate).toLocaleDateString('ko-KR')}
                            </span>
                          </div>
                          <p className="text-muted-foreground mb-3 leading-relaxed">
                            {review.content}
                          </p>
                          <div className="flex items-center space-x-4">
                            <Button variant={"ghost"} size="sm" onClick={()=>{handleRecommend(review, true)}}>
                              <ThumbsUp fill={review.isRecommended === true ? theme==="dark"?"#fff":"#000" : "none"} strokeWidth={review.isRecommended===true?1:2} className={"h-4 w-4 mr-1"} />
                              좋아요 {reviewRecommendApi.formatLikes(review.likeCount)}
                            </Button>
                            <Button variant={"ghost"} size="sm" onClick={()=>{handleRecommend(review, false)}}>
                              <ThumbsDown fill={review.isRecommended === false ? theme==="dark"?"#fff":"#000" : "none"} strokeWidth={review.isRecommended===false?1:2} className="h-4 w-4 mr-1" />
                              싫어요 {reviewRecommendApi.formatLikes(review.dislikeCount)}
                            </Button>
                          </div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
                
                {bookDetail.reviews.data.length === 0 && (
                  <Card>
                    <CardContent className="p-12 text-center">
                      <h3 className="text-lg mb-2">아직 리뷰가 없습니다</h3>
                      <p className="text-muted-foreground">
                        이 책을 읽으신 분이라면 첫 번째 리뷰를 작성해보세요!
                      </p>
                    </CardContent>
                  </Card>
                )}

                {/* 리뷰 페이징 */}
                {bookDetail.reviews.totalPages > 1 && (
                  <div className="mt-8 flex justify-center items-center space-x-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => loadReviews(0)}
                      disabled={reviewPage === 0}
                    >
                      처음
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => loadReviews(reviewPage - 1)}
                      disabled={reviewPage === 0}
                    >
                      이전
                    </Button>
                    
                    {/* 페이지 번호들 */}
                    {Array.from({ length: Math.min(5, bookDetail.reviews.totalPages) }, (_, i) => {
                      const startPage = Math.max(0, Math.min(reviewPage - 2, bookDetail.reviews.totalPages - 5));
                      const pageNum = startPage + i;
                      if (pageNum >= bookDetail.reviews.totalPages) return null;
                      
                      return (
                        <Button
                          key={pageNum}
                          variant={pageNum === reviewPage ? "default" : "outline"}
                          size="sm"
                          onClick={() => loadReviews(pageNum)}
                        >
                          {pageNum + 1}
                        </Button>
                      );
                    })}
                    
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => loadReviews(reviewPage + 1)}
                      disabled={bookDetail.reviews.isLast}
                    >
                      다음
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => loadReviews(bookDetail.reviews.totalPages - 1)}
                      disabled={bookDetail.reviews.isLast}
                    >
                      마지막
                    </Button>
                  </div>
                )}

                {/* 리뷰 페이징 정보 */}
                {bookDetail.reviews.totalElements > 0 && (
                  <div className="mt-4 text-center text-sm text-muted-foreground">
                    전체 {bookDetail.reviews.totalElements}개의 리뷰 중 {reviewPage + 1}/{bookDetail.reviews.totalPages} 페이지
                  </div>
                )}
              </div>
            </TabsContent>
          </Tabs>
        </div>
      </div>
    </div>
  );
}