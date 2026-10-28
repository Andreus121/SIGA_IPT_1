import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Spinner, Modal, Form } from 'react-bootstrap';
import { getCourses, createCourse } from '../../services/AdminService';

const AdminCourses = () => {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [saving, setSaving] = useState(false);
  const [formData, setFormData] = useState({
    code: '', 
    studyPlanCode: '', 
    name: '', 
    semester: 1, 
    sct: 0, 
    theoryHours: 0, 
    exerciseHours: 0, 
    labHours: 0
  });

  const fetchCourses = async () => {
    setLoading(true);
    try {
      const data = await getCourses();
      setCourses(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchCourses(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await createCourse(formData);
      setShowModal(false);
      fetchCourses();
    } catch (err) {
      alert('Error al crear asignatura');
    } finally {
      setSaving(false);
    }
  };

  if (loading && courses.length === 0) return <Spinner animation="border" className="d-block mx-auto mt-5" />;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3>Malla: Asignaturas</h3>
        <Button onClick={() => setShowModal(true)}>+ Nueva Asignatura</Button>
      </div>

      <Card className="shadow-sm border-0">
        <Table responsive hover className="mb-0">
          <thead className="table-dark">
            <tr>
              <th>Código</th>
              <th>Nombre</th>
              <th>Plan</th>
              <th>Sem.</th>
              <th>SCT</th>
              <th>Horas (T/E/L)</th>
            </tr>
          </thead>
          <tbody>
            {courses.map(c => (
              <tr key={c.id}>
                <td className="fw-bold">{c.code}</td>
                <td>{c.name}</td>
                <td>{c.studyPlanCode}</td>
                <td>{c.semester}</td>
                <td>{c.sct}</td>
                <td>{c.theoryHours} / {c.exerciseHours} / {c.labHours}</td>
              </tr>
            ))}
          </tbody>
        </Table>
      </Card>

      <Modal show={showModal} onHide={() => setShowModal(false)} size="lg">
        <Form onSubmit={handleSubmit}>
          <Modal.Header closeButton><Modal.Title>Registrar Asignatura</Modal.Title></Modal.Header>
          <Modal.Body className="row">
            {/* Código de la Asignatura */}
            <Form.Group className="col-md-6 mb-3">
              <Form.Label>Código Asignatura</Form.Label>
              <Form.Control type="text" onChange={e => setFormData({...formData, code: e.target.value})} required />
            </Form.Group>
            {/* Nombre de la Asignatura */}
            <Form.Group className="col-md-6 mb-3">
              <Form.Label>Nombre</Form.Label>
              <Form.Control type="text" onChange={e => setFormData({...formData, name: e.target.value})} required />
            </Form.Group>
            {/* Plan de Estudio Asociado */}
            <Form.Group className="col-md-6 mb-3">
              <Form.Label>Plan de Estudio Asociado</Form.Label>
              <Form.Control type="text" onChange={e => setFormData({...formData, studyPlanCode: e.target.value})} required />
            </Form.Group>
            {/* Semestre de la Malla */}
            <Form.Group className="col-md-6 mb-3">
              <Form.Label>Semestre de la Malla</Form.Label>
              <Form.Control type="number" onChange={e => setFormData({...formData, semester: e.target.value})} required />
            </Form.Group>
            {/* Créditos SCT*/}
            <Form.Group className="col-md-3 mb-3">
              <Form.Label>Créditos SCT</Form.Label>
              <Form.Control type="number" onChange={e => setFormData({...formData, sct: NUMBER(e.target.value)})} required />
            </Form.Group>
            {/* Horas de teoría */}
            <Form.Group className="col-md-3 mb-3">
              <Form.Label>Hrs Teoría</Form.Label>
              <Form.Control type="number" onChange={e => setFormData({...formData, theoryHours: NUMBER(e.target.value)})} required />
            </Form.Group>
            {/* Horas de ejercicios */}
            <Form.Group className="col-md-3 mb-3">
              <Form.Label>Hrs Ejercicios</Form.Label>
              <Form.Control type="number" onChange={e => setFormData({...formData, exerciseHours: NUMBER(e.target.value)})} required />
            </Form.Group>
            {/* Horas de laboratorio */}
            <Form.Group className="col-md-3 mb-3">
              <Form.Label>Hrs Lab</Form.Label>
              <Form.Control type="number" onChange={e => setFormData({...formData, labHours: NUMBER(e.target.value)})} required />
            </Form.Group>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowModal(false)}>Cancelar</Button>
            <Button variant="primary" type="submit" disabled={saving}>Guardar</Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </div>
  );
};
export default AdminCourses;