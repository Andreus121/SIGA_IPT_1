import React, { useState, useEffect } from 'react';
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { NavLink, Outlet } from 'react-router-dom';
import keycloak from '../../config/keycloak';
import { getUserRole } from '../../utils/auth';

const TeacherLayout = () => {
  const [userProfile, setUserProfile] = useState(null);
  const role = getUserRole();

  useEffect(() => {
    if (keycloak.authenticated) {
      keycloak.loadUserProfile()
        .then((profile) => setUserProfile(profile))
        .catch((error) => console.error("Error al cargar perfil", error));
    }
  }, []);

  const handleLogout = () => {
    keycloak.logout({
      redirectUri: window.location.origin
    });
  };

  if (role !== 'TEACHER') {
    return (
      <Container className="mt-5 text-center text-danger">
        <h3>Acceso no autorizado</h3>
        <p>Su cuenta no posee el rol de docente requerido.</p>
        <Button variant="primary" onClick={handleLogout} className="mt-3">
          Cerrar sesión
        </Button>
      </Container>
    );
  }

  return (
    <>
      <Navbar bg="primary" variant="dark" expand="lg" className="shadow-sm mb-4">
        <Container>
          <Navbar.Brand as={NavLink} to="/teacher" end className="fw-bold">
            SIGA - Docentes
          </Navbar.Brand>
          <Navbar.Toggle aria-controls="teacher-navbar-nav" />
          <Navbar.Collapse id="teacher-navbar-nav">
            <Nav className="me-auto">
              <Nav.Link as={NavLink} to="/teacher" end>
                Inicio
              </Nav.Link>
              <Nav.Link as={NavLink} to="/teacher/sections">
                Mis Clases y Notas
              </Nav.Link>
            </Nav>
            <div className="d-flex align-items-center gap-3">
              {userProfile && (
                <span className="navbar-text text-light small">
                  Prof. {userProfile.lastName}
                </span>
              )}
              <Button variant="outline-light" size="sm" onClick={handleLogout}>
                Cerrar sesión
              </Button>
            </div>
          </Navbar.Collapse>
        </Container>
      </Navbar>
      <Container className="pb-5">
        <Outlet />
      </Container>
    </>
  );
};

export default TeacherLayout;