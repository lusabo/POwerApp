import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
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
  templateUrl: './epics.component.html',
  styleUrls: ['./epics.component.css'],
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    TranslateModule
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EpicsComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder).nonNullable;

  readonly epics = signal<Epic[]>([]);
  readonly domainCycles = signal<DomainCycle[]>([]);
  readonly loading = signal(false);

  readonly form = this.fb.group({
    epicKey: ['', Validators.required],
    domainCycleId: ['']
  });

  readonly filterForm = this.fb.group({
    domainCycleId: ['']
  });

  constructor() {}

  ngOnInit(): void {
    this.loadDomainCycles();
    this.loadEpics();
  }

  loadDomainCycles() {
    this.http.get<DomainCycle[]>(`${environment.apiUrl}/domain-cycles`).subscribe((data) => this.domainCycles.set(data));
  }

  loadEpics() {
    const dcId = this.filterForm.getRawValue().domainCycleId;
    const params: Record<string, string> = {};
    if (dcId) params.domainCycleId = dcId;
    this.http.get<Epic[]>(`${environment.apiUrl}/epics`, { params }).subscribe((data) => this.epics.set(data));
  }

  associateEpic() {
    if (this.form.invalid) return;
    this.loading.set(true);
    const payload = {
      epicKey: this.form.getRawValue().epicKey,
      domainCycleId: this.form.getRawValue().domainCycleId
        ? Number(this.form.getRawValue().domainCycleId)
        : null
    };
    this.http.post<Epic>(`${environment.apiUrl}/epics`, payload).subscribe({
      next: () => {
        this.loading.set(false);
        this.form.reset();
        this.loadEpics();
      },
      error: () => this.loading.set(false)
    });
  }

  reload() {
    this.loading.set(true);
    this.http.post<Epic[]>(`${environment.apiUrl}/epics/reload`, {}).subscribe({
      next: (data) => {
        this.epics.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
