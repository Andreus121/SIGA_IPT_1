import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Spinner, Alert, Form, Badge } from 'react-bootstrap';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { getRegistrationsBySection, updateGrade } from '../../services/TeacherService';

const TeacherGradesEntryView = () => {
  const { sectionCode } = useParams();
  const [searchParams] = useSearchParams();
  const periodCode = searchParams.get('period');
  const navigate = useNavigate();

  const [registrations, setRegistrations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState(null);
  
  // Estado local para manejar los inputs antes de enviarlos
  const [gradesInput, setGradesInput] = useState({});

  const fetchRegistrations = async () => {
    setLoading(true);
    try {
      const data = await getRegistrationsBySection(sectionCode, periodCode);
      setRegistrations(data);
      
      // Inicializar el estado de los inputs con las notas actuales
      const initialGrades = {};
      data.forEach(reg => {
        initialGrades[reg.id] = reg.final_grade ?? '';
      });
      setGradesInput(initialGrades);
    } catch (err) {
      setMessage({ type: 'danger', text: 'Error al obtener los alumnos de esta sección.' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (sectionCode && periodCode) {
      fetchRegistrations();
    }
  }, [sectionCode, periodCode]);

  const handleGradeChange = (registrationId, value) => {
    setGradesInput(prev => ({
      ...prev,
      [registrationId]: value
    }));
  };

  const handleSaveGrade = async (registrationId) => {
    const grade = parseFloat(gradesInput[registrationId]);
    if (isNaN(grade) || grade < 1.0 || grade > 7.0) {
      setMessage({ type: 'warning', text: 'La nota debe ser un número entre 1.0 y 7.0' });
      return;
    }

    setSaving(true);
    setMessage(null);
    try {
      await updateGrade({
      registration_id: registrationId,
      final_grade: grade
      });
      setMessage({ type: 'success', text: 'Calificación guardada correctamente.' });
      await fetchRegistrations(); // Recargar datos para mostrar el estado actualizado
    } catch (err) {
      setMessage({ type: 'danger', text: 'Error al guardar la calificación.' });
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="text-center my-5"><Spinner animation="border" variant="primary" /></div>;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3>Gestión de Calificaciones</h3>
        <Button variant="secondary" onClick={() => navigate('/teacher/sections')}>
          Volver a Mis Clases
        </Button>
      </div>

      {message && <Alert variant={message.type} dismissible onClose={() => setMessage(null)}>{message.text}</Alert>}

      <Card className="shadow-sm border-0">
        <Card.Header className="bg-dark text-white fw-bold">
          Alumnos Inscritos - Sección {sectionCode}
        </Card.Header>
        <Card.Body className="p-0">
          <Table responsive hover className="mb-0 text-center align-middle">
            <thead className="table-light">
              <tr>
                <th>RUN Alumno</th>
                <th>Estado Inscripción</th>
                <th>Nota Final (1.0 - 7.0)</th>
                <th>Acción</th>
              </tr>
            </thead>
            <tbody>
              {registrations.length === 0 ? (
                <tr><td colSpan="4" className="text-muted">No hay alumnos inscritos en esta sección.</td></tr>
              ) : (
                registrations.map(reg => (
                  <tr key={reg.id}>
                    <td className="fw-bold">{reg.student_run}</td>
                    <td>
                      <Badge bg={reg.status === 'REGISTERED' ? 'info' : 'secondary'}>
                        {reg.status}
                      </Badge>
                    </td>
                    <td style={{ width: '200px' }}>
                      <Form.Control
                        type="number"
                        step="0.1"
                        min="1.0"
                        max="7.0"
                        value={gradesInput[reg.id]}
                        onChange={(e) => handleGradeChange(reg.id, e.target.value)}
                        disabled={saving}
                      />
                    </td>
                    <td>
                      <Button 
                        variant="success" 
                        size="sm"
                        disabled={
                          saving ||
                          gradesInput[reg.id] === '' ||
                          String(gradesInput[reg.id]) === String(reg.final_grade ?? '')
                        }
                        onClick={() => handleSaveGrade(reg.id)}
                      >
                        Guardar Nota
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

export default TeacherGradesEntryView;