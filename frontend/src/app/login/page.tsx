"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Checkbox } from "@/components/ui/checkbox";
import { BookOpen } from "lucide-react";
import { useLogin } from "../_hooks/useLogin";
import { useSignup } from "../_hooks/useSignup";
import { LoginForm, SignupForm } from "@/types/auth";
import { toast } from "sonner";
import { validateSignupForm, validateLoginForm } from "../utils/formValidation";
import { useFormValidation } from "../_hooks/useFormValidation";
import { PasswordInput } from "@/components/ui/password-input";
import { FormField } from "@/components/ui/form-field";

export default function LoginPage() {
  const router = useRouter();
  const { login } = useLogin();
  const { signup } = useSignup();
  const [currentTab, setCurrentTab] = useState("login");
  const [agreeToTerms, setAgreeToTerms] = useState(false);

  // 로그인 폼 관리
  const loginFormValidation = useFormValidation<LoginForm>({
    initialValues: { email: "", password: "" },
    validate: (values) => validateLoginForm(values.email, values.password),
    onSubmit: async (values) => {
      const result = await login(values);
      if (result.success) {
        toast.success("로그인되었습니다!");
        router.push("/");
      } else {
        toast.error(result.error || "로그인에 실패했습니다.");
      }
      return result;
    },
  });

  // 회원가입 폼 관리
  const signupFormValidation = useFormValidation<SignupForm>({
    initialValues: { name: "", email: "", password: "", confirmPassword: "" },
    validate: (values) =>
      validateSignupForm(
        values.name,
        values.email,
        values.password,
        values.confirmPassword
      ),
    onSubmit: async (values) => {
      // 이용약관 동의 체크
      if (!agreeToTerms) {
        toast.error("이용약관에 동의해주세요.");
        return { success: false, error: "이용약관에 동의해주세요." };
      }

      const result = await signup(values);
      if (result.success) {
        toast.success("회원가입이 완료되었습니다! 로그인해주세요.");
        signupFormValidation.resetForm();
        setAgreeToTerms(false);
        setCurrentTab("login");
      } else {
        toast.error(result.error || "회원가입에 실패했습니다.");
      }
      return result;
    },
  });

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        {/* 헤더 */}
        <div className="text-center mb-8">
          <BookOpen className="h-12 w-12 text-primary mx-auto mb-4" />
          <h1 className="text-2xl mb-2">책 관리 시스템</h1>
          <p className="text-muted-foreground">독서 기록을 시작해보세요</p>
        </div>

        <Tabs
          value={currentTab}
          onValueChange={setCurrentTab}
          className="w-full"
        >
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="login">로그인</TabsTrigger>
            <TabsTrigger value="signup">회원가입</TabsTrigger>
          </TabsList>

          {/* 로그인 탭 */}
          <TabsContent value="login">
            <Card>
              <CardHeader>
                <CardTitle>로그인</CardTitle>
                <CardDescription>
                  계정에 로그인하여 독서 기록을 확인하세요
                </CardDescription>
              </CardHeader>
              <CardContent>
                <form
                  onSubmit={loginFormValidation.handleSubmit}
                  className="space-y-4"
                >
                  <FormField
                    label="이메일"
                    htmlFor="login-email"
                    error={loginFormValidation.errors.email}
                  >
                    <Input
                      id="login-email"
                      type="email"
                      placeholder="이메일을 입력하세요"
                      value={loginFormValidation.values.email}
                      onChange={(e) =>
                        loginFormValidation.setValue("email", e.target.value)
                      }
                      onBlur={() => loginFormValidation.validateField("email")}
                      className={
                        loginFormValidation.errors.email
                          ? "border-destructive"
                          : ""
                      }
                      disabled={loginFormValidation.isSubmitting}
                    />
                  </FormField>

                  <FormField
                    label="비밀번호"
                    htmlFor="login-password"
                    error={loginFormValidation.errors.password}
                  >
                    <PasswordInput
                      id="login-password"
                      placeholder="비밀번호를 입력하세요"
                      value={loginFormValidation.values.password}
                      onChange={(value) =>
                        loginFormValidation.setValue("password", value)
                      }
                      error={loginFormValidation.errors.password}
                      disabled={loginFormValidation.isSubmitting}
                    />
                  </FormField>

                  <div className="text-right">
                    <button
                      type="button"
                      className="text-sm text-primary underline hover:text-primary/80"
                    >
                      비밀번호를 잊으셨나요?
                    </button>
                  </div>

                  <Button
                    type="submit"
                    className="w-full"
                    disabled={loginFormValidation.isSubmitting}
                  >
                    {loginFormValidation.isSubmitting
                      ? "로그인 중..."
                      : "로그인"}
                  </Button>
                </form>
              </CardContent>
            </Card>
          </TabsContent>

          {/* 회원가입 탭 */}
          <TabsContent value="signup">
            <Card>
              <CardHeader>
                <CardTitle>회원가입</CardTitle>
                <CardDescription>
                  새 계정을 만들어 독서 여정을 시작하세요
                </CardDescription>
              </CardHeader>
              <CardContent>
                <form
                  onSubmit={signupFormValidation.handleSubmit}
                  className="space-y-4"
                >
                  <FormField
                    label="이름"
                    htmlFor="signup-name"
                    error={signupFormValidation.errors.name}
                    required
                  >
                    <Input
                      id="signup-name"
                      type="text"
                      placeholder="이름을 입력하세요"
                      value={signupFormValidation.values.name}
                      onChange={(e) =>
                        signupFormValidation.setValue("name", e.target.value)
                      }
                      onBlur={() => signupFormValidation.validateField("name")}
                      className={
                        signupFormValidation.errors.name
                          ? "border-destructive"
                          : ""
                      }
                      disabled={signupFormValidation.isSubmitting}
                    />
                  </FormField>

                  <FormField
                    label="이메일"
                    htmlFor="signup-email"
                    error={signupFormValidation.errors.email}
                    required
                  >
                    <Input
                      id="signup-email"
                      type="email"
                      placeholder="이메일을 입력하세요"
                      value={signupFormValidation.values.email}
                      onChange={(e) =>
                        signupFormValidation.setValue("email", e.target.value)
                      }
                      onBlur={() => signupFormValidation.validateField("email")}
                      className={
                        signupFormValidation.errors.email
                          ? "border-destructive"
                          : ""
                      }
                      disabled={signupFormValidation.isSubmitting}
                    />
                  </FormField>

                  <FormField
                    label="비밀번호"
                    htmlFor="signup-password"
                    error={signupFormValidation.errors.password}
                    required
                  >
                    <PasswordInput
                      id="signup-password"
                      placeholder="최소 8자 이상"
                      value={signupFormValidation.values.password}
                      onChange={(value) =>
                        signupFormValidation.setValue("password", value)
                      }
                      error={signupFormValidation.errors.password}
                      disabled={signupFormValidation.isSubmitting}
                    />
                  </FormField>

                  <FormField
                    label="비밀번호 확인"
                    htmlFor="confirm-password"
                    error={signupFormValidation.errors.confirmPassword}
                    required
                  >
                    <PasswordInput
                      id="confirm-password"
                      placeholder="비밀번호를 다시 입력하세요"
                      value={signupFormValidation.values.confirmPassword}
                      onChange={(value) =>
                        signupFormValidation.setValue("confirmPassword", value)
                      }
                      error={signupFormValidation.errors.confirmPassword}
                      disabled={signupFormValidation.isSubmitting}
                    />
                  </FormField>

                  <div className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="terms"
                        checked={agreeToTerms}
                        onCheckedChange={(checked: boolean) =>
                          setAgreeToTerms(checked)
                        }
                        disabled={signupFormValidation.isSubmitting}
                      />
                      <label
                        htmlFor="terms"
                        className="text-sm leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                      >
                        <span className="text-muted-foreground">
                          <button
                            type="button"
                            className="text-primary underline"
                          >
                            이용약관
                          </button>{" "}
                          및{" "}
                          <button
                            type="button"
                            className="text-primary underline"
                          >
                            개인정보처리방침
                          </button>
                          에 동의합니다 *
                        </span>
                      </label>
                    </div>
                  </div>

                  <Button
                    type="submit"
                    className="w-full"
                    disabled={signupFormValidation.isSubmitting}
                  >
                    {signupFormValidation.isSubmitting
                      ? "회원가입 중..."
                      : "회원가입"}
                  </Button>
                </form>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>

        <div className="text-center mt-6">
          <Button variant="ghost" onClick={() => router.push("/")}>
            홈으로 돌아가기
          </Button>
        </div>
      </div>
    </div>
  );
}
