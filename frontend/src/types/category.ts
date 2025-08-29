import { apiFetch } from "@/lib/apiFetch";
import { ApiResponse } from "./auth";

export type Category = {
    name: string
};

export const getCategories = async () => {
    return apiFetch<ApiResponse>('/categories');
};