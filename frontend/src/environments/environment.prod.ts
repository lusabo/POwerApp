interface Environment {
  readonly production: boolean;
  readonly apiUrl: string;
}

export const environment: Environment = {
  production: true,
  apiUrl: '/api'
} as const;
