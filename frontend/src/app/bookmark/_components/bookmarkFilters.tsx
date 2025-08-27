
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Search } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Category } from '@/types/category';
import { getReadState } from '@/lib/bookmarkUtils';
import { Button } from '@/components/ui/button';

interface BookmarkFiltersProps {
  searchKeyword: string;
  onSearchKeywordChange: (keyword: string) => void;
  selectedCategory: string;
  onCategoryChange: (category: string) => void;
  categories: Category[];
  selectedReadState: string;
  onReadStateChange: (state: string) => void;
  readStates: string[];
  setCurrentPage: (page: number) => void;
}

export function BookmarkFilters({
  searchKeyword,
  onSearchKeywordChange,
  selectedCategory,
  onCategoryChange,
  categories,
  selectedReadState,
  onReadStateChange,
  readStates,
  setCurrentPage
}: BookmarkFiltersProps) {
  return (
    <div className="mb-8 space-y-4">
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
          <Input
            placeholder="책 제목 또는 저자 검색..."
            value={searchKeyword}
            onChange={(e) => onSearchKeywordChange(e.target.value)}
            className="pl-10"
          />
        </div>
        <Select value={selectedCategory} onValueChange={(value) => {
          onCategoryChange(value);
          setCurrentPage(0);
        }}>
          <SelectTrigger className="w-full sm:w-48">
            <SelectValue placeholder="카테고리 선택" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">모든 카테고리</SelectItem>
            {categories.map((category) => (
              <SelectItem key={category.name} value={category.name}>{category.name}</SelectItem>
            ))}
          </SelectContent>
        </Select>
        <Select value={selectedReadState} onValueChange={(value) => {
          onReadStateChange(value);
          setCurrentPage(0);
        }}>
          <SelectTrigger className="w-full sm:w-48">
            <SelectValue placeholder="읽기 상태 선택" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">모든 상태</SelectItem>
            {readStates.map((readState) => (
              <SelectItem key={readState} value={readState}>{getReadState(readState)}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
    </div>
  );
}