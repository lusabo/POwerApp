import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { environment } from '../../environments/environment';
import { TranslateModule } from '@ngx-translate/core';

interface ProjectConfig {
  projectName: string | null;
  jiraKey: string | null;
  board: string | null;
  boardId: number | null;
  ceremoniesDays: number | null;
  featureTeam: string | null;
}

@Component({
  selector: 'app-project-config',
  templateUrl: './project-config.component.html',
  styleUrls: ['./project-config.component.css'],
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, TranslateModule],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProjectConfigComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly http = inject(HttpClient);
  private readonly snack = inject(MatSnackBar);

  readonly saving = signal(false);
  readonly editing = signal(true);
  readonly showToken = signal(false);
  readonly currentConfig = signal<ProjectConfig | null>(null);

  readonly form = this.fb.nonNullable.group({
    projectName: ['', Validators.required],
    jiraKey: ['', Validators.required],
    board: ['', Validators.required],
    boardId: this.fb.nonNullable.control({ value: '', disabled: true }),
    ceremoniesDays: [''],
    featureTeam: ['']
  });

  ngOnInit(): void {
    // Garante que a chamada ao backend ocorra na navegação direta (/project-config).
    queueMicrotask(() => this.load());
  }

  load() {
    this.http.get<ProjectConfig>(`${environment.apiUrl}/project-config`).subscribe({
      next: (config) => {
        this.applyConfig(config);
      },
      error: () => {
        this.editing.set(true);
      }
    });
  }

  private applyConfig(config: ProjectConfig | null) {
    if (!config) {
      this.editing.set(true);
      return;
    }
    this.currentConfig.set(config);
    this.form.patchValue({
      projectName: config.projectName ?? '',
      jiraKey: config.jiraKey ?? '',
      board: config.board ?? '',
      boardId: config.boardId?.toString() ?? '',
      ceremoniesDays: config.ceremoniesDays?.toString() ?? '',
      featureTeam: config.featureTeam ?? ''
    });
    const hasData =
      [
        config.projectName,
        config.jiraKey,
        config.board,
        config.featureTeam,
        config.boardId,
        config.ceremoniesDays
      ].some((v) => v !== null && v !== undefined && `${v}`.toString().trim() !== '');
    this.editing.set(!hasData);
  }

  save() {
    if (this.form.invalid) {
      return;
    }
    this.saving.set(true);
    this.http.post<ProjectConfig>(`${environment.apiUrl}/project-config`, this.form.getRawValue()).subscribe({
      next: (config) => {
        this.saving.set(false);
        this.applyConfig(config);
        this.snack.open('Configuração salva com sucesso', 'Fechar', { duration: 2000 });
      },
      error: () => {
        this.saving.set(false);
        this.snack.open('Não foi possível salvar a configuração', 'Fechar', { duration: 2500 });
      }
    });
  }

  toggleEdit() {
    this.editing.set(true);
  }

  toggleToken() {
    this.showToken.update((v) => !v);
  }

  maskedToken(): string {
    const token = this.form.getRawValue().jiraKey || this.currentConfig()?.jiraKey || '';
    if (!token) {
      return '****';
    }
    return this.showToken() ? token : '•'.repeat(Math.min(token.length, 6));
  }
}
