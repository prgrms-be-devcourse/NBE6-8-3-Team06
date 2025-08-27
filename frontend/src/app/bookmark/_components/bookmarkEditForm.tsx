import React, { useState, useEffect, useMemo } from "react";
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { Bookmark, UpdateBookmark } from "@/types/bookmarkData";

interface BookmarkEditFormProps {
    bookmark: Bookmark;
    onSave: (updateData: UpdateBookmark) => void;
    onCancel: () => void;
}

export function BookmarkEditForm({ bookmark, onSave, onCancel }: BookmarkEditFormProps) {
    const today = new Date().toISOString().split("T")[0];
    const [formData, setFormData] = useState({
        readState: bookmark.readState,
        startReadDate: bookmark.startReadDate?.substring(0, 10) || today,
        endReadDate: bookmark.endReadDate?.substring(0, 10) || today,
        readPage: bookmark.readPage || 0,
    });

    useEffect(() => {
        setFormData({
            readState: bookmark.readState,
            startReadDate: bookmark.startReadDate?.substring(0, 10) || today,
            endReadDate: bookmark.endReadDate?.substring(0, 10) || today,
            readPage: bookmark.readPage || 0,
        });
    }, [bookmark]);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        const dataToSend = {
            ...formData,
            startReadDate: formData.startReadDate ? `${formData.startReadDate}T00:00:00` : null,
            endReadDate: formData.endReadDate ? `${formData.endReadDate}T00:00:00` : null,
        };
        onSave(dataToSend);
    };

    const handleValueChange = (field: keyof typeof formData, value: any) => {
        setFormData(prev => ({ ...prev, [field]: value }));
    };

    const handlePageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const totalPage = bookmark.book.totalPage;
        let newPage = parseInt(e.target.value.trim()) || 0;
        if (newPage > totalPage) {
            newPage = totalPage;
        }
        if (newPage < 0) {
            newPage = 0;
        }

        if (newPage === totalPage) {
            setFormData(prev => ({
                ...prev,
                readState: 'READ',
                readPage: newPage,
                endReadDate: new Date().toISOString().split('T')[0],
            }));
        } else {
            setFormData(prev => ({
                ...prev,
                readPage: newPage,
            }));
        }
    };

    const isFormValid = useMemo(() => {
        if (formData.readState === 'READING') {
            return !!formData.startReadDate && !!formData.readPage;
        }
        if (formData.readState === 'READ') {
            return !!formData.startReadDate && !!formData.endReadDate && formData.endReadDate >= formData.startReadDate;
        }
        return true;
    }, [formData]);

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                    <Label htmlFor="status">읽기 상태</Label>
                    <Select value={formData.readState} onValueChange={(value) => handleValueChange('readState', value)}>
                        <SelectTrigger>
                            <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="WISH">읽고 싶은 책</SelectItem>
                            <SelectItem value="READING">읽고 있는 책</SelectItem>
                            <SelectItem value="READ">읽은 책</SelectItem>
                        </SelectContent>
                    </Select>
                </div>

                {formData.readState === 'READING' && (
                    <div className="space-y-2">
                        <Label htmlFor="currentPage">현재 페이지</Label>
                        <Input
                            id="currentPage"
                            type="number"
                            value={formData.readPage || ''}
                            onChange={handlePageChange}
                            placeholder="현재 읽고 있는 페이지"
                        />
                    </div>
                )}

                {formData.readState === 'READ' && (
                    <>
                        <div className="space-y-2">
                            <Label htmlFor="dateFinished">완독일</Label>
                            <Input
                                id="endReadDate"
                                type="date"
                                value={formData.endReadDate || ''}
                                onChange={(e) => handleValueChange('endReadDate', e.target.value)}
                                min={formData.startReadDate || undefined}
                                max={today}
                            />
                        </div>
                    </>
                )}
            </div>

            {(formData.readState === 'READING' || formData.readState === 'READ') && (
                <div className="space-y-2">
                    <Label htmlFor="dateStarted">시작일</Label>
                    <Input
                        id="startReadDate"
                        type="date"
                        value={formData.startReadDate || ''}
                        onChange={(e) => handleValueChange('startReadDate', e.target.value)}
                        min={bookmark.book.publishDate?.substring(0,10) || undefined}
                        max={formData.endReadDate || today}
                    />
                </div>
            )}

            <div className="flex justify-end space-x-2 pt-4">
                <Button type="button" variant="outline" onClick={onCancel}>
                    취소
                </Button>
                <Button type="submit" disabled={!isFormValid}>
                    저장
                </Button>
            </div>
        </form>
    );
}