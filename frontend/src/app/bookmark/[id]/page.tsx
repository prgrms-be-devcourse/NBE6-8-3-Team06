"use client"

import React, { useState, useEffect, useCallback } from "react";
import { useParams, useRouter } from "next/navigation";
import { getBookmark } from "../../../types/bookmarkAPI";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ArrowLeft } from "lucide-react";
import { BookmarkDetail } from "../../../types/bookmarkData";
import { useAuth } from "../../_hooks/auth-context";
import { BookmarkSummaryCard } from "./_components/bookmarkSummaryCard";
import { BookInfoTab } from "./_components/bookInfoTab";
import { ReviewTab } from "./_components/reviewTab";
import { NotesTab } from "./_components/notesTab";


export default function Page() {
  const router = useRouter();
  const params = useParams();
  const { isLoggedIn, isLoading: isAuthLoading } = useAuth();
  const bookmarkId = parseInt(params.id as string);
  const [bookmark, setBookmark] = useState<BookmarkDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const onNavigate = (path: string) => {
    router.push(path);
  };

  useEffect(() => {
    if (!isAuthLoading && !isLoggedIn) {
      onNavigate('/login');
    }
  }, [isAuthLoading, isLoggedIn, router]);

  const fetchBookmark = useCallback(async () => {
    setIsLoading(true);
    setError('');
    try {
      const response = await getBookmark(bookmarkId);
      setBookmark(response.data);
    } catch (error) {
      console.error('❌ 에러 데이터:', (error as any).data);
      setError(error instanceof Error ? error.message : '내 책 정보를 불러올 수 없습니다.');
    } finally {
      setIsLoading(false);
    }
  }, [bookmarkId]);

  useEffect(() => {
    if (!isAuthLoading && isLoggedIn) {
      if (!bookmarkId) return;

      fetchBookmark();
    }
  }, [isAuthLoading, isLoggedIn, bookmarkId, fetchBookmark]);

  if (isAuthLoading) {
    return <div className="flex justify-center items-center h-screen">로딩 중...</div>;
  };

  if (isLoading) {
    return <div className="text-center py-20">데이터를 불러오는 중입니다...</div>;
  };

  if (error) {
    return <div className="text-center py-20 text-red-500">{error}</div>;
  };

  if (!bookmark) {
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

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* 뒤로가기 버튼 */}
      <Button
        variant="ghost"
        onClick={() => onNavigate('/bookmark')}
        className="mb-6"
      >
        <ArrowLeft className="h-4 w-4 mr-2" />
        내 책 목록으로 돌아가기
      </Button>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* 책 기본 정보 */}
        <div className="lg:col-span-1">
          <BookmarkSummaryCard bookmark={bookmark} onNavigate={onNavigate} />
        </div>

        {/* 상세 정보 */}
        <div className="lg:col-span-2">
          <Tabs defaultValue="info" className="w-full">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="info">기본 정보</TabsTrigger>
              <TabsTrigger value="review">내 리뷰</TabsTrigger>
              <TabsTrigger value="notes">노트 ({bookmark.notes.length})</TabsTrigger>
            </TabsList>

            <TabsContent value="info" className="mt-6">
              <BookInfoTab book={bookmark.book} />
            </TabsContent>

            <TabsContent value="review" className="mt-6">
              <ReviewTab id={bookmarkId} readState={bookmark.readState} review={bookmark.review} onNavigate={onNavigate} />
            </TabsContent>

            <TabsContent value="notes" className="mt-6">
              <NotesTab id={bookmark.id} notes={bookmark.notes} onNavigate={onNavigate} />
            </TabsContent>
          </Tabs>
        </div>
      </div>
    </div>
  );
}