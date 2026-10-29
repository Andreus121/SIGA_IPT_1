import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Spinner, Alert, Modal, Form, Badge } from 'react-bootstrap';
import { getAllTeachers, createTeacher, updateTeacherStatus } from '../../services/AdminService';

const AdminTeachers = () => {
  const [teachers, setTeachers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const [saving, setSaving] = useState(false);

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [formCreateData, setFormCreateData] = useState({
    run: '', 
    full_name: '', 
    email: '', 
    professional_title: '', 
    academic_degree: ''
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

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await createTeacher(formCreateData);
      setShowCreateModal(false);
      setFormCreateData({ 
        run: '', 
        full_name: '', 
        email: '', 
        professional_title: '', 
        academic_degree: '' 
      });
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
        <Button variant="primary" onClick={() => setShowCreateModal(true)}>+ Nuevo Docente</Button>
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
                  <td>{tc.full_name}</td>
                  <td>{tc.email}</td>
                  <td>{tc.academic_degree} / {tc.professional_title}</td>
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
      <Modal show={showCreateModal} onHide={() => setShowCreateModal(false)}>
        <Form onSubmit={handleCreateSubmit}>
          <Modal.Header closeButton>
            <Modal.Title>Registrar Nuevo Docente</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Form.Group className="mb-3">
              <Form.Label>RUN</Form.Label>
              <Form.Control 
                type="text" 
                name="run" 
                value={formCreateData.run} 
                onChange={e => setFormCreateData({...formCreateData, run: e.target.value})} 
                required 
                placeholder="Ej: 12345678-9" 
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Nombre Completo</Form.Label>
              <Form.Control 
                type="text" 
                name="full_name" 
                value={formCreateData.full_name} 
                onChange={e => setFormCreateData({...formCreateData, full_name: e.target.value})} 
                required 
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Correo Electrónico</Form.Label>
              <Form.Control 
                type="email" 
                name="email" 
                value={formCreateData.email} 
                onChange={e => setFormCreateData({...formCreateData, email: e.target.value})} 
                required 
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Título Profesional</Form.Label>
              <Form.Control 
                type="text" 
                name="professional_title" 
                value={formCreateData.professional_title} 
                onChange={e => setFormCreateData({...formCreateData, professional_title: e.target.value})} 
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Grado Académico</Form.Label>
              <Form.Control 
                type="text" 
                name="academic_degree" 
                value={formCreateData.academic_degree} 
                onChange={e => setFormCreateData({...formCreateData, academic_degree: e.target.value})} 
              />
            </Form.Group>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowCreateModal(false)}>Cancelar</Button>
            <Button variant="primary" type="submit" disabled={saving}>
              {saving ? 'Guardando...' : 'Guardar Docente'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </div>
  );
};

export default AdminTeachers;