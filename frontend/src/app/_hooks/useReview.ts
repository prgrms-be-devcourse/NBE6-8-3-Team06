import { apiFetch } from "@/lib/apiFetch";
import { ApiResponse } from "@/types/auth";
import { PageResponseDto, ReviewResponseDto } from "@/types/book";
import { useState } from "react";

export const useReviewRecommend = () =>{

  const createReviewRecommend = async (reviewId:number, isRecommend:boolean) => {
    const res = await apiFetch<ApiResponse>(`/reviewRecommend/${reviewId}/${isRecommend}`,{
      method:"POST",
      headers:{
        "Content-Type":"application/json"
      }});
  }

  const modifyReviewRecomend = async (reviewId:number, isRecommend:boolean) => {
    const res = await apiFetch<ApiResponse>(`/reviewRecommend/${reviewId}/${isRecommend}`,{
      method:"PUT",
      headers:{
        "Content-Type":"application/json"
      }
    });
  }

  const deleteReviewRecommend = async (reviewId:number) => {
    const res = await apiFetch<ApiResponse>(`/reviewRecommend/${reviewId}`, {
      method:"DELETE",
      headers:{
        "Content-Type":"application/json"
      }
    });
  }

  const formatLikes = (num:number) => {
    const units = [
      { value: 1_000_000_000_000, symbol: 'T' }, // 조 (Trillion)
      { value: 1_000_000_000, symbol: 'B' },     // 십억 (Billion)
      { value: 1_000_000, symbol: 'M' },         // 백만 (Million)
      { value: 1_000, symbol: 'K' },             // 천 (Thousand)
    ];
  
    for (const unit of units) {
      if (num >= unit.value) {
        return (num / unit.value).toFixed(1).replace(/\.0$/, '') + unit.symbol;
      }
    }
  
    return num.toString(); // 단위 적용 안 되는 작은 숫자
  }

  return {
    createReviewRecommend,
    modifyReviewRecomend,
    deleteReviewRecommend,
    formatLikes
  }
}

export const useReview = (initBookId:number) =>{
  const [bookId,setBookId] = useState<number>(initBookId);
  
  const getReview = async () =>{
    const res = await apiFetch<ApiResponse>(`/reviews/${bookId}`, {
      method:"GET",
      headers:{
        "Content-Type":"application/json",
      }});
    const data:ReviewResponseDto = res.data;
    return data;
  }

  const getReviews = async (page:number) => {
    const res = await apiFetch<ApiResponse<PageResponseDto<ReviewResponseDto>>>(`/reviews/${bookId}/list?page=${page}`,{
      method:"GET"
    })
    const data:PageResponseDto<ReviewResponseDto> = res.data;
    return data;
  }

  const createReview = async ({ rating, content } : {rating:number, content:string}) => {
      await apiFetch<ApiResponse>(`/reviews/${bookId}`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body:JSON.stringify({"content":content, "rate":rating})
        });
  }

  const editReview = async({rating, content}:{rating:Number, content:string}) => {
    await apiFetch<ApiResponse>(`/reviews/${bookId}`, {
      method:"PUT",
      headers:{
        "Content-Type":"application/json",
      },
      body:JSON.stringify({"content":content, "rate":rating})
    });
  }

  const deleteReview = async() =>{
    await apiFetch<ApiResponse>(`/reviews/${bookId}`, {
      method:"DELETE",
      headers:{
        "Content-Type":"application/json",
      },
    })
  }

  

  return {
      createReview,
      editReview,
      deleteReview,
      getReview,
      getReviews,
      setBookId
  }
}