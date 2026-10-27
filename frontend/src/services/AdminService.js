import axiosClient from '../api/axiosClient';

// === ESTUDIANTES ===
export const getAllStudents = async () => {
  const response = await axiosClient.get('/api/students');
  return response.data;
};

export const createStudent = async (studentData) => {
  const response = await axiosClient.post('/api/students', studentData);
  return response.data;
};

export const updateStudent = async (id, studentData) => {
  const response = await axiosClient.put(`/api/students/${id}`, studentData);
  return response.data;
};

// === DOCENTES ===
export const getAllTeachers = async () => {
  const response = await axiosClient.get('/api/teachers');
  return response.data;
};

export const createTeacher = async (teacherData) => {
  const response = await axiosClient.post('/api/teachers', teacherData);
  return response.data;
};

export const updateTeacher = async (id, teacherData) => {
  const response = await axiosClient.put(`/api/teachers/${id}`, teacherData);
  return response.data;
};

// Cambiar estado del docente (ej: ACTIVE a INACTIVE)
export const updateTeacherStatus = async (id, status) => {
  const response = await axiosClient.patch(`/api/teachers/${id}/status`, { status });
  return response.data;
};