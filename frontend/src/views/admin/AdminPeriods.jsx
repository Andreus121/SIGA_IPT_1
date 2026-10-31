import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Spinner, Modal, Form, Badge } from 'react-bootstrap';
import { getPeriods, createPeriod, closeSemester } from '../../services/AdminService';

const AdminPeriods = () => {
  const [periods, setPeriods] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [saving, setSaving] = useState(false);
  const [year, setYear] = useState(new Date().getFullYear());
  const [semester, setSemester] = useState(1);

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

    // si un período ya es activo, no permitir crear otro
    if(periods.some(p => p.status === 'OPEN')) {
      alert('Ya existe un período activo. Cierre el período actual antes de abrir uno nuevo.');
      setSaving(false);
      return;
    }

    const numericYear = Number(year);
    const numericSemester = Number(semester);
    if(!Number.isInteger(numericYear) || numericYear < 2000 || numericYear > 9999) {
      alert('Ingrese un año válido.');
      setSaving(false);
      return;
    }

    if(![1, 2].includes(numericSemester)) {
      alert('Seleccione un semestre válido.');
      setSaving(false);
      return;
    }

    const periodCode = `${numericYear}-${numericSemester}`;
    try {
      await createPeriod({
        period_code: periodCode,
        status: 'OPEN'
      });
      setShowModal(false);
      fetchPeriods();
    } catch (err) {
      alert(err.response?.data?.message || 'Error al crear el período');
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
              <th>Período (Año-Semestre)</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {periods.map(p => (
              <tr key={p.period_code}>
                <td className="fw-bold">{p.period_code}</td>
                <td>
                  <Badge bg={p.status === 'OPEN' ? 'success' : 'secondary'}>
                    {p.status === 'OPEN' ? 'Abierto' : 'Cerrado'}
                  </Badge>
                </td>
                <td>
                  <Button 
                  variant="danger" 
                  size="sm" 
                  onClick={() => handleCloseSemester(p.period_code)}
                  disabled={p.status === 'CLOSED'}
                  >
                    Cerrar Semestre
                  </Button>
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
              <Form.Label>Año</Form.Label>
              <Form.Control 
                type="number"
                min="2000"
                max="9999"
                value={year} 
                onChange={e => setYear(Number(e.target.value))}
                required 
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Semestre</Form.Label>
              <Form.Select 
                value={semester} 
                onChange={e => setSemester(Number(e.target.value))}
                required 
              >
                <option value={0}>Seleccione un semestre</option>
                <option value={1}>1</option>
                <option value={2}>2</option>
              </Form.Select>
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