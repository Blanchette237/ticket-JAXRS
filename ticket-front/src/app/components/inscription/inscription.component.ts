import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ClientService } from '../../services/client.service';
import { OrganiserService } from '../../services/organiser.service';
import { SessionService } from '../../services/session.service';
import { UserRole } from '../../models/user.model';

@Component({
  selector: 'app-inscription',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-card">
        <div class="auth-header">
          <span class="logo">🎵</span>
          <h1>TicketApp</h1>
          <p>Créez votre compte pour continuer</p>
        </div>

        <!-- Choix du rôle -->
        <div class="role-tabs">
          <button [class.active]="role === 'CLIENT'" (click)="role = 'CLIENT'">
            🎟️ Je suis client
          </button>
          <button [class.active]="role === 'ORGANISATEUR'" (click)="role = 'ORGANISATEUR'">
            🎤 Je suis organisateur
          </button>
        </div>

        <form (ngSubmit)="submit()" #f="ngForm">
          <div class="form-row">
            <div class="form-group">
              <label>Nom</label>
              <input type="text" [(ngModel)]="form.name" name="name"
                     required placeholder="Dupont" class="form-input" />
            </div>
            <div class="form-group">
              <label>Prénom</label>
              <input type="text" [(ngModel)]="form.firstname" name="firstname"
                     required placeholder="Jean" class="form-input" />
            </div>
          </div>

          <div class="form-group">
            <label>Email</label>
            <input type="email" [(ngModel)]="form.email" name="email"
                   required placeholder="jean@exemple.com" class="form-input" />
          </div>

          <div class="form-group">
            <label>Mot de passe</label>
            <input type="password" [(ngModel)]="form.password" name="password"
                   required placeholder="••••••••" class="form-input" />
          </div>

          <div *ngIf="erreur" class="error-msg">{{ erreur }}</div>
          <div *ngIf="succes" class="succes-msg">{{ succes }}</div>

          <button type="submit" class="btn-primary" [disabled]="loading || !f.valid">
            {{ loading ? 'Création...' : 'Créer mon compte' }}
          </button>
        </form>

        <p class="switch-link">
          Déjà un compte ?
          <a routerLink="/connexion">Se connecter</a>
        </p>
      </div>
    </div>
  `,
  styles: [`
    .auth-page {
      min-height: 100vh; display: flex; align-items: center; justify-content: center;
      background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
      padding: 2rem;
    }
    .auth-card {
      background: #fff; border-radius: 16px; padding: 2.5rem;
      width: 100%; max-width: 480px;
      box-shadow: 0 20px 60px rgba(0,0,0,0.3);
    }
    .auth-header { text-align: center; margin-bottom: 2rem; }
    .logo { font-size: 2.5rem; }
    .auth-header h1 { font-size: 1.8rem; color: #1a1a2e; margin: 0.3rem 0 0.2rem; }
    .auth-header p { color: #888; font-size: 0.95rem; }

    .role-tabs { display: flex; gap: 0; margin-bottom: 1.8rem; border-radius: 10px; overflow: hidden; border: 1.5px solid #e94560; }
    .role-tabs button {
      flex: 1; padding: 0.7rem; border: none; background: #fff;
      color: #e94560; font-weight: 600; cursor: pointer; font-size: 0.9rem;
      transition: all 0.2s;
    }
    .role-tabs button.active { background: #e94560; color: #fff; }

    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.4rem; margin-bottom: 1.1rem; }
    .form-group label { font-weight: 600; color: #444; font-size: 0.9rem; }
    .form-input {
      padding: 0.75rem; border: 1.5px solid #e0e0e0; border-radius: 8px;
      font-size: 0.95rem; outline: none; transition: border-color 0.2s;
    }
    .form-input:focus { border-color: #e94560; }

    .btn-primary {
      width: 100%; background: #e94560; color: #fff; border: none;
      padding: 0.85rem; border-radius: 10px; font-size: 1rem;
      font-weight: 700; cursor: pointer; margin-top: 0.5rem; transition: background 0.2s;
    }
    .btn-primary:hover:not(:disabled) { background: #c73652; }
    .btn-primary:disabled { background: #ccc; cursor: not-allowed; }

    .error-msg { color: #c62828; background: #fce4ec; padding: 0.7rem; border-radius: 8px; margin-bottom: 1rem; font-size: 0.9rem; }
    .succes-msg { color: #2e7d32; background: #e8f5e9; padding: 0.7rem; border-radius: 8px; margin-bottom: 1rem; font-size: 0.9rem; }

    .switch-link { text-align: center; margin-top: 1.5rem; color: #888; font-size: 0.9rem; }
    .switch-link a { color: #e94560; font-weight: 600; text-decoration: none; }
    .switch-link a:hover { text-decoration: underline; }
  `]
})
export class InscriptionComponent {
  role: UserRole = 'CLIENT';
  form = { name: '', firstname: '', email: '', password: '' };
  loading = false;
  erreur: string | null = null;
  succes: string | null = null;

  constructor(
    private clientService: ClientService,
    private organiserService: OrganiserService,
    private sessionService: SessionService,
    private router: Router
  ) {}

  submit(): void {
    this.loading = true;
    this.erreur = null;
    this.succes = null;

    const obs = this.role === 'CLIENT'
      ? this.clientService.create(this.form)
      : this.organiserService.create(this.form);

    obs.subscribe({
      next: (id: number) => {
        this.sessionService.login({
          userId: id,
          name: this.form.name,
          firstname: this.form.firstname,
          email: this.form.email,
          role: this.role
        });
        this.loading = false;
        this.router.navigate(
          this.role === 'ORGANISATEUR' ? ['/organisateur'] : ['/concerts']
        );
      },
      error: (err) => {
        this.erreur = err.error?.message || 'Erreur lors de la création du compte.';
        this.loading = false;
      }
    });
  }
}
