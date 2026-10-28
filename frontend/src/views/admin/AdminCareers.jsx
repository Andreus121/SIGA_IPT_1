import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Spinner, Alert, Modal, Form } from 'react-bootstrap';
import { getCareers, createCareer } from '../../services/AdminService';

const AdminCareers = () => {
  const [careers, setCareers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [saving, setSaving] = useState(false);
  const [formData, setFormData] = useState({ code: '', name: '', degree: '', totalSemesters: '' });

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

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await createCareer(formData);
      setShowModal(false);
      setFormData({ code: '', name: '', degree: '', totalSemesters: '' });
      fetchCareers();
    } catch (err) {
      alert('Error al crear la carrera');
    } finally {
      setSaving(false);
    }
  };

  if (loading && careers.length === 0) return <Spinner animation="border" className="d-block mx-auto mt-5" />;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3>Gestión de Carreras</h3>
        <Button onClick={() => setShowModal(true)}>+ Nueva Carrera</Button>
      </div>

      <Card className="shadow-sm border-0">
        <Table responsive hover className="mb-0">
          <thead className="table-dark">
            <tr>
              <th>Código</th>
              <th>Nombre</th>
              <th>Grado</th>
              <th>Semestres Totales</th>
            </tr>
          </thead>
          <tbody>
            {careers.map(c => (
              <tr key={c.id}>
                <td className="fw-bold">{c.code}</td>
                <td>{c.name}</td>
                <td>{c.degree}</td>
                <td>{c.totalSemesters}</td>
              </tr>
            ))}
          </tbody>
        </Table>
      </Card>

      <Modal show={showModal} onHide={() => setShowModal(false)}>
        <Form onSubmit={handleSubmit}>
          <Modal.Header closeButton><Modal.Title>Registrar Carrera</Modal.Title></Modal.Header>
          <Modal.Body>
            <Form.Group className="mb-3">
              <Form.Label>Código (Ej: INF)</Form.Label>
              <Form.Control type="text" value={formData.code} onChange={e => setFormData({...formData, code: e.target.value})} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Nombre</Form.Label>
              <Form.Control type="text" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Grado Académico</Form.Label>
              <Form.Control type="text" value={formData.degree} onChange={e => setFormData({...formData, degree: e.target.value})} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Semestres Totales</Form.Label>
              <Form.Control type="number" value={formData.totalSemesters} onChange={e => setFormData({...formData, totalSemesters: e.target.value})} required />
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
export default AdminCareers;