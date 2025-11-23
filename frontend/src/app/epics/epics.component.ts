import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { environment } from '../../environments/environment';

interface DomainCycle {
  id: number;
  name: string;
}

interface Epic {
  id: number;
  epicKey: string;
  name: string;
  effortSize?: string;
  issuesCount?: number;
  storyPointsSum?: number;
  domainCycleId?: number;
  domainCycleName?: string;
}

@Component({
  selector: 'app-epics',
  templateUrl: './epics.component.html'
})
export class EpicsComponent implements OnInit {
  epics: Epic[] = [];
  domainCycles: DomainCycle[] = [];
  loading = false;

  form = this.fb.group({
    epicKey: ['', Validators.required],
    domainCycleId: ['']
  });

  filterForm = this.fb.group({
    domainCycleId: ['']
  });

  constructor(private http: HttpClient, private fb: FormBuilder) {}

  ngOnInit(): void {
    this.loadDomainCycles();
    this.loadEpics();
  }

  loadDomainCycles() {
    this.http.get<DomainCycle[]>(`${environment.apiUrl}/domain-cycles`).subscribe((data) => (this.domainCycles = data));
  }

  loadEpics() {
    const dcId = this.filterForm.value.domainCycleId;
    const params: any = {};
    if (dcId) params.domainCycleId = dcId;
    this.http.get<Epic[]>(`${environment.apiUrl}/epics`, { params }).subscribe((data) => (this.epics = data));
  }

  associateEpic() {
    if (this.form.invalid) return;
    this.loading = true;
    const payload = {
      epicKey: this.form.value.epicKey,
      domainCycleId: this.form.value.domainCycleId ? Number(this.form.value.domainCycleId) : null
    };
    this.http.post<Epic>(`${environment.apiUrl}/epics`, payload).subscribe({
      next: () => {
        this.loading = false;
        this.form.reset();
        this.loadEpics();
      },
      error: () => (this.loading = false)
    });
  }

  reload() {
    this.loading = true;
    this.http.post<Epic[]>(`${environment.apiUrl}/epics/reload`, {}).subscribe({
      next: (data) => {
        this.epics = data;
        this.loading = false;
      },
      error: () => (this.loading = false)
    });
  }
}
