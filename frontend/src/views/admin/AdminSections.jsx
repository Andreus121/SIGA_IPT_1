import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Spinner, Modal, Form, Badge } from 'react-bootstrap';
import { getPeriods, getSections, createSection, getCourses, getAllTeachers, getTeacherById } from '../../services/AdminService';

//constantes
const DAYS = ['L', 'M', 'W', 'J', 'V'];
const MODULES = [1, 2, 3, 4, 5, 6];

const AdminSections = () => {
  const [periods, setPeriods] = useState([]);
  const [selectedPeriod, setSelectedPeriod] = useState("");
  const [sections, setSections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [courses, setCourses] = useState([]);
  const [teachers, setTeachers] = useState([]);
  // crear una seccion
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [formCreateData, setFormCreateData] = useState({ 
    code: '', 
    course_code: '', 
    period_code: '', 
    teacher_id: '', 
    capacity: 40,
    schedules: []
  });
  // json de un schedule
  // {day: 'L'/'M'/'X'/'J'/'V', module_number: 1-6}

  const fetchCourses = async () => {
    setLoading(true);
    try {
      const data = await getCourses();
      setCourses(data);
      console.log('Courses fetched:', data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const fetchTeachers = async () => {
    setLoading(true);
    try {
      const data = await getAllTeachers();
      setTeachers(data);
      console.log('Teachers fetched:', data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };
  
  // funcion la primera vez que carga la vista
  useEffect(() => {
    const initData = async () => {
      setLoading(true);
      try {
        const [coursesData, teachersData, periodsData] = await Promise.all([
          getCourses(),
          getAllTeachers(),
          getPeriods()
        ]);

        setCourses(coursesData);
        setTeachers(teachersData);
        setPeriods(periodsData);
      } catch (err) {
        console.error('Error al cargar datos iniciales:', err);
      } finally {
        setLoading(false);
      }
    };

    initData();
  }, []);

  // Escuchar cambios en selectedPeriod para buscar las secciones correspondientes
  useEffect(() => {
    const fetchSections = async () => {
      if (!selectedPeriod) {
        setSections([]);
        return;
      }
      try {
        const data = await getSections(selectedPeriod);
        setSections(data);
      } catch (err) {
        console.error('Error al obtener secciones:', err);
      }
    };

    fetchSections();
  }, [selectedPeriod]);

  // Mostrar las asignaturas de la seccion seleccionada
  const handlePeriodChange = (e) => {
    const newPeriod = e.target.value;
    setSelectedPeriod(newPeriod);
  };

  // Comprobar si un bloque específico está seleccionado
  const isBlockSelected = (day, module_number) => {
    return formCreateData.schedules.some(
      s => s.day === day && s.module_number === module_number
    );
  };

  // Alternar la selección de un bloque horario (Día - Módulo)
  const toggleScheduleBlock = (day, moduleNumber) => {
    const currentSchedules = [...formCreateData.schedules];
    
    // Verificar si el bloque ya fue seleccionado
    const index = currentSchedules.findIndex(
      s => s.day === day && s.module_number === moduleNumber
    );

    if (index >= 0) {
      // Si ya existe, se elimina
      currentSchedules.splice(index, 1);
    } else {
      // Si no existe, se añade con la estructura especificada: { day, module_number }
      currentSchedules.push({ day, module_number: moduleNumber });
    }

    setFormCreateData({
      ...formCreateData,
      schedules: currentSchedules
    });
  };

  const handleCreateSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);

    const payload = { 
      ...formCreateData, 
      period_code: selectedPeriod,
      teacher_id: Number(formCreateData.teacher_id),
      capacity: Number(formCreateData.capacity)
    };

    console.log("Payload enviado a createSection:", JSON.stringify(payload, null, 2));

    try {
      await createSection(payload);
      setShowCreateModal(false);
      alert('Sección creada exitosamente');
      setSelectedPeriod(''); // Resetear el período seleccionado para forzar la recarga
      setSelectedPeriod(payload.period_code); // Reestablecer el período seleccionado
    } catch (err) {
      const msg = err.response?.data?.message || 'Error al crear la sección';
      alert(`Error: ${msg}`);
    } finally {
      setSaving(false);
    }
  };

  // funcion auxiliar para encontrar un profesor por su id
  const getTeacherName = (teacherId) => {
    if (!teacherId) return 'Sin asignar';
    
    // buscar en la lista local cargada en estado
    const foundTeacher = teachers.find(t => String(t.id) === String(teacherId));
    if (foundTeacher) {
      return foundTeacher.full_name;
    }

    // si no se encuentra en el estado inicial, llamamos a getTeacherById para traerlo y actualizar la lista
    getTeacherById(teacherId)
      .then(singleTeacher => {
        if (singleTeacher && singleTeacher.id) {
          setTeachers(prev => [...prev, singleTeacher]);
        }
      })
      .catch(err => console.error(`Error al obtener docente ID ${teacherId}:`, err));

    return `Cargando docente (${teacherId})...`;
  };
  
  if (loading && !selectedPeriod) return <Spinner animation="border" className="d-block mx-auto mt-5" />;

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h3>Secciones y Horarios</h3>
        <Button 
          onClick={() => setShowCreateModal(true)} 
          disabled={!selectedPeriod}>
            + Programar Sección
          </Button>
      </div>

      <Form.Group className="mb-4" style={{ maxWidth: '300px' }}>
        <Form.Label>Filtrar por Período</Form.Label>
        <Form.Select 
          value={selectedPeriod} 
          onChange={e => handlePeriodChange(e)}>
            <option value="">Seleccione un período</option>
            {periods.map(p => 
              <option key={p.period_code} value={p.period_code}>
                {p.period_code}
              </option>)
            }
        </Form.Select>
      </Form.Group>

      <Card className="shadow-sm border-0">
        <Table responsive hover className="mb-0 text-center">
          <thead className="table-dark">
            <tr>
              <th>Sección</th>
              <th>Asignatura</th>
              <th>Docente</th>
              <th>CuposTotales</th>
              <th>Horarios</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {sections.length === 0 ? (
              <tr><td colSpan="6">No hay secciones programadas.</td></tr>
            ) : (
              sections.map(s => (
                <tr key={s.id}>
                  <td className="fw-bold">{s.code}</td>
                  <td>{s.course_code}</td>
                  <td>{getTeacherName(s.teacher_id)}</td>
                  <td>{s.capacity}</td>
                  <td>
                    {s.schedules && s.schedules.length > 0 ? (
                      <ul className="list-unstyled mb-0">
                        {s.schedules.map((schedule, index) => (
                          <li key={index}>{schedule}</li>
                        ))}
                      </ul>
                    ) : (
                      <span className="text-muted">No hay horarios</span>
                    )}
                  </td>
                  <td>
                    <Button variant="outline-primary" size="sm" className="me-2">Editar</Button>
                    <Button variant="outline-danger" size="sm">Eliminar</Button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </Table>
      </Card>

      <Modal show={showCreateModal} onHide={() => setShowCreateModal(false)}>
        <Form onSubmit={handleCreateSubmit}>
          <Modal.Header closeButton><Modal.Title>Nueva Sección</Modal.Title></Modal.Header>
          <Modal.Body>
            <Form.Group className="mb-3">
              <Form.Label>Código Sección (Ej: INF101-1)</Form.Label>
              <Form.Control 
                type="text" 
                onChange={e => setFormCreateData({...formCreateData, code: e.target.value})} 
                required 
              />
            </Form.Group>
            <Form.Group className=" mb-3">
              <Form.Label>Asignatura</Form.Label>
              <Form.Select 
                value={formCreateData.course_code}
                onChange={e => setFormCreateData({...formCreateData, course_code: e.target.value})}
              > 
               <option value="">Seleccione una asignatura</option>
                {courses
                  .map(c => (
                    <option key={c.id} value={c.code}>
                      {c.code} - {c.name}
                    </option>
                  ))
                }
              </Form.Select>
            </Form.Group>
            <Form.Group className=" mb-3">
              <Form.Label>Docente de la Sección</Form.Label>
              <Form.Select 
                value={formCreateData.teacher_id}
                onChange={e => setFormCreateData({...formCreateData, teacher_id: e.target.value})}
              > 
               <option value="">Seleccione un docente</option>
                {teachers
                  .map(t => (
                    <option key={t.id} value={t.id}>
                      {t.full_name}
                    </option>
                  ))
                }
              </Form.Select>
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label>Capacidad Máxima</Form.Label>
              <Form.Control 
                type="number"
                min="1"
                max="60"
                value={formCreateData.capacity} 
                onChange={e => setFormCreateData({...formCreateData, capacity: e.target.value})} 
                required 
              />
            </Form.Group>
            {/* Matriz de Selección Múltiple de Horarios */}
            <hr className="my-4" />
            <div className="d-flex justify-content-between align-items-center mb-2">
              <Form.Label className="fw-bold mb-0">Selección de Horarios (Día y Módulo)</Form.Label>
              <Badge bg="info">
                {formCreateData.schedules.length} Bloque(s) seleccionado(s)
              </Badge>
            </div>

            <Table bordered size="sm" className="text-center align-middle">
              <thead className="table-light">
                <tr>
                  <th>Módulo</th>
                  {DAYS.map(d => <th key={d}>{d}</th>)}
                </tr>
              </thead>
              <tbody>
                {MODULES.map(mod => (
                  <tr key={mod}>
                    <td className="fw-bold text-secondary">Módulo {mod}</td>
                    {DAYS.map(day => {
                      const selected = isBlockSelected(day, mod);
                      return (
                        <td key={`${day}-${mod}`} style={{ cursor: 'pointer' }}>
                          <Button
                            type="button"
                            size="sm"
                            variant={selected ? "primary" : "outline-light"}
                            className={selected ? "" : "text-dark border-0 w-100"}
                            onClick={() => toggleScheduleBlock(day, mod)}
                          >
                            {selected ? `✓ ${day}-${mod}` : `-`}
                          </Button>
                        </td>
                      );
                    })}
                  </tr>
                ))}
              </tbody>
            </Table>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowCreateModal(false)}>Cancelar</Button>
            <Button variant="primary" type="submit" disabled={saving}>Guardar</Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </div>
  );
};
export default AdminSections;