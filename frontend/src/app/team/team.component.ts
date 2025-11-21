import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { environment } from '../../environments/environment';

interface TeamMember {
  id: number;
  name: string;
  role?: string;
  weeklyLoadHours?: number;
}

@Component({
  selector: 'app-team',
  templateUrl: './team.component.html'
})
export class TeamComponent implements OnInit {
  members: TeamMember[] = [];
  form = this.fb.group({
    name: ['', Validators.required],
    role: [''],
    weeklyLoadHours: [null]
  });

  constructor(private http: HttpClient, private fb: FormBuilder) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.http.get<TeamMember[]>(`${environment.apiUrl}/team`).subscribe((data) => (this.members = data));
  }

  save() {
    if (this.form.invalid) return;
    this.http.post(`${environment.apiUrl}/team`, this.form.value).subscribe(() => {
      this.form.reset();
      this.load();
    });
  }

  remove(id: number) {
    this.http.delete(`${environment.apiUrl}/team/${id}`).subscribe(() => this.load());
  }
}
