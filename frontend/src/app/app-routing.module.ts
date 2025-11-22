import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './auth/login.component';
import { RegisterComponent } from './auth/register.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { HolidaysComponent } from './holidays/holidays.component';
import { SprintsComponent } from './sprints/sprints.component';
import { EpicsComponent } from './epics/epics.component';
import { DomainCyclesComponent } from './domain-cycles/domain-cycles.component';
import { TeamComponent } from './team/team.component';
import { ProjectConfigComponent } from './project-config/project-config.component';
import { AuthGuard } from './core/auth.guard';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: '', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'holidays', component: HolidaysComponent, canActivate: [AuthGuard] },
  { path: 'sprints', component: SprintsComponent, canActivate: [AuthGuard] },
  { path: 'epics', component: EpicsComponent, canActivate: [AuthGuard] },
  { path: 'domain-cycles', component: DomainCyclesComponent, canActivate: [AuthGuard] },
  { path: 'team', component: TeamComponent, canActivate: [AuthGuard] },
  { path: 'project-config', component: ProjectConfigComponent, canActivate: [AuthGuard] }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
