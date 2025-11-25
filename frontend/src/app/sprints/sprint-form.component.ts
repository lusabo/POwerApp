import { ChangeDetectionStrategy, Component, OnInit, effect, inject, input, output } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { AbsenceInput, SprintCreatePayload, TeamMember } from './sprints.model';

type AbsenceFormGroup = FormGroup<{
  teamMemberId: FormControl<number>;
  days: FormControl<number | null>;
}>;

@Component({
  selector: 'app-sprint-form',
  templateUrl: './sprint-form.component.html',
  styleUrls: ['./sprint-form.component.css'],
  imports: [ReactiveFormsModule, TranslateModule],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SprintFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder).nonNullable;

  readonly members = input<TeamMember[]>([]);
  readonly loading = input(false);
  // domain cycles provided by parent component
  readonly domainCycles = input<{ id: number; name: string }[]>([]);
  readonly saveSprint = output<SprintCreatePayload>();

  readonly form = this.fb.group({
    name: ['', Validators.required],
    goal: [''],
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    domainCycleId: [null as number | null],
    operationsSpikesDays: [0, [Validators.min(0)]],
    absences: this.fb.array<AbsenceFormGroup>([])
  });

  constructor() {
    effect(() => this.resetAbsences(this.members()));
  }

  ngOnInit(): void {
    this.resetAbsences(this.members());
  }

  get absencesArray(): FormArray<AbsenceFormGroup> {
    return this.form.get('absences') as FormArray<AbsenceFormGroup>;
  }

  private resetAbsences(members: TeamMember[]): void {
    const abs = this.absencesArray;
    abs.clear();
    (members || []).forEach((m) =>
      abs.push(
        this.fb.group({
          teamMemberId: this.fb.control(m.id),
          days: this.fb.control<number | null>(0, [Validators.min(0)])
        })
      )
    );
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const absences = (this.absencesArray.getRawValue() as AbsenceInput[]).filter(
      (a) => a && a.days !== null && a.days !== undefined
    );
    const raw = this.form.getRawValue();
    const payload: SprintCreatePayload = {
      name: raw.name ?? '',
      goal: raw.goal ?? '',
      startDate: raw.startDate ?? '',
      endDate: raw.endDate ?? '',
      domainCycleId: (raw.domainCycleId as number | null) ?? null,
      operationsSpikesDays: raw.operationsSpikesDays ?? 0,
      absences
    };
    this.saveSprint.emit(payload);
  }
}
