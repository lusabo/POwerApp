import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { SprintsService } from '../sprints/sprints.service';
import { Sprint } from '../sprints/sprints.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
  imports: [CommonModule, TranslateModule],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent implements OnInit {
  private readonly sprintsService = inject(SprintsService);
  private readonly cdr = inject(ChangeDetectorRef);

  sprints: Sprint[] = [];
  displayedSprints: Sprint[] = [];
  forecastBySprintId: Record<number, number | null> = {};
  activeSprintId: number | null = null;
  loading = false;
  error: string | null = null;

  ngOnInit(): void {
    this.loadSprints();
  }

  private loadSprints(): void {
    this.loading = true;
    this.error = null;

    this.sprintsService.list().subscribe({
      next: (sprints) => {
        this.sprints = sprints ?? [];
        this.processSprints();
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Erro ao carregar sprints.';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  private processSprints(): void {
    const sorted = [...this.sprints]
      .filter((s) => s.endDate)
      .sort(
        (a, b) =>
          new Date(b.endDate).getTime() - new Date(a.endDate).getTime()
      );

    this.displayedSprints = sorted.slice(0, 3);

    const active = this.sprints.find((s) => s.sprintState === 'active');
    this.activeSprintId = active ? active.id : null;

    this.forecastBySprintId = {};

    this.displayedSprints.forEach((sprint) => {
      const startTime = new Date(sprint.startDate).getTime();
      const previous = this.sprints
        .filter(
          (other) =>
            other.id !== sprint.id &&
            other.endDate &&
            new Date(other.endDate).getTime() <= startTime
        )
        .sort(
          (a, b) =>
            new Date(b.endDate).getTime() - new Date(a.endDate).getTime()
        )
        .slice(0, 6);

      this.forecastBySprintId[sprint.id] = this.calculateForecast(
        sprint,
        previous
      );
    });
  }

  private calculateForecast(
    activeSprint: Sprint,
    previousSprints: Sprint[]
  ): number | null {
    if (!activeSprint || !previousSprints.length) {
      // eslint-disable-next-line no-console
      console.log(
        '[Dashboard] Forecast: sem sprint ativa ou sprints anteriores suficientes.',
        { sprintId: activeSprint?.id }
      );
      return null;
    }

    const lastSix = previousSprints.slice(0, 6);

    const avgCapacityPercent =
      lastSix.reduce(
        (sum, s) => sum + (s.capacityFinalPercent ?? 0),
        0
      ) / lastSix.length;

    const avgDelivered =
      lastSix.reduce(
        (sum, s) => sum + (s.storyPointsCompleted ?? 0),
        0
      ) / lastSix.length;

    const currentCapacityPercent = activeSprint.capacityFinalPercent ?? 0;

    if (!currentCapacityPercent) {
      // eslint-disable-next-line no-console
      console.log('[Dashboard] Forecast: capacidade final da sprint ativa é zero ou nula.', {
        activeSprintId: activeSprint.id,
        currentCapacityPercent,
        avgCapacityPercent,
        avgDelivered
      });
      return null;
    }

    const forecast =
      (avgDelivered * currentCapacityPercent) / avgCapacityPercent;

    // eslint-disable-next-line no-console
    console.log('[Dashboard] Forecast de Story Points calculado:', {
      sprintId: activeSprint.id,
      sprintName: activeSprint.name,
      sprintCapacityFinalPercent: currentCapacityPercent,
      previousSprintIds: lastSix.map((s) => s.id),
      avgCapacityPercent,
      avgDelivered,
      forecast
    });

    return Number.isFinite(forecast) ? forecast : null;
  }

  formattedForecastFor(sprint: Sprint): string {
    const value = this.forecastBySprintId[sprint.id];
    if (value == null) {
      return '-';
    }
    return value.toFixed(1);
  }

  formatDate(value: string | null | undefined): string {
    if (!value) {
      return '-';
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return '-';
    }
    return date.toLocaleDateString('pt-BR');
  }

  stateLabelKey(sprint: Sprint): string {
    switch (sprint.sprintState) {
      case 'active':
        return 'dashboard.state.active';
      case 'future':
        return 'dashboard.state.future';
      case 'closed':
        return 'dashboard.state.closed';
      default:
        return '';
    }
  }

  stateClass(sprint: Sprint): string {
    switch (sprint.sprintState) {
      case 'active':
        return 'state-active';
      case 'future':
        return 'state-future';
      case 'closed':
        return 'state-closed';
      default:
        return '';
    }
  }
}
