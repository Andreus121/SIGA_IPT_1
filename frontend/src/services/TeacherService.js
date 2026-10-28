import axiosClient from '../api/axiosClient';

// Obtener información del docente por email (asumiendo futura implementación en backend)
const getTeacherByEmail = async (email) => {
  const response = await axiosClient.get(`/api/teachers/email/${email}`);
  return response.data;
};

// Obtener los períodos académicos
const getAcademicPeriods = async () => {
  const response = await axiosClient.get('/api/periods');
  return response.data;
};

// Obtener todas las secciones de un período
const getSectionsByPeriod = async (periodCode) => {
  const response = await axiosClient.get(`/api/sections/period/${periodCode}`);
  return response.data;
};

// Obtener los alumnos inscritos en una sección específica
const getRegistrationsBySection = async (sectionCode, periodCode) => {
  const response = await axiosClient.get(`/api/registrations/section/${sectionCode}`, {
    params: { periodCode }
  });
  return response.data;
};

// Ingresar o actualizar la calificación final
const updateGrade = async (gradeData) => {
  const response = await axiosClient.put('/api/grades', gradeData);
  return response.data;
};

export {
  getTeacherByEmail,
  getAcademicPeriods,
  getSectionsByPeriod,
  getRegistrationsBySection,
  updateGrade
};