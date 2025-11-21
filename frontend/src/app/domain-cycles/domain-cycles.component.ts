import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { environment } from '../../environments/environment';

interface DomainCycle {
  id: number;
  name: string;
  startDate: string;
  endDate: string;
}

@Component({
  selector: 'app-domain-cycles',
  templateUrl: './domain-cycles.component.html'
})
export class DomainCyclesComponent implements OnInit {
  cycles: DomainCycle[] = [];
  form = this.fb.group({
    name: ['', Validators.required],
    startDate: ['', Validators.required],
    endDate: ['', Validators.required]
  });

  constructor(private http: HttpClient, private fb: FormBuilder) {}

  ngOnInit(): void {
    this.load();
  }

  load() {
    this.http.get<DomainCycle[]>(`${environment.apiUrl}/domain-cycles`).subscribe((data) => (this.cycles = data));
  }

  save() {
    if (this.form.invalid) return;
    this.http.post(`${environment.apiUrl}/domain-cycles`, this.form.value).subscribe(() => {
      this.form.reset();
      this.load();
    });
  }
}
