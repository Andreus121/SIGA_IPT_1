import React, { useState, useEffect } from 'react';
import { Card, Table, Form, Spinner, Alert, Badge } from 'react-bootstrap';
import keycloak from '../../config/keycloak';
import {
  getStudentByEmail,
  getAcademicPeriods,
  getRegistrationsByStudent,
  getSectionsByPeriod
} from '../../services/StudentService';

const StudentScheduleView = () => {
  const [student, setStudent] = useState(null);
  const [periods, setPeriods] = useState([]);
  const [selectedPeriod, setSelectedPeriod] = useState('');
  const [scheduleGrid, setScheduleGrid] = useState({});
  const [loading, setLoading] = useState(true);

  const days = [
    { code: 'L', name: 'Lunes' },
    { code: 'M', name: 'Martes' },
    { code: 'W', name: 'Miércoles' },
    { code: 'J', name: 'Jueves' },
    { code: 'V', name: 'Viernes' }
  ];

  const modules = [1, 2, 3, 4, 5, 6];

  useEffect(() => {
    const init = async () => {
      try {
        const userEmail = keycloak.tokenParsed?.email;
        if (userEmail) {
          const studentData = await getStudentByEmail(userEmail);
          setStudent(studentData);

          const periodList = await getAcademicPeriods();
          setPeriods(periodList);

          const openPeriod = periodList.find((p) => p.status === 'OPEN') || periodList[0];
          if (openPeriod) {
            setSelectedPeriod(openPeriod.periodCode);
          }
        }
      } catch (err) {
        console.error("Error cargando perfil u horarios", err);
      } finally {
        setLoading(false);
      }
    };

    init();
  }, []);

  useEffect(() => {
    const buildSchedule = async () => {
      if (!student || !selectedPeriod) return;
      setLoading(true);

      try {
        const registrations = await getRegistrationsByStudent(student.run, selectedPeriod);
        const sections = await getSectionsByPeriod(selectedPeriod);

        // Mapeo matriz [day-moduleNumber] -> info sección
        const grid = {};
        registrations.forEach((reg) => {
          const matchedSection = sections.find((s) => s.code === reg.sectionCode);
          if (matchedSection && matchedSection.schedules) {
            matchedSection.schedules.forEach((sch) => {
              const key = `${sch.day}-${sch.moduleNumber}`;
              grid[key] = {
                courseCode: matchedSection.courseCode,
                sectionCode: matchedSection.code
              };
            });
          }
        });

        setScheduleGrid(grid);
      } catch (err) {
        console.error("Error al construir la grilla de horarios", err);
      } finally {
        setLoading(false);
      }
    };

    buildSchedule();
  }, [selectedPeriod, student]);

  if (loading && !selectedPeriod) {
    return (
      <div className="text-center my-5">
        <Spinner animation="border" variant="primary" />
        <p className="mt-2">Generando horario...</p>
      </div>
    );
  }

  return (
    <div>
      <h3 className="mb-4">Horario de Clases</h3>

      <Form.Group className="mb-4" style={{ maxWidth: '300px' }}>
        <Form.Label>Seleccionar Período Académico</Form.Label>
        <Form.Select value={selectedPeriod} onChange={(e) => setSelectedPeriod(e.target.value)}>
          {periods.map((p) => (
            <option key={p.periodCode} value={p.periodCode}>
              {p.periodCode}
            </option>
          ))}
        </Form.Select>
      </Form.Group>

      <Card className="shadow-sm border-0">
        <Card.Body className="p-0">
          <Table bordered responsive className="mb-0 text-center align-middle">
            <thead className="table-dark">
              <tr>
                <th>Módulo</th>
                {days.map((d) => (
                  <th key={d.code}>{d.name}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {modules.map((m) => (
                <tr key={m}>
                  <td className="fw-bold table-light">Módulo {m}</td>
                  {days.map((d) => {
                    const slot = scheduleGrid[`${d.code}-${m}`];
                    return (
                      <td key={d.code} style={{ height: '70px', width: '18%' }}>
                        {slot ? (
                          <div className="p-2 bg-primary text-white rounded">
                            <div className="fw-bold">{slot.courseCode}</div>
                            <small className="d-block">{slot.sectionCode}</small>
                          </div>
                        ) : (
                          <span className="text-muted small">-</span>
                        )}
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </Table>
        </Card.Body>
      </Card>
    </div>
  );
};

export default StudentScheduleView;