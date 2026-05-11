import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ConcertService } from '../../services/concert.service';
import { TicketService } from '../../services/ticket.service';
import { Concert } from '../../models/concert.model';

@Component({
  selector: 'app-concert-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="page-container">
      <a routerLink="/concerts" class="back-link">← Retour aux concerts</a>

      <div *ngIf="loading" class="loading">Chargement...</div>
      <div *ngIf="erreur" class="error-msg">{{ erreur }}</div>

      <div *ngIf="concert" class="detail-layout">
        <div class="detail-card">
          <div class="detail-header">
            <h1>{{ concert.lieu }}</h1>
            <div class="popularite">
              <span *ngFor="let s of stars(concert.popularite)">★</span>
              <span *ngFor="let s of emptyStars(concert.popularite)" class="empty">★</span>
            </div>
          </div>

          <p class="description">{{ concert.description }}</p>

          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">Date</span>
              <span class="info-value">{{ concert.date | date:'EEEE dd MMMM yyyy' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Heure</span>
              <span class="info-value">{{ concert.date | date:'HH:mm' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Capacité totale</span>
              <span class="info-value">{{ concert.capaciteMax }} places</span>
            </div>
            <div class="info-item">
              <span class="info-label">Places disponibles</span>
              <span class="info-value" [class.urgent]="concert.capacite / concert.capaciteMax < 0.2">
                {{ concert.capacite }} places
              </span>
            </div>
          </div>

          <div class="progress-bar">
            <div class="progress-fill"
                 [style.width.%]="(1 - concert.capacite / concert.capaciteMax) * 100"
                 [class.urgent]="concert.capacite / concert.capaciteMax < 0.2">
            </div>
          </div>
          <p class="progress-label">
            {{ ((1 - concert.capacite / concert.capaciteMax) * 100).toFixed(0) }}% des places vendues
          </p>
        </div>

        <!-- Formulaire d'achat de ticket -->
        <div class="ticket-form-card">
          <h2>Réserver un ticket</h2>

          <div *ngIf="concert.capacite <= 0" class="complet-msg">
            Ce concert est complet.
          </div>

          <form *ngIf="concert.capacite > 0" (ngSubmit)="acheterTicket()" #form="ngForm">
            <div class="form-group">
              <label>Votre ID client</label>
              <input type="number" [(ngModel)]="clientId" name="clientId"
                     required min="1" placeholder="Ex: 1" class="form-input" />
            </div>

            <div class="form-group">
              <label>Numéro de place</label>
              <input type="text" [(ngModel)]="numeroPlace" name="numeroPlace"
                     required placeholder="Ex: VIP1, A12, B34" class="form-input" />
              <small class="hint">
                Zones : VIP (+50€), A (+20€), B (+10€), autres (+5€)<br>
                Places 1-10 : +20€ | Places 11-30 : +10€
              </small>
            </div>

            <div *ngIf="prixEstime" class="prix-estime">
              Prix estimé : <strong>{{ prixEstime }}€</strong>
              <small> (calculé selon zone, numéro et popularité)</small>
            </div>

            <div *ngIf="succes" class="succes-msg">{{ succes }}</div>
            <div *ngIf="achatErreur" class="error-msg">{{ achatErreur }}</div>

            <button type="submit" class="btn-primary" [disabled]="achatEnCours || !form.valid">
              {{ achatEnCours ? 'Réservation...' : 'Confirmer la réservation' }}
            </button>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .page-container { max-width: 1000px; margin: 0 auto; padding: 2rem; }
    .back-link { color: #e94560; text-decoration: none; font-weight: 500; display: inline-block; margin-bottom: 1.5rem; }
    .back-link:hover { text-decoration: underline; }

    .detail-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 2rem; }
    @media(max-width: 700px) { .detail-layout { grid-template-columns: 1fr; } }

    .detail-card, .ticket-form-card {
      background: #fff; border-radius: 12px; padding: 2rem;
      box-shadow: 0 2px 12px rgba(0,0,0,0.08); border: 1px solid #eee;
    }
    .detail-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem; }
    .detail-header h1 { font-size: 1.5rem; color: #1a1a2e; margin: 0; }
    .popularite { color: #f5a623; font-size: 1.2rem; }
    .popularite .empty { color: #ddd; }
    .description { color: #666; margin-bottom: 1.5rem; line-height: 1.6; }

    .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem; }
    .info-item { display: flex; flex-direction: column; gap: 0.2rem; }
    .info-label { font-size: 0.78rem; color: #999; text-transform: uppercase; letter-spacing: 0.05em; }
    .info-value { font-weight: 600; color: #1a1a2e; }
    .info-value.urgent { color: #e94560; }

    .progress-bar { background: #eee; border-radius: 4px; height: 8px; margin-bottom: 0.4rem; overflow: hidden; }
    .progress-fill { height: 100%; background: #4caf50; border-radius: 4px; }
    .progress-fill.urgent { background: #e94560; }
    .progress-label { font-size: 0.8rem; color: #999; }

    .ticket-form-card h2 { color: #1a1a2e; margin-bottom: 1.5rem; }
    .form-group { margin-bottom: 1.2rem; display: flex; flex-direction: column; gap: 0.4rem; }
    .form-group label { font-weight: 600; color: #444; font-size: 0.9rem; }
    .form-input {
      padding: 0.7rem; border: 1.5px solid #ddd; border-radius: 8px;
      font-size: 1rem; transition: border-color 0.2s; outline: none;
    }
    .form-input:focus { border-color: #e94560; }
    .hint { color: #999; font-size: 0.8rem; line-height: 1.5; }

    .prix-estime {
      background: #fff8e1; border: 1px solid #ffe082; border-radius: 8px;
      padding: 0.8rem; margin-bottom: 1rem; color: #f57c00;
    }
    .btn-primary {
      width: 100%; background: #e94560; color: #fff; border: none;
      padding: 0.8rem; border-radius: 8px; cursor: pointer; font-weight: 700;
      font-size: 1rem; transition: background 0.2s;
    }
    .btn-primary:hover:not(:disabled) { background: #c73652; }
    .btn-primary:disabled { background: #ccc; cursor: not-allowed; }

    .succes-msg { color: #2e7d32; background: #e8f5e9; padding: 0.7rem; border-radius: 8px; margin-bottom: 1rem; }
    .error-msg, .achat-erreur { color: #c62828; background: #fce4ec; padding: 0.7rem; border-radius: 8px; margin-bottom: 1rem; }
    .complet-msg { color: #c62828; font-weight: 600; text-align: center; padding: 2rem; }
    .loading { text-align: center; padding: 3rem; color: #666; }
  `]
})
export class ConcertDetailComponent implements OnInit {
  concert: Concert | null = null;
  loading = true;
  erreur: string | null = null;

  clientId: number | null = null;
  numeroPlace = '';
  achatEnCours = false;
  succes: string | null = null;
  achatErreur: string | null = null;
  prixEstime: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private concertService: ConcertService,
    private ticketService: TicketService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.concertService.getById(id).subscribe({
      next: (c) => { this.concert = c; this.loading = false; },
      error: () => { this.erreur = 'Concert introuvable.'; this.loading = false; }
    });
  }

  acheterTicket(): void {
    if (!this.clientId || !this.numeroPlace || !this.concert) return;
    this.achatEnCours = true;
    this.succes = null;
    this.achatErreur = null;

    this.ticketService.create({
      utilisateurId: this.clientId,
      concertId: this.concert.concertId,
      numeroPlace: this.numeroPlace
    }).subscribe({
      next: () => {
        this.succes = 'Ticket réservé avec succès !';
        this.achatEnCours = false;
        this.concert!.capacite--;
        this.numeroPlace = '';
      },
      error: (err) => {
        this.achatErreur = err.error?.message || 'Erreur lors de la réservation.';
        this.achatEnCours = false;
      }
    });
  }

  stars(n: number): number[] { return Array(Math.round(n || 0)); }
  emptyStars(n: number): number[] { return Array(5 - Math.round(n || 0)); }
}
