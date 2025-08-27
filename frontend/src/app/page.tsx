"use client";

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { ImageWithFallback } from '@/components/ImageWithFallback';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { BookOpen, Plus, Star, TrendingUp } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { useAuth } from '@/app/_hooks/auth-context';
import { getBookmarkReadStates, getBookmarks } from '@/types/bookmarkAPI';
import { Bookmark, BookmarkReadStates } from '@/types/bookmarkData';
import { getReadState, getReadStateColor } from '@/lib/bookmarkUtils';

export default function HomePage() {
  const { isLoggedIn, isLoading: isAuthLoading } = useAuth();

  const [stats, setStats] = useState<BookmarkReadStates | null>(null);
  const [recentBooks, setRecentBooks] = useState<Bookmark[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isLoggedIn && !isAuthLoading) {

      const fetchLoggedInData = async () => {
        setIsLoading(true);
        setError('');
        try {
          const [stateResponse, recentBooksResponse] = await Promise.all([
            getBookmarkReadStates({
              category: null,
              readState: null,
              keyword: null,
            }),
            getBookmarks({
              page: 0,
              size: 3,
              sort: "modifyDate,desc",
              category: null,
              readState: null,
              keyword: null,
            })
          ]);
          console.log("readState API : ", stateResponse.data);
          console.log("bookmarks API : ", recentBooksResponse.data.data);
          setStats(stateResponse.data);
          setRecentBooks(recentBooksResponse.data.data);
        } catch (error) {
          console.error('❌ 에러 데이터:', (error as any).data);
          setError(error instanceof Error ? error.message : '내 책 정보를 가져올 수 없습니다.');
        } finally {
          setIsLoading(false);
        }
      };
      fetchLoggedInData();
    } else if (!isAuthLoading && !isLoggedIn) {
      setIsLoading(false);
    }
  }, [isLoggedIn, isAuthLoading]);

  if (isAuthLoading || isLoading) {
    return <div className="text-center py-20">데이터를 불러오는 중입니다...</div>
  }
  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* 헤로 섹션 */}
      <div className="text-center mb-12">
        <h1 className="text-4xl mb-4">나만의 독서 기록</h1>
        <p className="text-xl text-muted-foreground mb-8">
          다양한 책을 탐색하고, 읽은 책들을 기록하며, 리뷰를 남겨보세요
        </p>

        {isLoggedIn ? (
          <div className="flex justify-center space-x-4">
            <Link href="/bookmark" passHref>
              <Button size="lg">
                <BookOpen className="mr-2 h-5 w-5" />
                내 책 보기
              </Button>
            </Link>
            <Link href="/books" passHref>
              <Button size="lg" variant="outline">
                <Plus className="mr-2 h-5 w-5" />
                책 추가하기
              </Button>
            </Link>
          </div>
        ) : (
          <Link href="/login" passHref>
            <Button size="lg">
              시작하기
            </Button>
          </Link>
        )}
      </div>

      {isLoggedIn && (
        <>
          {/* 통계 카드 */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-12">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm">총 책 수</CardTitle>
                <BookOpen className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl">{stats?.totalCount || 0}</div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm">읽은 책</CardTitle>
                <TrendingUp className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl">{stats?.readState?.READ || 0}</div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm">읽고 싶은 책</CardTitle>
                <Plus className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl">{stats?.readState?.WISH || 0}</div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm">평균 평점</CardTitle>
                <Star className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl">{(stats?.avgRate ?? 0).toFixed(1)}</div>
              </CardContent>
            </Card>
          </div>

          {/* 최근 책들 */}
          <div className="mb-12">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl">최근 활동</h2>
              <Link href="/bookmark" passHref>
                <Button variant="outline">
                  내 책 관리
                </Button>
              </Link>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {recentBooks.map((bookmark) => (
                <Card key={bookmark.id} className="flex flex-col h-full">
                  <CardHeader>
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <CardTitle className="text-lg">{bookmark.book.title}</CardTitle>
                        <CardDescription>{bookmark.book.authors.join(', ')}</CardDescription>
                      </div>
                      <ImageWithFallback
                        src={bookmark.book.imageUrl}
                        alt={bookmark.book.title}
                        width={60}
                        height={90}
                        className="w-12 h-16 object-cover rounded"
                      />
                    </div>
                  </CardHeader>
                  <CardContent className="mt-auto">
                    <div className="flex items-center">
                      <Badge className={`mt-2 ${getReadStateColor(bookmark.readState)}`}>
                        {getReadState(bookmark.readState)}
                      </Badge>
                      {bookmark.review?.rate > 0 && (
                        <div className="flex items-center ml-auto">
                          <Star className="h-4 w-4 fill-yellow-400 text-yellow-400 mr-1" />
                          <span className="text-sm">{bookmark.review?.rate}</span>
                        </div>
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        </>
      )}

      {/* 기능 소개 섹션 */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        <Card>
          <CardHeader>
            <BookOpen className="h-8 w-8 text-primary mb-2" />
            <CardTitle>책 관리</CardTitle>
            <CardDescription>
              읽은 책, 읽고 있는 책, 읽고 싶은 책을 체계적으로 관리하세요
            </CardDescription>
          </CardHeader>
        </Card>

        <Card>
          <CardHeader>
            <Star className="h-8 w-8 text-primary mb-2" />
            <CardTitle>리뷰 작성</CardTitle>
            <CardDescription>
              읽은 책에 대한 생각과 평점을 기록하고 공유하세요
            </CardDescription>
          </CardHeader>
        </Card>

        <Card>
          <CardHeader>
            <TrendingUp className="h-8 w-8 text-primary mb-2" />
            <CardTitle>독서 통계</CardTitle>
            <CardDescription>
              독서 패턴을 분석하고 목표를 설정해보세요
            </CardDescription>
          </CardHeader>
        </Card>
      </div>
    </div>
  );
}
