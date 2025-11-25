import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
import { DatePipe, PercentPipe } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { Sprint } from './sprints.model';

@Component({
  selector: 'app-sprint-table',
  templateUrl: './sprint-table.component.html',
  styleUrls: ['./sprint-table.component.css'],
  imports: [DatePipe, PercentPipe, TranslateModule],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SprintTableComponent {
  readonly sprints = input<Sprint[]>([]);
  readonly loading = input(false);
  readonly rowLoadingId = input<number | null>(null);
  readonly reload = output<number>();

  private readonly sortDirection = signal<'asc' | 'desc'>('asc');

  readonly sortedSprints = computed(() => {
    const data = this.sprints() ?? [];
    const direction = this.sortDirection();
    return [...data].sort((a, b) => {
      const aName = (a.name ?? '').toLocaleLowerCase();
      const bName = (b.name ?? '').toLocaleLowerCase();
      const cmp = aName.localeCompare(bName);
      return direction === 'asc' ? cmp : -cmp;
    });
  });

  toggleNameSort(): void {
    this.sortDirection.update((current) => (current === 'asc' ? 'desc' : 'asc'));
  }
}
