import { Card, CardContent } from "@/components/ui/card";
import { BookmarkReadStates } from "@/types/bookmarkData";

interface BookmarkStatsProps {
    stats: BookmarkReadStates | undefined;
}

export function BookmarkStats({ stats } : BookmarkStatsProps ) {
    return (

      <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-8">
        <Card>
        <CardContent className="p-4">
          <div className="text-2xl">{stats?.totalCount || 0}</div>
          <p className="text-sm text-muted-foreground">총 책 수</p>
        </CardContent>
      </Card>
      <Card>
        <CardContent className="p-4">
          <div className="text-2xl text-green-600">{stats?.readState.READ || 0}</div>
          <p className="text-sm text-muted-foreground">읽은 책</p>
        </CardContent>
      </Card>
      <Card>
        <CardContent className="p-4">
          <div className="text-2xl text-blue-600">{stats?.readState.READING || 0}</div>
          <p className="text-sm text-muted-foreground">읽고 있는 책</p>
        </CardContent>
      </Card>
      <Card>
        <CardContent className="p-4">
          <div className="text-2xl text-gray-600">{stats?.readState.WISH || 0}</div>
          <p className="text-sm text-muted-foreground">읽고 싶은 책</p>
        </CardContent>
      </Card>
      <Card>
        <CardContent className="p-4">
          <div className="text-2xl text-yellow-600">{(stats?.avgRate ?? 0).toFixed(1)}</div>
          <p className="text-sm text-muted-foreground">평균 평점</p>
        </CardContent>
      </Card>
      </div>
    );
}