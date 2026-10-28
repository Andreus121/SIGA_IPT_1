import React, { useState, useEffect } from 'react';
import { Navbar, Nav, Container, Button } from 'react-bootstrap';
import { NavLink, Outlet } from 'react-router-dom';
import keycloak from '../../config/keycloak';
import { getUserRole } from '../../utils/auth';

const StudentLayout = () => {
  const [userProfile, setUserProfile] = useState(null);
  const role = getUserRole();

  useEffect(() => {
    if (keycloak.authenticated) {
      keycloak.loadUserProfile()
        .then((profile) => {
          setUserProfile(profile);
        })
        .catch((error) => {
          console.error("Error al cargar el perfil del usuario", error);
        });
    }
  }, []);

  const handleLogout = () => {
    keycloak.logout({
      redirectUri: window.location.origin
    });
  };

  // Validación de seguridad para acceso exclusivo a estudiantes
  if (role !== 'STUDENT') {
    return (
      <Container className="mt-5 text-center text-danger">
        <h3>Acceso no autorizado</h3>
        <p>Su cuenta no posee el rol de estudiante requerido para acceder a este portal.</p>
        <Button variant="primary" onClick={handleLogout} className="mt-3">
          Cerrar sesión
        </Button>
      </Container>
    );
  }

  return (
    <>
      <Navbar bg="dark" variant="dark" expand="lg" className="shadow-sm mb-4">
        <Container>
          <Navbar.Brand as={NavLink} to="/student" end className="fw-bold text-primary">
            SIGA - Estudiante
          </Navbar.Brand>
          <Navbar.Toggle aria-controls="student-navbar-nav" />
          <Navbar.Collapse id="student-navbar-nav">
            <Nav className="me-auto">
              <Nav.Link as={NavLink} to="/student" end>
                Inicio
              </Nav.Link>
              <Nav.Link as={NavLink} to="/student/info">
                Malla y asignaturas
              </Nav.Link>
              <Nav.Link as={NavLink} to="/student/enrollment">
                Matrícula
              </Nav.Link>
              <Nav.Link as={NavLink} to="/student/registration">
                Toma de ramos
              </Nav.Link>
              <Nav.Link as={NavLink} to="/student/schedule">
                Horario de clases
              </Nav.Link>
              <Nav.Link as={NavLink} to="/student/grades">
                Mis notas
              </Nav.Link>
            </Nav>

            <div className="d-flex align-items-center gap-3">
              {userProfile && (
                <span className="navbar-text text-light small">
                  {userProfile.firstName} {userProfile.lastName}
                </span>
              )}
              <Button variant="outline-danger" size="sm" onClick={handleLogout}>
                Cerrar sesión
              </Button>
            </div>
          </Navbar.Collapse>
        </Container>
      </Navbar>

      {/* Renderiza dinámicamente la vista seleccionada */}
      <Container className="pb-5">
        <Outlet />
      </Container>
    </>
  );
};

export default StudentLayout;