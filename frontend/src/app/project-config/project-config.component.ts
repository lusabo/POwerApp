import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';
import { environment } from '../../environments/environment';

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
  templateUrl: './project-config.component.html'
})
export class ProjectConfigComponent implements OnInit {
  saving = false;
  form = this.fb.group({
    projectName: ['', Validators.required],
    jiraKey: ['', Validators.required],
    board: ['', Validators.required],
    boardId: [{ value: '', disabled: true }],
    ceremoniesDays: [''],
    featureTeam: ['']
  });

  constructor(private fb: FormBuilder, private http: HttpClient, private snack: MatSnackBar) {}

  ngOnInit(): void {
    this.load();
  }

  load() {
    this.http.get<ProjectConfig>(`${environment.apiUrl}/project-config`).subscribe((config) => {
      this.form.patchValue({
        projectName: config.projectName ?? '',
        jiraKey: config.jiraKey ?? '',
        board: config.board ?? '',
        boardId: config.boardId?.toString() ?? '',
        ceremoniesDays: config.ceremoniesDays?.toString() ?? '',
        featureTeam: config.featureTeam ?? ''
      });
    });
  }

  save() {
    if (this.form.invalid) {
      return;
    }
    this.saving = true;
    this.http.post<ProjectConfig>(`${environment.apiUrl}/project-config`, this.form.value).subscribe({
      next: (config) => {
        this.saving = false;
        this.form.patchValue({
          projectName: config.projectName ?? '',
          jiraKey: config.jiraKey ?? '',
          board: config.board ?? '',
          boardId: config.boardId?.toString() ?? '',
          ceremoniesDays: config.ceremoniesDays?.toString() ?? '',
          featureTeam: config.featureTeam ?? ''
        });
        this.snack.open('Configuração salva com sucesso', 'Fechar', { duration: 2000 });
      },
      error: () => {
        this.saving = false;
        this.snack.open('Não foi possível salvar a configuração', 'Fechar', { duration: 2500 });
      }
    });
  }
}
