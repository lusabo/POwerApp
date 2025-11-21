import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  form = this.fb.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private snack: MatSnackBar
  ) {}

  submit() {
    if (this.form.invalid) {
      return;
    }
    const { name, email, password } = this.form.value;
    this.auth.register(name!, email!, password!).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err) => this.snack.open(err.error || 'Erro ao registrar', 'Fechar', { duration: 3000 })
    });
  }
}
