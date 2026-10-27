import React from 'react';
import { Row, Col, Card, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';

const AdminDashboard = () => {
  return (
    <div>
      <h2 className="mb-4">Panel de Control General</h2>
      <Row className="g-4">
        <Col md={6}>
          <Card className="shadow-sm border-0 h-100">
            <Card.Header className="bg-primary text-white fw-bold">Gestión de Usuarios</Card.Header>
            <Card.Body>
              <p className="text-muted">Administre los perfiles de alumnos y profesores del instituto.</p>
              <div className="d-flex gap-2">
                <Button as={Link} to="/admin/students" variant="outline-primary">Ver Estudiantes</Button>
                <Button as={Link} to="/admin/teachers" variant="outline-primary">Ver Docentes</Button>
              </div>
            </Card.Body>
          </Card>
        </Col>

        <Col md={6}>
          <Card className="shadow-sm border-0 h-100">
            <Card.Header className="bg-success text-white fw-bold">Programación Académica</Card.Header>
            <Card.Body>
              <p className="text-muted">Gestione períodos, apertura de secciones y horarios.</p>
              <div className="d-flex gap-2">
                <Button as={Link} to="/admin/periods" variant="outline-success">Períodos</Button>
                <Button as={Link} to="/admin/sections" variant="outline-success">Secciones</Button>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default AdminDashboard;