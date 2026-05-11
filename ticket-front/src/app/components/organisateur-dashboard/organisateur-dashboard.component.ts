import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ConcertService } from '../../services/concert.service';
import { SessionService } from '../../services/session.service';
import { Concert } from '../../models/concert.model';
import { SessionUser } from '../../models/user.model';

@Component({
  selector: 'app-organisateur-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="page-container">
      <div class="page-header">
        <div>
          <h1>Tableau de bord organisateur</h1>
          <p class="subtitle" *ngIf="user">Bonjour, {{ user.firstname }} {{ user.name }} · ID : {{ user.userId }}</p>
        </div>
        <button (click)="showForm = !showForm" class="btn-primary">
          {{ showForm ? '✕ Annuler' : '+ Créer un concert' }}
        </button>
      </div>

      <!-- Formulaire création concert -->
      <div class="form-card" *ngIf="showForm">
        <h2>Nouveau concert</h2>
        <form (ngSubmit)="creerConcert()" #f="ngForm">
          <div class="form-row">
            <div class="form-group">
              <label>Lieu</label>
              <input type="text" [(ngModel)]="nouveauConcert.lieu" name="lieu"
                     required placeholder="Zénith Paris" class="form-input" />
            </div>
            <div class="form-group">
              <label>Capacité (places)</label>
              <input type="number" [(ngModel)]="nouveauConcert.capacite" name="capacite"
                     required min="1" placeholder="500" class="form-input" />
            </div>
          </div>
          <div class="form-group">
            <label>Description</label>
            <textarea [(ngModel)]="nouveauConcert.description" name="description"
                      placeholder="Décrivez l'événement..." class="form-input textarea"></textarea>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Date et heure</label>
              <input type="datetime-local" [(ngModel)]="nouveauConcert.dateTime" name="dateTime"
                     required class="form-input" />
            </div>
            <div class="form-group">
              <label>Popularité (1 à 5)</label>
              <select [(ngModel)]="nouveauConcert.popularite" name="popularite" class="form-input">
                <option [value]="1">⭐ 1 — Peu connu</option>
                <option [value]="2">⭐⭐ 2</option>
                <option [value]="3">⭐⭐⭐ 3 — Connu</option>
                <option [value]="4">⭐⭐⭐⭐ 4</option>
                <option [value]="5">⭐⭐⭐⭐⭐ 5 — Très populaire</option>
              </select>
            </div>
          </div>

          <div *ngIf="createErreur" class="error-msg">{{ createErreur }}</div>
          <div *ngIf="createSucces" class="succes-msg">{{ createSucces }}</div>

          <button type="submit" class="btn-primary" [disabled]="createLoading || !f.valid">
            {{ createLoading ? 'Création...' : 'Créer le concert' }}
          </button>
        </form>
      </div>

      <!-- Liste des concerts -->
      <h2 class="section-title">Vos concerts</h2>

      <div *ngIf="loadingConcerts" class="loading">Chargement...</div>

      <div *ngIf="!loadingConcerts && mesConcerts.length === 0" class="empty-state">
        Vous n'avez pas encore créé de concert.
      </div>

      <div class="concerts-grid">
        <div class="concert-card" *ngFor="let concert of mesConcerts">
          <div class="card-top">
            <h3>{{ concert.lieu }}</h3>
            <span class="badge" [class.complet]="concert.capacite <= 0">
              {{ concert.capacite <= 0 ? 'COMPLET' : concert.capacite + ' places' }}
            </span>
          </div>
          <p class="concert-desc">{{ concert.description }}</p>
          <div class="concert-meta">
            <span>📅 {{ concert.date | date:'dd/MM/yyyy HH:mm' }}</span>
            <span>🏟️ {{ concert.capaciteMax }} places total</span>
            <span>⭐ Popularité {{ concert.popularite }}/5</span>
          </div>
          <div class="progress-bar">
            <div class="progress-fill"
                 [style.width.%]="(1 - concert.capacite / concert.capaciteMax) * 100"
                 [class.urgent]="concert.capacite / concert.capaciteMax < 0.2">
            </div>
          </div>
          <p class="progress-label">
            {{ ((1 - concert.capacite / concert.capaciteMax) * 100).toFixed(0) }}% vendu —
            {{ concert.capaciteMax - concert.capacite }} ticket(s) vendus
          </p>
          <div class="card-actions">
            <button (click)="supprimerConcert(concert)" class="btn-danger">Supprimer</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .page-container { max-width: 1100px; margin: 0 auto; padding: 2rem; }
    .page-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem; }
    .page-header h1 { font-size: 1.8rem; color: #1a1a2e; margin-bottom: 0.3rem; }
    .subtitle { color: #888; font-size: 0.9rem; }
    .section-title { font-size: 1.3rem; color: #1a1a2e; margin: 2rem 0 1rem; }

    .form-card { background: #fff; border-radius: 12px; padding: 2rem; margin-bottom: 2rem; box-shadow: 0 2px 12px rgba(0,0,0,0.08); border: 1px solid #eee; }
    .form-card h2 { margin-bottom: 1.5rem; color: #1a1a2e; }
    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.4rem; margin-bottom: 1.1rem; }
    .form-group label { font-weight: 600; color: #444; font-size: 0.9rem; }
    .form-input {
      padding: 0.7rem; border: 1.5px solid #ddd; border-radius: 8px;
      font-size: 0.95rem; outline: none; transition: border-color 0.2s; font-family: inherit;
    }
    .form-input:focus { border-color: #e94560; }
    .textarea { resize: vertical; min-height: 80px; }

    .btn-primary {
      background: #e94560; color: #fff; border: none; padding: 0.7rem 1.4rem;
      border-radius: 8px; cursor: pointer; font-weight: 700; font-size: 0.95rem; transition: background 0.2s;
    }
    .btn-primary:hover:not(:disabled) { background: #c73652; }
    .btn-primary:disabled { background: #ccc; cursor: not-allowed; }
    .btn-danger {
      background: transparent; color: #c62828; border: 1.5px solid #c62828;
      padding: 0.4rem 0.9rem; border-radius: 6px; cursor: pointer; font-weight: 600; font-size: 0.85rem; transition: all 0.2s;
    }
    .btn-danger:hover { background: #c62828; color: #fff; }

    .concerts-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 1.5rem; }
    .concert-card { background: #fff; border-radius: 12px; padding: 1.5rem; box-shadow: 0 2px 12px rgba(0,0,0,0.07); border: 1px solid #eee; }
    .card-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.7rem; }
    .card-top h3 { font-size: 1.1rem; color: #1a1a2e; margin: 0; }
    .badge { background: #e8f5e9; color: #2e7d32; padding: 0.2rem 0.6rem; border-radius: 20px; font-size: 0.78rem; font-weight: 600; white-space: nowrap; }
    .badge.complet { background: #fce4ec; color: #c62828; }
    .concert-desc { color: #777; font-size: 0.87rem; margin-bottom: 0.8rem; }
    .concert-meta { display: flex; flex-direction: column; gap: 0.25rem; font-size: 0.85rem; color: #555; margin-bottom: 0.8rem; }
    .progress-bar { background: #eee; border-radius: 4px; height: 6px; overflow: hidden; margin-bottom: 0.3rem; }
    .progress-fill { height: 100%; background: #4caf50; border-radius: 4px; }
    .progress-fill.urgent { background: #e94560; }
    .progress-label { font-size: 0.78rem; color: #999; margin-bottom: 1rem; }
    .card-actions { display: flex; justify-content: flex-end; }

    .error-msg { color: #c62828; background: #fce4ec; padding: 0.7rem; border-radius: 8px; margin-bottom: 1rem; font-size: 0.9rem; }
    .succes-msg { color: #2e7d32; background: #e8f5e9; padding: 0.7rem; border-radius: 8px; margin-bottom: 1rem; font-size: 0.9rem; }
    .loading, .empty-state { text-align: center; padding: 3rem; color: #999; }
  `]
})
export class OrganisateurDashboardComponent implements OnInit {
  user: SessionUser | null = null;
  mesConcerts: Concert[] = [];
  loadingConcerts = true;
  showForm = false;

  nouveauConcert = { lieu: '', description: '', dateTime: '', capacite: 100, popularite: 3 };
  createLoading = false;
  createErreur: string | null = null;
  createSucces: string | null = null;

  constructor(
    private concertService: ConcertService,
    private sessionService: SessionService
  ) {}

  ngOnInit(): void {
    this.user = this.sessionService.user;
    this.chargerConcerts();
  }

  chargerConcerts(): void {
    this.loadingConcerts = true;
    this.concertService.getAll().subscribe({
      next: (data) => { this.mesConcerts = data; this.loadingConcerts = false; },
      error: () => { this.loadingConcerts = false; }
    });
  }

  creerConcert(): void {
    if (!this.user) return;
    this.createLoading = true;
    this.createErreur = null;
    this.createSucces = null;

    this.concertService.create({
      organiserId: this.user.userId,
      lieu: this.nouveauConcert.lieu,
      description: this.nouveauConcert.description,
      dateTime: this.nouveauConcert.dateTime,
      capacite: this.nouveauConcert.capacite,
      popularite: this.nouveauConcert.popularite
    }).subscribe({
      next: () => {
        this.createSucces = 'Concert créé avec succès !';
        this.createLoading = false;
        this.showForm = false;
        this.nouveauConcert = { lieu: '', description: '', dateTime: '', capacite: 100, popularite: 3 };
        this.chargerConcerts();
      },
      error: (err) => {
        this.createErreur = err.error?.message || 'Erreur lors de la création.';
        this.createLoading = false;
      }
    });
  }

  supprimerConcert(concert: Concert): void {
    if (!confirm(`Supprimer le concert "${concert.lieu}" ?`)) return;
    this.concertService.delete(concert.concertId).subscribe({
      next: () => { this.mesConcerts = this.mesConcerts.filter(c => c.concertId !== concert.concertId); },
      error: () => alert('Impossible de supprimer ce concert.')
    });
  }
}
