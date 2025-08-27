export interface ValidationResult {
  isValid: boolean;
  error?: string;
}

export const validateEmail = (email: string): ValidationResult => {
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  if (!email.trim()) {
    return { isValid: false, error: "이메일을 입력해주세요." };
  }

  if (!emailPattern.test(email)) {
    return { isValid: false, error: "올바른 이메일 형식을 입력해주세요." };
  }

  return { isValid: true };
};

export const validatePassword = (password: string): ValidationResult => {
  if (!password) {
    return { isValid: false, error: "비밀번호를 입력해주세요." };
  }

  if (password.length < 8) {
    return { isValid: false, error: "비밀번호는 최소 8자 이상이어야 합니다." };
  }

  return { isValid: true };
};

export const validateName = (name: string): ValidationResult => {
  if (!name.trim()) {
    return { isValid: false, error: "이름을 입력해주세요." };
  }

  if (name.trim().length < 2) {
    return { isValid: false, error: "이름은 2자 이상이어야 합니다." };
  }

  return { isValid: true };
};

export const validatePasswordConfirmation = (
  password: string,
  confirmPassword: string
): ValidationResult => {
  if (!confirmPassword) {
    return { isValid: false, error: "비밀번호 확인을 입력해주세요." };
  }

  if (password !== confirmPassword) {
    return { isValid: false, error: "비밀번호가 일치하지 않습니다." };
  }

  return { isValid: true };
};

export const validateLoginForm = (email: string, password: string) => {
  const emailValidation = validateEmail(email);
  const passwordValidation = validatePassword(password);

  return {
    email: emailValidation.error || "",
    password: passwordValidation.error || "",
  };
};

export const validateSignupForm = (
  name: string,
  email: string,
  password: string,
  confirmPassword: string
) => {
  const nameValidation = validateName(name);
  const emailValidation = validateEmail(email);
  const passwordValidation = validatePassword(password);
  const confirmValidation = validatePasswordConfirmation(
    password,
    confirmPassword
  );

  return {
    name: nameValidation.error || "",
    email: emailValidation.error || "",
    password: passwordValidation.error || "",
    confirmPassword: confirmValidation.error || "",
  };
};
