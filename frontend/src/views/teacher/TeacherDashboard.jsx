import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Spinner, Alert, Badge, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import keycloak from '../../config/keycloak';
import { getTeacherByEmail } from '../../services/TeacherService';

const TeacherDashboard = () => {
  const [teacher, setTeacher] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const userEmail = keycloak.tokenParsed?.email;
        if (userEmail) {
          const data = await getTeacherByEmail(userEmail);
          console.log("Teacher data fetched:", data);
          setTeacher(data);
        }
      } catch (err) {
        setError("Error al cargar los datos del docente. Verifique que el correo esté registrado.");
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);

  if (loading) {
    return <div className="text-center my-5"><Spinner animation="border" variant="primary" /></div>;
  }

  if (error) {
    return <Alert variant="danger">{error}</Alert>;
  }

  return (
    <div>
      <h2 className="mb-4">Portal del Docente</h2>
      <Row className="g-4">
        <Col md={8}>
          <Card className="shadow-sm border-0">
            <Card.Header className="bg-primary text-white fw-bold">Mis Datos Académicos</Card.Header>
            <Card.Body>
              <Row>
                <Col sm={6} className="mb-3">
                  <strong>Nombre Completo:</strong> <p className="text-muted">{teacher?.full_name}</p>
                </Col>
                <Col sm={6} className="mb-3">
                  <strong>RUN:</strong> <p className="text-muted">{teacher?.run}</p>
                </Col>
                <Col sm={6} className="mb-3">
                  <strong>Título Profesional:</strong> <p className="text-muted">{teacher?.professional_title}</p>
                </Col>
                <Col sm={6} className="mb-3">
                  <strong>Grado Académico:</strong> <p className="text-muted">{teacher?.academic_degree}</p>
                </Col>
                <Col sm={12}>
                  <strong>Estado:</strong>{' '}
                  <Badge bg={teacher?.status === 'ACTIVE' ? 'success' : 'secondary'}>{teacher?.status}</Badge>
                </Col>
              </Row>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4}>
          <Card className="shadow-sm border-0 bg-light">
            <Card.Body>
              <h5>Gestión Académica</h5>
              <p className="text-muted small">Acceda rápidamente al registro de calificaciones.</p>
              <div className="d-grid mt-3">
                <Button as={Link} to="/teacher/sections" variant="primary">
                  Ver Mis Clases y Notas
                </Button>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default TeacherDashboard;