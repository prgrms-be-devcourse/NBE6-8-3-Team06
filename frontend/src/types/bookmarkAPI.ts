import { apiFetch } from "@/lib/apiFetch";
import { BookmarkReadStateSearch, BookmarkSearch, CreateBookmark, UpdateBookmark } from "./bookmarkData";

export const getBookmarks = async ({ page, size, sort, category, readState, keyword }:BookmarkSearch) => {
    const params = new URLSearchParams({
        page: String(page),
        size: String(size),
        sort: String(sort),
    });
    if(category && category !== 'all') params.append('category', category);
    if(readState && readState !== 'all') params.append('readState', readState);
    if(keyword) params.append('keyword', keyword);
    return apiFetch(`/bookmarks?${params.toString()}`);
};

export const getBookmark = async (id:number) => {
    return apiFetch(`/bookmarks/${id}`);
};

export const createBookmark = async (data:CreateBookmark) => {
    return apiFetch('/bookmarks', {
        method: 'POST',
        body: JSON.stringify(data),
    });
};

export const updateBookmark = async (id:number, data:UpdateBookmark) => {
    return apiFetch(`/bookmarks/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data),
    });
};

export const deleteBookmark = async (id: number) => {
    return apiFetch(`/bookmarks/${id}`, {
        method: 'DELETE',
    });
};

export const getBookmarkReadStates = async ({ category, readState, keyword }:BookmarkReadStateSearch) => {
    const params = new URLSearchParams();
    if(category && category !== 'all') params.append('category', category);
    if(readState && readState !== 'all') params.append('readState', readState);
    if(keyword) params.append('keyword', keyword);
    return apiFetch(`/bookmarks/read-states?${params.toString()}`);
};
