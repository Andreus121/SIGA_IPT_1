import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Spinner, Alert, Form } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import keycloak from '../../config/keycloak';
import { getTeacherByEmail, getAcademicPeriods, getSectionsByPeriod } from '../../services/TeacherService';

const TeacherSectionsView = () => {
  const [teacher, setTeacher] = useState(null);
  const [periods, setPeriods] = useState([]);
  const [selectedPeriod, setSelectedPeriod] = useState('');
  const [mySections, setMySections] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const init = async () => {
      try {
        const userEmail = keycloak.tokenParsed?.email;
        if (userEmail) {
          const teacherData = await getTeacherByEmail(userEmail);
          setTeacher(teacherData);
          
          const periodList = await getAcademicPeriods();
          setPeriods(periodList);
          
          const openPeriod = periodList.find(p => p.status === 'OPEN') || periodList[0];
          if (openPeriod) setSelectedPeriod(openPeriod.period_code);
        }
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    init();
  }, []);

  useEffect(() => {
    const fetchSections = async () => {
      if (!teacher || !selectedPeriod) return;
      setLoading(true);
      try {
        const allSections = await getSectionsByPeriod(selectedPeriod);
        // Filtramos las secciones donde el docente coincida con el profesor actual
        const filtered = allSections.filter(sec => sec.teacher_id === teacher.id);
        setMySections(filtered);
      } catch (err) {
        console.error("Error al obtener secciones", err);
      } finally {
        setLoading(false);
      }
    };
    fetchSections();
  }, [selectedPeriod, teacher]);

  if (loading && !selectedPeriod) return <div className="text-center my-5"><Spinner animation="border" variant="primary" /></div>;

  return (
    <div>
      <h3 className="mb-4">Mis Clases Asignadas</h3>
      <Form.Group className="mb-4" style={{ maxWidth: '300px' }}>
        <Form.Label>Seleccionar Período Académico</Form.Label>
        <Form.Select value={selectedPeriod} onChange={(e) => setSelectedPeriod(e.target.value)}>
          {periods.map(p => (
            <option key={p.period_code} value={p.period_code}>{p.period_code}</option>
          ))}
        </Form.Select>
      </Form.Group>

      <Card className="shadow-sm border-0">
        <Card.Header className="bg-primary text-white fw-bold">Secciones - {selectedPeriod}</Card.Header>
        <Card.Body className="p-0">
          <Table responsive hover className="mb-0 text-center align-middle">
            <thead className="table-light">
              <tr>
                <th>Código Sección</th>
                <th>Asignatura</th>
                <th>Capacidad</th>
                <th>Acción</th>
              </tr>
            </thead>
            <tbody>
              {mySections.length === 0 ? (
                <tr><td colSpan="4" className="text-muted">No tiene clases asignadas en este período.</td></tr>
              ) : (
                mySections.map(sec => (
                  <tr key={sec.id}>
                    <td className="fw-bold">{sec.code}</td>
                    <td>{sec.course_code}</td>
                    <td>{sec.capacity} alumnos</td>
                    <td>
                      <Button 
                        variant="outline-primary" 
                        size="sm"
                        onClick={() => navigate(`/teacher/grades/${sec.code}?period=${selectedPeriod}`)}
                      >
                        Ver Alumnos / Calificar
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

export default TeacherSectionsView;