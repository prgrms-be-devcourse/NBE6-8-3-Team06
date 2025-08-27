export enum ReadState {
  WISH = 'WISH',
  READING = 'READING',
  READ = 'READ'
}

export interface ReviewResponseDto {
  id: number;
  content: string;
  rate: number;
  memberName: string;
  memberId: number;
  likeCount: number;
  dislikeCount: number;
  isRecommended: boolean | null;
  createdDate: string;
  modifiedDate: string;
}

export interface BookDetailDto {
  id: number;
  title: string;
  imageUrl: string;
  publisher: string;
  isbn13: string;
  totalPage: number;
  publishedDate: string;
  avgRate: number;
  categoryName: string;
  authors: string[];
  readState?: ReadState;
  reviews: PageResponseDto<ReviewResponseDto>;
}

export interface BookSearchDto {
  id: number;
  title: string;
  imageUrl: string;
  publisher: string;
  isbn13: string;
  totalPage: number;
  publishedDate: string; // LocalDateTime from backend will be serialized as string
  avgRate: number;
  categoryName: string;
  authors: string[];
  readState: ReadState;
}

// API 공통 응답 구조
interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T;
}

// PageResponseDto 타입 정의 (백엔드와 일치)
export interface PageResponseDto<T> {
  data: T[];
  pageNumber: number;
  pageSize: number;
  totalPages: number;
  totalElements: number;
  isLast: boolean;
}

export interface BooksResponse {
  books: BookSearchDto[];
  pageInfo: {
    currentPage: number;
    totalPages: number;
    totalElements: number;
    isLast: boolean;
  };
}

// 공통 응답 처리 함수
async function processApiResponse(response: ApiResponse<PageResponseDto<BookSearchDto>>): Promise<BooksResponse> {
  console.log('📦 백엔드 응답 원본:', response);
  console.log('📊 응답 타입:', typeof response);
  
  if (response) {
    console.log('📋 응답 키들:', Object.keys(response));
    console.log('✅ resultCode:', response.resultCode);
    console.log('💬 msg:', response.msg);
  }
  
  // API 공통 응답에서 data 필드 추출
  if (response && typeof response === 'object' && 'data' in response) {
    const pageData = response.data;
    console.log('📄 PageResponseDto 데이터:', pageData);
    
    if (pageData && typeof pageData === 'object' && 'data' in pageData) {
      console.log('📚 책 배열:', pageData.data);
      console.log('📊 총 원소 개수:', pageData.totalElements);
      console.log('📄 총 페이지 수:', pageData.totalPages);
      console.log('🔢 현재 페이지:', pageData.pageNumber);
      console.log('📏 페이지 크기:', pageData.pageSize);
      console.log('🔚 마지막 페이지인가?', pageData.isLast);
      
      if (Array.isArray(pageData.data)) {
        console.log('✅ 책 배열 추출 성공 - 첫 번째 책:', pageData.data[0]);
        console.log('📊 추출된 책 개수:', pageData.data.length);
        
        return {
          books: pageData.data,
          pageInfo: {
            currentPage: pageData.pageNumber,
            totalPages: pageData.totalPages,
            totalElements: pageData.totalElements,
            isLast: pageData.isLast
          }
        };
      }
    }
  }
  
  console.warn('⚠️ 예상하지 못한 응답 구조:', response);
  return {
    books: [],
    pageInfo: {
      currentPage: 0,
      totalPages: 0,
      totalElements: 0,
      isLast: true
    }
  };
}

export async function fetchBooks(page: number = 0, size: number = 9, sort: string = "title", direction: string = "asc"): Promise<BooksResponse> {
  const { apiFetch } = await import('@/lib/apiFetch');
  
  try {
    const sortParam = `${sort},${direction}`;
    console.log(`🔍 API 호출 시작: /books?page=${page}&size=${size}&sort=${sortParam}`);
    const response = await apiFetch<ApiResponse<PageResponseDto<BookSearchDto>>>(`/books?page=${page}&size=${size}&sort=${sortParam}`);
    console.log('📦 fetchBooks 응답 받음:', response);
    return await processApiResponse(response);
  } catch (error) {
    console.error('❌ fetchBooks API 호출 에러 상세:', error);
    console.error('❌ 에러 타입:', typeof error);
    console.error('❌ 에러 메시지:', error instanceof Error ? error.message : String(error));
    if (error instanceof Error && (error as any).data) {
      console.error('❌ 에러 데이터:', (error as any).data);
    }
    throw error;
  }
}

export async function searchBooks(query: string, page: number = 0, size: number = 9, sort: string = "title", direction: string = "asc"): Promise<BooksResponse> {
  const { apiFetch } = await import('@/lib/apiFetch');
  
  try {
    const sortParam = `${sort},${direction}`;
    console.log(`🔍 검색 API 호출 시작: /books/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}&sort=${sortParam}`);
    const response = await apiFetch<ApiResponse<PageResponseDto<BookSearchDto>>>(`/books/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}&sort=${sortParam}`);
    return await processApiResponse(response);
  } catch (error) {
    console.error('❌ 검색 API 호출 에러:', error);
    throw error;
  }
}

export async function searchBookByIsbn(isbn: string): Promise<BooksResponse> {
  const { apiFetch } = await import('@/lib/apiFetch');
  
  try {
    console.log(`📖 ISBN 검색 API 호출 시작: /books/isbn/${isbn}`);
    const response = await apiFetch<ApiResponse<BookSearchDto>>(`/books/isbn/${isbn}`);
    
    console.log('📦 ISBN 검색 API 응답 원본:', response);
    
    if (response && typeof response === 'object' && 'data' in response) {
      const book = response.data;
      console.log('📚 ISBN 검색 결과:', book);
      
      if (book) {
        return {
          books: [book],
          pageInfo: {
            currentPage: 0,
            totalPages: 1,
            totalElements: 1,
            isLast: true
          }
        };
      }
    }
    
    console.warn('⚠️ ISBN 검색 결과 없음:', response);
    return {
      books: [],
      pageInfo: {
        currentPage: 0,
        totalPages: 0,
        totalElements: 0,
        isLast: true
      }
    };
  } catch (error) {
    console.error('❌ ISBN 검색 API 호출 에러:', error);
    throw error;
  }
}

export async function fetchBookDetail(bookId: number, reviewPage: number = 0): Promise<BookDetailDto> {
  const { apiFetch } = await import('@/lib/apiFetch');
  
  try {
    let url = `/books/${bookId}`;
    if (reviewPage > 0) {
      url += `?page=${reviewPage}`;
    }
    console.log(`📖 책 상세 정보 API 호출 시작: ${url}`);
    const response = await apiFetch<ApiResponse<BookDetailDto>>(url);
    
    console.log('📦 책 상세 정보 API 응답 원본:', response);
    
    if (response && typeof response === 'object' && 'data' in response) {
      const bookDetail = response.data;
      console.log('📚 책 상세 정보 결과:', bookDetail);
      
      if (bookDetail) {
        return bookDetail;
      }
    }
    
    throw new Error('책 상세 정보를 찾을 수 없습니다.');
  } catch (error) {
    console.error('❌ 책 상세 정보 API 호출 에러:', error);
    throw error;
  }
}

export async function fetchBooksByCategory(categoryName: string, page: number = 0, size: number = 9, sort: string = "title", direction: string = "asc"): Promise<BooksResponse> {
  const { apiFetch } = await import('@/lib/apiFetch');
  
  try {
    const encodedCategoryName = encodeURIComponent(categoryName);
    const sortParam = `${sort},${direction}`;
    console.log(`🔍 카테고리별 책 조회 API 호출 시작: /books/categories?categoryName=${encodedCategoryName}&page=${page}&size=${size}&sort=${sortParam}`);
    const response = await apiFetch<ApiResponse<PageResponseDto<BookSearchDto>>>(`/books/categories?categoryName=${encodedCategoryName}&page=${page}&size=${size}&sort=${sortParam}`);
    console.log('📦 카테고리별 책 조회 응답 받음:', response);
    return await processApiResponse(response);
  } catch (error) {
    console.error('❌ 카테고리별 책 조회 API 호출 에러:', error);
    throw error;
  }
}

export async function searchBooksByCategory(query: string, categoryName: string, page: number = 0, size: number = 9, sort: string = "title", direction: string = "asc"): Promise<BooksResponse> {
  const { apiFetch } = await import('@/lib/apiFetch');
  
  try {
    const encodedQuery = encodeURIComponent(query);
    const encodedCategoryName = encodeURIComponent(categoryName);
    const sortParam = `${sort},${direction}`;
    console.log(`🔍 카테고리별 검색 API 호출 시작: /books/search/category?query=${encodedQuery}&categoryName=${encodedCategoryName}&page=${page}&size=${size}&sort=${sortParam}`);
    const response = await apiFetch<ApiResponse<PageResponseDto<BookSearchDto>>>(`/books/search/category?query=${encodedQuery}&categoryName=${encodedCategoryName}&page=${page}&size=${size}&sort=${sortParam}`);
    console.log('📦 카테고리별 검색 응답 받음:', response);
    return await processApiResponse(response);
  } catch (error) {
    console.error('❌ 카테고리별 검색 API 호출 에러:', error);
    throw error;
  }
}

export async function addToMyBooks(bookId: number): Promise<void> {
  const { apiFetch } = await import('@/lib/apiFetch');
  
  try {
    console.log(`📚 내 목록에 추가 API 호출 시작: /bookmarks`);
    await apiFetch('/bookmarks', {
      method: 'POST',
      body: JSON.stringify({ 
        bookId: bookId,
        readState: ReadState.WISH 
      })
    });
    console.log('✅ 내 목록에 추가 완료');
  } catch (error) {
    console.error('❌ 내 목록에 추가 API 호출 에러:', error);
    throw error;
  }
}