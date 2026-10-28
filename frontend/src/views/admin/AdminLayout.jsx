import React, { useState, useEffect } from 'react';
import { Navbar, Nav, NavDropdown, Container, Button } from 'react-bootstrap';
import { NavLink, Outlet } from 'react-router-dom';
import keycloak from '../../config/keycloak';
import { getUserRole } from '../../utils/auth';

const AdminLayout = () => {
  const [userName, setUserName] = useState('');
  const role = getUserRole();

  useEffect(() => {
    if (keycloak.authenticated) {
      keycloak.loadUserProfile().then((profile) => {
        setUserName(profile.firstName || 'Administrador');
      });
    }
  }, []);

  const handleLogout = () => {
    keycloak.logout({
      redirectUri: window.location.origin
    });
  };

  if (role !== 'ADMIN') {
    return (
      <Container className="mt-5 text-center text-danger">
        <h3>Acceso no autorizado</h3>
        <p>Esta área es exclusiva para administradores del sistema.</p>
        <Button variant="primary" onClick={handleLogout} className="mt-3">Cerrar sesión</Button>
      </Container>
    );
  }

  return (
    <>
      <Navbar bg="dark" variant="dark" expand="lg" className="shadow-sm mb-4">
        <Container>
          <Navbar.Brand as={NavLink} to="/admin" end className="fw-bold text-info">
            SIGA - Administración
          </Navbar.Brand>
          <Navbar.Toggle aria-controls="admin-navbar-nav" />
          <Navbar.Collapse id="admin-navbar-nav">
            <Nav className="me-auto">
              <Nav.Link as={NavLink} to="/admin" end>Inicio</Nav.Link>
              
              <NavDropdown title="Usuarios" id="nav-dropdown-users">
                <NavDropdown.Item as={NavLink} to="/admin/students">Gestión de Estudiantes</NavDropdown.Item>
                <NavDropdown.Item as={NavLink} to="/admin/teachers">Gestión de Docentes</NavDropdown.Item>
              </NavDropdown>

              <NavDropdown title="Curriculum" id="nav-dropdown-curriculum">
                <NavDropdown.Item as={NavLink} to="/admin/careers">Carreras</NavDropdown.Item>
                <NavDropdown.Item as={NavLink} to="/admin/study-plans">Planes de Estudio</NavDropdown.Item>
                <NavDropdown.Item as={NavLink} to="/admin/courses">Asignaturas</NavDropdown.Item>
              </NavDropdown>

              <NavDropdown title="Programación" id="nav-dropdown-programming">
                <NavDropdown.Item as={NavLink} to="/admin/periods">Períodos Académicos</NavDropdown.Item>
                <NavDropdown.Item as={NavLink} to="/admin/sections">Secciones y Horarios</NavDropdown.Item>
              </NavDropdown>

              <NavDropdown title="Registros" id="nav-dropdown-records">
                <NavDropdown.Item as={NavLink} to="/admin/enrollments">Matrículas</NavDropdown.Item>
                <NavDropdown.Item as={NavLink} to="/admin/registrations">Toma de Ramos</NavDropdown.Item>
              </NavDropdown>
            </Nav>

            <div className="d-flex align-items-center gap-3">
              <span className="navbar-text text-light small">Hola, {userName}</span>
              <Button variant="outline-danger" size="sm" onClick={handleLogout}>Salir</Button>
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

export default AdminLayout;