"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import BookCard from "@/components/BookCard";
import { Search, BookOpen, Star, Filter, Plus } from "lucide-react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { ImageWithFallback } from "@/components/ImageWithFallback";
import { useRouter } from "next/navigation";
import { usePathname } from "next/navigation";
import {
  BookSearchDto,
  ReadState,
  fetchBooks,
  searchBooks,
  searchBookByIsbn,
  fetchBooksByCategory,
  searchBooksByCategory,
  BooksResponse,
} from "@/types/book";
import { useAuth } from "@/app/_hooks/auth-context";
import { toast } from "sonner";
import { createBookmark } from "@/types/bookmarkAPI.js";
import { getCategories, Category } from "@/types/category";

interface BooksPageProps {
  onNavigate: (page: string) => void;
  onBookClick: (bookId: number) => void;
}

export default function BooksPage() {
  const [searchTerm, setSearchTerm] = useState("");
  const [searchType, setSearchType] = useState<"title" | "isbn" | "category">("title");
  const [selectedCategory, setSelectedCategory] = useState("all");
  const [sortBy, setSortBy] = useState("title");
  const [books, setBooks] = useState<BookSearchDto[]>([]);
  const [categories, setCategories] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [isSearching, setIsSearching] = useState(false);
  const router = useRouter();
  const pathName = usePathname();
  const { isLoggedIn } = useAuth();
  const onBookClick = (id: number) => {
    router.push(`${pathName}/${id}`);
  };

  const loadCategories = async () => {
    try {
      const response = await getCategories();
      console.log("📂 카테고리 목록:", response);
      if (response && Array.isArray(response)) {
        setCategories(["all", ...response.map((cat: Category) => cat.name)]);
      } else if (response && (response as any).data && Array.isArray((response as any).data)) {
        setCategories(["all", ...(response as any).data.map((cat: Category) => cat.name)]);
      }
    } catch (error) {
      console.error("❌ 카테고리 목록 조회 실패:", error);
      setCategories(["all"]);
    }
  };

  const getSortParams = (sortBy: string) => {
    const params = (() => {
      switch (sortBy) {
        case "title":
          return { sort: "title", direction: "asc" };
        case "author":
          return { sort: "author", direction: "asc" };
        case "rating":
          return { sort: "avgRate", direction: "desc" };
        case "published":
          return { sort: "publishedDate", direction: "desc" };
        default:
          return { sort: "title", direction: "asc" };
      }
    })();
    console.log(`🔄 정렬 파라미터: sortBy=${sortBy} → sort=${params.sort}, direction=${params.direction}`);
    return params;
  };

  const loadBooks = async (page: number = 0, query?: string, type?: "title" | "isbn" | "category", category?: string) => {
    try {
      setLoading(true);
      console.log(`🚀 books 페이지에서 API 호출 시작 - 페이지: ${page}, 검색어: ${query}, 타입: ${type}, 카테고리: ${category}`);
      
      const { sort, direction } = getSortParams(sortBy);
      
      let response: BooksResponse;
      if (query && query.trim()) {
        if (type === "isbn") {
          response = await searchBookByIsbn(query);
        } else {
          // 카테고리가 선택되어 있고 "all"이 아닌 경우 카테고리별 검색
          if (category && category !== "all") {
            response = await searchBooksByCategory(query, category, page, 9, sort, direction);
          } else {
            response = await searchBooks(query, page, 9, sort, direction);
          }
        }
      } else if (category && category !== "all") {
        response = await fetchBooksByCategory(category, page, 9, sort, direction);
      } else {
        response = await fetchBooks(page, 9, sort, direction);
      }
      
      console.log("📚 받아온 응답:", response);
      setBooks(response.books);
      setCurrentPage(response.pageInfo.currentPage);
      setTotalPages(response.pageInfo.totalPages);
      setTotalElements(response.pageInfo.totalElements);
    } catch (err) {
      console.error("💥 에러 발생:", err);
      setError(
        err instanceof Error ? err.message : "책을 불러오는데 실패했습니다."
      );
    } finally {
      setLoading(false);
    }
  };

  // 검색 처리 함수
  const handleSearch = () => {
    setCurrentPage(0); // 검색 시 첫 페이지로 이동
    setIsSearching(true);
    loadBooks(0, searchTerm, searchType, selectedCategory);
  };

  // 검색어 초기화 함수
  const handleClearSearch = () => {
    setSearchTerm('');
    setCurrentPage(0);
    setIsSearching(false);
    loadBooks(0, undefined, undefined, selectedCategory); // 현재 선택된 카테고리로 로드
  };

  // 카테고리 변경 핸들러
  const handleCategoryChange = (category: string) => {
    setSelectedCategory(category);
    setCurrentPage(0);
    setIsSearching(false);
    setSearchTerm('');
    loadBooks(0, undefined, undefined, category);
  };

  // 페이지 로드 함수 (검색 상태 유지)
  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    if (isSearching && searchTerm.trim()) {
      // ISBN 검색은 페이징이 없으므로 제목/저자 검색만 페이징 적용
      if (searchType === "title") {
        loadBooks(page, searchTerm, searchType, selectedCategory);
      }
    } else {
      loadBooks(page, undefined, undefined, selectedCategory);
    }
  };

  useEffect(() => {
    loadBooks(0);
    loadCategories();
  }, []);

  useEffect(() => {
    if (isSearching && searchTerm.trim()) {
      if (searchType === "title") {
        loadBooks(0, searchTerm, searchType, selectedCategory);
      }
    } else {
      loadBooks(0, undefined, undefined, selectedCategory);
    }
  }, [sortBy]);

  // 로그인 상태 변경 시 책 목록 새로고침
  useEffect(() => {
    if (isSearching && searchTerm.trim()) {
      if (searchType === "title") {
        loadBooks(currentPage, searchTerm, searchType, selectedCategory);
      }
    } else {
      loadBooks(currentPage, undefined, undefined, selectedCategory);
    }
  }, [isLoggedIn]);


  // Helper function to get display text for read state
  const getReadStateText = (readState: ReadState) => {
    switch (readState) {
      case ReadState.READ:
        return "읽은 책";
      case ReadState.READING:
        return "읽고 있는 책";
      case ReadState.WISH:
        return "읽고 싶은 책";
      default:
        return "";
    }
  };

  const filteredBooks = books;

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center">
          <p>책을 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center">
          <p className="text-red-500">{error}</p>
          <Button onClick={() => window.location.reload()} className="mt-4">
            다시 시도
          </Button>
        </div>
      </div>
    );
  }

  const sortOptions = [
    { value: "title", label: "제목순" },
    { value: "author", label: "저자순" },
    { value: "rating", label: "평점순" },
    { value: "published", label: "출간일순" },
  ];

  const renderStars = (rating: number) => {
    return [...Array(5)].map((_, i) => (
      <Star
        key={i}
        className={`h-4 w-4 ${
          i < Math.floor(rating)
            ? "fill-yellow-400 text-yellow-400"
            : "text-gray-300"
        }`}
      />
    ));
  };

  const addToMyBooks = async (bookId: number, status: string) => {
    if (!isLoggedIn) {
      toast.error("로그인을 해주세요.", {
        action: {
          label:"이동",
          onClick: ()=>{router.push("/login")}
        }
      })
      return;
    }
    
    try {
      // 상태 텍스트를 ReadState enum으로 변환
      let readState: ReadState;
      switch (status) {
        case "읽고 싶은 책":
          readState = ReadState.WISH;
          break;
        case "읽고 있는 책":
          readState = ReadState.READING;
          break;
        case "읽은 책":
          readState = ReadState.READ;
          break;
        default:
          readState = ReadState.WISH;
      }
      
      // API 호출
      await createBookmark({
        bookId: bookId,
        readState: readState
      });
      
      // 성공 시 책 목록 새로고침 (readState 업데이트를 위해)
      if (isSearching && searchTerm.trim()) {
        if (searchType === "title") {
          await loadBooks(currentPage, searchTerm, searchType, selectedCategory);
        } else {
          await loadBooks(currentPage, searchTerm, searchType, selectedCategory);
        }
      } else {
        await loadBooks(currentPage, undefined, undefined, selectedCategory);
      }
      
    } catch (error) {
      console.error("북마크 추가 실패:", error);
    }
  };


  const getStatusColor = (status: string) => {
    switch (status) {
      case "읽은 책":
        return "bg-green-100 text-green-800";
      case "읽고 있는 책":
        return "bg-blue-100 text-blue-800";
      case "읽고 싶은 책":
        return "bg-gray-100 text-gray-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl mb-2">책 탐색</h1>
        <p className="text-muted-foreground">
          총 {totalElements}권의 책이 등록되어 있습니다. 관심 있는 책을 찾아 내
          목록에 추가해보세요.
        </p>
      </div>

      {/* 검색 및 필터 */}
      <div className="mb-8 space-y-4">
        <div className="flex flex-col sm:flex-row gap-4">
          <Select value={searchType} onValueChange={(value: "title" | "isbn") => setSearchType(value)}>
            <SelectTrigger className="w-full sm:w-32">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="title">제목/저자</SelectItem>
              <SelectItem value="isbn">ISBN</SelectItem>
            </SelectContent>
          </Select>
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
            <Input
              placeholder={searchType === "isbn" ? "ISBN을 입력하세요..." : "책 제목, 저자로 검색..."}
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  handleSearch();
                }
              }}
              className="pl-10"
            />
          </div>
          <Button onClick={handleSearch} disabled={loading}>
            검색
          </Button>
          {isSearching && (
            <Button variant="outline" onClick={handleClearSearch}>
              전체보기
            </Button>
          )}
          <Select value={selectedCategory} onValueChange={handleCategoryChange}>
            <SelectTrigger className="w-full sm:w-48">
              <SelectValue placeholder="카테고리 선택" />
            </SelectTrigger>
            <SelectContent>
              {categories.map((category) => (
                <SelectItem key={category} value={category}>
                  {category === "all" ? "모든 카테고리" : category}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={sortBy} onValueChange={setSortBy}>
            <SelectTrigger className="w-full sm:w-48">
              <SelectValue placeholder="정렬 기준" />
            </SelectTrigger>
            <SelectContent>
              {sortOptions.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* 검색 결과 */}
      <div className="mb-6">
        <p className="text-sm text-muted-foreground">
          {isSearching ? (
            `"${searchTerm}"에 대한 검색 결과: ${totalElements}개의 책`
          ) : (
            `${totalElements}개의 책이 검색되었습니다`
          )}
        </p>
      </div>

      {/* 책 목록 */}
      {books.length === 0 ? (
        <div className="text-center py-12">
          <BookOpen className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
          <p className="text-muted-foreground">
            검색 조건에 맞는 책이 없습니다.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredBooks.map((book) => (
            <Card
              key={book.id}
              className="h-full flex flex-col cursor-pointer hover:shadow-lg transition-shadow"
            >
              <CardHeader onClick={() => onBookClick(book.id)}>
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <CardTitle className="line-clamp-2">{book.title}</CardTitle>
                    <CardDescription>{book.authors.join(", ")}</CardDescription>
                    <div className="flex items-center gap-2 mt-2">
                      <Badge variant="secondary">{book.categoryName}</Badge>
                      {book.readState && (
                        <Badge
                          className={getStatusColor(getReadStateText(book.readState))}
                        >
                          {getReadStateText(book.readState)}
                        </Badge>
                      )}
                    </div>
                  </div>
                  <ImageWithFallback
                    src={
                      book.imageUrl ||
                      `https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=80&h=120&fit=crop&crop=center&sig=${book.id}`
                    }
                    alt={book.title}
                    className="w-16 h-24 object-cover rounded ml-4"
                  />
                </div>
              </CardHeader>
              <CardContent
                className="flex-1 flex flex-col"
                onClick={() => onBookClick(book.id)}
              >
                <div className="flex-1 space-y-3">

                  <div className="flex items-center justify-between text-sm text-muted-foreground">
                    <span>{book.totalPage}쪽</span>
                    <span>{new Date(book.publishedDate).getFullYear()}년</span>
                  </div>

                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-1">
                      {renderStars(book.avgRate)}
                      <span className="text-sm ml-2">
                        {book.avgRate.toFixed(1)}
                      </span>
                      <span className="text-xs text-muted-foreground">
                        {book.publisher}
                      </span>
                    </div>
                  </div>
                </div>

                <div
                  className="mt-4 pt-4 border-t"
                  onClick={(e) => e.stopPropagation()}
                >
                  {book.readState ? (
                    <Button className="w-full" disabled>
                      내 목록에 추가됨
                    </Button>
                  ) : (
                    <Button
                      className="w-full"
                      onClick={() => addToMyBooks(book.id, "읽고 싶은 책")}
                    >
                      <Plus className="h-4 w-4 mr-2" />내 목록에 추가
                    </Button>
                  )}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* 페이징 버튼 */}
      {totalPages > 1 && searchType !== "isbn" && (
        <div className="mt-8 flex justify-center items-center space-x-2">
          <Button
            variant="outline"
            disabled={currentPage === 0}
            onClick={() => handlePageChange(currentPage - 1)}
          >
            이전
          </Button>

          <div className="flex space-x-1">
            {Array.from({ length: Math.min(totalPages, 5) }, (_, index) => {
              let pageNum;
              if (totalPages <= 5) {
                pageNum = index;
              } else if (currentPage <= 2) {
                pageNum = index;
              } else if (currentPage >= totalPages - 3) {
                pageNum = totalPages - 5 + index;
              } else {
                pageNum = currentPage - 2 + index;
              }

              return (
                <Button
                  key={pageNum}
                  variant={currentPage === pageNum ? "default" : "outline"}
                  size="sm"
                  onClick={() => handlePageChange(pageNum)}
                >
                  {pageNum + 1}
                </Button>
              );
            })}
          </div>

          <Button
            variant="outline"
            disabled={currentPage === totalPages - 1}
            onClick={() => handlePageChange(currentPage + 1)}
          >
            다음
          </Button>
        </div>
      )}

      {/* 페이지 정보 */}
      <div className="mt-4 text-center text-sm text-muted-foreground">
        {totalElements > 0 && (
          <p>
            페이지 {currentPage + 1} / {totalPages}
            (총 {totalElements}개 중 {currentPage * 9 + 1}-
            {Math.min((currentPage + 1) * 9, totalElements)}개 표시)
          </p>
        )}
      </div>
    </div>
  );
}
