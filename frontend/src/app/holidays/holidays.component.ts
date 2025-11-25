import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from '../../environments/environment';

interface Holiday {
  id: number;
  description: string;
  date: string;
}

@Component({
  selector: 'app-holidays',
  templateUrl: './holidays.component.html',
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, TranslateModule],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HolidaysComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder).nonNullable;

  readonly holidays = signal<Holiday[]>([]);
  readonly saving = signal(false);
  readonly form = this.fb.group({
    description: ['', Validators.required],
    date: ['', Validators.required]
  });

  ngOnInit() {
    this.load();
  }

  load() {
    this.http.get<Holiday[]>(`${environment.apiUrl}/holidays`).subscribe((data) => this.holidays.set(data));
  }

  save() {
    if (this.form.invalid) return;
    this.saving.set(true);
    this.http.post(`${environment.apiUrl}/holidays`, this.form.getRawValue()).subscribe({
      next: () => {
        this.form.reset();
        this.load();
      },
      complete: () => this.saving.set(false)
    });
  }

  remove(id: number) {
    this.http.delete(`${environment.apiUrl}/holidays/${id}`).subscribe(() => this.load());
  }
}
