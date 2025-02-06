import { PassedInitialConfig } from 'angular-auth-oidc-client';

export const authConfig: PassedInitialConfig = {
  config: {
    authority: 'http://keycloak.default.svc.cluster.local:8080/realms/dev', // Keycloak authority
    redirectUrl: 'http://localhost:30000', // Ensure this matches your frontend
    postLogoutRedirectUri: 'http://localhost:30000', // Ensure this matches your frontend
    clientId: 'angular-client', // Client ID registered in Keycloak
    scope: 'openid profile offline_access', // Scopes for access
    responseType: 'code', // Use 'code' for authorization code flow
    silentRenew: true, // Enable silent renew
    useRefreshToken: true, // Use refresh token
    // renewTimeBeforeTokenExpiresInSeconds: 30, // Uncomment to set renew time before token expires
  }
};
