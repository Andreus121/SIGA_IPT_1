import axiosClient from '../api/axiosClient';

// Obtener información del estudiante autenticado mediante su email de Keycloak
const getStudentByEmail = async (email) => {
  const response = await axiosClient.get(`/api/students/email/${email}`);
  return response.data;
};

// Obtener asignaturas pertenecientes a un plan de estudio
const getCoursesByStudyPlan = async (studyPlanCode) => {
  const response = await axiosClient.get(`/api/courses/plan/${studyPlanCode}`);
  return response.data;
};

// Obtener historial de matrículas del estudiante por RUN
const getEnrollmentsByStudent = async (studentRun) => {
  const response = await axiosClient.get(`/api/enrollments/student/${studentRun}`);
  return response.data;
};

// Registrar una nueva matrícula anual o semestral
const createEnrollment = async (enrollmentData) => {
  const response = await axiosClient.post('/api/enrollments', enrollmentData);
  return response.data;
};

// Obtener listado de períodos académicos
const getAcademicPeriods = async () => {
  const response = await axiosClient.get('/api/periods');
  return response.data;
};

// Obtener secciones disponibles por período
const getSectionsByPeriod = async (periodCode) => {
  const response = await axiosClient.get(`/api/sections/period/${periodCode}`);
  return response.data;
};

// Obtener ramos inscritos del estudiante por RUN y período
const getRegistrationsByStudent = async (studentRun, periodCode) => {
  const response = await axiosClient.get(`/api/registrations/student/${studentRun}`, {
    params: { periodCode }
  });
  return response.data;
};

// Inscribir una nueva asignatura/sección
const createRegistration = async(registrationData) => {
  const response = await axiosClient.post('/api/registrations', registrationData);
  return response.data;
};

// Anular la inscripción de un ramo
const deleteRegistration = async(registrationId) => {
  await axiosClient.delete(`/api/registrations/${registrationId}`);
};

// Obtener información de la carrera por código
const getCareerByCode = async (careerCode) => {
  const response = await axiosClient.get(`/api/careers/${careerCode}`);
  return response.data;
};

export {
  getStudentByEmail,
  getCoursesByStudyPlan,
  getEnrollmentsByStudent,
  createEnrollment,
  getAcademicPeriods,
  getSectionsByPeriod,
  getRegistrationsByStudent,
  createRegistration,
  deleteRegistration,
  getCareerByCode
};