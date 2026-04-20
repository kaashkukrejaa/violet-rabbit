import { useQuery } from '@tanstack/react-query';
import { studentsApi } from '../api/client';

export function useStudents() {
  return useQuery({
    queryKey: ['students'],
    queryFn: studentsApi.list,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
}

export function useStudentProfile(studentId: number | null) {
  return useQuery({
    queryKey: ['student', studentId],
    queryFn: () => studentsApi.getProfile(studentId!),
    enabled: studentId !== null,
    staleTime: 2 * 60 * 1000, // 2 minutes
  });
}

export function useStudentSchedule(studentId: number | null) {
  return useQuery({
    queryKey: ['schedule', studentId],
    queryFn: () => studentsApi.getSchedule(studentId!),
    enabled: studentId !== null,
    staleTime: 1 * 60 * 1000, // 1 minute
  });
}
