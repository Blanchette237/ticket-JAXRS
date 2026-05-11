import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { SessionService } from '../../services/session.service';
import { SessionUser } from '../../models/user.model';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar">
      <div class="navbar-brand">
        <a routerLink="/concerts" style="text-decoration:none;display:flex;align-items:center;gap:0.5rem;">
          <span class="logo">🎵</span>
          <span class="brand-name">TicketApp</span>
        </a>
      </div>

      <ul class="navbar-links">
        <li><a routerLink="/concerts" routerLinkActive="active">Concerts</a></li>
        <li *ngIf="user?.role === 'CLIENT'">
          <a routerLink="/mes-tickets" routerLinkActive="active">Mes tickets</a>
        </li>
        <li *ngIf="user?.role === 'ORGANISATEUR'">
          <a routerLink="/organisateur" routerLinkActive="active">Mon dashboard</a>
        </li>
      </ul>

      <div class="navbar-right">
        <!-- Connecté -->
        <ng-container *ngIf="user">
          <div class="user-info">
            <span class="user-badge" [class.organisateur]="user.role === 'ORGANISATEUR'">
              {{ user.role === 'ORGANISATEUR' ? '🎤' : '🎟️' }}
              {{ user.firstname }} {{ user.name }}
            </span>
            <span class="user-id">ID : {{ user.userId }}</span>
          </div>
          <button (click)="deconnecter()" class="btn-logout">Déconnexion</button>
        </ng-container>

        <!-- Non connecté -->
        <ng-container *ngIf="!user">
          <a routerLink="/inscription" class="btn-inscription">S'inscrire</a>
        </ng-container>
      </div>
    </nav>
  `,
  styles: [`
    .navbar {
      display: flex; align-items: center; justify-content: space-between;
      background: #1a1a2e; padding: 0 2rem; height: 64px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.3); flex-wrap: wrap; gap: 0.5rem;
    }
    .navbar-brand { display: flex; align-items: center; }
    .brand-name { color: #fff; font-size: 1.3rem; font-weight: 700; }
    .logo { font-size: 1.5rem; }

    .navbar-links { list-style: none; display: flex; gap: 1.5rem; margin: 0; padding: 0; }
    .navbar-links a { color: #ccc; text-decoration: none; font-weight: 500; padding: 0.3rem 0.6rem; border-radius: 6px; transition: all 0.2s; font-size: 0.95rem; }
    .navbar-links a:hover, .navbar-links a.active { color: #e94560; background: rgba(233,69,96,0.1); }

    .navbar-right { display: flex; align-items: center; gap: 1rem; }
    .user-info { display: flex; flex-direction: column; align-items: flex-end; }
    .user-badge { color: #fff; font-size: 0.88rem; font-weight: 600; }
    .user-badge.organisateur { color: #ffd700; }
    .user-id { color: #888; font-size: 0.75rem; }

    .btn-logout {
      background: transparent; color: #e94560; border: 1.5px solid #e94560;
      padding: 0.35rem 0.9rem; border-radius: 6px; cursor: pointer;
      font-size: 0.85rem; font-weight: 600; transition: all 0.2s;
    }
    .btn-logout:hover { background: #e94560; color: #fff; }

    .btn-inscription {
      background: #e94560; color: #fff; text-decoration: none;
      padding: 0.45rem 1.1rem; border-radius: 8px; font-weight: 600;
      font-size: 0.9rem; transition: background 0.2s;
    }
    .btn-inscription:hover { background: #c73652; }
  `]
})
export class NavbarComponent implements OnInit {
  user: SessionUser | null = null;

  constructor(private sessionService: SessionService, private router: Router) {}

  ngOnInit(): void {
    this.sessionService.user$.subscribe(u => this.user = u);
  }

  deconnecter(): void {
    this.sessionService.logout();
    this.router.navigate(['/concerts']);
  }
}
