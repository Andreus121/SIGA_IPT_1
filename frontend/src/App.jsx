import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Container, Spinner } from 'react-bootstrap';
import keycloak from './config/keycloak';

// Componentes de Autenticación y Redirección
import RoleRedirect from './components/RoleRedirect';
import ProtectedRoute from './components/ProtectedRoute';

// Vistas del Estudiante
import StudentLayout from './views/student/StudentLayout';
import StudentDashboard from './views/student/StudentDashboard';
import StudentInfo from './views/student/StudentInfo';
import StudentEnrollment from './views/student/StudentEnrollment';
import StudentRegistration from './views/student/StudentRegistration';
import StudentSchedule from './views/student/StudentSchedule';
import StudentGrades from './views/student/StudentGrades';

// Vistas de Docente
import TeacherDashboard from './views/teacher/TeacherDashboard';
import TeacherLayout from './views/teacher/TeacherLayout';
import TeacherSections from './views/teacher/TeacherSections';
import TeacherGradesEntry from './views/teacher/TeacherGradesEntry';

// Vistas de Administrador
import AdminDashboard from './views/admin/AdminDashboard';
import AdminLayout from './views/admin/AdminLayout';
import AdminStudents from './views/admin/AdminStudents';
import AdminTeachers from './views/admin/AdminTeachers';

const App = () => {
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    // 'login-required' fuerza el login si no hay sesión activa
    keycloak.init({ onLoad: 'login-required', checkLoginIframe: false })
      .then((authenticated) => {
        if (authenticated) {
          setIsInitialized(true);
        }
      })
      .catch((error) => {
        console.error("Keycloak initialization failed", error);
      });
  }, []);

  if (!isInitialized) {
    return (
      <Container className="d-flex justify-content-center align-items-center vh-100">
        <Spinner animation="border" variant="primary" />
        <span className="ms-3">Autenticando...</span>
      </Container>
    );
  }

  return (
    <Router>
      <Routes>
        <Route path="/" element={<RoleRedirect />} />

        {/* Rutas del Estudiante */}
        <Route 
          path="/student" 
          element={
            <ProtectedRoute>
              <StudentLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<StudentDashboard />} />
          <Route path="info" element={<StudentInfo />} />
          <Route path="enrollment" element={<StudentEnrollment />} />
          <Route path="registration" element={<StudentRegistration />} />
          <Route path="schedule" element={<StudentSchedule />} />
          <Route path="grades" element={<StudentGrades />} />
        </Route>
        
        {/* Rutas para Docente */}
        <Route 
          path="/teacher" 
          element={
            <ProtectedRoute>
              <TeacherLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<TeacherDashboard />} />
          <Route path="sections" element={<TeacherSections />} />
          <Route path="grades/:sectionCode" element={<TeacherGradesEntry />} />
        </Route>

        {/* Rutas para Admin */}
        <Route 
          path="/admin" 
          element={
            <ProtectedRoute>
              <AdminLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<AdminDashboard />} />
          <Route path="students" element={<AdminStudents />} />
          <Route path="teachers" element={<AdminTeachers />} />
        </Route>
      </Routes>
    </Router>
  );
};

export default App;