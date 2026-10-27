import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Container, Spinner } from 'react-bootstrap';
import keycloak from './config/keycloak';
import RoleRedirect from './components/RoleRedirect';
import StudentDashboard from './views/student/StudentDashboard';
import TeacherDashboard from './views/teacher/TeacherDashboard';
import AdminDashboard from './views/admin/AdminDashboard';

const App = () => {
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    // inicializa Keycloak y maneja la autenticación
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

  // mostrar un spinner de carga mientras Keycloak se inicializa
  if (!isInitialized) {
    return (
      <Container className="d-flex justify-content-center align-items-center vh-100">
        <Spinner animation="border" variant="primary" />
        <span className="ms-3">Authenticating...</span>
      </Container>
    );
  }

  return (
    <Router>
      <Routes>
        <Route path="/" element={<RoleRedirect />} />
        <Route path="/admin/*" element={<AdminDashboard />} />
        <Route path="/teacher/*" element={<TeacherDashboard />} />
        <Route path="/student/*" element={<StudentDashboard />} />
      </Routes>
    </Router>
  );
};

export default App;