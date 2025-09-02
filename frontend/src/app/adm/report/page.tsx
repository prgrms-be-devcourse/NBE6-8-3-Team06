"use client"

import { Alert, AlertDescription } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Textarea } from "@/components/ui/textarea";
import { AlertTriangle, ArrowLeft, Ban, Check, Clock, Edit, Eye, Shield, X } from "lucide-react";
import { useState } from "react";

interface ReportedReview {
  id: number;
  reviewId: number;
  bookTitle: string;
  bookAuthor: string;
  reviewAuthor: string;
  reviewContent: string;
  rating: number;
  reportedBy: string;
  reportReason: string;
  reportDescription: string;
  reportDate: string;
  status: 'pending' | 'approved' | 'rejected' | 'deleted';
}

export default function AdminReportsPage() {
  const [reports, setReports] = useState<ReportedReview[]>([]);
  const [selectedReport, setSelectedReport] = useState<ReportedReview | null>(null);
  const [activeTab, setActiveTab] = useState('pending');
  const [adminMessage, setAdminMessage] = useState('');
  const [actionType, setActionType] = useState<'approved' | 'report_rejected' | 'modification_requested' | 'deleted' | null>(null);
  const [isMessageDialogOpen, setIsMessageDialogOpen] = useState(false);

  const pendingReports = reports.filter(report => report.status === 'pending');
  const processedReports = reports.filter(report => report.status !== 'pending');

  const handleApproveReview = (reportId: number, message?: string) => {
    const report = reports.find(r => r.id === reportId);
    
    setReports(prev => 
      prev.map(report => 
        report.id === reportId ? { ...report, status: 'approved' as const } : report
      )
    );
    setSelectedReport(null);
    setAdminMessage('');
    setIsMessageDialogOpen(false);
  };

  const handleReportReject = (reportId: number, message?: string) => {
    const report = reports.find(r => r.id === reportId);
    
    setReports(prev => 
      prev.map(report => 
        report.id === reportId ? { ...report, status: 'rejected' as const } : report
      )
    );
    setSelectedReport(null);
    setAdminMessage('');
    setIsMessageDialogOpen(false);
  };

  const handleModificationRequest = (reportId: number, message?: string) => {
    const report = reports.find(r => r.id === reportId);
    
    setReports(prev => 
      prev.map(report => 
        report.id === reportId ? { ...report, status: 'approved' as const } : report
      )
    );
    setSelectedReport(null);
    setAdminMessage('');
    setIsMessageDialogOpen(false);
  };

  const handleDeleteReview = (reportId: number, message?: string) => {
    const report = reports.find(r => r.id === reportId);
    
    setReports(prev => 
      prev.map(report => 
        report.id === reportId ? { ...report, status: 'deleted' as const } : report
      )
    );
    setSelectedReport(null);
    setAdminMessage('');
    setIsMessageDialogOpen(false);
  };

  const openMessageDialog = (type: 'approved' | 'report_rejected' | 'modification_requested' | 'deleted') => {
    setActionType(type);
    setIsMessageDialogOpen(true);
  };

  const handleConfirmAction = () => {
    if (!selectedReport || !actionType) return;
    
    if (actionType === 'approved') {
      handleApproveReview(selectedReport.id, adminMessage);
    } else if (actionType === 'report_rejected') {
      handleReportReject(selectedReport.id, adminMessage);
    } else if (actionType === 'modification_requested') {
      handleModificationRequest(selectedReport.id, adminMessage);
    } else {
      handleDeleteReview(selectedReport.id, adminMessage);
    }
  };

  const handleRejectReport = (reportId: number) => {
    setReports(prev => 
      prev.map(report => 
        report.id === reportId ? { ...report, status: 'rejected' as const } : report
      )
    );
    setSelectedReport(null);
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'pending':
        return <Badge variant="secondary" className="bg-yellow-100 text-yellow-800"><Clock className="w-3 h-3 mr-1" />대기 중</Badge>;
      case 'approved':
        return <Badge variant="secondary" className="bg-green-100 text-green-800"><Check className="w-3 h-3 mr-1" />승인됨</Badge>;
      case 'deleted':
        return <Badge variant="destructive"><X className="w-3 h-3 mr-1" />삭제됨</Badge>;
      case 'rejected':
        return <Badge variant="outline"><Ban className="w-3 h-3 mr-1" />신고 기각</Badge>;
      default:
        return null;
    }
  };

  const getReasonBadge = (reason: string) => {
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

  if (selectedReport) {
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
                  신고 ID: #{selectedReport.id} • 신고일: {selectedReport.reportDate}
                </p>
              </div>
              {getStatusBadge(selectedReport.status)}
            </div>
          </CardHeader>
          <CardContent className="space-y-6">
            {/* 신고 정보 */}
            <div>
              <h3 className="font-medium mb-3">신고 정보</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 p-4 bg-muted rounded-lg">
                <div>
                  <label className="text-sm font-medium text-muted-foreground">신고자</label>
                  <p>{selectedReport.reportedBy}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-muted-foreground">신고 사유</label>
                  <div className="mt-1">
                    {getReasonBadge(selectedReport.reportReason)}
                  </div>
                </div>
                <div className="md:col-span-2">
                  <label className="text-sm font-medium text-muted-foreground">신고 상세 내용</label>
                  <p className="mt-1">{selectedReport.reportDescription}</p>
                </div>
              </div>
            </div>

            <Separator />

            {/* 책 정보 */}
            <div>
              <h3 className="font-medium mb-3">책 정보</h3>
              <div className="p-4 bg-muted rounded-lg">
                <h4 className="font-medium">{selectedReport.bookTitle}</h4>
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
                    <p className="font-medium">{selectedReport.reviewAuthor}</p>
                    <div className="flex items-center space-x-1">
                      {[...Array(5)].map((_, i) => (
                        <span key={i} className={i < selectedReport.rating ? "text-yellow-400" : "text-gray-300"}>
                          ★
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
                <p className="text-foreground">{selectedReport.reviewContent}</p>
              </div>
            </div>

            {selectedReport.status === 'pending' && (
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

  return (
    <div className="container mx-auto px-4 py-8 max-w-6xl">
      <div className="flex items-center space-x-3 mb-8">
        <Shield className="h-8 w-8 text-primary" />
        <div>
          <h1 className="text-2xl font-medium">신고 관리</h1>
          <p className="text-muted-foreground">사용자가 신고한 리뷰를 검토하고 처리할 수 있습니다.</p>
        </div>
      </div>

      <Alert className="mb-6">
        <AlertTriangle className="h-4 w-4" />
        <AlertDescription>
          신고된 리뷰는 신중하게 검토한 후 처리해주세요. 커뮤니티 가이드라인을 기준으로 판단하시기 바랍니다.
        </AlertDescription>
      </Alert>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-2 mb-6">
          <TabsTrigger value="pending" className="flex items-center space-x-2">
            <Clock className="h-4 w-4" />
            <span>처리 대기 ({pendingReports.length})</span>
          </TabsTrigger>
          <TabsTrigger value="processed" className="flex items-center space-x-2">
            <Check className="h-4 w-4" />
            <span>처리 완료 ({processedReports.length})</span>
          </TabsTrigger>
        </TabsList>

        <TabsContent value="pending">
          {pendingReports.length === 0 ? (
            <Card>
              <CardContent className="py-8 text-center">
                <Shield className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="font-medium mb-2">처리 대기 중인 신고가 없습니다</h3>
                <p className="text-muted-foreground">모든 신고가 처리되었습니다.</p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-4">
              {pendingReports.map((report) => (
                <Card key={report.id} className="hover:shadow-md transition-shadow">
                  <CardContent className="py-4">
                    <div className="flex justify-between items-start">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3 mb-2">
                          <h3 className="font-medium">{report.bookTitle}</h3>
                          {getReasonBadge(report.reportReason)}
                          {getStatusBadge(report.status)}
                        </div>
                        <p className="text-sm text-muted-foreground mb-2">
                          리뷰 작성자: {report.reviewAuthor} • 신고자: {report.reportedBy} • {report.reportDate}
                        </p>
                        <p className="text-sm text-muted-foreground line-clamp-2">
                          {report.reportDescription}
                        </p>
                      </div>
                      <Button
                        onClick={() => setSelectedReport(report)}
                        variant="outline"
                        size="sm"
                        className="ml-4 flex items-center space-x-2"
                      >
                        <Eye className="h-4 w-4" />
                        <span>상세 보기</span>
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="processed">
          {processedReports.length === 0 ? (
            <Card>
              <CardContent className="py-8 text-center">
                <Check className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="font-medium mb-2">처리 완료된 신고가 없습니다</h3>
                <p className="text-muted-foreground">아직 처리된 신고가 없습니다.</p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-4">
              {processedReports.map((report) => (
                <Card key={report.id} className="hover:shadow-md transition-shadow">
                  <CardContent className="py-4">
                    <div className="flex justify-between items-start">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3 mb-2">
                          <h3 className="font-medium">{report.bookTitle}</h3>
                          {getReasonBadge(report.reportReason)}
                          {getStatusBadge(report.status)}
                        </div>
                        <p className="text-sm text-muted-foreground mb-2">
                          리뷰 작성자: {report.reviewAuthor} • 신고자: {report.reportedBy} • {report.reportDate}
                        </p>
                        <p className="text-sm text-muted-foreground line-clamp-2">
                          {report.reportDescription}
                        </p>
                      </div>
                      <Button
                        onClick={() => setSelectedReport(report)}
                        variant="outline"
                        size="sm"
                        className="ml-4 flex items-center space-x-2"
                      >
                        <Eye className="h-4 w-4" />
                        <span>상세 보기</span>
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}