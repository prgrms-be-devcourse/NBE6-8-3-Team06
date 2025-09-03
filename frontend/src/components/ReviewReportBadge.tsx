"use client"

import { Ban, Check, Clock, X } from "lucide-react";
import { Badge } from "./ui/badge";

export const getStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING':
        return <Badge variant="secondary" className="bg-yellow-100 text-yellow-800"><Clock className="w-3 h-3 mr-1" />대기 중</Badge>;
      case 'ACCEPT':
        return <Badge variant="secondary" className="bg-green-100 text-green-800"><Check className="w-3 h-3 mr-1" />승인됨</Badge>;
      case 'DELETE':
        return <Badge variant="destructive"><X className="w-3 h-3 mr-1" />삭제됨</Badge>;
      case 'REJECT':
        return <Badge variant="outline"><Ban className="w-3 h-3 mr-1" />신고 기각</Badge>;
      default:
        return null;
    }
};

export const getReasonBadge = (reason: string) => {
    switch (reason) {
      case '부적절한 언어':
        return <Badge variant="destructive">부적절한 언어</Badge>;
      case '스포일러':
        return <Badge variant="secondary" className="bg-orange-100 text-orange-800">스포일러</Badge>;
      case '건설적이지 않은 비판':
        return <Badge variant="outline">건설적이지 않은 비판</Badge>;
      case '허위 정보':
        return <Badge variant="destructive">허위 정보</Badge>;
      default:
        return <Badge variant="outline">{reason}</Badge>;
    }
  };