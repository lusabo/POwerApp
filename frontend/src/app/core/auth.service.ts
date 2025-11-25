import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { tap } from 'rxjs';

interface AuthResponse {
  token: string;
  name: string;
  email: string;
}

const TOKEN_KEY = 'token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly tokenState = signal<string | null>(localStorage.getItem(TOKEN_KEY));

  readonly authenticated = computed(() => !!this.tokenState());

  login(email: string, password: string) {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/login`, { email, password })
      .pipe(tap((resp) => this.saveSession(resp.token)));
  }

  register(name: string, email: string, password: string) {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/register`, { name, email, password })
      .pipe(tap((resp) => this.saveSession(resp.token)));
  }

  me() {
    return this.http.get<AuthResponse>(`${environment.apiUrl}/auth/me`);
  }

  logout() {
    localStorage.removeItem(TOKEN_KEY);
    this.tokenState.set(null);
    void this.router.navigate(['/login']);
  }

  token(): string | null {
    return this.tokenState();
  }

  isAuthenticated(): boolean {
    return this.authenticated();
  }

  private saveSession(token: string) {
    localStorage.setItem(TOKEN_KEY, token);
    this.tokenState.set(token);
  }
}
