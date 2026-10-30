import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Badge, Spinner, Alert, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import keycloak from '../../config/keycloak';
import { getStudentByEmail, getCareerByCode } from '../../services/StudentService';

const StudentDashboard = () => {
  const [studentData, setStudentData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [careerData, setCareerData] = useState(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const userEmail = keycloak.tokenParsed?.email;
        if (userEmail) {
          const data = await getStudentByEmail(userEmail);
          setStudentData(data);
          if (data.career_code) {
            const career = await getCareerByCode(data.career_code);
            console.log("Datos de la carrera obtenidos:", career);
            setCareerData(career);
          }

        } else {
          setError('No se pudo determinar el correo electrónico de la sesión.');
        }
      } catch (err) {
        setError('Error al cargar la información del perfil del estudiante.');
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, []);

  if (loading) {
    return (
      <div className="text-center my-5">
        <Spinner animation="border" variant="primary" />
        <p className="mt-2">Cargando datos del estudiante...</p>
      </div>
    );
  }

  if (error) {
    return <Alert variant="danger">{error}</Alert>;
  }

  return (
    <div>
      <h2 className="mb-4">Bienvenido(a), {studentData?.fullName}</h2>
      
      <Row className="g-4 mb-4">
        <Col md={8}>
          <Card className="shadow-sm border-0">
            <Card.Header className="bg-primary text-white fw-bold">
              Ficha del Estudiante
            </Card.Header>
            <Card.Body>
              <Row>
                <Col sm={6} className="mb-3">
                  <strong>RUN:</strong> <p className="text-muted mb-0">{studentData?.run}</p>
                </Col>
                <Col sm={6} className="mb-3">
                  <strong>Correo Institucional:</strong> <p className="text-muted mb-0">{studentData?.email}</p>
                </Col>
                <Col sm={6} className="mb-3">
                  <strong>Carrera:</strong> <p className="text-muted mb-0">{careerData?.name || studentData?.career_code}</p>
                </Col>
                <Col sm={6} className="mb-3">
                  <strong>Plan de Estudios:</strong> <p className="text-muted mb-0">{studentData?.study_plan_code}</p>
                </Col>
                <Col sm={6}>
                  <strong>Estado Académico:</strong>{' '}
                  <div>
                    <Badge bg={studentData?.status === 'REGULAR' ? 'success' : 'warning'}>
                      {studentData?.status}
                    </Badge>
                  </div>
                </Col>
              </Row>
            </Card.Body>
          </Card>
        </Col>

        <Col md={4}>
          <Card className="shadow-sm border-0 bg-light">
            <Card.Body>
              <h5>Accesos Rápidos</h5>
              <div className="d-grid gap-2 mt-3">
                <Button as={Link} to="/student/registration" variant="outline-primary">
                  Ir a Toma de Ramos
                </Button>
                <Button as={Link} to="/student/schedule" variant="outline-secondary">
                  Ver Horario de Clases
                </Button>
                <Button as={Link} to="/student/grades" variant="outline-info">
                  Consultar Notas
                </Button>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default StudentDashboard;