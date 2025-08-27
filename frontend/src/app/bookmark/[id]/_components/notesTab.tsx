'use client';

import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { FileText } from "lucide-react";
import { BookmarkNoteDetail } from "@/types/bookmarkData";
import { Badge } from "@/components/ui/badge";

interface NotesTabProps {
    id: number;
    notes: BookmarkNoteDetail[];
    onNavigate: (path: string) => void;
}
export function NotesTab({ id, notes, onNavigate } : NotesTabProps ) {
    return (
        <div className="space-y-4">
        <div className="flex justify-between items-center">
          <h3 className="text-lg font-medium">독서 노트</h3>
          <Button
            variant="outline"
            onClick={() => onNavigate(`/bookmark/${id}/notes`)}
          >
            <FileText className="h-4 w-4 mr-2" />
            노트 관리
          </Button>
        </div>

        {notes.length > 0 ? (
          <div className="space-y-4">
            {notes.slice(0, 3).map((note) => (
              <Card key={note.id}>
                <CardContent className="p-4">
                  <div className="flex justify-between items-start mb-2">
                    <h4 className="font-medium">{note.title}</h4>
                    {note.page && (
                      <Badge variant="secondary">{note.page}쪽</Badge>
                    )}
                  </div>
                  <p className="text-sm text-muted-foreground mb-2 line-clamp-2">
                    {note.content}
                  </p>
                  <p className="text-xs text-muted-foreground">
                    {note.modifiedDate ? note.modifiedDate : note.createDate}
                  </p>
                </CardContent>
              </Card>
            ))}
            {notes.length > 3 && (
              <Button
                variant="outline"
                className="w-full"
                onClick={() => onNavigate(`/bookmark/${id}/notes`)}
              >
                모든 노트 보기 ({notes.length}개)
              </Button>
            )}
          </div>
        ) : (
          <Card>
            <CardContent className="p-8 text-center">
              <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground mb-4">
                아직 작성한 노트가 없습니다.
              </p>
              <Button onClick={() => onNavigate(`/bookmark/${id}/notes`)}>
                첫 번째 노트 작성하기
              </Button>
            </CardContent>
          </Card>
        )}
      </div>
    );
}