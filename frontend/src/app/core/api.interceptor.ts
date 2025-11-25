import { Injectable, inject } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable()
export class ApiInterceptor implements HttpInterceptor {
  private readonly auth = inject(AuthService);

  private getLanguage(): string {
    const stored = typeof localStorage !== 'undefined' ? localStorage.getItem('powerapp-lang') : null;
    return stored || 'pt';
  }

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.auth.token();
    const headers: Record<string, string> = { 'Accept-Language': this.getLanguage() };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const clone = req.clone({ setHeaders: headers });
    return next.handle(clone).pipe(
      catchError((error) => {
        const isAuthRoute = req.url.includes('/auth/');
        const isAsset = req.url.includes('/assets/');
        if (error.status === 401 && !isAuthRoute && !isAsset) {
          this.auth.logout();
        }
        return throwError(() => error);
      })
    );
  }
}
