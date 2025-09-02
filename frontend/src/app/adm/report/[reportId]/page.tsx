"use client"

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { Textarea } from "@/components/ui/textarea";
import { AlertTriangle, ArrowLeft, Ban, Check, Clock, Edit, X } from "lucide-react";
import { useState } from "react";
import { Badge } from "@/components/ui/badge";
import { getReasonBadge, getStatusBadge } from "@/components/ReviewReportBadge";
import { ReviewReportDetailResponseDto } from "@/types/review";

export default function page(){
    const [selectedReport, setSelectedReport] = useState<ReviewReportDetailResponseDto | null>(null);
    const [actionType, setActionType] = useState<'approved' | 'report_rejected' | 'modification_requested' | 'deleted' | null>(null);
    const [isMessageDialogOpen, setIsMessageDialogOpen] = useState(false);
    const [adminMessage, setAdminMessage] = useState('');

      const handleConfirmAction = () => {
        if (!selectedReport || !actionType) return;
        
        if (actionType === 'approved') {
          
        } else if (actionType === 'report_rejected') {
          
        } else if (actionType === 'modification_requested') {
          
        } else {
          
        }
      };

      const openMessageDialog = (type: 'approved' | 'report_rejected' | 'modification_requested' | 'deleted') => {
        setActionType(type);
        setIsMessageDialogOpen(true);
      };
    

    if (selectedReport == null){
        return <></>
    }

    return (
        <div className="container mx-auto px-4 py-8 max-w-4xl">
          <Button
            onClick={() => setSelectedReport(null)}
            variant="ghost"
            className="mb-6 flex items-center space-x-2"
          >
            <ArrowLeft className="h-4 w-4" />
            <span>목록으로 돌아가기</span>
          </Button>
  
          <Card>
            <CardHeader>
              <div className="flex justify-between items-start">
                <div>
                  <CardTitle className="flex items-center space-x-2">
                    <AlertTriangle className="h-5 w-5 text-destructive" />
                    <span>신고된 리뷰 상세</span>
                  </CardTitle>
                  <p className="text-muted-foreground mt-1">
                    신고 ID: #{selectedReport.id} • 신고일: {selectedReport.createdDate}
                  </p>
                </div>
                {getStatusBadge(selectedReport.reportState)}
              </div>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* 신고 정보 */}
              <div>
                <h3 className="font-medium mb-3">신고 정보</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 p-4 bg-muted rounded-lg">
                  <div>
                    <label className="text-sm font-medium text-muted-foreground">신고자</label>
                    <p>{selectedReport.memberName}</p>
                  </div>
                  <div>
                    <label className="text-sm font-medium text-muted-foreground">신고 사유</label>
                    <div className="mt-1">
                      {getReasonBadge(selectedReport.reason)}
                    </div>
                  </div>
                  <div className="md:col-span-2">
                    <label className="text-sm font-medium text-muted-foreground">신고 상세 내용</label>
                    <p className="mt-1">{selectedReport.description}</p>
                  </div>
                </div>
              </div>
  
              <Separator />
  
              {/* 책 정보 */}
              <div>
                <h3 className="font-medium mb-3">책 정보</h3>
                <div className="p-4 bg-muted rounded-lg">
                  <h4 className="font-medium">{selectedReport.bookName}</h4>
                  <p className="text-muted-foreground">{selectedReport.bookAuthor}</p>
                </div>
              </div>
  
              <Separator />
  
              {/* 리뷰 내용 */}
              <div>
                <h3 className="font-medium mb-3">신고된 리뷰</h3>
                <div className="p-4 border rounded-lg">
                  <div className="flex justify-between items-start mb-3">
                    <div>
                      <p className="font-medium">{selectedReport.review.memberName}</p>
                      <div className="flex items-center space-x-1">
                        {[...Array(5)].map((_, i) => (
                          <span key={i} className={i < selectedReport.review.rate ? "text-yellow-400" : "text-gray-300"}>
                            ★
                          </span>
                        ))}
                      </div>
                    </div>
                  </div>
                  <p className="text-foreground">{selectedReport.review.content}</p>
                </div>
              </div>
  
              {selectedReport.reportState === 'pending' && (
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-4">
                  <Button 
                    onClick={() => openMessageDialog('approved')}
                    variant="outline"
                    className="flex items-center justify-center space-x-2 border-green-200 text-green-700 hover:bg-green-50"
                  >
                    <Check className="h-4 w-4" />
                    <span>리뷰 승인</span>
                  </Button>
                  <Button 
                    onClick={() => openMessageDialog('report_rejected')}
                    variant="outline"
                    className="flex items-center justify-center space-x-2 border-blue-200 text-blue-700 hover:bg-blue-50"
                  >
                    <Ban className="h-4 w-4" />
                    <span>신고 기각</span>
                  </Button>
                  <Button 
                    onClick={() => openMessageDialog('modification_requested')}
                    variant="outline"
                    className="flex items-center justify-center space-x-2 border-orange-200 text-orange-700 hover:bg-orange-50"
                  >
                    <Edit className="h-4 w-4" />
                    <span>수정 요청</span>
                  </Button>
                  <Button 
                    onClick={() => openMessageDialog('deleted')}
                    variant="destructive"
                    className="flex items-center justify-center space-x-2"
                  >
                    <X className="h-4 w-4" />
                    <span>리뷰 삭제</span>
                  </Button>
                </div>
              )}
  
              {/* 어드민 메시지 다이얼로그 */}
              <Dialog open={isMessageDialogOpen} onOpenChange={setIsMessageDialogOpen}>
                <DialogContent className="max-w-md">
                  <DialogHeader>
                    <DialogTitle>
                      {actionType === 'approved' ? '리뷰 승인 처리' : 
                       actionType === 'report_rejected' ? '신고 기각 처리' :
                       actionType === 'modification_requested' ? '수정 요청 처리' : '리뷰 삭제 처리'}
                    </DialogTitle>
                    <DialogDescription>
                      {actionType === 'approved' 
                        ? '리뷰를 승인합니다. 리뷰 작성자에게 전달할 메시지를 입력해주세요 (선택사항).'
                        : actionType === 'report_rejected'
                        ? '신고를 부당하다고 판단하여 기각합니다. 신고자에게 전달할 메시지를 입력해주세요 (선택사항).'
                        : actionType === 'modification_requested'
                        ? '리뷰 수정을 요청합니다. 리뷰 작성자에게 전달할 메시지를 입력해주세요 (필수).'
                        : '리뷰를 삭제합니다. 리뷰 작성자에게 전달할 메시지를 입력해주세요 (필수).'
                      }
                    </DialogDescription>
                  </DialogHeader>
                  <div className="space-y-4">
                    <div className="space-y-2">
                      <Label htmlFor="admin-message">어드민 메시지</Label>
                      <Textarea
                        id="admin-message"
                        placeholder={
                          actionType === 'approved' 
                            ? "리뷰가 커뮤니티 가이드라인에 적합하다고 판단되어 승인되었습니다."
                            : actionType === 'report_rejected'
                            ? "신고 내용을 검토한 결과, 해당 리뷰는 커뮤니티 가이드라인에 위반되지 않는다고 판단됩니다."
                            : actionType === 'modification_requested'
                            ? "리뷰에서 일부 수정이 필요한 부분이 발견되었습니다. 가이드라인에 따라 수정해 주시기 바랍니다."
                            : "리뷰가 커뮤니티 가이드라인에 위반되어 삭제되었습니다. 향후 가이드라인을 준수해 주시기 바랍니다."
                        }
                        value={adminMessage}
                        onChange={(e) => setAdminMessage(e.target.value)}
                        rows={3}
                      />
                    </div>
                    <div className="flex justify-end space-x-2">
                      <Button variant="outline" onClick={() => setIsMessageDialogOpen(false)}>
                        취소
                      </Button>
                      <Button 
                        onClick={handleConfirmAction}
                        variant={actionType === 'deleted' ? 'destructive' : 'default'}
                        disabled={(actionType === 'deleted' || actionType === 'modification_requested') && !adminMessage.trim()}
                      >
                        {actionType === 'approved' ? '승인하기' : 
                         actionType === 'report_rejected' ? '신고 기각하기' :
                         actionType === 'modification_requested' ? '수정 요청하기' : '삭제하기'}
                      </Button>
                    </div>
                  </div>
                </DialogContent>
              </Dialog>
            </CardContent>
          </Card>
        </div>
      );
}