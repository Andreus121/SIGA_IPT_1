import axiosClient from '../api/axiosClient';

// Obtener información del estudiante autenticado mediante su email de Keycloak
export const getStudentByEmail = async (email) => {
  const response = await axiosClient.get(`/api/students/email/${email}`);
  return response.data;
};

// Obtener asignaturas pertenecientes a un plan de estudio
export const getCoursesByStudyPlan = async (studyPlanCode) => {
  const response = await axiosClient.get(`/api/courses/plan/${studyPlanCode}`);
  return response.data;
};

// Obtener historial de matrículas del estudiante por RUN
export const getEnrollmentsByStudent = async (studentRun) => {
  const response = await axiosClient.get(`/api/enrollments/student/${studentRun}`);
  return response.data;
};

// Registrar una nueva matrícula anual o semestral
export const createEnrollment = async (enrollmentData) => {
  const response = await axiosClient.post('/api/enrollments', enrollmentData);
  return response.data;
};

// Obtener listado de períodos académicos
export const getAcademicPeriods = async () => {
  const response = await axiosClient.get('/api/periods');
  return response.data;
};

// Obtener secciones disponibles por período
export const getSectionsByPeriod = async (periodCode) => {
  const response = await axiosClient.get(`/api/sections/period/${periodCode}`);
  return response.data;
};

// Obtener ramos inscritos del estudiante por RUN y período
export const getRegistrationsByStudent = async (studentRun, periodCode) => {
  const response = await axiosClient.get(`/api/registrations/student/${studentRun}`, {
    params: { periodCode }
  });
  return response.data;
};

// Inscribir una nueva asignatura/sección
export const createRegistration = async (registrationData) => {
  const response = await axiosClient.post('/api/registrations', registrationData);
  return response.data;
};

// Anular la inscripción de un ramo
export const deleteRegistration = async (registrationId) => {
  await axiosClient.delete(`/api/registrations/${registrationId}`);
};