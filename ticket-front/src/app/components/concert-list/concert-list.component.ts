import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ConcertService } from '../../services/concert.service';
import { Concert } from '../../models/concert.model';

@Component({
  selector: 'app-concert-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Concerts à venir</h1>
        <p class="subtitle">Découvrez les prochains événements et réservez votre place</p>
      </div>

      <div *ngIf="loading" class="loading">Chargement des concerts...</div>
      <div *ngIf="erreur" class="error-msg">{{ erreur }}</div>

      <div *ngIf="!loading && concerts.length === 0 && !erreur" class="empty-state">
        Aucun concert disponible pour le moment.
      </div>

      <div class="concerts-grid">
        <div class="concert-card" *ngFor="let concert of concerts">
          <div class="card-header">
            <div class="popularite">
              <span *ngFor="let s of stars(concert.popularite)">★</span>
              <span *ngFor="let s of emptyStars(concert.popularite)" class="empty">★</span>
            </div>
            <span class="badge" [class.complet]="concert.capacite <= 0">
              {{ concert.capacite <= 0 ? 'COMPLET' : concert.capacite + ' places' }}
            </span>
          </div>

          <h2 class="concert-lieu">{{ concert.lieu }}</h2>
          <p class="concert-desc">{{ concert.description }}</p>

          <div class="concert-meta">
            <span class="meta-item">📅 {{ concert.date | date:'dd/MM/yyyy HH:mm' }}</span>
            <span class="meta-item">🏟️ {{ concert.capaciteMax }} places total</span>
          </div>

          <div class="progress-bar">
            <div class="progress-fill"
                 [style.width.%]="(1 - concert.capacite / concert.capaciteMax) * 100"
                 [class.urgent]="concert.capacite / concert.capaciteMax < 0.2">
            </div>
          </div>
          <p class="progress-label">
            {{ ((1 - concert.capacite / concert.capaciteMax) * 100).toFixed(0) }}% vendu
          </p>

          <div class="card-actions">
            <a [routerLink]="['/concerts', concert.concertId]" class="btn-primary"
               [class.disabled]="concert.capacite <= 0">
              {{ concert.capacite <= 0 ? 'Complet' : 'Réserver' }}
            </a>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .page-container { max-width: 1200px; margin: 0 auto; padding: 2rem; }
    .page-header { text-align: center; margin-bottom: 2.5rem; }
    .page-header h1 { font-size: 2rem; color: #1a1a2e; margin-bottom: 0.5rem; }
    .subtitle { color: #666; font-size: 1.05rem; }

    .concerts-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 1.5rem;
    }
    .concert-card {
      background: #fff;
      border-radius: 12px;
      padding: 1.5rem;
      box-shadow: 0 2px 12px rgba(0,0,0,0.08);
      border: 1px solid #eee;
      transition: transform 0.2s, box-shadow 0.2s;
    }
    .concert-card:hover {
      transform: translateY(-3px);
      box-shadow: 0 6px 20px rgba(0,0,0,0.12);
    }
    .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
    .popularite { color: #f5a623; font-size: 1rem; }
    .popularite .empty { color: #ddd; }
    .badge {
      background: #e8f5e9; color: #2e7d32;
      padding: 0.2rem 0.7rem; border-radius: 20px; font-size: 0.8rem; font-weight: 600;
    }
    .badge.complet { background: #fce4ec; color: #c62828; }

    .concert-lieu { font-size: 1.2rem; color: #1a1a2e; margin: 0 0 0.5rem; }
    .concert-desc { color: #777; font-size: 0.9rem; margin-bottom: 1rem; min-height: 40px; }
    .concert-meta { display: flex; flex-direction: column; gap: 0.3rem; margin-bottom: 1rem; }
    .meta-item { color: #555; font-size: 0.88rem; }

    .progress-bar { background: #eee; border-radius: 4px; height: 6px; margin-bottom: 0.3rem; overflow: hidden; }
    .progress-fill { height: 100%; background: #4caf50; border-radius: 4px; transition: width 0.3s; }
    .progress-fill.urgent { background: #e94560; }
    .progress-label { font-size: 0.75rem; color: #999; margin-bottom: 1rem; }

    .card-actions { display: flex; justify-content: flex-end; }
    .btn-primary {
      background: #e94560; color: #fff; border: none; padding: 0.6rem 1.4rem;
      border-radius: 8px; cursor: pointer; font-weight: 600; text-decoration: none;
      transition: background 0.2s;
    }
    .btn-primary:hover { background: #c73652; }
    .btn-primary.disabled { background: #ccc; cursor: not-allowed; pointer-events: none; }

    .loading, .error-msg, .empty-state { text-align: center; padding: 3rem; color: #666; }
    .error-msg { color: #e94560; }
  `]
})
export class ConcertListComponent implements OnInit {
  concerts: Concert[] = [];
  loading = true;
  erreur: string | null = null;

  constructor(private concertService: ConcertService) {}

  ngOnInit(): void {
    this.concertService.getAll().subscribe({
      next: (data) => { this.concerts = data; this.loading = false; },
      error: () => { this.erreur = 'Impossible de charger les concerts. Le serveur est-il démarré ?'; this.loading = false; }
    });
  }

  stars(n: number): number[] { return Array(Math.round(n)); }
  emptyStars(n: number): number[] { return Array(5 - Math.round(n)); }
}
