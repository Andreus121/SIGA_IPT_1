import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Spinner, Modal, Form, Row, Col } from 'react-bootstrap';
import { getPeriods, getSections, createSection } from '../../services/AdminService';

const AdminSections = () => {
  const [periods, setPeriods] = useState([]);
  const [selectedPeriod, setSelectedPeriod] = useState('');
  const [sections, setSections] = useState([]);
  const [loading, setLoading] = useState(true);
  
  const [showModal, setShowModal] = useState(false);
  const [saving, setSaving] = useState(false);
  const [formData, setFormData] = useState({ code: '', courseCode: '', periodCode: '', teacherId: '', capacity: 40 });

  useEffect(() => {
    const init = async () => {
      try {
        const p = await getPeriods();
        setPeriods(p);
        if(p.length > 0) setSelectedPeriod(p[0].periodCode);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    init();
  }, []);

  const fetchSections = async () => {
    if (!selectedPeriod) return;
    setLoading(true);
    try {
      const data = await getSections(selectedPeriod);
      setSections(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchSections(); }, [selectedPeriod]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      // Por simplicidad en la UI inicial, enviamos el array de schedules vacío o quemado. 
      // En una versión final, aquí agregarías inputs dinámicos para los días y módulos.
      const payload = { ...formData, periodCode: selectedPeriod, schedules: [] };
      await createSection(payload);
      setShowModal(false);
      fetchSections();
    } catch (err) {
      alert('Error al crear sección');
    } finally {
      setSaving(false);
    }
  };

  if (loading && !selectedPeriod) return <Spinner animation="border" className="d-block mx-auto mt-5" />;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3>Secciones y Horarios</h3>
        <Button onClick={() => setShowModal(true)} disabled={!selectedPeriod}>+ Programar Sección</Button>
      </div>

      <Form.Group className="mb-4" style={{ maxWidth: '300px' }}>
        <Form.Label>Filtrar por Período</Form.Label>
        <Form.Select value={selectedPeriod} onChange={e => setSelectedPeriod(e.target.value)}>
          {periods.map(p => <option key={p.periodCode} value={p.periodCode}>{p.periodCode}</option>)}
        </Form.Select>
      </Form.Group>

      <Card className="shadow-sm border-0">
        <Table responsive hover className="mb-0 text-center">
          <thead className="table-dark">
            <tr>
              <th>Sección</th>
              <th>Asignatura</th>
              <th>ID Docente</th>
              <th>Cupos</th>
            </tr>
          </thead>
          <tbody>
            {sections.length === 0 ? (
              <tr><td colSpan="4">No hay secciones programadas.</td></tr>
            ) : (
              sections.map(s => (
                <tr key={s.id}>
                  <td className="fw-bold">{s.code}</td>
                  <td>{s.courseCode}</td>
                  <td>{s.teacherId}</td>
                  <td>{s.capacity}</td>
                </tr>
              ))
            )}
          </tbody>
        </Table>
      </Card>

      <Modal show={showModal} onHide={() => setShowModal(false)}>
        <Form onSubmit={handleSubmit}>
          <Modal.Header closeButton><Modal.Title>Nueva Sección</Modal.Title></Modal.Header>
          <Modal.Body>
            <Form.Group className="mb-3">
              <Form.Label>Código Sección (Ej: INF101-1)</Form.Label>
              <Form.Control type="text" onChange={e => setFormData({...formData, code: e.target.value})} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Código Asignatura</Form.Label>
              <Form.Control type="text" onChange={e => setFormData({...formData, courseCode: e.target.value})} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>ID del Docente Asignado</Form.Label>
              <Form.Control type="number" onChange={e => setFormData({...formData, teacherId: e.target.value})} required />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Capacidad Máxima</Form.Label>
              <Form.Control type="number" value={formData.capacity} onChange={e => setFormData({...formData, capacity: e.target.value})} required />
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
export default AdminSections;