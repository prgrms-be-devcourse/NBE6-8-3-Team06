"use client";

import { useState, useEffect } from "react";
import { useSafeFetch } from "./useSafeFetch";

const NEXT_PUBLIC_API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";

// 요청 DTO
export interface NoteRequest {
    title: string;
    content: string;
    page?: string | null; // optional
}

// 응답 DTO
export interface NoteResponse {
    id: number;
    title: string;
    content: string;
    page?: string | null;
    createDate: string; // ISO 형식 날짜 문자열
    modifyDate: string;
}

// 책 정보
type BookInfo = {
    title: string;
    imageUrl: string;
    category: string;
    author: string[];
};

export function useNote(bookmarkId: number) {
    const [notes, setNotes] = useState<NoteResponse[]>([]);
    const [bookInfo, setBookInfo] = useState<BookInfo>({
        title: "",
        imageUrl: "",
        category: "",
        author: [],
    });

    const { safeFetch } = useSafeFetch(); // 공통 fetch 훅 사용

    const fetchNotes = async () => {
        const json = await safeFetch(`${NEXT_PUBLIC_API_BASE_URL}/bookmarks/${bookmarkId}/notes`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        });

        console.log(json);
        const notes = json.data.notes;
        const bookInfo = json.data.bookInfo;

        setNotes(notes);
        setBookInfo({
            title: bookInfo.title,
            imageUrl: bookInfo.imageUrl || "",
            category: bookInfo.category || "",
            author: bookInfo.author || [],
        });
    };

    const addNote = async (newNote: Partial<NoteRequest>) => {
        await safeFetch(`${NEXT_PUBLIC_API_BASE_URL}/bookmarks/${bookmarkId}/notes`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(newNote),
        });
        await fetchNotes();
    };

    const updateNote = async (noteId: number, updatedNote: Partial<NoteRequest>) => {
        await safeFetch(`${NEXT_PUBLIC_API_BASE_URL}/bookmarks/${bookmarkId}/notes/${noteId}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(updatedNote),
        });
        await fetchNotes();
    };

    const deleteNote = async (noteId: number) => {
        await safeFetch(`${NEXT_PUBLIC_API_BASE_URL}/bookmarks/${bookmarkId}/notes/${noteId}`, {
            method: "DELETE",
        });
        await fetchNotes();
    };

    useEffect(() => {
        fetchNotes();
    }, [bookmarkId]);

    return {
        notes,
        bookInfo,
        addNote,
        updateNote,
        deleteNote,
    };
}