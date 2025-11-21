import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { tap } from 'rxjs/operators';

interface AuthResponse {
  token: string;
  name: string;
  email: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private http: HttpClient, private router: Router) {}

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
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }

  get token() {
    return localStorage.getItem('token');
  }

  isAuthenticated() {
    return !!this.token;
  }

  private saveSession(token: string) {
    localStorage.setItem('token', token);
  }
}
