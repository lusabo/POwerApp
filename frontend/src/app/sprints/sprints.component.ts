import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { environment } from '../../environments/environment';

interface Sprint {
  id: number;
  name: string;
  sprintId?: number | null;
  jiraSprintId?: number | null;
  startDate: string;
  endDate: string;
  storyPointsCompleted?: number;
}

interface JiraSprint {
  sprintId: number;
  sprintName: string;
  startDate: string | null;
  endDate: string | null;
  completeDate: string | null;
  storyPointsDelivered: number;
}

@Component({
  selector: 'app-sprints',
  templateUrl: './sprints.component.html',
  styleUrls: ['./sprints.component.css']
})
export class SprintsComponent implements OnInit {
  sprints: Sprint[] = [];
  loading = false;
  jiraSprint: JiraSprint | null = null;

  searchForm = this.fb.group({
    sprintName: ['', Validators.required]
  });

  constructor(private http: HttpClient, private fb: FormBuilder) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.http.get<Sprint[]>(`${environment.apiUrl}/sprints`).subscribe((data) => (this.sprints = data));
  }

  fetchJiraSprint() {
    if (this.searchForm.invalid) return;
    const sprintName = this.searchForm.value.sprintName ?? '';
    this.loading = true;
    this.http.post<JiraSprint>(`${environment.apiUrl}/sprints/jira`, { sprintName }).subscribe({
      next: (resp) => {
        this.jiraSprint = resp;
        this.load();
        this.loading = false;
      },
      error: () => {
        this.jiraSprint = null;
        this.loading = false;
      }
    });
  }
}
