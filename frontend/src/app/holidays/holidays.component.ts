import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { environment } from '../../environments/environment';

interface Holiday {
  id: number;
  description: string;
  date: string;
}

@Component({
  selector: 'app-holidays',
  templateUrl: './holidays.component.html'
})
export class HolidaysComponent implements OnInit {
  holidays: Holiday[] = [];
  form = this.fb.group({
    description: ['', Validators.required],
    date: ['', Validators.required]
  });

  constructor(private http: HttpClient, private fb: FormBuilder) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.http.get<Holiday[]>(`${environment.apiUrl}/holidays`).subscribe((data) => (this.holidays = data));
  }

  save() {
    if (this.form.invalid) return;
    this.http.post(`${environment.apiUrl}/holidays`, this.form.value).subscribe(() => {
      this.form.reset();
      this.load();
    });
  }

  remove(id: number) {
    this.http.delete(`${environment.apiUrl}/holidays/${id}`).subscribe(() => this.load());
  }
}
