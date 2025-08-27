"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Separator } from "@radix-ui/react-separator";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { User, Mail, Trash2, AlertTriangle } from "lucide-react";
import { apiFetch } from "@/lib/apiFetch";
import { toast } from "@/lib/toast";
import { useAuth } from "../_hooks/auth-context";

interface UserProfile {
  name: string;
  email: string;
}

export default function ProfilePage() {
  const router = useRouter();
  const { logout } = useAuth();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isDeletingAccount, setIsDeletingAccount] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);

  // 프로필 정보 가져오기
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await apiFetch<UserProfile>("/user/my");
        setProfile(response);
      } catch (error) {
        console.error("프로필 정보를 가져오는데 실패했습니다.", error);
        toast.error("프로필 정보를 불러올 수 없습니다.");
      } finally {
        setIsLoading(false);
      }
    };
    fetchProfile();
  }, []);

  // 회원 탈퇴 처리
  const handleDeleteAccount = async () => {
    setIsDeletingAccount(true);

    try {
      await apiFetch("/user/my", {
        method: "DELETE",
      });

      toast.success("회원탈퇴가 완료되었습니다.");
      setShowDeleteDialog(false);
      await logout();
      router.push("/"); // 홈 이동
    } catch (error) {
      console.error("회원탈퇴 실패", error);
      toast.error("회원탈퇴에 실패했습니다. 다시 시도해주세요.");
    } finally {
      setIsDeletingAccount(false);
    }
  };

  if (isLoading) {
    return (
      <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
          <p className="mt-2 text-muted-foreground">
            프로필 정보 불러오는 중...
          </p>
        </div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Card>
          <CardContent className="text-center py-8">
            <AlertTriangle className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
            <h3 className="text-lg font-medium mb-2">
              프로필 정보를 불러올 수 없습니다
            </h3>
            <p className="text-muted-foreground mb-4">
              잠시 후 다시 시도해주세요.
            </p>
            <Button onClick={() => router.push("/")}>홈으로 돌아가기</Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* 페이지 헤더 */}
      <div className="text-center mb-8">
        <h1 className="text-3xl font-bold mb-2">내 프로필</h1>
        <p className="text-muted-foreground">계정 정보를 확인하고 관리하세요</p>
      </div>

      {/* 프로필 정보 카드 */}
      <Card className="mb-8">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <User className="h-5 w-5" />
            계정 정보
          </CardTitle>
          <CardDescription>현재 등록된 계정 정보입니다</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* 이름 */}
          <div className="flex items-center space-x-4">
            <div className="flex-shrink-0">
              <div className="w-12 h-12 bg-primary/10 rounded-full flex items-center justify-center">
                <User className="h-6 w-6 text-primary" />
              </div>
            </div>
            <div className="flex-1">
              <p className="text-sm font-medium text-muted-foreground">이름</p>
              <p className="text-lg font-medium">{profile.name}</p>
            </div>
          </div>

          <Separator />

          {/* 이메일 */}
          <div className="flex items-center space-x-4">
            <div className="flex-shrink-0">
              <div className="w-12 h-12 bg-primary/10 rounded-full flex items-center justify-center">
                <Mail className="h-6 w-6 text-primary" />
              </div>
            </div>
            <div className="flex-1">
              <p className="text-sm font-medium text-muted-foreground">
                이메일
              </p>
              <p className="text-lg font-medium">{profile.email}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* 계정 관리 섹션 */}
      <Card>
        <CardHeader>
          <CardTitle className="text-destructive">주의사항</CardTitle>
          <CardDescription>
            신중하게 진행해주세요. 이 작업은 되돌릴 수 없습니다.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-between p-4 border border-destructive/20 rounded-lg bg-destructive/5">
            <div className="flex-1">
              <h4 className="font-medium text-destructive mb-1">계정 삭제</h4>
              <p className="text-sm text-muted-foreground">
                모든 데이터가 영구적으로 삭제됩니다. 이 작업은 되돌릴 수
                없습니다.
              </p>
            </div>
            <Dialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
              <DialogTrigger asChild>
                <Button
                  variant="destructive"
                  size="sm"
                  disabled={isDeletingAccount}
                  className="ml-4"
                >
                  <Trash2 className="h-4 w-4 mr-2" />
                  계정 삭제
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle className="flex items-center gap-2">
                    <AlertTriangle className="h-5 w-5 text-destructive" />
                    정말로 계정을 삭제하시겠습니까?
                  </DialogTitle>
                  <DialogDescription className="space-y-2">
                    <p>
                      이 작업은 <strong>되돌릴 수 없습니다</strong>.
                    </p>
                    <p>계정을 삭제하면 다음 정보들이 영구적으로 삭제됩니다:</p>
                    <ul className="list-disc list-inside space-y-1 text-sm">
                      <li>계정 정보 (이름, 이메일)</li>
                      <li>저장된 책 목록</li>
                      <li>작성한 리뷰</li>
                      <li>작성한 노트</li>
                    </ul>
                  </DialogDescription>
                </DialogHeader>
                <DialogFooter>
                  <Button
                    variant="outline"
                    onClick={() => setShowDeleteDialog(false)}
                    disabled={isDeletingAccount}
                  >
                    취소
                  </Button>
                  <Button
                    variant="destructive"
                    onClick={handleDeleteAccount}
                    disabled={isDeletingAccount}
                  >
                    {isDeletingAccount ? "삭제 중..." : "계정 삭제"}
                  </Button>
                </DialogFooter>
              </DialogContent>
            </Dialog>
          </div>
        </CardContent>
      </Card>

      {/* 하단 버튼 */}
      <div className="text-center mt-8">
        <Button variant="outline" onClick={() => router.push("/")}>
          홈으로 돌아가기
        </Button>
      </div>
    </div>
  );
}
