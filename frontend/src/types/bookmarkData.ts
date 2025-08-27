export type Bookmark = {
    id: number;
    bookId: number;
    book: BookmarkBookDetail;
    readState: string;
    readPage: number;
    readingRate: number;
    createDate: string;
    startReadDate: string;
    endReadDate: string;
    review: BookmarkReviewDetail;
};
export type BookmarkBookDetail = {
    id: number;
    isbn13: string;
    title: string;
    imageUrl: string;
    publisher: string;
    totalPage: number;
    avgRate: number;
    category: string;
    publishDate: string;
    authors: string[];
    description: string;
};

export type BookmarkDetail = {
    id: number;
    bookId: number;
    book: BookmarkBookDetail;
    readState: string;
    readPage: number;
    readingRate: number;
    createDate: string;
    startReadDate: string;
    endReadDate: string;
    readingDuration: number;
    review: BookmarkReviewDetail;
    notes: BookmarkNoteDetail[];
};

export type BookmarkReviewDetail = {
    id: number;
    content: string;
    rate: number;
    date: string;
};

export type BookmarkNoteDetail = {
    id: number;
    title: string;
    content: string;
    page: number;
    createDate: string;
    modifiedDate: string;
};

export type BookmarkPage = {
    data: Bookmark[];
    pageNumber: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    isLast: boolean;
};

export type CreateBookmark = {
    bookId: number;
};

export type UpdateBookmark = {
    readState: string;
    startReadDate: string;
    endReadDate: string;
    readPage: number;
};

export type BookmarkReadStates = {
    totalCount: number;
    avgRate: number;
    readState: ReadState;
};
export type ReadState = {
    READ: number;
    READING: number;
    WISH: number;
}