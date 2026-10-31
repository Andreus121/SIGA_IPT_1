import React, { useState, useEffect } from 'react';
import { Card, Table, Form, Spinner, Button, Row, Col, Badge, Alert } from 'react-bootstrap';
import {
  getPeriods,
  getSections,
  getRegistrationsBySection,
  deleteRegistration
} from '../../services/AdminService';

const AdminRegistrations = () => {
  const [periods, setPeriods] = useState([]);
  const [selectedPeriod, setSelectedPeriod] = useState('');
  
  const [sections, setSections] = useState([]);
  const [selectedSection, setSelectedSection] = useState('');
  
  const [registrations, setRegistrations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    const initPeriods = async () => {
      try {
        const p = await getPeriods();
        setPeriods(p);
        if (p.length > 0) {
          const openPeriod = p.find(period => period.status === 'OPEN') || p[0];
          setSelectedPeriod(openPeriod.period_code);
        }
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    initPeriods();
  }, []);

  useEffect(() => {
    const fetchSections = async () => {
      if (!selectedPeriod) return;
      setSections([]);
      setSelectedSection('');
      setRegistrations([]);
      
      try {
        const data = await getSections(selectedPeriod);
        setSections(data);
        if (data.length > 0) {
          setSelectedSection(data[0].code);
        }
      } catch (err) {
        console.error(err);
      }
    };
    fetchSections();
  }, [selectedPeriod]);

  const fetchRegistrations = async () => {
    if (!selectedSection || !selectedPeriod) return;
    setLoading(true);
    try {
      const data = await getRegistrationsBySection(selectedSection, selectedPeriod);
      setRegistrations(data);
    } catch (err) {
      setMessage({ type: 'danger', text: 'Error al cargar las tomas de ramos.' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRegistrations();
  }, [selectedSection, selectedPeriod]);

  const handleForceDelete = async (registrationId, studentRun) => {
    if (!window.confirm(`¿Está seguro de que desea anular administrativamente la inscripción del alumno ${studentRun} en la sección ${selectedSection}? Esta acción es irreversible.`)) {
      return;
    }

    setActionLoading(true);
    setMessage(null);
    try {
      await deleteRegistration(registrationId);
      setMessage({ type: 'success', text: 'Toma de ramo eliminada correctamente.' });
      await fetchRegistrations();
    } catch (err) {
      setMessage({ type: 'danger', text: 'Error al intentar eliminar la toma de ramo.' });
    } finally {
      setActionLoading(false);
    }
  };

  if (loading && !selectedPeriod) {
    return <Spinner animation="border" className="d-block mx-auto mt-5" variant="primary" />;
  }

  return (
    <div>
      <h3 className="mb-4">Auditoría y Soporte de Inscripciones</h3>

      {message && <Alert variant={message.type} dismissible onClose={() => setMessage(null)}>{message.text}</Alert>}

      <Row className="mb-4">
        <Col md={4}>
          <Form.Group>
            <Form.Label>Período Académico</Form.Label>
            <Form.Select value={selectedPeriod} onChange={(e) => setSelectedPeriod(e.target.value)}>
              {periods.map((p) => (
                <option key={p.period_code} value={p.period_code}>{p.period_code}</option>
              ))}
            </Form.Select>
          </Form.Group>
        </Col>
        <Col md={4}>
          <Form.Group>
            <Form.Label>Sección</Form.Label>
            <Form.Select 
              value={selectedSection} 
              onChange={(e) => setSelectedSection(e.target.value)}
              disabled={sections.length === 0}
            >
              {sections.length === 0 ? (
                <option value="">Sin secciones programadas</option>
              ) : (
                sections.map((s) => (
                  <option key={s.id} value={s.code}>{s.code} ({s.course_code})</option>
                ))
              )}
            </Form.Select>
          </Form.Group>
        </Col>
      </Row>

      <Card className="shadow-sm border-0">
        <Card.Header className="bg-dark text-white fw-bold">
          Nómina de Inscritos - Sección {selectedSection || 'N/A'}
        </Card.Header>
        <Card.Body className="p-0">
          <Table responsive hover className="mb-0 text-center align-middle">
            <thead className="table-light">
              <tr>
                <th>RUN Alumno</th>
                <th>Fecha Inscripción</th>
                <th>Estado</th>
                <th>Nota Actual</th>
                <th>Acción Administrativa</th>
              </tr>
            </thead>
            <tbody>
              {registrations.length === 0 ? (
                <tr>
                  <td colSpan="5" className="text-muted">No hay alumnos inscritos en esta sección.</td>
                </tr>
              ) : (
                registrations.map((reg) => (
                  <tr key={reg.id}>
                    <td className="fw-bold">{reg.student_run}</td>
                    <td>{new Date(reg.registration_date).toLocaleString()}</td>
                    <td>
                      <Badge bg={reg.status === 'REGISTERED' ? 'info' : 'secondary'}>
                        {reg.status}
                      </Badge>
                    </td>
                    <td>{reg.final_grade ?? '-'}</td>
                    <td>
                      <Button 
                        variant="outline-danger" 
                        size="sm"
                        disabled={actionLoading}
                        onClick={() => handleForceDelete(reg.id, reg.student_run)}
                      >
                        Eliminar
                      </Button>
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

export default AdminRegistrations;
  