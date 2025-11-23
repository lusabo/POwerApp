import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AbsenceInput, SprintCreatePayload, TeamMember } from './sprints.model';

@Component({
  selector: 'app-sprint-form',
  templateUrl: './sprint-form.component.html',
  styleUrls: ['./sprint-form.component.css']
})
export class SprintFormComponent implements OnInit, OnChanges {
  @Input() members: TeamMember[] = [];
  @Input() loading = false;
  @Output() saveSprint = new EventEmitter<SprintCreatePayload>();

  form: FormGroup = this.fb.group({
    name: ['', Validators.required],
    operationsSpikesDays: [0, [Validators.min(0)]],
    absences: this.fb.array<FormGroup>([])
  });

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.resetAbsences();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['members']) {
      this.resetAbsences();
    }
  }

  get absencesArray(): FormArray<FormGroup> {
    return this.form.get('absences') as FormArray<FormGroup>;
  }

  private resetAbsences(): void {
    const abs = this.absencesArray;
    abs.clear();
    (this.members || []).forEach((m) =>
      abs.push(
        this.fb.group({
          teamMemberId: [m.id],
          days: [0, [Validators.min(0)]]
        })
      )
    );
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const absences = (this.absencesArray.value as AbsenceInput[]).filter(
      (a) => a && a.days !== null && a.days !== undefined
    );
    const payload: SprintCreatePayload = {
      name: this.form.value.name,
      operationsSpikesDays: this.form.value.operationsSpikesDays ?? 0,
      absences
    };
    this.saveSprint.emit(payload);
  }
}
