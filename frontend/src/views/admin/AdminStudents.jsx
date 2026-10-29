import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Spinner, Alert, Modal, Form, Badge } from 'react-bootstrap';
import { getAllStudents, createStudent, getCareers } from '../../services/AdminService';

const AdminStudents = () => {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Carreras ya existentes para el formulario
  const [careers, setCareers] = useState([]);

  // Estados para el Modal de crear alumno
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [saving, setSaving] = useState(false);
  const [formCreateData, setFormCreateData] = useState({
    run: '', 
    full_name: '', 
    email: '', 
    career_code: ''
  });

  const fetchStudents = async () => {
    setLoading(true);
    try {
      const data = await getAllStudents();
      setStudents(data);
      console.log("Estudiantes obtenidos:", data);
    } catch (err) {
      setError('Error al obtener la lista de estudiantes.');
    } finally {
      setLoading(false);
    }
  };

  const fetchCareers = async () => {
    try {
      const response = await getCareers();
      setCareers(response);
    } catch (err) {
      console.error('Error al obtener las carreras:', err);
    }
  };

  useEffect(() => {
    fetchStudents();
  }, []);

  const handleOpenCreateModal = () => {
    setFormCreateData({ run: '', full_name: '', email: '', career_code: ''});
    fetchCareers();
    setShowCreateModal(true);
  }

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await createStudent(formCreateData);
      setShowCreateModal(false);
      setFormCreateData({ 
        run: '', 
        full_name: '', 
        email: '', 
        career_code: ''
      });
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
        <Button variant="primary" onClick={() => handleOpenCreateModal()}>+ Nuevo Estudiante</Button>
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
                  <td>{st.full_name}</td>
                  <td>{st.email}</td>
                  <td>{st.career_code} ({st.study_plan_code})</td>
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
      <Modal show={showCreateModal} onHide={() => setShowCreateModal(false)}>
        <Form onSubmit={handleCreateSubmit}>
          <Modal.Header closeButton>
            <Modal.Title>Registrar Nuevo Estudiante</Modal.Title>
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
              <Form.Label>Correo Electrónico (Keycloak)</Form.Label>
              <Form.Control 
                type="email" 
                name="email" 
                value={formCreateData.email}
                onChange={e => setFormCreateData({...formCreateData, email: e.target.value})} 
                required 
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Código de Carrera</Form.Label>
              <Form.Select 
                value={formCreateData.career_code}
                onChange={e => setFormCreateData({...formCreateData, career_code: e.target.value})} 
                required
                >
                <option value="">Seleccione una carrera...</option>
                {careers.map(c => (
                  <option key={c.id} value={c.code}>
                    {c.code} - {c.name}
                  </option>
                ))}
              </Form.Select>
            </Form.Group>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowCreateModal(false)}>Cancelar</Button>
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