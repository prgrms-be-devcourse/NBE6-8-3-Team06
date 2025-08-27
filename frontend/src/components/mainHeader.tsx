"use client";

import React from "react";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "../app/_hooks/auth-context";
import { Button } from "@/components/ui/button";
import { BookOpen, Home, User, LogOut, Library, Sun, Moon } from "lucide-react";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { useTheme } from "next-themes";
import { Toaster } from "@/components/ui/sonner";

function ThemeButton(){
    const { setTheme } = useTheme()
    return (
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="outline" size="icon">
            <Sun className="h-[1.2rem] w-[1.2rem] scale-100 rotate-0 transition-all dark:scale-0 dark:-rotate-90" />
            <Moon className="absolute h-[1.2rem] w-[1.2rem] scale-0 rotate-90 transition-all dark:scale-100 dark:rotate-0" />
            <span className="sr-only">Toggle theme</span>
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          <DropdownMenuItem onClick={() => setTheme("light")}>
            Light
          </DropdownMenuItem>
          <DropdownMenuItem onClick={() => setTheme("dark")}>
            Dark
          </DropdownMenuItem>
          <DropdownMenuItem onClick={() => setTheme("system")}>
            System
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    )
  }

// 기존 NavigationContent의 이름을 Header로 변경
export function Header({ children }: { children: React.ReactNode }) {
  const currentPage = usePathname();
  const router = useRouter();
  const { isLoggedIn, logout } = useAuth();
  const onNavigate = (url: string) => {
    router.push(url);
  };

  return (
    <>
      <Toaster richColors></Toaster>
      <nav className="border-b bg-card">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center space-x-8">
              <h1 className="text-xl font-medium text-primary">
                책 관리 시스템
              </h1>

              <div className="flex space-x-4">
                <Button
                  variant={currentPage === "/" ? "default" : "ghost"}
                  onClick={() => onNavigate("/")}
                  className="flex items-center space-x-2"
                >
                  <Home className="h-4 w-4" />
                  <span>홈</span>
                </Button>

                <Button
                  variant={currentPage === "/books" ? "default" : "ghost"}
                  onClick={() => onNavigate("/books")}
                  className="flex items-center space-x-2"
                >
                  <BookOpen className="h-4 w-4" />
                  <span>책 탐색</span>
                </Button>

                {isLoggedIn && (
                  <Button
                    variant={currentPage === "/bookmark" ? "default" : "ghost"}
                    onClick={() => onNavigate("/bookmark")}
                    className="flex items-center space-x-2"
                  >
                    <Library className="h-4 w-4" />
                    <span>내 책</span>
                  </Button>
                )}
              </div>
            </div>

            <div className="flex items-center space-x-4">
              {isLoggedIn ? (
                <>
                  <Button
                    variant="ghost"
                    onClick={() => onNavigate("/profile")}
                    className="flex items-center space-x-2"
                  >
                    <User className="h-4 w-4" />
                    <span>내 프로필</span>
                  </Button>
                  <Button
                    variant="outline"
                    onClick={logout} // 변경: 실제 로그아웃 함수 사용
                    className="flex items-center space-x-2"
                  >
                    <LogOut className="h-4 w-4" />
                    <span>로그아웃</span>
                  </Button>
                </>
              ) : (
                <Button
                  variant={currentPage === "/login" ? "default" : "outline"}
                  onClick={() => onNavigate("/login")}
                >
                  로그인
                </Button>
              )}
              <ThemeButton />
            </div>
          </div>
        </div>
      </nav>
      <main>{children}</main>
    </>
  );
}