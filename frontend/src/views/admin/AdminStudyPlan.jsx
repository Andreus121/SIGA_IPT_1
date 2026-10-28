import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Spinner, Modal, Form, Badge } from 'react-bootstrap';
import { getStudyPlans, createStudyPlan, setCurrentStudyPlan } from '../../services/AdminService';

const AdminStudyPlans = () => {
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [saving, setSaving] = useState(false);
  const [formData, setFormData] = useState({ planCode: '', careerCode: '', resolutionYear: '' });

  const fetchPlans = async () => {
    setLoading(true);
    try {
      const data = await getStudyPlans();
      setPlans(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchPlans(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await createStudyPlan(formData);
      setShowModal(false);
      fetchPlans();
    } catch (err) {
      alert('Error al crear plan');
    } finally {
      setSaving(false);
    }
  };

  const handleSetCurrent = async (planCode) => {
    if(!window.confirm(`¿Establecer ${planCode} como plan vigente?`)) return;
    try {
      await setCurrentStudyPlan(planCode);
      fetchPlans();
    } catch (err) {
      alert('Error al actualizar vigencia');
    }
  };

  if (loading && plans.length === 0) return <Spinner animation="border" className="d-block mx-auto mt-5" />;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3>Planes de Estudio</h3>
        <Button onClick={() => setShowModal(true)}>+ Nuevo Plan</Button>
      </div>

      <Card className="shadow-sm border-0">
        <Table responsive hover className="mb-0 align-middle">
          <thead className="table-dark">
            <tr>
              <th>Código Plan</th>
              <th>Carrera</th>
              <th>Año Resolución</th>
              <th>Estado</th>
              <th>Acción</th>
            </tr>
          </thead>
          <tbody>
            {plans.map(p => (
              <tr key={p.id}>
                <td className="fw-bold">{p.planCode}</td>
                <td>{p.careerCode}</td>
                <td>{p.resolutionYear}</td>
                <td>
                  <Badge bg={p.isCurrent ? 'success' : 'secondary'}>
                    {p.isCurrent ? 'VIGENTE' : 'HISTÓRICO'}
                  </Badge>
                </td>
                <td>
                  {!p.isCurrent && (
                    <Button variant="outline-success" size="sm" onClick={() => handleSetCurrent(p.planCode)}>
                      Marcar Vigente
                    </Button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </Table>
      </Card>

      <Modal show={showModal} onHide={() => setShowModal(false)}>
        <Form onSubmit={handleSubmit}>
          <Modal.Header closeButton><Modal.Title>Registrar Plan de Estudio</Modal.Title></Modal.Header>
          <Modal.Body>
            <Form.Group className="mb-3">
              <Form.Label>Código del Plan (Ej: INF-2026)</Form.Label>
              <Form.Control type="text" onChange={e => setFormData({...formData, planCode: e.target.value})} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Código de Carrera Asociada</Form.Label>
              <Form.Control type="text" onChange={e => setFormData({...formData, careerCode: e.target.value})} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Año de Resolución</Form.Label>
              <Form.Control type="number" onChange={e => setFormData({...formData, resolutionYear: e.target.value})} required />
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
export default AdminStudyPlans;