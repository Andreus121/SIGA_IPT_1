import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Spinner, Alert, Modal, Form, Badge } from 'react-bootstrap';
import { getAllStudents, createStudent } from '../../services/AdminService';

const AdminStudents = () => {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Estados para el Modal
  const [showModal, setShowModal] = useState(false);
  const [saving, setSaving] = useState(false);
  const [formData, setFormData] = useState({
    run: '', fullName: '', email: '', careerCode: '', studyPlanCode: ''
  });

  const fetchStudents = async () => {
    setLoading(true);
    try {
      const data = await getAllStudents();
      setStudents(data);
    } catch (err) {
      setError('Error al obtener la lista de estudiantes.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStudents();
  }, []);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await createStudent(formData);
      setShowModal(false);
      setFormData({ run: '', fullName: '', email: '', careerCode: '', studyPlanCode: '' });
      fetchStudents();
    } catch (err) {
      alert(err.response?.data?.message || 'Error al crear estudiante');
    } finally {
      setSaving(false);
    }
  };

  if (loading && students.length === 0) return <div className="text-center my-5"><Spinner animation="border" /></div>;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3>Gestión de Estudiantes</h3>
        <Button variant="primary" onClick={() => setShowModal(true)}>+ Nuevo Estudiante</Button>
      </div>

      {error && <Alert variant="danger">{error}</Alert>}

      <Card className="shadow-sm border-0">
        <Card.Body className="p-0">
          <Table responsive hover className="mb-0 align-middle">
            <thead className="table-dark">
              <tr>
                <th>RUN</th>
                <th>Nombre</th>
                <th>Correo</th>
                <th>Carrera (Plan)</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {students.map(st => (
                <tr key={st.id}>
                  <td className="fw-bold">{st.run}</td>
                  <td>{st.fullName}</td>
                  <td>{st.email}</td>
                  <td>{st.careerCode} ({st.studyPlanCode})</td>
                  <td>
                    <Badge bg={st.status === 'REGULAR' ? 'success' : 'warning'}>{st.status}</Badge>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        </Card.Body>
      </Card>

      {/* Modal Crear Estudiante */}
      <Modal show={showModal} onHide={() => setShowModal(false)}>
        <Form onSubmit={handleSubmit}>
          <Modal.Header closeButton>
            <Modal.Title>Registrar Nuevo Estudiante</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Form.Group className="mb-3">
              <Form.Label>RUN</Form.Label>
              <Form.Control type="text" name="run" value={formData.run} onChange={handleChange} required placeholder="Ej: 12345678-9" />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Nombre Completo</Form.Label>
              <Form.Control type="text" name="fullName" value={formData.fullName} onChange={handleChange} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Correo Electrónico (Keycloak)</Form.Label>
              <Form.Control type="email" name="email" value={formData.email} onChange={handleChange} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Código de Carrera</Form.Label>
              <Form.Control type="text" name="careerCode" value={formData.careerCode} onChange={handleChange} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Código Plan de Estudios</Form.Label>
              <Form.Control type="text" name="studyPlanCode" value={formData.studyPlanCode} onChange={handleChange} required />
            </Form.Group>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowModal(false)}>Cancelar</Button>
            <Button variant="primary" type="submit" disabled={saving}>
              {saving ? 'Guardando...' : 'Guardar Estudiante'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </div>
  );
};

export default AdminStudents;