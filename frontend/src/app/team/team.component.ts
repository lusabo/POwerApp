import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { environment } from '../../environments/environment';

interface TeamMember {
  id: number;
  name: string;
  role?: string;
}

@Component({
  selector: 'app-team',
  templateUrl: './team.component.html',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TeamComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder).nonNullable;

  readonly members = signal<TeamMember[]>([]);
  readonly saving = signal(false);
  readonly form = this.fb.group({
    name: ['', Validators.required],
    role: ['']
  });

  ngOnInit() {
    this.load();
  }

  load() {
    this.http.get<TeamMember[]>(`${environment.apiUrl}/team`).subscribe((data) => this.members.set(data));
  }

  save() {
    if (this.form.invalid) return;
    this.saving.set(true);
    this.http.post(`${environment.apiUrl}/team`, this.form.getRawValue()).subscribe({
      next: () => {
        this.form.reset();
        this.load();
      },
      complete: () => this.saving.set(false)
    });
  }

  remove(id: number) {
    this.http.delete(`${environment.apiUrl}/team/${id}`).subscribe(() => this.load());
  }
}
