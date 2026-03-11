const getEnvVar = (key, defaultValue = '') => {
  const value = import.meta.env[key];
  return value !== undefined ? value : defaultValue;
};

export const config = {
  apiUrl: getEnvVar('VITE_API_URL', 'http://localhost:8080'),
  googleAuthUrl: getEnvVar('VITE_GOOGLE_AUTH_URL', 'http://localhost:8080/login/oauth2/authorization/google'),
};