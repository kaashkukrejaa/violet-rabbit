import { useQuery } from '@tanstack/react-query';
import { coursesApi } from '../api/client';

export function useCourseOfferings() {
  return useQuery({
    queryKey: ['offerings'],
    queryFn: coursesApi.listOfferings,
    staleTime: 10 * 60 * 1000, // 10 minutes (course offerings don't change often)
  });
}
