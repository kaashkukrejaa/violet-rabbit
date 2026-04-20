import { useMutation, useQueryClient } from '@tanstack/react-query';
import { enrollmentsApi, toApiError } from '../api/client';
import { useToast } from '../contexts/ToastContext';

export function useEnrollInCourse() {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({ studentId, courseId }: { studentId: number; courseId: number }) =>
      enrollmentsApi.enroll(studentId, courseId),
    onSuccess: (newSchedule, variables) => {
      queryClient.setQueryData(['schedule', variables.studentId], newSchedule);
      queryClient.invalidateQueries({ queryKey: ['student', variables.studentId] });

      const enrolledCourse = newSchedule.items.find(
        (item) => item.course.id === variables.courseId
      );
      showToast(
        `Enrolled in ${enrolledCourse?.course.code || 'course'}`,
        'success'
      );
    },
    onError: (error: unknown) => {
      showToast(toApiError(error).message, 'error');
    },
  });
}

export function useDropCourse() {
  const queryClient = useQueryClient();
  const { showToast } = useToast();

  return useMutation({
    mutationFn: ({ studentId, courseId }: { studentId: number; courseId: number }) =>
      enrollmentsApi.drop(studentId, courseId),
    onSuccess: (newSchedule, variables) => {
      queryClient.setQueryData(['schedule', variables.studentId], newSchedule);
      queryClient.invalidateQueries({ queryKey: ['student', variables.studentId] });

      showToast('Course dropped successfully', 'success');
    },
    onError: (error: unknown) => {
      showToast(toApiError(error).message, 'error');
    },
  });
}
