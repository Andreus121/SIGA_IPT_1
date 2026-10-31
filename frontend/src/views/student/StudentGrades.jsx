import React, { useState, useEffect } from 'react';
import { Card, Table, Form, Spinner, Badge, Alert } from 'react-bootstrap';
import keycloak from '../../config/keycloak';
import {
  getStudentByEmail,
  getAcademicPeriods,
  getRegistrationsByStudent
} from '../../services/StudentService';

const StudentGradesView = () => {
  const [student, setStudent] = useState(null);
  const [periods, setPeriods] = useState([]);
  const [selectedPeriod, setSelectedPeriod] = useState('');
  const [registrations, setRegistrations] = useState([]);
  const [loading, setLoading] = useState(true);

  const selectedPeriodData = periods.find((period) => period.period_code === selectedPeriod);
  const getRegistrationStatus = (registration) => {
    if (selectedPeriodData?.status === 'CLOSED' && registration.final_grade !== null && registration.final_grade !== undefined) {
      return registration.final_grade >= 4.0 ? 'APROBADO' : 'REPROBADO';
    }
    return registration.status;
  };

  useEffect(() => {
    const init = async () => {
      try {
        const userEmail = keycloak.tokenParsed?.email;
        if (userEmail) {
          const studentData = await getStudentByEmail(userEmail);
          setStudent(studentData);

          const periodList = await getAcademicPeriods();
          setPeriods(periodList);

          if (periodList.length > 0) {
            setSelectedPeriod(periodList[0].period_code);
          }
        }
      } catch (err) {
        console.error("Error al cargar calificaciones", err);
      } finally {
        setLoading(false);
      }
    };

    init();
  }, []);

  useEffect(() => {
    const fetchGrades = async () => {
      if (!student || !selectedPeriod) return;
      setLoading(true);
      try {
        const data = await getRegistrationsByStudent(student.run, selectedPeriod);
        setRegistrations(data);
        console.log("Registrations fetched:", data);
      } catch (err) {
        console.error("Error al obtener notas del estudiante", err);
      } finally {
        setLoading(false);
      }
    };

    fetchGrades();
  }, [selectedPeriod, student]);

  if (loading && !selectedPeriod) {
    return (
      <div className="text-center my-5">
        <Spinner animation="border" variant="primary" />
        <p className="mt-2">Cargando libreta de notas...</p>
      </div>
    );
  }

  return (
    <div>
      <h3 className="mb-4">Mis Calificaciones</h3>

      <Form.Group className="mb-4" style={{ maxWidth: '300px' }}>
        <Form.Label>Seleccionar Período Académico</Form.Label>
        <Form.Select value={selectedPeriod} onChange={(e) => setSelectedPeriod(e.target.value)}>
          {periods.map((p) => (
            <option key={p.period_code} value={p.period_code}>
              {p.period_code}
            </option>
          ))}
        </Form.Select>
      </Form.Group>

      <Card className="shadow-sm border-0">
        <Card.Header className="bg-primary text-white fw-bold">
          Calificaciones Finales - Período {selectedPeriod}
        </Card.Header>
        <Card.Body className="p-0">
          <Table responsive hover className="mb-0 text-center align-middle">
            <thead className="table-light">
              <tr>
                <th>Asignatura</th>
                <th>Sección</th>
                <th>Fecha de Inscripción</th>
                <th>Estado Inscripción</th>
                <th>Nota Final</th>
              </tr>
            </thead>
            <tbody>
              {registrations.length === 0 ? (
                <tr>
                  <td colSpan="5" className="text-muted">No registras ramos inscritos en este período.</td>
                </tr>
              ) : (
                registrations.map((reg) => (
                  <tr key={reg.id}>
                    <td className="fw-bold">{reg.course_code}</td>
                    <td>{reg.section_code}</td>
                    <td>{new Date(reg.registration_date).toLocaleDateString()}</td>
                    <td>
                      <Badge bg={getRegistrationStatus(reg) === 'APROBADO' ? 'success' : getRegistrationStatus(reg) === 'REPROBADO' ? 'danger' : 'info'}>
                        {getRegistrationStatus(reg)}
                      </Badge>
                    </td>
                    <td>
                      {reg.final_grade !== null && reg.final_grade !== undefined ? (
                        <Badge
                          bg={reg.final_grade >= 4.0 ? 'success' : 'danger'}
                          style={{ fontSize: '1rem' }}
                        >
                          {reg.final_grade.toFixed(1)}
                        </Badge>
                      ) : (
                        <span className="text-muted fst-italic">Pendiente</span>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </Table>
        </Card.Body>
      </Card>
    </div>
  );
};

export default StudentGradesView;