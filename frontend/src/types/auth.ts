export interface LoginForm {
  email: string;
  password: string;
}

export interface SignupForm {
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export interface User {
  id: string;
  email: string;
  name: string;
}

export interface ApiResponse<T = any> {
  resultCode: string;
  msg: string;
  data: T;
}
