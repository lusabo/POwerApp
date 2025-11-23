import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BehaviorSubject, Subject, takeUntil, finalize } from 'rxjs';
import { SprintsService } from './sprints.service';
import { Sprint, SprintCreatePayload, TeamMember } from './sprints.model';

@Component({
  selector: 'app-sprints',
  templateUrl: './sprints.component.html',
  styleUrls: ['./sprints.component.css']
})
export class SprintsComponent implements OnInit, OnDestroy {
  sprints$ = new BehaviorSubject<Sprint[]>([]);
  members$ = new BehaviorSubject<TeamMember[]>([]);
  loading$ = new BehaviorSubject<boolean>(false);
  rowLoadingId: number | null = null;
  private destroy$ = new Subject<void>();

  constructor(private service: SprintsService, private snack: MatSnackBar) {}

  ngOnInit() {
    this.loadMembers();
    this.loadSprints();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadSprints() {
    this.service
      .list()
      .pipe(takeUntil(this.destroy$))
      .subscribe((data) => this.sprints$.next(data));
  }

  private loadMembers() {
    this.service
      .team()
      .pipe(takeUntil(this.destroy$))
      .subscribe((data) => this.members$.next(data));
  }

  onSave(payload: SprintCreatePayload) {
    this.loading$.next(true);
    this.service
      .create(payload)
      .pipe(
        finalize(() => this.loading$.next(false)),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: () => {
          this.snack.open('Sprint salva e capacidade recalculada', 'Fechar', { duration: 2000 });
          this.loadSprints();
          this.loadMembers();
        },
        error: () => this.snack.open('Não foi possível salvar a sprint', 'Fechar', { duration: 2500 })
      });
  }

  onReload(id: number) {
    this.rowLoadingId = id;
    this.loading$.next(true);
    this.service
      .reload(id)
      .pipe(
        finalize(() => {
          this.loading$.next(false);
          this.rowLoadingId = null;
        }),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: () => this.loadSprints(),
        error: (err) =>
          this.snack.open(err?.error || 'Sprint não está fechada ou não pôde ser recarregada', 'Fechar', {
            duration: 3000
          })
      });
  }
}
