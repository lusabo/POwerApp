import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { environment } from '../../environments/environment';

interface Sprint {
  id: number;
  name: string;
  startDate: string;
  endDate: string;
  capacity?: number;
  storyPointsCompleted?: number;
}

@Component({
  selector: 'app-sprints',
  templateUrl: './sprints.component.html'
})
export class SprintsComponent implements OnInit {
  sprints: Sprint[] = [];
  capacityResult: string | null = null;

  form = this.fb.group({
    name: ['', Validators.required],
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    capacity: [null],
    storyPointsCompleted: [null]
  });

  constructor(private http: HttpClient, private fb: FormBuilder) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.http.get<Sprint[]>(`${environment.apiUrl}/sprints`).subscribe((data) => (this.sprints = data));
  }

  save() {
    if (this.form.invalid) return;
    this.http.post(`${environment.apiUrl}/sprints`, this.form.value).subscribe(() => {
      this.form.reset();
      this.load();
    });
  }

  calcCapacity(id: number) {
    this.http.get<any>(`${environment.apiUrl}/sprints/${id}/capacity`).subscribe((resp) => {
      this.capacityResult = `Capacidade: ${resp.capacity} (dias úteis ${resp.workingDays}, feriados ${resp.holidayDays})`;
    });
  }
}
