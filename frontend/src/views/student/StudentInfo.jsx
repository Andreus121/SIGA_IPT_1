import React, { useState, useEffect } from 'react';
import { Card, Table, Badge, Spinner, Alert, Row, Col } from 'react-bootstrap';
import keycloak from '../../config/keycloak';
import { getStudentByEmail, getCoursesByStudyPlan } from '../../services/StudentService';

const StudentCurriculumView = () => {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchCurriculum = async () => {
      try {
        const userEmail = keycloak.tokenParsed?.email;
        if (userEmail) {
          const student = await getStudentByEmail(userEmail);
          const courseList = await getCoursesByStudyPlan(student.study_plan_code);
          setCourses(courseList);
        }
      } catch (err) {
        setError('Error al obtener las asignaturas de la malla curricular.');
      } finally {
        setLoading(false);
      }
    };

    fetchCurriculum();
  }, []);

  if (loading) {
    return (
      <div className="text-center my-5">
        <Spinner animation="border" variant="primary" />
        <p className="mt-2">Cargando malla curricular...</p>
      </div>
    );
  }

  if (error) {
    return <Alert variant="danger">{error}</Alert>;
  }

  // Agrupar asignaturas por semestre
  const semesters = [1, 2, 3, 4];

  return (
    <div>
      <h3 className="mb-4">Malla Curricular y Asignaturas</h3>
      <Row>
        {semesters.map((sem) => {
          const semesterCourses = courses.filter((c) => c.semester === sem);
          return (
            <Col md={12} key={sem} className="mb-4">
              <Card className="shadow-sm border-0">
                <Card.Header className="bg-secondary text-white fw-bold">
                  Semestre {sem}
                </Card.Header>
                <Card.Body className="p-0">
                  <Table responsive hover className="mb-0 text-center align-middle">
                    <thead className="table-light">
                      <tr>
                        <th>Código</th>
                        <th>Nombre</th>
                        <th>Teoría (hrs)</th>
                        <th>Ejercicios (hrs)</th>
                        <th>Lab (hrs)</th>
                        <th>Créditos SCT</th>
                        <th>Prerrequisitos</th>
                      </tr>
                    </thead>
                    <tbody>
                      {semesterCourses.length === 0 ? (
                        <tr>
                          <td colSpan="7" className="text-muted">
                            Sin asignaturas registradas para este semestre.
                          </td>
                        </tr>
                      ) : (
                        semesterCourses.map((course) => (
                          <tr key={course.id}>
                            <td className="fw-bold">{course.code}</td>
                            <td>{course.name}</td>
                            <td>{course.theory_hours}</td>
                            <td>{course.exercise_hours}</td>
                            <td>{course.lab_hours}</td>
                            <td>
                              <Badge bg="info">{course.sct} SCT</Badge>
                            </td>
                            {/* Si no tiene prerequisitos mostramos un guion */}
                            <td>{course.prerequisites_codes.length > 0 ? course.prerequisites_codes.join(', ') : '-'}</td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </Table>
                </Card.Body>
              </Card>
            </Col>
          );
        })}
      </Row>
    </div>
  );
};

export default StudentCurriculumView;