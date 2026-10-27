import { getUserRole } from '../../utils/auth';
import { Link } from 'react-router-dom';

const StudentDashboard = () => {
  const role = getUserRole();

  return (
    <div className="container mt-5 text-center">
      <h3> Vista estudiante</h3>
    </div>);
};

export default StudentDashboard;