import React, { useState, useEffect } from 'react';
import { Card, Table, Form, Spinner, Badge } from 'react-bootstrap';
import { getPeriods, getEnrollmentsByPeriod, deleteEnrollment } from '../../services/AdminService';

const AdminEnrollments = () => {
  const [periods, setPeriods] = useState([]);
  const [selectedPeriod, setSelectedPeriod] = useState('');
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);

  // Cargar períodos académicos al montar el componente
  useEffect(() => {
    const init = async () => {
      try {
        const p = await getPeriods();
        console.log("Períodos obtenidos:", p);
        setPeriods(p);
        if (p.length > 0) {
          const openPeriod = p.find(period => period.status === 'OPEN') || p[0];
          setSelectedPeriod(openPeriod.period_code);
        }
      } catch (err) {
        console.error("Error al cargar períodos", err);
      } finally {
        setLoading(false);
      }
    };
    init();
  }, []);

  const fetchEnrollments = async () => {
      if (!selectedPeriod) return;
      setLoading(true);
      try {
        const data = await getEnrollmentsByPeriod(selectedPeriod);
        setEnrollments(data);
      } catch (err) {
        console.error("Error al cargar matrículas", err);
      } finally {
        setLoading(false);
      }
    };

  // Cargar inscripciones cuando cambia el período seleccionado
  useEffect(() => {
    fetchEnrollments();
  }, [selectedPeriod, fetchEnrollments]);

  const handleDeletSubmit = async (id) => {
    try {
      console.log("Eliminando matrícula con ID:", id);
      await deleteEnrollment(id);
      console.log("Matrícula eliminada con éxito");
      fetchEnrollments(); // Refrescar la lista después de eliminar
    } catch (err) {
      console.error("Error al eliminar matrícula", err);
    }
  };

  if (loading && !selectedPeriod) {
    return <Spinner animation="border" className="d-block mx-auto mt-5" variant="primary" />;
  }

  return (
    <div>
      <h3 className="mb-4">Auditoría de Matrículas</h3>

      <Form.Group className="mb-4" style={{ maxWidth: '300px' }}>
        <Form.Label>Filtrar por Período Académico</Form.Label>
        <Form.Select value={selectedPeriod} onChange={(e) => setSelectedPeriod(e.target.value)}>
          {periods.map((p) => (
            <option key={p.period_code} value={p.period_code}>
              {p.period_code}
            </option>
          ))}
        </Form.Select>
      </Form.Group>

      <Card className="shadow-sm border-0">
        <Card.Header className="bg-dark text-white fw-bold">
          Alumnos Matriculados - {selectedPeriod}
        </Card.Header>
        <Card.Body className="p-0">
          <Table responsive hover className="mb-0 text-center align-middle">
            <thead className="table-light">
              <tr>
                <th>ID Matrícula</th>
                <th>RUN Alumno</th>
                <th>Fecha de Matrícula</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {enrollments.length === 0 ? (
                <tr>
                  <td colSpan="4" className="text-muted">No hay alumnos matriculados en este período.</td>
                </tr>
              ) : (
                enrollments.map((enr) => (
                  <tr key={enr.id}>
                    <td>{enr.id}</td>
                    <td className="fw-bold">{enr.student_run}</td>
                    <td>{new Date(enr.enrollment_date).toLocaleString()}</td>
                    <td>
                      <button
                        type="button"
                        className="btn btn-danger btn-sm"
                        onClick={() => handleDeletSubmit(enr.id)}
                      >
                        ELIMINAR
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </Table>
        </Card.Body>
      </Card>
    </div>
  );
};

export default AdminEnrollments;