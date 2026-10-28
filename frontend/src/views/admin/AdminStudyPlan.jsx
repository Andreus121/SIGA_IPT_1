import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Spinner, Modal, Form, Badge } from 'react-bootstrap';
import { getStudyPlans, createStudyPlan, setCurrentStudyPlan, getCareers } from '../../services/AdminService';

const AdminStudyPlans = () => {
  const [plans, setPlans] = useState([]);
  const [careers, setCareers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [saving, setSaving] = useState(false);
  const [formData, setFormData] = useState({ 
    code: '', 
    careerCode: '', 
    name: '',
    setCurrent: false
  });

  const fetchData = async () => {
    setLoading(true);
    try {
      // obtener planes de estudio y carreras
      const plansData = await getStudyPlans();
      setPlans(plansData);
      const careersData = await getCareers();
      setCareers(careersData);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);

    console.log("Payload exacto que sale a Axios:", JSON.stringify(formData));
    try {
      await createStudyPlan(formData.code, formData.careerCode, formData.name, formData.setCurrent);
      // esconder el modal tras guardar con éxito
      setShowModal(false);
      // recargar la lista de planes de estudio
      fetchData(); 
    } catch (err) {
      alert('Error al crear plan');
    } finally {
      setSaving(false);
    }
  };

  const handleSetCurrent = async (planCode, planName) => {
    if(!window.confirm(`¿Establecer ${planCode} - ${planName} como plan vigente?`)) return;
    try {
      await setCurrentStudyPlan(planCode);
      fetchData();
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
              <th>Nombre plan</th>
              <th>Código carrera</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {plans.map(p => (
              <tr key={p.id}>
                <td className="fw-bold">{p.planCode}</td>
                <td>{p.name}</td>
                <td>{p.careerCode}</td>
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
            {/* código del Plan */}
            <Form.Group className="mb-3">
              <Form.Label>Código del Plan</Form.Label>
              <Form.Control 
              type="text" 
              value={formData.code}
              onChange={e => setFormData({...formData, code: e.target.value})} 
                required 
              />
            </Form.Group>
            {/* código de carrera */}
            <Form.Group className="mb-3">
              <Form.Label>Código de Carrera Asociada</Form.Label>
              <Form.Select 
                value={formData.careerCode}
                onChange={e => setFormData({...formData, careerCode: e.target.value})} 
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
            {/* nombre del plan de estudio */}
            <Form.Group className="mb-3">
              <Form.Label>Nombre del Plan</Form.Label>
              <Form.Control 
              type="text" 
              value={formData.name}
              onChange={e => setFormData({...formData, name: e.target.value})} 
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
    </div>
  );
};
export default AdminStudyPlans;