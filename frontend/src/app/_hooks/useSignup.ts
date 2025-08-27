import { useState } from "react";
import { apiFetch } from "@/lib/apiFetch";
import { SignupForm, ApiResponse } from "@/types/auth";

export function useSignup() {
  const [isLoading, setIsLoading] = useState(false);

  const signup = async (
    form: SignupForm
  ): Promise<{ success: boolean; error?: string }> => {
    setIsLoading(true);

    try {
      // confirmPassword는 서버에 보내지 않음
      const { confirmPassword, ...signupData } = form;

      const res = await apiFetch<ApiResponse>("/user/signup", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(signupData),
      });

      return { success: true };
    } catch (err: any) {
      const errorMsg = err.message || "회원가입에 실패했습니다.";
      return { success: false, error: errorMsg };
    } finally {
      setIsLoading(false);
    }
  };

  return { signup, isLoading };
}
