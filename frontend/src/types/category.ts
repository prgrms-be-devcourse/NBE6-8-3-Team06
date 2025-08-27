import { apiFetch } from "@/lib/apiFetch";

export type Category = {
    name: string
};

export const getCategories = async () => {
    return apiFetch('/categories');
};