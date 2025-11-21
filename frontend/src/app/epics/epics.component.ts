import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-epics',
  templateUrl: './epics.component.html'
})
export class EpicsComponent {
  progress: any;
  form = this.fb.group({
    epicKey: ['', Validators.required]
  });

  constructor(private http: HttpClient, private fb: FormBuilder) {}

  load() {
    if (this.form.invalid) return;
    const epicKey = this.form.value.epicKey;
    this.http.get(`${environment.apiUrl}/epics/${epicKey}`).subscribe((data) => (this.progress = data));
  }
}
