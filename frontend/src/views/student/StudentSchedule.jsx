import React, { useState, useEffect } from 'react';
import { Card, Table, Form, Spinner, Alert, Badge } from 'react-bootstrap';
import keycloak from '../../config/keycloak';
import {
  getStudentByEmail,
  getAcademicPeriods,
  getRegistrationsByStudent,
  getSectionsByPeriod,
  getCourseByCodeAndPlan
} from '../../services/StudentService';

const StudentScheduleView = () => {
  const [student, setStudent] = useState(null);
  const [periods, setPeriods] = useState([]);
  const [selectedPeriod, setSelectedPeriod] = useState('');
  const [scheduleGrid, setScheduleGrid] = useState({});
  const [loading, setLoading] = useState(true);
  const [sections, setSections] = useState([]);

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
            setSelectedPeriod(openPeriod.period_code);
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
        const periodSections = await getSectionsByPeriod(selectedPeriod);

        const enrolledSections = registrations
          .map((reg) => {
            const sectionCode = reg.sectionCode || reg.section_code;
            return periodSections.find(
              (section) => (section.code || section.sectionCode) === sectionCode
            );
          })
          .filter(Boolean);

        const sectionsWithCourseNames = await Promise.all(
          enrolledSections.map(async (section) => {
            const sectionDetails = await getCourseByCodeAndPlan(
              section.course_code,
              student.study_plan_code
            );

            return {
              ...section,
              course_name: sectionDetails.name
            };
          })
        );

        setSections(sectionsWithCourseNames);
        // Mapeo matriz [day-moduleNumber] -> info sección
        const grid = {};
        registrations.forEach((reg) => {
          const sectionCode = reg.sectionCode || reg.section_code;
          const matchedSection = periodSections.find(
            (section) => (section.code || section.sectionCode) === sectionCode
          );
          if (matchedSection && matchedSection.schedules) {
            matchedSection.schedules.forEach((sch) => {
              const day = sch.day || sch.dayCode;
              const moduleNumber = sch.moduleNumber || sch.module_number;
              const key = `${day}-${moduleNumber}`;
              grid[key] = {
                courseCode: matchedSection.courseCode || matchedSection.course_code,
                sectionCode: matchedSection.code || matchedSection.sectionCode
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
            <option key={p.period_code} value={p.period_code}>
              {p.period_code}
            </option>
          ))}
        </Form.Select>
      </Form.Group>

      <Card className="shadow-sm border-0 mb-4">
        <Card.Body>
          <Card.Title>Lista de ramos</Card.Title>
          {sections.length > 0 ? (
            <Table bordered hover responsive className="mb-0 align-middle">
              <thead>
                <tr>
                  <th>Código</th>
                  <th>Nombre</th>
                  <th>Horario</th>
                </tr>
              </thead>
              <tbody>
                {sections.map((section) => {
                  const schedules = (section.schedules || [])
                    .map((schedule) => {
                      const day = schedule.day || schedule.dayCode;
                      const moduleNumber = schedule.moduleNumber || schedule.module_number;
                      return day && moduleNumber ? `${day}${moduleNumber}` : null;
                    })
                    .filter(Boolean)
                    .join(' - ');

                  return (
                    <tr key={section.code || section.sectionCode}>
                      <td>{section.course_code}</td>
                      <td>{section.course_name}</td>
                      <td>{schedules || '-'}</td>
                    </tr>
                  );
                })}
              </tbody>
            </Table>
          ) : (
            <p className="text-muted mb-0">No hay ramos inscritos en este período.</p>
          )}
        </Card.Body>
      </Card>

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