import axiosClient from '../api/axiosClient';

// Obtener información del docente por email (asumiendo futura implementación en backend)
export const getTeacherByEmail = async (email) => {
  const response = await axiosClient.get(`/api/teachers/email/${email}`);
  return response.data;
};

// Obtener los períodos académicos
export const getAcademicPeriods = async () => {
  const response = await axiosClient.get('/api/periods');
  return response.data;
};

// Obtener todas las secciones de un período
export const getSectionsByPeriod = async (periodCode) => {
  const response = await axiosClient.get(`/api/sections/period/${periodCode}`);
  return response.data;
};

// Obtener los alumnos inscritos en una sección específica
export const getRegistrationsBySection = async (sectionCode, periodCode) => {
  const response = await axiosClient.get(`/api/registrations/section/${sectionCode}`, {
    params: { periodCode }
  });
  return response.data;
};

// Ingresar o actualizar la calificación final
export const updateGrade = async (gradeData) => {
  const response = await axiosClient.put('/api/grades', gradeData);
  return response.data;
};