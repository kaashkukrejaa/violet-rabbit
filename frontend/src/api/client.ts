/**
 * Type-safe API client for backend communication.
 * All requests go through /api proxy (configured in vite.config.ts).
 */
import axios, { AxiosError } from 'axios';
import type {
  ApiError,
  CourseOffering,
  Schedule,
  StudentProfile,
  StudentSummary,
} from '../types';

const client = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

/**
 * Normalizes any error into a typed ApiError format.
 * Extracts structured error from backend or creates generic error for unexpected failures.
 */
export function toApiError(err: unknown): ApiError {
  if (err instanceof AxiosError && err.response?.data) {
    const data = err.response.data as Partial<ApiError>;
    if (data.type && data.message) return data as ApiError;
  }
  const message =
    err instanceof Error ? err.message : 'An unexpected error occurred';
  return { type: 'unknown', message };
}

export const studentsApi = {
  list: () => client.get<StudentSummary[]>('/students').then((r) => r.data),
  getProfile: (id: number) =>
    client.get<StudentProfile>(`/students/${id}`).then((r) => r.data),
  getSchedule: (id: number) =>
    client.get<Schedule>(`/students/${id}/schedule`).then((r) => r.data),
};

export const coursesApi = {
  listOfferings: () =>
    client.get<CourseOffering[]>('/courses/offerings').then((r) => r.data),
};

export const enrollmentsApi = {
  enroll: (studentId: number, courseId: number) =>
    client.post<Schedule>('/enrollments', { studentId, courseId }).then((r) => r.data),
  drop: (studentId: number, courseId: number) =>
    client
      .delete<Schedule>('/enrollments', {
        data: { studentId, courseId },
      })
      .then((r) => r.data),
};
