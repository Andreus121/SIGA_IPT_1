import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Spinner, Modal, Form } from 'react-bootstrap';
import { getCourses, createCourse, getStudyPlans, getCareers, deleteCourse } from '../../services/AdminService';

const AdminCourses = () => {
  const [courses, setCourses] = useState([]);
  const [plans, setPlans] = useState([]);
  const [careersMap, setCareersMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  // prerequisitos seleccionados en cualquiera de los formularios (crear o editar)
  const [prereq1, setPrereq1] = useState('');
  const [prereq2, setPrereq2] = useState('');
  const [prereq3, setPrereq3] = useState('');

  // mostrar u ocultar modal de crear asignatura
  const [showCreateModal, setCreateShowModal] = useState(false);
  // estado inicial del formulario para crear una nueva asignatura
  const initialCreateFormData = {
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
  const [createFormData, setCreateFormData] = useState(initialCreateFormData);


  // mostrar u ocultar modal de crear asignatura
  const [showEditModal, setShowEditModal] = useState(false);
  // code de la asignatura que se está editando
  const [editingCode, setEditingCode] = useState(null);
  // estado inicial de formulario de actualizar asignatura
  const initialEditFormData = {
    name: '',
    semester: 1,
    sct: 0,
    theoryHours: 0,
    exerciseHours: 0,
    labHours: 0,
    prerequisitesCodes: []
  };
  // formulario para actualizar una asignatura
  const [formEditData, setFormEditData] = useState(initialEditFormData);

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

  //abrir el modal de crear asignatura y limpiar el formulario
  const handleOpenCreateModal = () => {
    setCreateFormData(initialCreateFormData);
    setPrereq1('');
    setPrereq2('');
    setPrereq3('');
    setCreateShowModal(true);
  }

  // llamado a crear una asignatura
  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);

    // si uno de los prerequisitos es del mismo semestre, no se puede crear la asignatura
    const currentPlan = plans.find(p => p.code === createFormData.studyPlanCode);
    if (currentPlan) {
      const selectedPrereqCourses = courses.filter(c => createFormData.prerequisitesCodes.includes(c.code) && c.study_plan_code === currentPlan.code);
      if (selectedPrereqCourses.some(c => c.semester === createFormData.semester)) {
        alert('No se puede crear la asignatura: uno de los prerequisitos es del mismo semestre.');
        return;
      }
    }

    if (prereq1 !== '') createFormData.prerequisitesCodes.push(prereq1);
    if (prereq2 !== '') createFormData.prerequisitesCodes.push(prereq2);
    if (prereq3 !== '') createFormData.prerequisitesCodes.push(prereq3);

    // Llamamos a la función createCourse del servicio
    try {
      await createCourse(createFormData);
      setCreateShowModal(false);
      setCreateFormData(initialCreateFormData); // Limpiamos el formulario
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
  
  // abrir el modal de editar asignatura y llenar el formulario con los datos de la asignatura seleccionada
  const handleOpenEditModal = (course) => {
    setEditingCode(course.code); // O career.id según requiera tu API
    setFormEditData({
      name: course.name || '',
      semester: course.semester || '',
      sct: course.sct || 0,
      theoryHours: course.theory_hours || 0,
      exerciseHours: course.exercise_hours || 0,
      labHours: course.lab_hours || 0,
      prerequisitesCodes: course.prerequisites_codes || []
    });
    setPrereq1(course.prerequisites_codes[0] || '');
    setPrereq2(course.prerequisites_codes[1] || '');
    setPrereq3(course.prerequisites_codes[2] || '');
    setShowEditModal(true);
  };

  // llamado a actualizar una asignatura
  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await updateCareer(editingCode, formEditData);
      setShowEditModal(false);
      fetchData();
    } catch (err) {
      alert('Error al actualizar la carrera');
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
        <Button onClick={() => handleOpenCreateModal()}>+ Nueva Asignatura</Button>
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
                    <Button variant="outline-primary" size="sm" className="me-2" onClick={() => handleOpenEditModal(c)}>Editar</Button>
                    <Button variant="outline-danger" size="sm" onClick={() => handleDelete(c)}>Eliminar</Button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </Table>
      </Card>
      
      {/* ================= MODAL 1: CREAR ASIGNATURA ================= */}
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
                value={createFormData.code}
                onChange={e => setCreateFormData({...createFormData, code: e.target.value})} 
                required 
              />
            </Form.Group>

            {/* Nombre de la Asignatura */}
            <Form.Group className="col-md-6 mb-3">
              <Form.Label>Nombre</Form.Label>
              <Form.Control 
                type="text" 
                value={createFormData.name}
                onChange={e => setCreateFormData({...createFormData, name: e.target.value})} 
                required 
              />
            </Form.Group>

            {/* Selector de Plan de Estudio Asociado */}
            <Form.Group className="col-md-12 mb-3">
              <Form.Label>Plan de Estudio Asociado</Form.Label>
              <Form.Select 
                value={createFormData.studyPlanCode} 
                onChange={e => setCreateFormData({...createFormData, studyPlanCode: e.target.value})} 
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
                value={createFormData.semester}
                onChange={e => setCreateFormData({...createFormData, semester: Number(e.target.value)})} 
                required 
              />
            </Form.Group>

            {/* Créditos SCT */}
            <Form.Group className="col-md-3 mb-3">
              <Form.Label>Créditos SCT</Form.Label>
              <Form.Control 
                type="number" 
                min="0"
                value={createFormData.sct}
                onChange={e => setCreateFormData({...createFormData, sct: Number(e.target.value)})} 
                required 
              />
            </Form.Group>

            {/* Horas de Teoría */}
            <Form.Group className="col-md-2 mb-3">
              <Form.Label>Hrs Teoría</Form.Label>
              <Form.Control 
                type="number" 
                min="0"
                value={createFormData.theoryHours}
                onChange={e => setCreateFormData({...createFormData, theoryHours: Number(e.target.value)})} 
                required 
              />
            </Form.Group>

            {/* Horas de Ejercicios */}
            <Form.Group className="col-md-2 mb-3">
              <Form.Label>Hrs Ejercicios</Form.Label>
              <Form.Control 
                type="number" 
                min="0"
                value={createFormData.exerciseHours}
                onChange={e => setCreateFormData({...createFormData, exerciseHours: Number(e.target.value)})} 
                required 
              />
            </Form.Group>

            {/* Horas de Laboratorio */}
            <Form.Group className="col-md-2 mb-3">
              <Form.Label>Hrs Lab</Form.Label>
              <Form.Control 
                type="number" 
                min="0"
                value={createFormData.labHours}
                onChange={e => setCreateFormData({...createFormData, labHours: Number(e.target.value)})} 
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
                disabled={!createFormData.studyPlanCode}
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
                disabled={!createFormData.studyPlanCode}
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
                disabled={!createFormData.studyPlanCode}
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
    
      {/* ================= MODAL 2: EDITAR ASIGNATURA ================= */}
      <Modal show={showEditModal} onHide={() => setShowEditModal(false)}>
        <Form onSubmit={handleEditSubmit}>
          <Modal.Header closeButton>
            <Modal.Title>Editar Asignatura ({editingCode})</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Form.Group className="mb-3">
              <Form.Label>Nombre</Form.Label>
              <Form.Control 
                type="text" 
                value={formEditData.name} 
                onChange={e => setFormEditData({...formEditData, name: e.target.value})} 
                required 
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Semestre</Form.Label>
              <Form.Control 
                as="textarea"
                rows={3}
                value={formEditData.semester} 
                onChange={e => setFormEditData({...formEditData, semester: e.target.value})} 
                required 
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Hrs Teoría</Form.Label>
              <Form.Control 
                type="number" 
                min="0"
                value={formEditData.theoryHours}
                onChange={e => setFormEditData({...formEditData, theoryHours: Number(e.target.value)})} 
                required 
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Hrs Ejercicios</Form.Label>
              <Form.Control 
                type="number" 
                min="0"
                value={formEditData.labExerciseHours}
                onChange={e => setFormEditData({...formEditData, labExerciseHours: Number(e.target.value)})} 
                required 
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Hrs Laboratorio</Form.Label>
              <Form.Control 
                type="number"
                min="0"
                value={formEditData.labHours}
                onChange={e => setFormEditData({...formEditData, labHours: Number(e.target.value)})} 
                required 
              />
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowEditModal(false)}>Cancelar</Button>
            <Button variant="primary" type="submit" disabled={saving}>
              {saving ? 'Actualizando...' : 'Actualizar'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </div>
  );
};

export default AdminCourses;