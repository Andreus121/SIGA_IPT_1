import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Badge, Spinner, Alert, Form, Row, Col } from 'react-bootstrap';
import keycloak from '../../config/keycloak';
import {
  getStudentByEmail,
  getAcademicPeriods,
  getSectionsByPeriod,
  getCourseByCodeAndPlan,
  getRegistrationsByStudent,
  createRegistration,
  deleteRegistration,
  getEnrollmentsByStudent
} from '../../services/StudentService';

const StudentRegistrationView = () => {
  const [student, setStudent] = useState(null);
  const [periods, setPeriods] = useState([]);
  const [selectedPeriod, setSelectedPeriod] = useState('');
  const [availableSections, setAvailableSections] = useState([]);
  const [courses, setCourses] = useState([]);
  const [myRegistrations, setMyRegistrations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [enrollments, setEnrollments] = useState(null);

  useEffect(() => {
    const initData = async () => {
      try {
        const userEmail = keycloak.tokenParsed?.email;
        if (userEmail) {
          const studentData = await getStudentByEmail(userEmail);
          setStudent(studentData);

          const periodList = await getAcademicPeriods();
          setPeriods(periodList);

          const openPeriod = periodList.find((p) => p.status === 'OPEN');
          if (openPeriod) {
            setSelectedPeriod(openPeriod.period_code);
          }
        }
      } catch (err) {
        setMessage({ type: 'danger', text: 'Error al inicializar la vista de toma de ramos.' });
      } finally {
        setLoading(false);
      }
    };

    initData();
  }, []);

  const fetchRegistrationsAndSections = async () => {
    if (!student || !selectedPeriod) return;
    setLoading(true);
    try {
      const sections = await getSectionsByPeriod(selectedPeriod);
      setAvailableSections(sections);
      console.log("sections", sections);
      const planCode = student.study_plan_code;
      const courseCodes = [...new Set(sections.map((section) => section.course_code))];
      for (const courseCode of courseCodes) {
        const course = await getCourseByCodeAndPlan(courseCode, planCode);
        if (course) {
          courses.push(course);
        }
      }
      console.log("courses: ", courses);
      const registered = await getRegistrationsByStudent(student.run, selectedPeriod);
      setMyRegistrations(registered);
    } catch (err) {
      setMessage({ type: 'danger', text: 'Error al obtener oferta de secciones o ramos inscritos.' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRegistrationsAndSections();
  }, [selectedPeriod, student]);

  // Obtener todas las matriculas del estudiante por su rut
  // y verificar si tiene una matricula en el periodo seleccionado
  const fetchEnrollments = async () => {
    try {
      const enrollmentsStudent = await getEnrollmentsByStudent(student.run);
      setEnrollments(enrollmentsStudent);
      return enrollmentsStudent;
    } catch (err) {
      setMessage({ type: 'danger', text: 'Error al obtener las matrículas del estudiante.' });
      return [];
    }
  };

  const hasEnrollmentInSelectedPeriod = async (period_code) => {
    const enrollmentsStudent = await fetchEnrollments();
    return enrollmentsStudent.some(
      (enrollment) =>
        enrollment.period_code === period_code && enrollment.status === 'ACTIVE'
    );
  };

  const handleRegister = async (section_code) => {
    if (!(await hasEnrollmentInSelectedPeriod(selectedPeriod))) {
      setMessage({ type: 'danger', text: 'No puedes tomar ramos sin matricularte en el periodo academico' });
      return;
    }

    setActionLoading(true);
    setMessage(null);
    try {
      await createRegistration({
        student_run: student.run,
        section_code: section_code,
        period_code: selectedPeriod
      });
      setMessage({ type: 'success', text: `Asignatura/Sección ${section_code} inscrita correctamente.` });
      await fetchRegistrationsAndSections();
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Error al inscribir la sección.';
      setMessage({ type: 'danger', text: errorMsg });
    } finally {
      setActionLoading(false);
    }
  };

  const handleUnregister = async (registrationId) => {
    if (!window.confirm('¿Está seguro de que desea anular la inscripción de este ramo?')) return;
    
    setActionLoading(true);
    setMessage(null);
    try {
      await deleteRegistration(registrationId);
      setMessage({ type: 'info', text: 'Inscripción anulada correctamente.' });
      await fetchRegistrationsAndSections();
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Error al anular la inscripción.';
      setMessage({ type: 'danger', text: errorMsg });
    } finally {
      setActionLoading(false);
    }
  };

  if (loading && !selectedPeriod) {
    return (
      <div className="text-center my-5">
        <Spinner animation="border" variant="primary" />
        <p className="mt-2">Cargando toma de ramos...</p>
      </div>
    );
  }

  return (
    <div>
      <h3 className="mb-4">Toma de Ramos / Inscripción de Asignaturas</h3>

      {message && <Alert variant={message.type}>{message.text}</Alert>}

      <Form.Group className="mb-4" style={{ maxWidth: '300px' }}>
        <Form.Label>Seleccionar Período Académico</Form.Label>
        <Form.Select value={selectedPeriod} onChange={(e) => setSelectedPeriod(e.target.value)}>
          {periods.map((p) => (
            <option key={p. period_code} value={p.period_code}>
              {p. period_code}
            </option>
          ))}
        </Form.Select>
      </Form.Group>

      <Row>
        <Col md={12} className="mb-4">
          <Card className="shadow-sm border-0">
            <Card.Header className="bg-success text-white fw-bold">
              Mis Ramos Inscritos en {selectedPeriod}
            </Card.Header>
            <Card.Body className="p-0">
              <Table responsive hover className="mb-0 text-center align-middle">
                <thead className="table-light">
                  <tr>
                    <th>Asignatura</th>
                    <th>Sección</th>
                    <th>Acción</th>
                  </tr>
                </thead>
                <tbody>
                  {myRegistrations.length === 0 ? (
                    <tr>
                      <td colSpan="5" className="text-muted">No has inscrito ramos en este período.</td>
                    </tr>
                  ) : (
                    myRegistrations.map((reg) => (
                      <tr key={reg.id}>
                        <td className="fw-bold">
                          {courses.find((course) => course.code === reg.course_code)?.name || "error"}
                        </td>
                        <td>{reg.section_code}</td>
                        <td>
                          <Button
                            variant="outline-danger"
                            size="sm"
                            disabled={actionLoading}
                            onClick={() => handleUnregister(reg.id)}
                          >
                            Anular
                          </Button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </Table>
            </Card.Body>
          </Card>
        </Col>

        <Col md={12}>
          <Card className="shadow-sm border-0">
            <Card.Header className="bg-dark text-white fw-bold">
              Oferta de Secciones Disponibles ({selectedPeriod})
            </Card.Header>
            <Card.Body className="p-0">
              <Table responsive hover className="mb-0 text-center align-middle">
                <thead className="table-light">
                  <tr>
                    <th>Asignatura</th>
                    <th>Sección</th>
                    <th>Capacidad</th>
                    <th>Horarios</th>
                    <th>Acción</th>
                  </tr>
                </thead>
                <tbody>
                  {availableSections.length === 0 ? (
                    <tr>
                      <td colSpan="5" className="text-muted">No hay secciones ofertadas en este período.</td>
                    </tr>
                  ) : (
                    availableSections.map((sec) => {
                      const isAlreadyRegistered = myRegistrations.some((r) => r.sectionCode === sec.code);
                      return (
                        <tr key={sec.id}>
                          <td className="fw-bold">
                            {courses.find((course) => course.code === sec.course_code)?.name || "error"}
                          </td>
                          <td>{sec.code}</td>
                          <td>{sec.capacity} cupos</td>
                          <td>
                            {sec.schedules.map((sch, i) => (
                              <Badge key={i} bg="secondary" className="me-1">
                                {sch.day}-M{sch.moduleNumber}
                              </Badge>
                            ))}
                          </td>
                          <td>
                            <Button
                              variant="primary"
                              size="sm"
                              disabled={actionLoading || isAlreadyRegistered}
                              onClick={() => handleRegister(sec.code)}
                            >
                              {isAlreadyRegistered ? 'Inscrito' : 'Inscribir'}
                            </Button>
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </Table>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default StudentRegistrationView;