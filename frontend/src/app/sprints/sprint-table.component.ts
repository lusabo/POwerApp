import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Sprint } from './sprints.model';

@Component({
  selector: 'app-sprint-table',
  templateUrl: './sprint-table.component.html',
  styleUrls: ['./sprint-table.component.css']
})
export class SprintTableComponent {
  @Input() sprints: Sprint[] = [];
  @Input() loading = false;
  @Input() rowLoadingId: number | null = null;
  @Output() reload = new EventEmitter<number>();
}
