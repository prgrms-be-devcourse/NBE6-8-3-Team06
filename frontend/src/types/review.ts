
export interface ReviewReportResponseDto{
    id:number;
    createdDate:string;
    reason:string;
    description:string;
    memberName:string;
    reportState:string;
    reviewAuthor:string;
    bookName:string;
};

export interface ReviewDetailResponseDto{
    id:number;
    content:string;
    rate:number;
    memberName:string;
}

export interface ReviewReportDetailResponseDto{
    id:number;
    reason:string;
    description:string;
    memberName:string;
    createdDate:string;
    reportState:string;
    review:ReviewDetailResponseDto;
    bookName:string;
    bookAuthor:string;
}