"use client"

import { useReviewReport } from "@/app/_hooks/useReview";
import { getReasonBadge, getStatusBadge } from "@/components/ReviewReportBadge";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Textarea } from "@/components/ui/textarea";
import { PageResponseDto, ReviewResponseDto } from "@/types/book";
import { ReviewReportResponseDto } from "@/types/review";
import { AlertTriangle, ArrowLeft, Ban, Check, Clock, Edit, Eye, Shield, X } from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";


export default function AdminReportsPage() {
  const [notProceedReports, setNotProceedReports] = useState<ReviewReportResponseDto[]>([]);
  const [proceedReports, setProceedReports] = useState<ReviewReportResponseDto[]>([]);
  const [activeTab, setActiveTab] = useState('pending');
  const router = useRouter()
  const reviewReport = useReviewReport()

  const fetchReport = async(page:number, processed:boolean) => {
    const res:PageResponseDto<ReviewReportResponseDto> = await reviewReport.admSearchReviewReport(page, processed);
    if (res.data == null){
      return
    }
    if (processed){
      setProceedReports(res.data)
    }else{
      setNotProceedReports(res.data)
    }
  }

  useEffect(()=>{
    fetchReport(0, false);
    fetchReport(0, true);

  },[])

  const onNavigate = (path: string) => {
    router.push(path);
  };

  const moveToDetail = (reportId:number) => {
    onNavigate(`/adm/report/${reportId}`)
  }

  const changeTab = (value:string) => {
    if (value == activeTab){
      return
    }
    if (value === "pending"){
      fetchReport(0, false)
    }
    else if (value === "processed"){
      fetchReport(0, true)
    }
    setActiveTab(value)
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

      <Tabs value={activeTab} onValueChange={changeTab}>
        <TabsList className="grid w-full grid-cols-2 mb-6">
          <TabsTrigger value="pending" className="flex items-center space-x-2">
            <Clock className="h-4 w-4" />
            <span>처리 대기 ({notProceedReports.length})</span>
          </TabsTrigger>
          <TabsTrigger value="processed" className="flex items-center space-x-2">
            <Check className="h-4 w-4" />
            <span>처리 완료 ({proceedReports.length})</span>
          </TabsTrigger>
        </TabsList>

        <TabsContent value="pending">
          {notProceedReports.length === 0 ? (
            <Card>
              <CardContent className="py-8 text-center">
                <Shield className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="font-medium mb-2">처리 대기 중인 신고가 없습니다</h3>
                <p className="text-muted-foreground">모든 신고가 처리되었습니다.</p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-4">
              {notProceedReports.map((report) => (
                <Card key={report.id} className="hover:shadow-md transition-shadow">
                  <CardContent className="py-4">
                    <div className="flex justify-between items-start">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3 mb-2">
                          <h3 className="font-medium">{report.bookName}</h3>
                          {getReasonBadge(report.reason)}
                          {getStatusBadge(report.reportState)}
                        </div>
                        <p className="text-sm text-muted-foreground mb-2">
                          리뷰 작성자: {report.reviewAuthor} • 신고자: {report.memberName} • {report.createdDate}
                        </p>
                        <p className="text-sm text-muted-foreground line-clamp-2">
                          {report.description}
                        </p>
                      </div>
                      <Button
                        onClick={() => moveToDetail(report.id)}
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
          {proceedReports.length === 0 ? (
            <Card>
              <CardContent className="py-8 text-center">
                <Check className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="font-medium mb-2">처리 완료된 신고가 없습니다</h3>
                <p className="text-muted-foreground">아직 처리된 신고가 없습니다.</p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-4">
              {proceedReports.map((report) => (
                <Card key={report.id} className="hover:shadow-md transition-shadow">
                  <CardContent className="py-4">
                    <div className="flex justify-between items-start">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3 mb-2">
                          <h3 className="font-medium">{report.bookName}</h3>
                          {getReasonBadge(report.reason)}
                          {getStatusBadge(report.reportState)}
                        </div>
                        <p className="text-sm text-muted-foreground mb-2">
                          리뷰 작성자: {report.reviewAuthor} • 신고자: {report.memberName} • {report.createdDate}
                        </p>
                        <p className="text-sm text-muted-foreground line-clamp-2">
                          {report.description}
                        </p>
                      </div>
                      <Button
                        onClick={() => moveToDetail(report.id)}
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