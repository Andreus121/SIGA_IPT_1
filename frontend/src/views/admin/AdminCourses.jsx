import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Spinner, Modal, Form } from 'react-bootstrap';
import { getCourses, createCourse, getStudyPlans, getCareers, deleteCourse } from '../../services/AdminService';

const AdminCourses = () => {
  const [courses, setCourses] = useState([]);
  const [plans, setPlans] = useState([]);
  const [careersMap, setCareersMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setCreateShowModal] = useState(false);
  const [saving, setSaving] = useState(false);
  const [prereq1, setPrereq1] = useState('');
  const [prereq2, setPrereq2] = useState('');
  const [prereq3, setPrereq3] = useState('');


  // estado inicial del formulario para crear una nueva asignatura
  const initialFormData = {
    code: '', 
    studyPlanCode: '', 
    name: '', 
    semester: 1, 
    sct: 0, 
    theoryHours: 0, 
    exerciseHours: 0, 
    labHours: 0,
    prerequisitesCodes: []
  };

  // formulario para crear una nueva asignatura
  const [formData, setFormData] = useState(initialFormData);

  const fetchData = async () => {
    setLoading(true);
    try {
      // Cargamos asignaturas, planes de estudio y carreras en paralelo
      const [coursesData, plansData, careersData] = await Promise.all([
        getCourses(),
        getStudyPlans(),
        getCareers()
      ]);
      console.log('Datos cargados en AdminCourses:', { coursesData, plansData, careersData });
      setCourses(coursesData);
      setPlans(plansData);

      // Creamos un diccionario { "10450": "Técnico Analista Programador", ... }
      const map = {};
      careersData.forEach(c => {
        map[c.code] = c.name;
      });
      setCareersMap(map);

    } catch (err) {
      console.error('Error al cargar datos en AdminCourses:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);

    // si uno de los prerequisitos es del mismo semestre, no se puede crear la asignatura
    const currentPlan = plans.find(p => p.code === formData.studyPlanCode);
    if (currentPlan) {
      const selectedPrereqCourses = courses.filter(c => formData.prerequisitesCodes.includes(c.code) && c.study_plan_code === currentPlan.code);
      if (selectedPrereqCourses.some(c => c.semester === formData.semester)) {
        alert('No se puede crear la asignatura: uno de los prerequisitos es del mismo semestre.');
        return;
      }
    }

    if (prereq1 !== '') formData.prerequisitesCodes.push(prereq1);
    if (prereq2 !== '') formData.prerequisitesCodes.push(prereq2);
    if (prereq3 !== '') formData.prerequisitesCodes.push(prereq3);

    // Llamamos a la función createCourse del servicio
    try {
      await createCourse(formData);
      setCreateShowModal(false);
      setFormData(initialFormData); // Limpiamos el formulario
      setPrereq1('');
      setPrereq2('');
      setPrereq3('');
      // avisar que se creó con exito
      alert('Asignatura creada con éxito');
      fetchData();
    } catch (err) {
      const msg = err.response?.data?.message || 'Error al crear asignatura';
      alert(`Error: ${msg}`);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (course) => {
    if (!window.confirm(`¿Está seguro de eliminar la asignatura "${course.name}" (${course.code})?`)) {
      return;
    }

    try {
      // Ajusta entre course.id o course.code según tu backend
      await deleteCourse(course.study_plan_code, course.code);
      fetchData();
    } catch (err) {
      const msg = err.response?.data?.message || 'No se pudo eliminar la asignatura';
      alert(`Error al eliminar: ${msg}`);
    }
  };

  if (loading && courses.length === 0) return <Spinner animation="border" className="d-block mx-auto mt-5" />;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3>Malla: Asignaturas</h3>
        <Button onClick={() => setCreateShowModal(true)}>+ Nueva Asignatura</Button>
      </div>

      <Card className="shadow-sm border-0">
        <Table responsive hover className="mb-0 align-middle">
          <thead className="table-dark">
            <tr>
              <th>Código</th>
              <th>Nombre</th>
              <th>Plan</th>
              <th>Semestre</th>
              <th>Prerequisitos</th>
              <th>SCT</th>
              <th>Horas (T/E/L)</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {courses.length === 0 ? (
              <tr>
                <td colSpan="6" className="text-center text-muted py-3">
                  No hay asignaturas registradas.
                </td>
              </tr>
            ) : (
              courses.map(c => (
                <tr key={c.id || c.code}>
                  <td className="fw-bold">{c.code}</td>
                  <td>{c.name}</td>
                  <td>{c.study_plan_code}</td>
                  <td>{c.semester}</td>
                  {/* Si no tiene prerequisitos mostramos un guion */}
                  <td>{c.prerequisites_codes.length > 0 ? c.prerequisites_codes.join(', ') : '-'}</td>
                  <td>{c.sct}</td>
                  <td>{c.theory_hours} / {c.exercise_hours} / {c.lab_hours}</td>
                  <td>
                    <Button variant="outline-primary" size="sm" className="me-2">Editar</Button>
                    <Button variant="outline-danger" size="sm" onClick={() => handleDelete(c)}>Eliminar</Button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </Table>
      </Card>

      <Modal show={showCreateModal} onHide={() => setCreateShowModal(false)} size="lg">
        <Form onSubmit={handleCreateSubmit}>
          <Modal.Header closeButton>
            <Modal.Title>Registrar Asignatura</Modal.Title>
          </Modal.Header>
          <Modal.Body className="row">
            {/* Código de la Asignatura */}
            <Form.Group className="col-md-6 mb-3">
              <Form.Label>Código Asignatura</Form.Label>
              <Form.Control 
                type="text" 
                value={formData.code}
                onChange={e => setFormData({...formData, code: e.target.value})} 
                required 
              />
            </Form.Group>

            {/* Nombre de la Asignatura */}
            <Form.Group className="col-md-6 mb-3">
              <Form.Label>Nombre</Form.Label>
              <Form.Control 
                type="text" 
                value={formData.name}
                onChange={e => setFormData({...formData, name: e.target.value})} 
                required 
              />
            </Form.Group>

            {/* Selector de Plan de Estudio Asociado */}
            <Form.Group className="col-md-12 mb-3">
              <Form.Label>Plan de Estudio Asociado</Form.Label>
              <Form.Select 
                value={formData.studyPlanCode} 
                onChange={e => setFormData({...formData, studyPlanCode: e.target.value})} 
                required
              >
                <option value="">Seleccione un plan de estudio...</option>
                {plans.map(p => {
                  const careerName = careersMap[p.career_code] || 'Carrera s/n';
                  return (
                    <option key={p.id || p.code} value={p.code}>
                      {p.career_code} - {careerName} | Plan: {p.code} - {p.name}
                    </option>
                  );
                })}
              </Form.Select>
            </Form.Group>

            {/* Semestre de la Malla */}
            <Form.Group className="col-md-3 mb-3">
              <Form.Label>Semestre Malla</Form.Label>
              <Form.Control 
                type="number" 
                min="1"
                value={formData.semester}
                onChange={e => setFormData({...formData, semester: Number(e.target.value)})} 
                required 
              />
            </Form.Group>

            {/* Créditos SCT */}
            <Form.Group className="col-md-3 mb-3">
              <Form.Label>Créditos SCT</Form.Label>
              <Form.Control 
                type="number" 
                min="0"
                value={formData.sct}
                onChange={e => setFormData({...formData, sct: Number(e.target.value)})} 
                required 
              />
            </Form.Group>

            {/* Horas de Teoría */}
            <Form.Group className="col-md-2 mb-3">
              <Form.Label>Hrs Teoría</Form.Label>
              <Form.Control 
                type="number" 
                min="0"
                value={formData.theoryHours}
                onChange={e => setFormData({...formData, theoryHours: Number(e.target.value)})} 
                required 
              />
            </Form.Group>

            {/* Horas de Ejercicios */}
            <Form.Group className="col-md-2 mb-3">
              <Form.Label>Hrs Ejercicios</Form.Label>
              <Form.Control 
                type="number" 
                min="0"
                value={formData.exerciseHours}
                onChange={e => setFormData({...formData, exerciseHours: Number(e.target.value)})} 
                required 
              />
            </Form.Group>

            {/* Horas de Laboratorio */}
            <Form.Group className="col-md-2 mb-3">
              <Form.Label>Hrs Lab</Form.Label>
              <Form.Control 
                type="number" 
                min="0"
                value={formData.labHours}
                onChange={e => setFormData({...formData, labHours: Number(e.target.value)})} 
                required 
              />
            </Form.Group>

            {/* ================= SECCIÓN PRERREQUISITOS (0 A 3) ================= */}
            <hr className="my-3" />
            <h6 className="text-muted mb-3">Prerrequisitos (Opcional - Hasta 3 asignaturas)</h6>

            {/* Prerrequisito 1 */}
            <Form.Group className="col-md-4 mb-3">
              <Form.Label>Prerrequisito 1</Form.Label>
              <Form.Select 
                value={prereq1}
                onChange={e => setPrereq1(e.target.value)}
                disabled={!formData.studyPlanCode}
              >
                <option value="">Ninguno</option>
                {courses
                  .map(c => (
                    <option key={c.id} value={c.code}>
                      {c.code} - {c.name}
                    </option>
                  ))
                }
              </Form.Select>
            </Form.Group>

            {/* Prerrequisito 2 */}
            <Form.Group className="col-md-4 mb-3">
              <Form.Label>Prerrequisito 2</Form.Label>
              <Form.Select 
                value={prereq2}
                onChange={e => setPrereq2(e.target.value )}
                disabled={!formData.studyPlanCode}
              >
                <option value="">Ninguno</option>
                {courses
                  .map(c => (
                    <option key={c.id} value={c.code}>
                      {c.code} - {c.name}
                    </option>
                  ))
                }
              </Form.Select>
            </Form.Group>

            {/* Prerrequisito 3 */}
            <Form.Group className="col-md-4 mb-3">
              <Form.Label>Prerrequisito 3</Form.Label>
              <Form.Select 
                value={prereq3}
                onChange={e => setPrereq3(e.target.value)}
                disabled={!formData.studyPlanCode}
              >
                <option value="">Ninguno</option>
                {courses
                  .map(c => (
                    <option key={c.id} value={c.code}>
                      {c.code} - {c.name}
                    </option>
                  ))
                }
              </Form.Select>
            </Form.Group>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setCreateShowModal(false)}>Cancelar</Button>
            <Button variant="primary" type="submit" disabled={saving}>
              {saving ? 'Guardando...' : 'Guardar'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </div>
  );
};

export default AdminCourses;