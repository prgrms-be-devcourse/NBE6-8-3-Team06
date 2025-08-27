'use client';
import { BookmarkDetail } from "@/types/bookmarkData";
import { Card, CardContent } from "@/components/ui/card";
import { ImageWithFallback } from "@/components/ImageWithFallback";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { Separator } from "@/components/ui/separator";
import { Calendar, Edit, FileText, PenTool, BookOpen } from "lucide-react";
import { getReadState, getReadStateColor, renderStars } from "@/lib/bookmarkUtils";

interface BookmarkSummaryCardProps {
    bookmark: BookmarkDetail;
    onNavigate: (path: string) => void;
}

export function BookmarkSummaryCard({ bookmark, onNavigate }: BookmarkSummaryCardProps) {
    return (
        <Card>
            <CardContent className="p-6">
                <div className="text-center mb-6">
                    <ImageWithFallback
                        src={bookmark.book.imageUrl}
                        alt={bookmark.book.title}
                        className="w-48 h-72 object-cover rounded mx-auto mb-4"
                    />
                    <h1 className="text-2xl mb-2">{bookmark.book.title}</h1>
                    <p className="text-lg text-muted-foreground mb-4">{bookmark.book?.authors?.join(', ') || '저자 정보 없음'}</p>

                    <Badge className={`mb-4 ${getReadStateColor(bookmark?.readState)}`}>
                        {getReadState(bookmark?.readState)}
                    </Badge>

                    {bookmark.book.avgRate > 0 && (
                        <div className="flex items-center justify-center space-x-1 mb-4">
                            {renderStars(bookmark?.book.avgRate)}
                            <span className="text-lg ml-2">{bookmark.book.avgRate.toFixed(1)}</span>
                        </div>
                    )}
                </div>

                <Separator className="mb-6" />

                {/* 읽기 진도 */}
                {bookmark?.readState === 'READING' && bookmark.readPage && (
                    <div className="mb-6">
                        <div className="flex justify-between text-sm mb-2">
                            <span>읽기 진도</span>
                            <span>{bookmark?.readingRate}%</span>
                        </div>
                        <Progress value={bookmark.readingRate} className="mb-2" />
                        <div className="flex justify-between text-sm text-muted-foreground">
                            <span>{bookmark.readPage}쪽</span>
                            <span>{bookmark.book.totalPage}쪽</span>
                        </div>
                    </div>
                )}

                {/* 독서 정보 */}
                <div className="space-y-4">
                    <div className="flex justify-between">
                        <span className="text-sm text-muted-foreground">카테고리</span>
                        <span className="text-sm">{bookmark.book.category}</span>
                    </div>
                    <div className="flex justify-between">
                        <span className="text-sm text-muted-foreground">추가일</span>
                        <span className="text-sm flex items-center">
                            <Calendar className="h-4 w-4 mr-1" />
                            {bookmark.createDate?.substring(0, 10)}
                        </span>
                    </div>
                    {bookmark?.startReadDate && (
                        <div className="flex justify-between">
                            <span className="text-sm text-muted-foreground">시작일</span>
                            <span className="text-sm">{bookmark.startReadDate?.substring(0, 10)}</span>
                        </div>
                    )}
                    {bookmark?.endReadDate && (
                        <div className="flex justify-between">
                            <span className="text-sm text-muted-foreground">완독일</span>
                            <span className="text-sm">{bookmark.endReadDate?.substring(0, 10)}</span>
                        </div>
                    )}
                    {bookmark?.readingDuration > 0 && (
                        <div className="flex justify-between">
                            <span className="text-sm text-muted-foreground">
                                {bookmark?.readState === 'READ' ? '독서 기간' : '독서 중'}
                            </span>
                            <span className="text-sm">{bookmark.readingDuration}일</span>
                        </div>
                    )}
                    <div className="flex justify-between">
                        <span className="text-sm text-muted-foreground">페이지</span>
                        <span className="text-sm flex items-center">
                            <BookOpen className="h-4 w-4 mr-1" />
                            {bookmark.book.totalPage}쪽
                        </span>
                    </div>
                </div>

                <Separator className="my-6" />

                <div className="space-y-3">
                    {bookmark?.readState === 'READ' && !bookmark.review && (
                        <Button
                            className="w-full"
                            onClick={() => onNavigate(`/bookmark/${bookmark.id}/review`)}
                        >
                            <PenTool className="h-4 w-4 mr-2" />
                            리뷰 작성하기
                        </Button>
                    )}
                    {bookmark?.readState === 'READ' && bookmark.review && (
                        <Button
                            variant="outline"
                            className="w-full"
                            onClick={() => onNavigate(`/bookmark/${bookmark.id}/review`)}
                        >
                            <Edit className="h-4 w-4 mr-2" />
                            리뷰 수정하기
                        </Button>
                    )}
                    <Button
                        variant="outline"
                        className="w-full"
                        onClick={() => onNavigate(`/bookmark/${bookmark.id}/notes`)}
                    >
                        <FileText className="h-4 w-4 mr-2" />
                        노트 관리 ({bookmark.notes.length})
                    </Button>
                </div>
            </CardContent>
        </Card>
    );
}