export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";

export async function apiFetch<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const baseUrl = API_BASE_URL;
  const url = path;

  const method = options.method || 'GET';
  const headers: Record<string, string> = {
    ...(options.headers || {}),
  };
  
  // GET 요청이 아닌 경우에만 Content-Type 헤더 추가
  if (method !== 'GET') {
    headers["Content-Type"] = "application/json";
  }

  let res = await fetch(`${baseUrl}${url}`, {
    ...options,
    headers,
    credentials: "include",
  });
  // 401 에러 처리 (로그인, 회원가입, 토큰재발급은 제외)
  if (
    res.status === 401 &&
    !url.includes("/user/login") &&
    !url.includes("/user/signup") &&
    !url.includes("/user/reissue") &&
    !url.includes("/categories")
  ) {
    const reissueRes = await fetch(`${baseUrl}/user/reissue`, {
      method: "POST",
      credentials: "include",
    });

    if (reissueRes.ok) {
      await new Promise((r) => setTimeout(r, 100)); // 쿠키 재적용 시간 확보

      // 요청 재시도
      res = await fetch(`${baseUrl}${url}`, {
        ...options,
        headers,
        credentials: "include",
      });
    } else {
      throw new Error("세션이 만료되었습니다. 다시 로그인해주세요.");
    }
  }
  // 혹시 빈 요청을 주면
  if (res.status === 204) {
    return {} as T;
  }

  if (!res.ok) {
    const error = await res.json().catch(() => ({}));
    const cleanedMsg = error.msg?.split(": ").slice(-1)[0];
    const err = new Error(cleanedMsg || error.message || "API 요청 실패");
    (err as any).data = error.data;
    throw err;
  }

  return res.json();
}
