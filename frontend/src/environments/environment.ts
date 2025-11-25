interface Environment {
  readonly production: boolean;
  readonly apiUrl: string;
}

export const environment: Environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
} as const;
