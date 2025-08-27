'use client';

import { Card, CardHeader, CardContent, CardTitle } from "@/components/ui/card";
import { BookmarkBookDetail } from "@/types/bookmarkData";
import { Separator } from '@/components/ui/separator';

interface BookInfoTabProps {
    book: BookmarkBookDetail;
}
export function BookInfoTab( { book } : BookInfoTabProps ) {
    return (
        <Card>
        <CardHeader>
          <CardTitle>책 정보</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {book.description && (
          <div>
            <h4 className="font-medium mb-2">책 소개</h4>
            <p className="text-muted-foreground leading-relaxed">
              {book.description}
            </p>
          </div>
          )}
          <Separator />

          <div className="grid grid-cols-2 gap-4">
            <div>
              <span className="text-sm text-muted-foreground">출판사</span>
              <p className="font-medium">{book.publisher}</p>
            </div>
            <div>
              <span className="text-sm text-muted-foreground">출간일</span>
              <p className="font-medium">{book.publishDate?.substring(0,10)}</p>
            </div>
            <div>
              <span className="text-sm text-muted-foreground">ISBN</span>
              <p className="font-medium">{book.isbn13}</p>
            </div>
            <div>
              <span className="text-sm text-muted-foreground">페이지</span>
              <p className="font-medium">{book.totalPage}쪽</p>
            </div>
          </div>

          {/* 개인 메모는 작성을 어디서 하는가 
          {bookmark.notes && (
            <>
              <Separator />
              <div>
                <h4 className="font-medium mb-2">개인 메모</h4>
                <p className="text-muted-foreground leading-relaxed">
                  {bookmark.notes}
                </p>
              </div>
            </>
          )}*/}
        </CardContent>
      </Card>
    );
}