'use client';

import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { getBookmarks, updateBookmark, deleteBookmark, getBookmarkReadStates } from '../../types/bookmarkAPI';
import { BookmarkPage, Bookmark, BookmarkReadStates, UpdateBookmark } from '../../types/bookmarkData';
import { BookOpen, Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useAuth } from "../_hooks/auth-context";
import { getCategories, Category } from '@/types/category';
import { BookmarkCard } from './_components/bookmarkCard';
import { BookmarkStats } from './_components/bookmarkStats';
import { BookmarkFilters } from './_components/bookmarkFilters';
import { PaginationControls } from './_components/paginationControls';
import { useDebounce } from '../_hooks/useDebounce';
import { BookmarkEditForm } from './_components/bookmarkEditForm';

export default function Page() {
  const router = useRouter();
  const { isLoggedIn, isLoading: isAuthLoading } = useAuth();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [bookmarks, setBookmarks] = useState<BookmarkPage>();
  const [bookmarkReadStates, setBookmarkReadStates] = useState<BookmarkReadStates>(); // 전체 통계(상단)
  const [categories, setCategories] = useState<Category[]>([]);
  const [filteredReadState, setFilteredReadState] = useState<BookmarkReadStates>(); //검색 조건 후 통계(Tab)

  const [currentPage, setCurrentPage] = useState(0);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [selectedReadState, setSelectedReadState] = useState('all');
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [editBookmark, setEditBookmark] = useState<Bookmark | null>(null);

  const debouncedSearchKeyword = useDebounce(searchKeyword, 500);

  const onNavigate = (path: string) => {
    router.push(path);
  };

  useEffect(() => {
    if (!isAuthLoading && !isLoggedIn) {
      onNavigate('/login');
    }
  }, [isAuthLoading, isLoggedIn, onNavigate]);

  //북마크 목록, 필터링 통계 조회
  const fetchFilteredData = useCallback(async () => {
    if (!isLoggedIn) return;

    setIsLoading(true);
    setError('');
    try {
      const [bookmarksResponse, filteredReadStateResponse] = await Promise.all([
        getBookmarks({
          page: currentPage,
          size: 9,
          sort: "createDate,desc",
          category: selectedCategory,
          readState: selectedReadState,
          keyword: debouncedSearchKeyword,
        }),
        getBookmarkReadStates({
          category: selectedCategory,
          readState: undefined,
          keyword: debouncedSearchKeyword,
        }),
      ]);
      setBookmarks(bookmarksResponse.data);
      setFilteredReadState(filteredReadStateResponse.data);
    } catch (error) {
      console.error('❌ 에러 데이터:', (error as any).data);
      if (error instanceof Error && error.message.includes("데이터가 없습니다")) {
        setBookmarks({
          data: [],
          totalPages: 0,
          totalElements: 0,
          pageNumber: 0,
          pageSize: 0,
          isLast: true
        });
        setFilteredReadState({
          totalCount: 0,
          avgRate: 0.0,
          readState: {
            READ: 0,
            READING: 0,
            WISH: 0
          }
        });
      } else {
        setError(error instanceof Error ? error.message : '북마크 목록을 가져올 수 없습니다.');
      }
    } finally {
      setIsLoading(false);
    }
  }, [isLoggedIn, currentPage, selectedCategory, selectedReadState, debouncedSearchKeyword]);

  //북마크 전체 통계, 카테고리 목록 조회
  const fetchInitialData = useCallback(async () => {
    if (!isLoggedIn) return;

    try {
      const [statsResponse, categoriesResponse] = await Promise.all([
        getBookmarkReadStates({
          category: null,
          readState: null,
          keyword: null,
        }),
        getCategories(),
      ]);

      setBookmarkReadStates(statsResponse.data);
      setCategories(categoriesResponse.data);
    } catch (error) {
      console.error('❌ 에러 데이터:', (error as any).data);
      setError(error instanceof Error ? error.message : '초기 데이터 로딩에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  }, [isLoggedIn]);

  //필터 변경 시, 페이지 0으로 리셋
  useEffect(() => {
    setCurrentPage(0);
  }, [selectedCategory, selectedReadState, debouncedSearchKeyword]);

  //필터, 페이지 등 목록에 영향 주는 것이 변경되면 새로고침
  useEffect(() => {
    if (!isAuthLoading && isLoggedIn) {
      fetchFilteredData();
    }
  }, [isAuthLoading, isLoggedIn, fetchFilteredData]);

  //초기 데이터 한 번만 로딩
  useEffect(() => {
    if (!isAuthLoading && isLoggedIn) {
      fetchInitialData();
    }
  }, [isAuthLoading, isLoggedIn, fetchInitialData]);
 
  const handleEditClick = (bookmark: Bookmark) => {
    setEditBookmark(bookmark);
    setIsEditDialogOpen(true);
  };
  const handleCancelEdit = () => {
    setEditBookmark(null);
    setIsEditDialogOpen(false);
  };
  const handleSaveBookmark = async (updateData: UpdateBookmark) => {
    if (!editBookmark) return;
    try {
      await updateBookmark(editBookmark.id, updateData);
      setIsEditDialogOpen(false);
      setEditBookmark(null);
      await fetchInitialData();
      await fetchFilteredData();
    } catch (error) {
      console.error('❌ 에러 데이터:', (error as any).data);
      setError(error instanceof Error ? error.message : '북마크 업데이트가 실패했습니다.');
    }
  };

  const handleDeleteBookmark = async (bookmarkId: number) => {
    if (window.confirm("이 북마크를 삭제하시겠습니까?")) {
      try {
        await deleteBookmark(bookmarkId);
        await fetchInitialData();
        await fetchFilteredData();
      } catch (error) {
        console.error('❌ 에러 데이터:', (error as any).data);
        setError(error instanceof Error ? error.message : '북마크 삭제에 실패했습니다.');
      }
    }
  };

  const handlePageChange = (page: number) => {
    if (page >= 0 && page < (bookmarks?.totalPages || 0)) {
      setCurrentPage(page);
    }
  }

  if (isAuthLoading) {
    return <div className="flex justify-center items-center h-screen">로딩 중...</div>;
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl mb-2">내 책 목록</h1>
          <p className="text-muted-foreground">
            {bookmarkReadStates?.totalCount || 0} 권의 책을 등록했습니다.
          </p>
        </div>

        <Button onClick={() => onNavigate('books')}>
          <Plus className="mr-2 h-4 w-4" />
          새 책 추가하기
        </Button>
      </div>

      {/* 내 책 목록 통계 */}
      <BookmarkStats stats={bookmarkReadStates} />
      {/* 책 목록 검색 필터 */}
      <BookmarkFilters searchKeyword={searchKeyword} onSearchKeywordChange={setSearchKeyword}
        selectedCategory={selectedCategory} onCategoryChange={setSelectedCategory}
        categories={categories} selectedReadState={selectedReadState}
        onReadStateChange={setSelectedReadState} readStates={['READ', 'READING', 'WISH']}
        setCurrentPage={setCurrentPage}
      />

      {/* 책 목록 테이블 */}
      <Tabs value={selectedReadState} onValueChange={setSelectedReadState} className="w-full">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="all">모든 상태 ({filteredReadState?.totalCount || 0})</TabsTrigger>
          <TabsTrigger value="READ">읽은 책 ({filteredReadState?.readState.READ || 0})</TabsTrigger>
          <TabsTrigger value="READING">읽고 있는 책 ({filteredReadState?.readState.READING || 0})</TabsTrigger>
          <TabsTrigger value="WISH">읽고 싶은 책 ({filteredReadState?.readState.WISH || 0})</TabsTrigger>
        </TabsList>
        <div className="mt-6">
          {isLoading ? (
            <p className="text-center py-12">내 책 목록을 불러오는 중입니다...</p>
          ) : error ? (
            <p className="text-center py-12 text-red-500">{error}</p>
          ) : bookmarks?.data?.length === 0 ? (
            <div className="text-center py-12">
              <BookOpen className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground mb-4">표시할 책이 없습니다.</p>
              <Button onClick={() => onNavigate('books')}>새 책 추가하기</Button>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {bookmarks?.data?.map((bookmark) => (
                <BookmarkCard
                  key={bookmark.id}
                  bookmark={bookmark}
                  onNavigate={onNavigate}
                  onEditClick={handleEditClick}
                  onDeleteClick={handleDeleteBookmark}
                />
              ))}
            </div>
          )}
        </div>
      </Tabs>
      {/* 페이지  */}
      <PaginationControls currentPage={currentPage} totalPages={bookmarks?.totalPages || 0}
        onChangePage={handlePageChange}
      />

      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>북마크 편집</DialogTitle>
            <DialogDescription>{editBookmark?.book.title}의 정보를 수정합니다.</DialogDescription>
          </DialogHeader>
          {editBookmark && (
            <BookmarkEditForm
              bookmark={editBookmark}
              onSave={handleSaveBookmark}
              onCancel={handleCancelEdit}
            />
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}