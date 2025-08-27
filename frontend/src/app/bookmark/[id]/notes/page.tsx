"use client"
import { ImageWithFallback } from "@/components/ImageWithFallback";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { ArrowLeft, Edit, FileText, Plus, Save, Search, Trash2, X } from "lucide-react";
import { useRouter } from "next/navigation";
import { use, useState, useEffect } from "react";
import { useNote } from "./_hook/useNote";
import { useAuth } from "@/app/_hooks/auth-context";

interface Note {
  id: number;
  title: string;
  content: string;
  page?: string | null;
  createDate: string;
  modifyDate: string;
}

export default function page({ params }: { params: Promise<{ id: string }> }) {
  const { isLoggedIn, isLoading: isAuthLoading } = useAuth();
  const router = useRouter();
  const onNavigate = (e: string) => {
    router.push(e);
  }

  useEffect(() => {
    if (!isAuthLoading && !isLoggedIn) {
      onNavigate('/login');
    }
  }, [isAuthLoading, isLoggedIn, onNavigate]);

  const { id: idStr } = use(params);
  const bookmarkId = parseInt(idStr);
  
  const {
    notes, // 노트 데이터
    bookInfo, // 내 책 데이터
    addNote, // 노트 추가
    updateNote, // 노트 수정
    deleteNote // 노트 삭제
  } = useNote(bookmarkId);
  
  const [searchTerm, setSearchTerm] = useState('');
  const [isDialogOpen, setIsDialogOpen] = useState(false); // 새 노트 컴포넌트 열기 flag
  const [editingNote, setEditingNote] = useState<Note | null>(null);
  const [noteForm, setNoteForm] = useState({
    title: '',
    content: '',
    page: ''
  }); // 노트 폼

  const filteredNotes = (notes ?? []).filter(note =>
    note.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
    note.content.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // 페이지 입력
  const handlePageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const raw = e.target.value;

    // 숫자만 허용 (정규식: ^\d*$ → 0개 이상의 숫자만)
    if (!/^\d*$/.test(raw)) return;

    const trimmed = raw.trim();

    if (trimmed === "") {
      setNoteForm((prev) => ({ ...prev, page: "" }));
      return;
    }

    const parsed = parseInt(trimmed, 10);

    if (!isNaN(parsed) && parsed >= 1 && parsed <= 100000) {
      setNoteForm((prev) => ({ ...prev, page: trimmed }));
    }
  };

  const handleSaveNote = () => {
    if (!noteForm.title.trim() || !noteForm.content.trim()) return;

    if (editingNote) {
      // 노트 수정
      updateNote(editingNote.id, {
        title: noteForm.title,
        content: noteForm.content,
        page: noteForm.page,
      });
    } else {
      // 새 노트 추가
      addNote({
        title: noteForm.title,
        content: noteForm.content,
        page: noteForm.page,
      });
    }

    resetForm();
  };

  const handleEditNote = (note: Note) => {
    setEditingNote(note);
    setNoteForm({
      title: note.title,
      content: note.content,
      page: note.page ? note.page : ''
    });
    setIsDialogOpen(true);
  };

  const handleDeleteNote = (noteId: number) => {
    deleteNote(noteId);
  };

  const resetForm = () => {
    setNoteForm({ title: '', content: '', page: '' });
    setEditingNote(null);
    setIsDialogOpen(false);
  };

  const openNewNoteDialog = () => {
    resetForm();
    setIsDialogOpen(true);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* 뒤로가기 버튼 */}
      <Button
        variant="ghost"
        onClick={() => onNavigate(`/bookmark/${bookmarkId}`)}
        className="mb-6"
      >
        <ArrowLeft className="h-4 w-4 mr-2" />
        책 상세로 돌아가기
      </Button>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
        {/* 책 정보 */}
        <div className="lg:col-span-1">
          <Card>
            <CardContent className="p-6">
              <div className="text-center">
                <ImageWithFallback
                  src={bookInfo.imageUrl}
                  alt={`https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=150&h=225&fit=crop&crop=center&sig=1`}
                  className="w-32 h-48 object-cover rounded mx-auto mb-4"
                />
                <h2 className="text-lg mb-2">{bookInfo.title}</h2>
                <p className="text-sm text-muted-foreground mb-2">{bookInfo.author.join(", ")}</p> {/* 수정 필요 */}
                <Badge variant="secondary">{bookInfo.category}</Badge>
              </div>

              <div className="mt-6 pt-6 border-t">
                <div className="text-center">
                  <div className="text-2xl mb-1">{notes.length}</div>
                  <p className="text-sm text-muted-foreground">개의 노트</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 노트 목록 */}
        <div className="lg:col-span-3">
          <div className="flex justify-between items-center mb-6">
            <div>
              <h1 className="text-3xl mb-2">독서 노트</h1>
              <p className="text-muted-foreground">
                읽으면서 중요하다고 생각하는 내용을 기록해보세요
              </p>
            </div>

            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
              <DialogTrigger asChild>
                <Button onClick={openNewNoteDialog}>
                  <Plus className="h-4 w-4 mr-2" />
                  새 노트
                </Button>
              </DialogTrigger>
              <DialogContent className="max-w-2xl">
                <DialogHeader>
                  <DialogTitle>
                    {editingNote ? '노트 수정' : '새 노트 작성'}
                  </DialogTitle>
                  <DialogDescription>
                    {bookInfo.title}에 대한 독서 노트를 작성하세요
                  </DialogDescription>
                </DialogHeader>

                <div className="space-y-4">
                  <div className="grid grid-cols-4 gap-4">
                    <div className="col-span-3">
                      <Label htmlFor="title">제목 *</Label>
                      <Input
                        id="title"
                        value={noteForm.title}
                        onChange={(e) => setNoteForm({ ...noteForm, title: e.target.value })}
                        placeholder="노트 제목을 입력하세요"
                        maxLength={50} // 50자 제한
                      />
                    </div>
                    <div>
                      <Label htmlFor="page">페이지</Label>
                      <Input
                        id="page"
                        type="text"
                        inputMode="numeric"
                        pattern="[0-9]*"
                        value={noteForm.page ?? ""}
                        onChange={handlePageChange}
                        placeholder="1~100000"
                      />
                    </div>
                  </div>

                  <div>
                    <Label htmlFor="content">내용 *</Label>
                    <Textarea
                      id="content"
                      value={noteForm.content}
                      onChange={(e) => setNoteForm({ ...noteForm, content: e.target.value })}
                      placeholder="노트 내용을 입력하세요"
                      rows={8}
                      maxLength={1000} // 1000자 제한
                      className="resize-none break-all overflow-y-auto"
                    />
                    <div className="text-sm text-right text-muted-foreground mt-1">
                      {noteForm.content.length}/1000
                    </div>
                  </div>

                  <div className="flex justify-end space-x-2 pt-4">
                    <Button variant="outline" onClick={resetForm}>
                      <X className="h-4 w-4 mr-2" />
                      취소
                    </Button>
                    <Button
                      onClick={handleSaveNote}
                      disabled={!noteForm.title.trim() || !noteForm.content.trim()}
                    >
                      <Save className="h-4 w-4 mr-2" />
                      저장
                    </Button>
                  </div>
                </div>
              </DialogContent>
            </Dialog>
          </div>

          {/* 검색 */}
          <div className="mb-6">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
              <Input
                placeholder="노트 제목이나 내용으로 검색..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
          </div>

          {/* 노트 목록 */}
          {filteredNotes.length === 0 ? (
            <Card>
              <CardContent className="p-12 text-center">
                <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg mb-2">
                  {searchTerm ? '검색 결과가 없습니다' : '아직 작성한 노트가 없습니다'}
                </h3>
                <p className="text-muted-foreground mb-4">
                  {searchTerm ? '다른 키워드로 검색해보세요' : '책을 읽으면서 중요한 내용을 노트로 남겨보세요'}
                </p>
                {!searchTerm && (
                  <Button onClick={openNewNoteDialog}>
                    첫 번째 노트 작성하기
                  </Button>
                )}
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-4">
              {filteredNotes.map((note) => (
                <Card key={note.id}>
                  <CardHeader>
                    <div className="flex justify-between items-start">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <CardTitle className="text-lg">{note.title}</CardTitle>
                          {note.page && (
                            <Badge variant="outline">{note.page}쪽</Badge>
                          )}
                        </div>
                        <CardDescription>
                          {note.createDate.split('T')[0]}
                          {note.modifyDate && note.modifyDate !== note.createDate &&
                            ` (수정: ${note.modifyDate.split('T')[0]})`
                          }
                        </CardDescription>
                      </div>
                      <div className="flex space-x-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleEditNote({
                            ...note,
                            createDate: note.createDate,
                            modifyDate: note.modifyDate,
                          })}
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleDeleteNote(note.id)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <div className="text-muted-foreground whitespace-pre-wrap break-all leading-relaxed">
                      {note.content}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}