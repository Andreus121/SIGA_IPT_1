import React from 'react';
import { Navigate } from 'react-router-dom';
import { getUserRole } from '../utils/auth';

const RoleRedirect = () => {
  const role = getUserRole();

  switch (role) {
    case 'ADMIN':
      return <Navigate to="/admin" replace />;
    case 'TEACHER':
      return <Navigate to="/teacher" replace />;
    case 'STUDENT':
      return <Navigate to="/student" replace />;
    default:
      return (
        <div className="container mt-5 text-center text-danger">
          <h3>Acceso no autorizado</h3>
          <p>Su cuenta no tiene un rol válido asignado.</p>
        </div>
      );
  }
};

export default RoleRedirect;