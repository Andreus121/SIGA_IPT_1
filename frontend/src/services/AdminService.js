import axiosClient from '../api/axiosClient';

// ================ ESTUDIANTES ==================

// obtener todos los estudiantes
const getAllStudents = async () => {
  const response = await axiosClient.get('/api/students');
  return response.data;
};

// crear un nuevo estudiante
const createStudent = async (studentData) => {
  const response = await axiosClient.post('/api/students', studentData);
  return response.data;
};

// actualizar un estudiante existente
const updateStudent = async (id, studentData) => {
  const response = await axiosClient.put(`/api/students/${id}`, studentData);
  return response.data;
};

// ================ DOCENTES ==================

// obtener todos los docentes
const getAllTeachers = async () => {
  const response = await axiosClient.get('/api/teachers');
  return response.data;
};

// crear un nuevo docente
const createTeacher = async (teacherData) => {
  const response = await axiosClient.post('/api/teachers', teacherData);
  return response.data;
};

// actualizar un docente existente
const updateTeacher = async (id, teacherData) => {
  const response = await axiosClient.put(`/api/teachers/${id}`, teacherData);
  return response.data;
};

// cambiar estado del docente (ej: ACTIVE a INACTIVE)
const updateTeacherStatus = async (id, status) => {
  const response = await axiosClient.patch(`/api/teachers/${id}/status`, { status });
  return response.data;
};

// ================ CARRERAS ==================

// obtener todas las carreras
const getCareers = async () => {
  const response = await axiosClient.get('/api/careers');
  return response.data;
};

// crear una nueva carrera
const createCareer = async (data) => {
  const response = await axiosClient.post('/api/careers', data);
  return response.data;
};

// actualizar una carrera existente
const updateCareer = async (code, data) => {
  const response = await axiosClient.put(`/api/careers/${code}`, data);
  return response.data;
}

// eliminar una carrera existente
const deleteCareer = async (code) => {
  const response = await axiosClient.delete(`/api/careers/${code}`);
  return response.data;
}

// ================ PLANES DE ESTUDIO ==================

// obtener todos los planes de estudio
const getStudyPlans = async () => {
  const response = await axiosClient.get('/api/study-plans');
  return response.data;
};

// crear un nuevo plan de estudio
const createStudyPlan = async (code, careerCode, name, setCurrent) => {
  const response = await axiosClient.post('/api/study-plans', {code, careerCode, name, setCurrent });
  return response.data;
};

// establecer un plan de estudio como actual
const setCurrentStudyPlan = async (planCode) => {
  const response = await axiosClient.put(`/api/study-plans/${planCode}/set-current`);
  return response.data;
};

// borrar un plan de estudio
const deleteStudyPlan = async (planCode) => {
  const response = await axiosClient.delete(`/api/study-plans/${planCode}`);
  return response.data;
}

// obtener todas las asignaturas
const getCourses = async () => {
  const response = await axiosClient.get('/api/courses');
  return response.data;
};

// crear una nueva asignatura
const createCourse = async (data) => {
  const response = await axiosClient.post('/api/courses', data);
  return response.data;
};

// obtener todos los períodos académicos
const getPeriods = async () => {
  const response = await axiosClient.get('/api/periods');
  return response.data;
};

// crear un nuevo período académico
const createPeriod = async (data) => {
  const response = await axiosClient.post('/api/periods', data);
  return response.data;
};

// cerrar un período académico
const closeSemester = async (periodCode) => {
  const response = await axiosClient.post(`/api/semester-closing/${periodCode}`);
  return response.data;
};

// obtener todas las secciones de un periodo académico
const getSections = async (periodCode) => {
  const response = await axiosClient.get(`/api/sections/period/${periodCode}`);
  return response.data;
};

// crear una nueva sección
const createSection = async (data) => {
  const response = await axiosClient.post('/api/sections', data);
  return response.data;
};

// obtener todas las inscripciones (toma de ramos) de un período académico
const getEnrollmentsByPeriod = async (periodCode) => {
  const response = await axiosClient.get(`/api/enrollments/period/${periodCode}`);
  return response.data;
};

// obtener todas las inscripciones (toma de ramos) de una sección específica
const getRegistrationsBySection = async (sectionCode, periodCode) => {
  const response = await axiosClient.get(`/api/registrations/section/${sectionCode}`, {
    params: { periodCode }
  });
  return response.data;
};

// anular administrativamente la inscripción de un alumno
const forceDeleteRegistration = async (registrationId) => {
  await axiosClient.delete(`/api/registrations/${registrationId}`);
};

export {
  getAllStudents,
  createStudent,
  updateStudent,

  getAllTeachers,
  createTeacher,
  updateTeacher,
  updateTeacherStatus,

  getCareers,
  createCareer,
  updateCareer,
  deleteCareer,

  getStudyPlans,
  createStudyPlan,
  setCurrentStudyPlan,
  deleteStudyPlan,
  
  getCourses,
  createCourse,
  getPeriods,
  createPeriod,
  closeSemester,
  getSections,
  createSection,
  getEnrollmentsByPeriod,
  getRegistrationsBySection,
  forceDeleteRegistration
};
