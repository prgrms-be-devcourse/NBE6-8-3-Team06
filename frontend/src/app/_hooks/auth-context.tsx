"use client";

import { createContext, useContext, useState, useEffect } from "react";
import { apiFetch } from "@/lib/apiFetch";

type User = {
  id: string;
  email: string;
  name: string;
};
type AuthContextType = {
  user: User | null;
  isLoggedIn: boolean;
  isLoading: boolean;
  login: () => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  // 초기 mount 시 로그인 상태 및 사용자 정보 확인
  useEffect(() => {
    (async () => {
      try {
        const userData = await apiFetch<User>("/user/my");
        setUser(userData);
        setIsLoggedIn(true);
      } catch {
        setIsLoggedIn(false);
      } finally {
        setIsLoading(false);
      }
    })();
  }, []);

  const login = async () => {
    setIsLoading(true);
    try {
      const userData = await apiFetch<User>("/user/my");
      setUser(userData);
      setIsLoggedIn(true);
    } catch {
      setUser(null);
      setIsLoggedIn(false);
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async () => {
    setIsLoading(true);
    try {
      await apiFetch("/user/logout", { method: "POST" });
    } catch {}
    setIsLoading(false);
    setIsLoggedIn(false);
    setUser(null);
  };
  return (
    <AuthContext.Provider
      value={{ isLoggedIn, user, isLoading, login, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth는 <AuthProvider> 내부에서만 사용할 수 있습니다.");
  }
  return ctx;
}
