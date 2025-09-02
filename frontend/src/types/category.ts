import { apiFetch } from "@/lib/apiFetch";
import { ApiResponse } from "./auth";

export type Category = {
    name: string
};

export const getCategories = async () => {
    try {
        console.log('📂 카테고리 API 호출 시작: /categories');
        const response = await apiFetch<ApiResponse<Category[]>>('/categories');
        console.log('📂 카테고리 API 응답:', response);
        return response;
    } catch (error) {
        console.error('❌ 카테고리 API 호출 에러:', error);
        throw error;
    }
};