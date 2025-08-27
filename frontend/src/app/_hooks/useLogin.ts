import { useState } from "react";
import { apiFetch } from "@/lib/apiFetch";
import { LoginForm, ApiResponse } from "@/types/auth";
import { useAuth } from "./auth-context";

export function useLogin() {
  const [isLoading, setIsLoading] = useState(false);
  const { login: authLogin } = useAuth();

  const login = async (
    form: LoginForm
  ): Promise<{ success: boolean; error?: string }> => {
    setIsLoading(true);

    try {
      const res = await apiFetch<ApiResponse>("/user/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(form),
      });

      // AuthContext의 login을 호출하여 사용자 정보 갱신
      await authLogin();

      return { success: true };
    } catch (err: any) {
      const errorMsg = err.message || "로그인에 실패했습니다.";
      return { success: false, error: errorMsg };
    } finally {
      setIsLoading(false);
    }
  };

  return { login, isLoading };
}
