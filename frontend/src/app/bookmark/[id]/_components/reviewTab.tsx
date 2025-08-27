'use client';

import { Card, CardHeader, CardContent, CardTitle } from "@/components/ui/card";
import { BookmarkReviewDetail } from "@/types/bookmarkData";
import { Button } from "@/components/ui/button";
import { Edit, PenTool } from "lucide-react";
import { renderStars } from '@/lib/bookmarkUtils';

interface ReviewTabProps {
    id: number;
    readState: string;
    review: BookmarkReviewDetail;
    onNavigate: (path: string) => void;
}
export function ReviewTab({ id, readState, review, onNavigate } : ReviewTabProps ) {
    return (
        <Card>
        <CardHeader>
          <div className="flex justify-between items-center">
            <CardTitle>내 리뷰</CardTitle>
            {readState === 'READ' && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => onNavigate(`/bookmark/${id}/review`)}
              >
                <Edit className="h-4 w-4 mr-2" />
                {review ? '수정' : '작성'}
              </Button>
            )}
          </div>
        </CardHeader>
        <CardContent>
          {review ? (
            <div>
              {review.rate && (
                <div className="flex items-center space-x-1 mb-3">
                  {renderStars(review.rate)}
                  <span className="ml-2">{review.rate}</span>
                </div>
              )}
              <p className="text-muted-foreground leading-relaxed">
                {review.content}
              </p>
            </div>
          ) : (
            <div className="text-center py-8">
              <PenTool className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground mb-4">
                {readState === 'READ'
                  ? '아직 리뷰를 작성하지 않았습니다.'
                  : '책을 다 읽은 후 리뷰를 작성할 수 있습니다.'
                }
              </p>
              {readState === 'READ' && (
                <Button onClick={() => onNavigate(`/bookmark/${id}/review`)}>
                  리뷰 작성하기
                </Button>
              )}
            </div>
          )}
        </CardContent>
      </Card>
    );
}