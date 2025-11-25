import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from './core/auth.service';
import { LanguageService } from './core/language.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  imports: [RouterOutlet, RouterLink, MatToolbarModule, MatButtonModule, MatSelectModule, MatFormFieldModule, TranslateModule],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppComponent {
  readonly title = 'POwerApp';
  readonly languageOptions = [
    { value: 'pt', label: 'language.pt' },
    { value: 'en', label: 'language.en' }
  ] as const;

  private readonly authService = inject(AuthService);
  private readonly languageService = inject(LanguageService);

  readonly isAuthenticated = computed(() => this.authService.isAuthenticated());
  readonly currentLanguage = computed(() => this.languageService.getLanguage());

  logout() {
    this.authService.logout();
  }

  setLanguage(lang: string) {
    this.languageService.use(lang);
  }
}
