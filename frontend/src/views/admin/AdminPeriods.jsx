import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Spinner, Modal, Form, Badge } from 'react-bootstrap';
import { getPeriods, createPeriod, closeSemester } from '../../services/AdminService';

const AdminPeriods = () => {
  const [periods, setPeriods] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [saving, setSaving] = useState(false);
  const [formData, setFormData] = useState({ periodCode: '', year: new Date().getFullYear(), semester: 1, startDate: '', endDate: '' });

  const fetchPeriods = async () => {
    setLoading(true);
    try {
      const data = await getPeriods();
      setPeriods(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchPeriods(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await createPeriod(formData);
      setShowModal(false);
      fetchPeriods();
    } catch (err) {
      alert('Error al crear el período');
    } finally {
      setSaving(false);
    }
  };

  const handleCloseSemester = async (periodCode) => {
    if(!window.confirm(`¡PELIGRO! ¿Está seguro de cerrar el semestre ${periodCode}? Esto calculará promedios finales y no se puede deshacer.`)) return;
    try {
      await closeSemester(periodCode);
      fetchPeriods();
    } catch (err) {
      alert('Error al cerrar el semestre');
    }
  };

  if (loading && periods.length === 0) return <Spinner animation="border" className="d-block mx-auto mt-5" />;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3>Períodos Académicos</h3>
        <Button onClick={() => setShowModal(true)}>+ Abrir Nuevo Período</Button>
      </div>

      <Card className="shadow-sm border-0">
        <Table responsive hover className="mb-0 align-middle text-center">
          <thead className="table-dark">
            <tr>
              <th>Período</th>
              <th>Año / Sem.</th>
              <th>Fechas</th>
              <th>Estado</th>
              <th>Acción de Cierre</th>
            </tr>
          </thead>
          <tbody>
            {periods.map(p => (
              <tr key={p.id}>
                <td className="fw-bold">{p.periodCode}</td>
                <td>{p.year} / {p.semester}</td>
                <td>{p.startDate} a {p.endDate}</td>
                <td>
                  <Badge bg={p.status === 'OPEN' ? 'success' : 'danger'}>{p.status}</Badge>
                </td>
                <td>
                  {p.status === 'OPEN' && (
                    <Button variant="danger" size="sm" onClick={() => handleCloseSemester(p.periodCode)}>
                      Cerrar Semestre
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
          <Modal.Header closeButton><Modal.Title>Apertura de Período</Modal.Title></Modal.Header>
          <Modal.Body>
            <Form.Group className="mb-3">
              <Form.Label>Código (Ej: 2026-02)</Form.Label>
              <Form.Control type="text" onChange={e => setFormData({...formData, periodCode: e.target.value})} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Año</Form.Label>
              <Form.Control type="number" value={formData.year} onChange={e => setFormData({...formData, year: e.target.value})} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Semestre (1 o 2)</Form.Label>
              <Form.Control type="number" value={formData.semester} onChange={e => setFormData({...formData, semester: e.target.value})} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Fecha Inicio</Form.Label>
              <Form.Control type="date" onChange={e => setFormData({...formData, startDate: e.target.value})} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Fecha Fin</Form.Label>
              <Form.Control type="date" onChange={e => setFormData({...formData, endDate: e.target.value})} required />
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
export default AdminPeriods;