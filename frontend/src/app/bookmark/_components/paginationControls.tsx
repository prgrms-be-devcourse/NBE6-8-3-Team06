
import { Button } from '@/components/ui/button';

interface PaginationControlsProps {
    currentPage: number;
    totalPages: number;
    onChangePage: (page: number) => void;
}
export function PaginationControls({ currentPage, totalPages, onChangePage }: PaginationControlsProps) {
    if (totalPages <= 1) return null;
    return (
        <div className="flex justify-center items-center mt-8 space-x-4">
            <Button
                disabled={currentPage === 0}
                variant="outline"
                onClick={() => onChangePage(currentPage - 1)}
            >
                이전
            </Button>
            <div className="flex space-x-1">
                {Array.from({length: Math.min(totalPages, 5)}, (_, index) => {
                    let pageNum;
                    if(totalPages <= 5){
                        pageNum=index;
                    }else if(currentPage <= 2) {
                        pageNum = index;
                    }else if(currentPage >= totalPages - 3) {
                        pageNum = totalPages - 5 +index;
                    }else{
                        pageNum = currentPage - 2 + index;
                    }
                    return (
                        <Button
                        key={pageNum}
                        variant={currentPage === pageNum ? "default":"outline"}
                        size="sm"
                        onClick={() => onChangePage(pageNum)}>
                            {pageNum + 1}
                        </Button>
                    );
                })}
            </div>
            <Button
                onClick={() => onChangePage(currentPage + 1)}
                disabled={currentPage +1 >= totalPages}
                variant="outline"
            >
                다음
            </Button>
        </div>
    );
}