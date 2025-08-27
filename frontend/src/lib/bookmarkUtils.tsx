import React from "react";
import { Star } from "lucide-react";

// 읽기 상태 한글 변환
export const getReadState = (readState: string) => {
  switch (readState) {
    case 'READ': return '읽은 책';
    case 'READING': return '읽고 있는 책';
    case 'WISH': return '읽고 싶은 책';
    default: return '모든 상태';
  }
};

// 읽기 상태에 따른 Badge 색상 변환
export const getReadStateColor = (readState: string) => {
  switch (readState) {
    case 'READ': return 'bg-green-100 text-green-800';
    case 'READING': return 'bg-blue-100 text-blue-800';
    case 'WISH': return 'bg-gray-100 text-gray-800';
    default: return 'bg-gray-100 text-gray-800';
  }
};

// 별점 렌더링
export const renderStars = (rating?: number) => {
    if (!rating) return null;
    return [...Array(5)].map((_, i) => (
      <Star
        key={i}
        className={`h-4 w-4 ${i < Math.floor(rating)
            ? 'fill-yellow-400 text-yellow-400'
            : 'text-gray-300'
          }`}
      />
    ));
  };