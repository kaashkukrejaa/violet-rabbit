// Example Redux Toolkit store setup
import { configureStore } from '@reduxjs/toolkit';
// Import your slices here
// import coursesReducer from './slices/coursesSlice';
// import studentReducer from './slices/studentSlice';

export const store = configureStore({
  reducer: {
    // Add your reducers here
    // courses: coursesReducer,
    // student: studentReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
