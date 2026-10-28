import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Spinner, Alert, Modal, Form } from 'react-bootstrap';
import { getCareers, createCareer, updateCareer, deleteCareer} from '../../services/AdminService';

const AdminCareers = () => {
  const [careers, setCareers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  // form para crear carrera
  const [formCreateData, setCreateFormData] = useState({ 
    code: '', 
    name: '', 
    description: ''
  });
  // form para editar carrera
  const [formEditData, setEditFormData] = useState({
    name: '',
    description: '',
    status: 'ACTIVE'
  });
  // codigo de la carrera que se está editando
  const [editingCode, setEditingCode] = useState(null);

  // Estados independientes para modales
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);

  const fetchCareers = async () => {
    setLoading(true);
    try {
      const data = await getCareers();
      setCareers(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchCareers(); }, []);

  const handleOpenCreateModal = () => {
    setCreateFormData({ 
      code: '', 
      name: '', 
      description: '' 
    });
    setShowCreateModal(true);
  }

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await createCareer(formCreateData);
      setShowCreateModal(false);
      fetchCareers();
    } catch (err) {
      alert('Error al crear la carrera');
    } finally {
      setSaving(false);
    }
  };

  const handleOpenEditModal = (career) => {
    setEditingCode(career.code); // O career.id según requiera tu API
    setEditFormData({
      name: career.name || '',
      description: career.description || '',
      status: career.status || 'ACTIVE'
    });
    setShowEditModal(true);
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await updateCareer(editingCode, formEditData);
      setShowEditModal(false);
      fetchCareers();
    } catch (err) {
      alert('Error al actualizar la carrera');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (career) => {
    if (!window.confirm(`¿Está seguro de eliminar la carrera "${career.name}" (${career.code})?`)) {
      return;
    }

    try {
      // Ajusta entre career.id o career.code según tu backend
      await deleteCareer(career.code);
      fetchCareers();
    } catch (err) {
      const msg = err.response?.data?.message || 'No se pudo eliminar la carrera';
      alert(`Error al eliminar: ${msg}`);
    }
  };

  if (loading && careers.length === 0) return <Spinner animation="border" className="d-block mx-auto mt-5" />;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3>Gestión de Carreras</h3>
        <Button onClick={() => handleOpenCreateModal()}>+ Nueva Carrera</Button>
      </div>

      <Card className="shadow-sm border-0">
        <Table responsive hover className="mb-0">
          <thead className="table-dark">
            <tr>
              <th>Código</th>
              <th>Nombre</th>
              <th>Descripción</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {careers.map(c => (
              <tr key={c.id}>
                <td className="fw-bold">{c.code}</td>
                <td>{c.name}</td>
                <td>{c.description}</td>
                {/* Acciones */}
                <td>
                  <Button 
                      variant="outline-primary" 
                      size="sm" 
                      className="me-2"
                      onClick={() => handleOpenEditModal(c)}
                    >
                      Editar
                    </Button>
                    <Button 
                      variant="outline-danger" 
                      size="sm"
                      onClick={() => handleDelete(c)}
                    >
                      Eliminar
                    </Button>
                </td>
              </tr>
            ))}
          </tbody>
        </Table>
      </Card>

      {/* ================= MODAL 1: REGISTRAR CARRERA ================= */}
      <Modal show={showCreateModal} onHide={() => setCreateShowModal(false)}>
        <Form onSubmit={handleCreateSubmit}>
          <Modal.Header closeButton><Modal.Title>
            Registrar Carrera
          </Modal.Title></Modal.Header>
          <Modal.Body>
            {/* Código de la Carrera */}
            <Form.Group className="mb-3">
              <Form.Label>Código (Ej: INF)</Form.Label>
              <Form.Control 
                type="text" 
                value={formCreateData.code} 
                onChange={e => setCreateFormData({...formCreateData, code: e.target.value})} 
                required 
              />
            </Form.Group>
            {/* Nombre de la Carrera */}
            <Form.Group className="mb-3">
              <Form.Label>Nombre</Form.Label>
              <Form.Control 
                type="text" 
                value={formCreateData.name} 
                onChange={e => setCreateFormData({...formCreateData, name: e.target.value})} 
                required 
              />
            </Form.Group>
            {/*Descripción de la Carrera */}
            <Form.Group className="mb-3">
              <Form.Label>Descripción</Form.Label>
              <Form.Control 
                type="text" 
                value={formCreateData.description} 
                onChange={e => setCreateFormData({...formCreateData, description: e.target.value})} 
                required 
              />
            </Form.Group>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowModal(false)}>Cancelar</Button>
            <Button variant="primary" type="submit" disabled={saving}>Guardar</Button>
          </Modal.Footer>
        </Form>
      </Modal>

      {/* ================= MODAL 2: EDITAR CARRERA ================= */}
      <Modal show={showEditModal} onHide={() => setShowEditModal(false)}>
        <Form onSubmit={handleEditSubmit}>
          <Modal.Header closeButton>
            <Modal.Title>Editar Carrera ({editingCode})</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            <Form.Group className="mb-3">
              <Form.Label>Nombre</Form.Label>
              <Form.Control 
                type="text" 
                value={formEditData.name} 
                onChange={e => setEditFormData({...formEditData, name: e.target.value})} 
                required 
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Descripción</Form.Label>
              <Form.Control 
                as="textarea"
                rows={3}
                value={formEditData.description} 
                onChange={e => setEditFormData({...formEditData, description: e.target.value})} 
                required 
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Estado</Form.Label>
              <Form.Select 
                value={formEditData.status} 
                onChange={e => setEditFormData({...formEditData, status: e.target.value})}
                required
              >
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
              </Form.Select>
            </Form.Group>
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

export default AdminCareers;