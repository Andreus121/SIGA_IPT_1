import { getUserRole } from '../../utils/auth';
import { Link } from 'react-router-dom';

const TeacherDashboard = () => {
  const role = getUserRole();
  
  if (role == 'STUDENT') {
    return (
      <div className="container mt-5 text-center text-danger">
        <h3>Acceso no autorizado</h3>
        <p>Su cuenta no tiene un rol válido asignado para acceder a esta sección.</p>
        <Link to="/" className="btn btn-primary mt-3">
          Volver a la página principal
        </Link>
      </div>
    );
  }

  return (
    <div className="container mt-5 text-center">
      <h3> Vista docente</h3>
    </div>);
};

export default TeacherDashboard;