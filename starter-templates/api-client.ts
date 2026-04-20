// Example API client using axios
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add request/response interceptors if needed
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error);
    return Promise.reject(error);
  }
);

// Example API functions
export const coursesApi = {
  getAll: () => apiClient.get('/courses'),
  getById: (id: number) => apiClient.get(`/courses/${id}`),
};

export const studentsApi = {
  getById: (id: number) => apiClient.get(`/students/${id}`),
  getSchedule: (id: number) => apiClient.get(`/students/${id}/schedule`),
};

export const enrollmentsApi = {
  enroll: (studentId: number, courseId: number) =>
    apiClient.post('/enrollments', { studentId, courseId }),
};
