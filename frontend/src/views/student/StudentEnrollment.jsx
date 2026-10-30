import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Badge, Spinner, Alert, Form } from 'react-bootstrap';
import keycloak from '../../config/keycloak';
import {
  getStudentByEmail,
  getEnrollmentsByStudent,
  getAcademicPeriods,
  enrollStudent
} from '../../services/StudentService';

const StudentEnrollmentView = () => {
  const [student, setStudent] = useState(null);
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [activePeriods, setActivePeriods] = useState([]);
  const [selectedPeriod, setSelectedPeriod] = useState('');

  const fetchData = async () => {
    try {
      const userEmail = keycloak.tokenParsed?.email;
      if (userEmail) {
        const studentData = await getStudentByEmail(userEmail);
        setStudent(studentData);

        const history = await getEnrollmentsByStudent(studentData.run);
        setEnrollments(history);

        const periods = await getAcademicPeriods();
        const openPeriods = periods.filter((period) => period.status === 'OPEN');
        setActivePeriods(openPeriods);
        setSelectedPeriod(openPeriods.length > 0 ? openPeriods[0].period_code : '');
      }
    } catch (err) {
      setMessage({ type: 'danger', text: 'Error al cargar los datos de matrícula.' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleEnrollment = async (e) => {
    e.preventDefault();
    if (!selectedPeriod || !student) return;

    setActionLoading(true);
    setMessage(null);

    try {
      await enrollStudent(student.run, selectedPeriod);
      setMessage({ type: 'success', text: `Matrícula procesada con éxito para el período ${selectedPeriod}.` });
      await fetchData();
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Error al procesar la matrícula.';
      setMessage({ type: 'danger', text: errorMsg });
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="text-center my-5">
        <Spinner animation="border" variant="primary" />
        <p className="mt-2">Cargando estado de matrícula...</p>
      </div>
    );
  }

  return (
    <div>
      <h3 className="mb-4">Matrícula Académica</h3>

      {message && <Alert variant={message.type}>{message.text}</Alert>}

      <Card className="shadow-sm border-0 mb-4">
        <Card.Header className="bg-primary text-white fw-bold">Nueva Matrícula</Card.Header>
        <Card.Body>
          {activePeriods.length === 0 ? (
            <p className="text-muted mb-0">No hay periodos academicos abiertos.</p>
          ) : (
            <Form onSubmit={handleEnrollment} className="row align-items-end">
              <Form.Group className="col-md-6 mb-3 mb-md-0">
                <Form.Label>Seleccionar Período Académico Abierto</Form.Label>
                <Form.Select
                  value={selectedPeriod}
                  onChange={(e) => setSelectedPeriod(e.target.value)}
                  disabled={actionLoading}
                >
                  <option value={activePeriods[0].period_code}>
                    {activePeriods[0].period_code}
                  </option>
                </Form.Select>
              </Form.Group>
              <div className="col-md-6">
                <Button type="submit" variant="success" disabled={actionLoading || !selectedPeriod}>
                  {actionLoading ? 'Procesando...' : 'Matricularse en el Período'}
                </Button>
              </div>
            </Form>
          )}
        </Card.Body>
      </Card>

      <Card className="shadow-sm border-0">
        <Card.Header className="bg-secondary text-white fw-bold">Historial de Matrículas</Card.Header>
        <Card.Body className="p-0">
          <Table responsive hover className="mb-0 text-center align-middle">
            <thead className="table-light">
              <tr>
                <th>ID</th>
                <th>Período</th>
                <th>Fecha de Matrícula</th>
              </tr>
            </thead>
            <tbody>
              {enrollments.length === 0 ? (
                <tr>
                  <td colSpan="4" className="text-muted">No registras matrículas registradas.</td>
                </tr>
              ) : (
                enrollments.map((enr) => (
                  <tr key={enr.id}>
                    <td>{enr.id}</td>
                    <td className="fw-bold">{enr.period_code}</td>
                    <td>{enr.enrollment_date}</td>
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

export default StudentEnrollmentView;