import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Sprint, SprintCreatePayload, TeamMember } from './sprints.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SprintsService {
  private readonly http = inject(HttpClient);

  list(): Observable<Sprint[]> {
    return this.http.get<Sprint[]>(`${environment.apiUrl}/sprints`);
  }

  create(payload: SprintCreatePayload): Observable<Sprint> {
    return this.http.post<Sprint>(`${environment.apiUrl}/sprints`, payload);
  }

  reload(id: number): Observable<Sprint> {
    return this.http.post<Sprint>(`${environment.apiUrl}/sprints/${id}/reload`, {});
  }

  team(): Observable<TeamMember[]> {
    return this.http.get<TeamMember[]>(`${environment.apiUrl}/team`);
  }

  domainCycles(): Observable<{ id: number; name: string }[]> {
    return this.http.get<{ id: number; name: string }[]>(`${environment.apiUrl}/domain-cycles`);
  }
}
