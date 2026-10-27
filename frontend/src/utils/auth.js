import keycloak from '../config/keycloak';

export const getUserRole = () => {
  if (!keycloak.authenticated) return null;
  
  // Extraer roles de keycloak.realmAccess.roles
  const roles = keycloak.realmAccess?.roles || [];
  
  if (roles.includes('ADMIN')) return 'ADMIN';
  if (roles.includes('TEACHER')) return 'TEACHER';
  if (roles.includes('STUDENT')) return 'STUDENT';
  
  return 'UNAUTHORIZED';
};