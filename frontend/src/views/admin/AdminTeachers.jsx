import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Spinner, Alert, Modal, Form, Badge } from 'react-bootstrap';
import { getAllTeachers, createTeacher, updateTeacherStatus } from '../../services/adminService';

const AdminTeachers = () => {
  const [teachers, setTeachers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const [showModal, setShowModal] = useState(false);
  const [saving, setSaving] = useState(false);
  const [formData, setFormData] = useState({
    run: '', fullName: '', email: '', professionalTitle: '', academicDegree: ''
  });

  const fetchTeachers = async () => {
    setLoading(true);
    try {
      const data = await getAllTeachers();
      setTeachers(data);
    } catch (err) {
      setError('Error al obtener la lista de docentes.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTeachers();
  }, []);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await createTeacher(formData);
      setShowModal(false);
      setFormData({ run: '', fullName: '', email: '', professionalTitle: '', academicDegree: '' });
      fetchTeachers();
    } catch (err) {
      alert(err.response?.data?.message || 'Error al crear docente');
    } finally {
      setSaving(false);
    }
  };

  const toggleStatus = async (id, currentStatus) => {
    const newStatus = currentStatus === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    try {
      await updateTeacherStatus(id, newStatus);
      fetchTeachers();
    } catch (err) {
      alert('Error al cambiar el estado del docente.');
    }
  };

  if (loading && teachers.length === 0) return <div className="text-center my-5"><Spinner animation="border" /></div>;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3>Gestión de Docentes</h3>
        <Button variant="success" onClick={() => setShowModal(true)}>+ Nuevo Docente</Button>
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
                <th>Grado / Título</th>
                <th>Estado</th>
                <th>Acción</th>
              </tr>
            </thead>
            <tbody>
              {teachers.map(tc => (
                <tr key={tc.id}>
                  <td className="fw-bold">{tc.run}</td>
                  <td>{tc.fullName}</td>
                  <td>{tc.email}</td>
                  <td>{tc.academicDegree} / {tc.professionalTitle}</td>
                  <td>
                    <Badge bg={tc.status === 'ACTIVE' ? 'success' : 'secondary'}>{tc.status}</Badge>
                  </td>
                  <td>
                    <Button 
                      variant={tc.status === 'ACTIVE' ? 'outline-danger' : 'outline-success'} 
                      size="sm"
                      onClick={() => toggleStatus(tc.id, tc.status)}
                    >
                      {tc.status === 'ACTIVE' ? 'Desactivar' : 'Activar'}
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        </Card.Body>
      </Card>

      {/* Modal Crear Docente */}
      <Modal show={showModal} onHide={() => setShowModal(false)}>
        <Form onSubmit={handleSubmit}>
          <Modal.Header closeButton>
            <Modal.Title>Registrar Nuevo Docente</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Form.Group className="mb-3">
              <Form.Label>RUN</Form.Label>
              <Form.Control type="text" name="run" value={formData.run} onChange={handleChange} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Nombre Completo</Form.Label>
              <Form.Control type="text" name="fullName" value={formData.fullName} onChange={handleChange} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Correo Electrónico</Form.Label>
              <Form.Control type="email" name="email" value={formData.email} onChange={handleChange} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Título Profesional</Form.Label>
              <Form.Control type="text" name="professionalTitle" value={formData.professionalTitle} onChange={handleChange} />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Grado Académico</Form.Label>
              <Form.Control type="text" name="academicDegree" value={formData.academicDegree} onChange={handleChange} />
            </Form.Group>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowModal(false)}>Cancelar</Button>
            <Button variant="success" type="submit" disabled={saving}>
              {saving ? 'Guardando...' : 'Guardar Docente'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </div>
  );
};

export default AdminTeachers;