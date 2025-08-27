"use client"
import { useAuth } from "@/app/_hooks/auth-context";
import { useReview } from "@/app/_hooks/useReview";
import withLogin from "@/app/_hooks/withLogin";
import { ImageWithFallback } from "@/components/ImageWithFallback";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { getBookmark } from "@/types/bookmarkAPI";
import { BookmarkDetail } from "@/types/bookmarkData";
import { ArrowLeft, Save, Star, Trash2, UndoIcon, X } from "lucide-react";
import { useRouter } from "next/navigation";
import { use, useCallback, useEffect, useState } from "react";


export default withLogin(function page({params}:{params:Promise<{id:string}>}){
  const {id:bookmarkIdStr} = use(params);
  const bookmarkId = parseInt(bookmarkIdStr);
  const router = useRouter();
  const [hoveredRating, setHoveredRating] = useState(0);
  const [bookmark, setBookmark] = useState<BookmarkDetail | null>(null);
  const [review, setReview] = useState(bookmark?.review||null);
  const [rating, setRating] = useState(review?.rate|| 0);
  const [content, setContent] = useState(review?.content||'');
  const [book, setBook] = useState(bookmark?.book||null);
  const [bookId, setBookId] = useState(book?.id || null);
  const reviewApi = useReview(bookId||0);

  const fetchBookmark = async () => {
    const response = await getBookmark(bookmarkId);
    const bookmarkData = response.data as BookmarkDetail;
    setBookmark(response.data);
    setReview(bookmarkData.review || null);
    setBook(bookmarkData.book || null);
    setRating(bookmarkData.review?.rate || 0);
    setContent(bookmarkData.review?.content || '');
    setBookId(bookmarkData.book?.id || 0);
    reviewApi.setBookId(bookmarkData.book?.id || 0);
  }
  
  useEffect(()=>{
    if (!bookmark){
      fetchBookmark();
    }
  },[bookmarkId])
  
  const onNavigate = (e:string)=>{
    router.push(e);
  }

  if (!book) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center">
          <p>책을 찾을 수 없습니다.</p>
          <Button onClick={() => onNavigate('/bookmark')} className="mt-4">
            내 책 목록으로 돌아가기
          </Button>
        </div>
      </div>
    );
  }
  

  const handleStarClick = (starRating: number) => {
    setRating(starRating === rating ? 0 : starRating);
  };

  const handleStarHover = (starRating: number) => {
    setHoveredRating(starRating);
  };

  const handleStarLeave = () => {
    setHoveredRating(0);
  };

  const handleSave = async () => {
    // 여기서 실제로는 API 호출을 통해 리뷰를 저장
    if (!review){
      await reviewApi.createReview({rating,  content});
    }else{
      await reviewApi.editReview({rating, content})
    }
    onNavigate(`/bookmark/${bookmarkId}`);
  };

  const handleDelete = async () =>{
    await reviewApi.deleteReview();
    onNavigate(`/bookmark/${bookmarkId}`);
  }

  const handleCancel = () => {
    onNavigate(`/bookmark/${bookmarkId}`);
  };

  const renderStars = () => {
    return [...Array(5)].map((_, i) => {
      const starValue = i + 1;
      const isFilled = starValue <= (hoveredRating || rating);
      
      return (
        <button
          key={i}
          type="button"
          className="p-1"
          onClick={() => handleStarClick(starValue)}
          onMouseEnter={() => handleStarHover(starValue)}
          onMouseLeave={handleStarLeave}
        >
          <Star
            className={`h-8 w-8 transition-colors ${
              isFilled 
                ? 'fill-yellow-400 text-yellow-400' 
                : 'text-gray-300 hover:text-yellow-200'
            }`}
          />
        </button>
      );
    });
  };

  const getRatingText = (rating: number) => {
    switch (rating) {
      case 1: return '별로예요';
      case 2: return '그저 그래요';
      case 3: return '보통이에요';
      case 4: return '좋아요';
      case 5: return '최고예요';
      default: return '별점을 선택해주세요';
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* 뒤로가기 버튼 */}
      <Button 
        variant="ghost" 
        onClick={handleCancel}
        className="mb-6"
      >
        <ArrowLeft className="h-4 w-4 mr-2" />
        돌아가기
      </Button>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* 책 정보 */}
        <div className="lg:col-span-1">
          <Card>
            <CardContent className="p-6">
              <div className="text-center">
                <ImageWithFallback
                  src={`${book!.imageUrl}`}
                  alt={book!.title}
                  className="w-40 h-60 object-cover rounded mx-auto mb-4"
                />
                <h2 className="text-xl mb-2">{book!.title}</h2>
                <p className="text-muted-foreground mb-2">{book!.authors}</p>
                <p className="text-sm text-muted-foreground">{book!.category}</p>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 리뷰 작성 폼 */}
        <div className="lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>리뷰 작성</CardTitle>
              <CardDescription>
                이 책에 대한 솔직한 생각을 들려주세요
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* 별점 */}
              <div className="space-y-3">
                <Label>별점</Label>
                <div className="flex items-center space-x-2">
                  <div className="flex">
                    {renderStars()}
                  </div>
                  <span className="text-sm text-muted-foreground ml-4">
                    {getRatingText(hoveredRating || rating)}
                  </span>
                </div>
              </div>

              {/* 리뷰 작성 */}
              <div className="space-y-3">
                <Label htmlFor="review">리뷰</Label>
                <Textarea
                  id="review"
                  placeholder="이 책에 대한 생각을 자유롭게 작성해주세요. 어떤 점이 좋았는지, 아쉬웠는지, 누구에게 추천하고 싶은지 등을 포함하면 더욱 도움이 됩니다."
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  rows={10}
                  className="resize-none"
                />
                <div className="flex justify-between text-sm text-muted-foreground">
                  <span>최소 10자 이상 작성해주세요</span>
                  <span>{content.length}자</span>
                </div>
              </div>

              {/* 리뷰 작성 팁 */}
              <div className="bg-muted p-4 rounded-lg">
                <h4 className="font-medium mb-2">💡 좋은 리뷰 작성 팁</h4>
                <ul className="text-sm text-muted-foreground space-y-1">
                  <li>• 구체적인 내용이나 인상 깊었던 부분을 언급해보세요</li>
                  <li>• 어떤 독자에게 추천하고 싶은지 적어보세요</li>
                  <li>• 개인적인 경험이나 느낀 점을 솔직하게 표현해보세요</li>
                  <li>• 스포일러는 피해주세요</li>
                </ul>
              </div>

              {/* 버튼 */}
              <div className="flex space-x-3 pt-4">
                <Button 
                  onClick={handleSave}
                  disabled={rating === 0 || content.trim().length < 10}
                  className="flex-1"
                >
                  <Save className="h-4 w-4 mr-2" />
                  저장하기
                </Button>
                {review && <Button variant="destructive" onClick={handleDelete}>
                  <Trash2></Trash2>
                  삭제
                </Button>}
                <Button 
                  variant="outline"
                  onClick={handleCancel}
                >
                  <X className="h-4 w-4 mr-2" />
                  취소
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
})