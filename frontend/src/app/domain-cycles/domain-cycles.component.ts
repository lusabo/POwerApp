import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { DatePipe } from '@angular/common';
import { environment } from '../../environments/environment';

interface DomainCycle {
  id: number;
  name: string;
  startDate: string;
  endDate: string;
}

@Component({
  selector: 'app-domain-cycles',
  templateUrl: './domain-cycles.component.html',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, DatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DomainCyclesComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder).nonNullable;

  readonly cycles = signal<DomainCycle[]>([]);
  readonly saving = signal(false);
  readonly form = this.fb.group({
    name: ['', Validators.required],
    startDate: ['', Validators.required],
    endDate: ['', Validators.required]
  });

  ngOnInit(): void {
    this.load();
  }

  load() {
    this.http.get<DomainCycle[]>(`${environment.apiUrl}/domain-cycles`).subscribe((data) => this.cycles.set(data));
  }

  save() {
    if (this.form.invalid) return;
    this.saving.set(true);
    this.http.post(`${environment.apiUrl}/domain-cycles`, this.form.getRawValue()).subscribe({
      next: () => {
        this.form.reset();
        this.load();
      },
      complete: () => this.saving.set(false)
    });
  }
}
