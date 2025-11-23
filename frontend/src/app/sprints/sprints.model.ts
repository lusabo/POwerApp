export interface Sprint {
  id: number;
  name: string;
  jiraSprintId?: number | null;
  startDate: string;
  endDate: string;
  storyPointsCompleted?: number;
  operationsSpikesDays?: number | null;
  workingDays?: number | null;
  ceremonyDays?: number | null;
  holidayDays?: number | null;
  netCapacityDays?: number | null;
  teamSize?: number | null;
  absencesDays?: number | null;
  capacityTotal?: number | null;
  capacityPercent?: number | null;
  capacityFinal?: number | null;
  capacityFinalPercent?: number | null;
}

export interface TeamMember {
  id: number;
  name: string;
  role?: string;
}

export interface AbsenceInput {
  teamMemberId: number;
  days: number | null;
}

export interface SprintCreatePayload {
  name: string;
  operationsSpikesDays: number;
  absences: AbsenceInput[];
}
