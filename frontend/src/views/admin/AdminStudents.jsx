import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Spinner, Alert, Modal, Form, Badge } from 'react-bootstrap';
import { getAllStudents, createStudent, getCareers, updateStudent, deleteStudent } from '../../services/AdminService';

const AdminStudents = () => {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Carreras ya existentes para el formulario
  const [careers, setCareers] = useState([]);

  const [saving, setSaving] = useState(false);
  
  // Estados para el Modal de crear alumno
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [formCreateData, setFormCreateData] = useState({
    run: '', 
    full_name: '', 
    email: '', 
    career_code: '',
    password: ''
  });

  // estados para el Modal de editar alumno
  const [editingStudent, setEditingStudent] = useState(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [formEditData, setFormEditData] = useState({
    full_name: '', 
    email: '', 
    career_code: '',
    status: ''
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
    setFormCreateData({ run: '', full_name: '', email: '', career_code: '', password: '' });
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
        career_code: '',
        password: ''
      });
      fetchStudents();
    } catch (err) {
      alert(err.response?.data?.message || 'Error al crear estudiante');
    } finally {
      setSaving(false);
    }
  };

  const handleOpenEditModal = (student) => {
    setEditingStudent(student);
    setFormEditData({ 
      full_name: student.full_name || '', 
      email: student.email || '', 
      career_code: student.career_code || '',
      status: student.status || 'REGULAR'
    });
    fetchCareers();
    setShowEditModal(true);
  }

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await updateStudent(editingStudent.id, formEditData);
      setShowEditModal(false);
      fetchStudents();
      alert(`Estudiante ${formEditData.full_name} actualizado con éxito.`);
    } catch (err) {
      alert(err.response?.data?.message || 'Error al editar estudiante');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (student) => {
    if (!window.confirm(`¿Está seguro de eliminar al estudiante ${student.full_name}?`)) {
      return;
    }
    try {
      await deleteStudent(student.id);
      fetchStudents();
      alert(`Estudiante ${student.full_name} eliminado con éxito.`);
    } catch (err) {
      alert(err.response?.data?.message || 'Error al eliminar estudiante');
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
                <th>Acciones</th>
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
                    <Badge bg={st.status === 'REGULAR' ? 'success' : 'secondary'}>
                      {st.status==='REGULAR' ? 'Activo' : 
                      st.status==='TEMPORARY_WITHDRAWAL' ? 'Retiro Temporal' 
                      : 'Retirado'}
                    </Badge>
                  </td>
                  <td>
                    <Button variant="outline-primary" size="sm" onClick={() => handleOpenEditModal(st)}>Editar</Button>
                    <Button variant="outline-danger" size="sm" className="ms-2" onClick={() => handleDelete(st)}>Eliminar</Button>
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
              <Form.Label>Correo Electrónico</Form.Label>
              <Form.Control 
                type="email" 
                name="email" 
                value={formCreateData.email}
                onChange={e => setFormCreateData({...formCreateData, email: e.target.value})} 
                required 
              />
              <Form.Text className="text-muted">
                Este correo será el usuario de inicio de sesión en el sistema.
              </Form.Text>
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Contraseña inicial</Form.Label>
              <Form.Control 
                type="password" 
                name="password" 
                value={formCreateData.password}
                onChange={e => setFormCreateData({...formCreateData, password: e.target.value})} 
                required
                minLength={6}
                placeholder="Mínimo 6 caracteres"
              />
              <Form.Text className="text-muted">
                El estudiante usará esta contraseña para iniciar sesión.
              </Form.Text>
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
      {/* Modal Editar Estudiante */}
      <Modal show={showEditModal} onHide={() => setShowEditModal(false)}>
        <Form onSubmit={handleEditSubmit}>
          <Modal.Header closeButton>
            <Modal.Title>Editar Estudiante</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Form.Group className="mb-3">
              <Form.Label>Nombre Completo</Form.Label>
              <Form.Control 
                type="text" 
                name="full_name" 
                value={formEditData.full_name}
                onChange={e => setFormEditData({...formEditData, full_name: e.target.value})} 
                required 
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Correo Electrónico (Keycloak)</Form.Label>
              <Form.Control 
                type="email" 
                name="email" 
                value={formEditData.email}
                onChange={e => setFormEditData({...formEditData, email: e.target.value})} 
                required 
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Código de Carrera</Form.Label>
              <Form.Select 
                value={formEditData.career_code}
                onChange={e => setFormEditData({...formEditData, career_code: e.target.value})} 
                required
                >
                {careers.map(c => (
                  <option key={c.id} value={c.code}>
                    {c.code} - {c.name}
                  </option>
                ))}
              </Form.Select>
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Estado</Form.Label>
              <Form.Select 
                value={formEditData.status}
                onChange={e => setFormEditData({...formEditData, status: e.target.value})} 
                required
                >
                <option value="REGULAR">Activo</option>
                <option value="TEMPORARY_WITHDRAWAL">Retiro Temporal</option>
                <option value="POSTPONEMENT">Retirado</option>
              </Form.Select>
            </Form.Group>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowEditModal(false)}>Cancelar</Button>
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