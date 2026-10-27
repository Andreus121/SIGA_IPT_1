# Etapa 1: Build de la aplicación React
FROM node:20-alpine AS build
WORKDIR /app

# Argumentos de compilación para Vite (pasan a import.meta.env)
ARG VITE_KEYCLOAK_URL=http://localhost:8090
ARG VITE_KEYCLOAK_REALM=siga-realm
ARG VITE_KEYCLOAK_CLIENT_ID=siga-frontend
ARG VITE_API_URL=http://localhost:8080

ENV VITE_KEYCLOAK_URL=$VITE_KEYCLOAK_URL
ENV VITE_KEYCLOAK_REALM=$VITE_KEYCLOAK_REALM
ENV VITE_KEYCLOAK_CLIENT_ID=$VITE_KEYCLOAK_CLIENT_ID
ENV VITE_API_URL=$VITE_API_URL

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# Etapa 2: Servir con Nginx Alpine ligero
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
