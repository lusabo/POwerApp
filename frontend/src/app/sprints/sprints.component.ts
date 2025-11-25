import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SprintsService } from './sprints.service';
import { Sprint, SprintCreatePayload, TeamMember } from './sprints.model';
import { SprintFormComponent } from './sprint-form.component';
import { SprintTableComponent } from './sprint-table.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-sprints',
  templateUrl: './sprints.component.html',
  styleUrls: ['./sprints.component.css'],
  imports: [SprintFormComponent, SprintTableComponent, MatProgressSpinnerModule, TranslateModule],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SprintsComponent implements OnInit {
  private readonly service = inject(SprintsService);
  private readonly snack = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  readonly sprints = signal<Sprint[]>([]);
  readonly members = signal<TeamMember[]>([]);
  readonly domainCycles = signal<{ id: number; name: string }[]>([]);
  readonly loading = signal(false);
  readonly rowLoadingId = signal<number | null>(null);

  ngOnInit() {
    this.loadMembers();
    this.loadDomainCycles();
    this.loadSprints();
  }

  private loadSprints() {
    this.service
      .list()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((data) => this.sprints.set(data));
  }

  private loadMembers() {
    this.service
      .team()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((data) => this.members.set(data));
  }

  private loadDomainCycles() {
    this.service
      .domainCycles()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((data) => this.domainCycles.set(data));
  }

  onSave(payload: SprintCreatePayload) {
    this.loading.set(true);
    this.service
      .create(payload)
      .pipe(
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef)
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
    this.rowLoadingId.set(id);
    this.loading.set(true);
    this.service
      .reload(id)
      .pipe(
        finalize(() => {
          this.loading.set(false);
          this.rowLoadingId.set(null);
        }),
        takeUntilDestroyed(this.destroyRef)
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
