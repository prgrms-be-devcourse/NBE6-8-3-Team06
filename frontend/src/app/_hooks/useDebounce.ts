import { useState, useEffect } from "react";

/** 값 변경 후 일정 시간 뒤에 값 반환
 * 예) 검색어 입력 후 0.5초 뒤에 검색 실행
 * @param value 변경될 값
 * @param delay 디바운스 지연 시간 (밀리초)
 * @returns 디바운스된 값
*/
export function useDebounce<T>(value: T, delay: number): T {
    const [debouncedValue, setDebouncedValue] = useState<T>(value);

    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedValue(value);
        }, delay);

        return () => {
            clearTimeout(handler);
        }
    }, [value, delay]);

    return debouncedValue;
}