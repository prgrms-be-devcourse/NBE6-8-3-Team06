"use client";

import { useState, useCallback } from "react";

export interface FormErrors {
  [key: string]: string;
}

export interface UseFormValidationProps<T> {
  initialValues: T;
  validate: (values: T) => FormErrors;
  onSubmit: (values: T) => Promise<{ success: boolean; error?: string }>;
}

export function useFormValidation<T extends Record<string, any>>({
  initialValues,
  validate,
  onSubmit,
}: UseFormValidationProps<T>) {
  const [values, setValues] = useState<T>(initialValues);
  const [errors, setErrors] = useState<FormErrors>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [touched, setTouched] = useState<Record<string, boolean>>({});

  // 필드 값 변경
  const setValue = useCallback(
    (field: keyof T, value: any) => {
      setValues((prev) => ({ ...prev, [field]: value }));

      // 해당 필드 에러 클리어
      if (errors[field as string]) {
        setErrors((prev) => ({ ...prev, [field as string]: "" }));
      }
    },
    [errors]
  );

  // 필드 터치 상태 설정
  const setFieldTouched = useCallback((field: keyof T) => {
    setTouched((prev) => ({ ...prev, [field as string]: true }));
  }, []);

  // 실시간 검증 (필드 블러 시)
  const validateField = useCallback(
    (field: keyof T) => {
      const fieldErrors = validate(values);
      const fieldError = fieldErrors[field as string];

      setErrors((prev) => ({ ...prev, [field as string]: fieldError || "" }));
      setFieldTouched(field);
    },
    [values, validate]
  );

  // 전체 폼 검증
  const validateForm = useCallback(() => {
    const formErrors = validate(values);
    setErrors(formErrors);

    // 모든 필드를 touched로 설정
    const allTouched: Record<string, boolean> = {};
    Object.keys(values).forEach((key) => {
      allTouched[key] = true;
    });
    setTouched(allTouched);

    return Object.keys(formErrors).every((key) => !formErrors[key]);
  }, [values, validate]);

  // 폼 제출
  const handleSubmit = useCallback(
    async (e?: React.FormEvent) => {
      if (e) {
        e.preventDefault();
      }

      if (!validateForm()) {
        return { success: false, error: "입력 정보를 확인해주세요." };
      }

      setIsSubmitting(true);
      try {
        const result = await onSubmit(values);
        return result;
      } finally {
        setIsSubmitting(false);
      }
    },
    [values, validateForm, onSubmit]
  );

  // 폼 리셋
  const resetForm = useCallback(() => {
    setValues(initialValues);
    setErrors({});
    setTouched({});
    setIsSubmitting(false);
  }, [initialValues]);

  return {
    values,
    errors,
    touched,
    isSubmitting,
    setValue,
    validateField,
    handleSubmit,
    resetForm,
    isValid: Object.keys(errors).every((key) => !errors[key]),
  };
}
