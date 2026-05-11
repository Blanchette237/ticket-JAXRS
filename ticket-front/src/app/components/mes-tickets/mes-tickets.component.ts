import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TicketService } from '../../services/ticket.service';
import { Ticket } from '../../models/ticket.model';

@Component({
  selector: 'app-mes-tickets',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Mes Tickets</h1>
        <p class="subtitle">Consultez et gérez vos réservations</p>
      </div>

      <!-- Recherche par client ID -->
      <div class="search-bar">
        <input type="number" [(ngModel)]="clientId" placeholder="Votre ID client"
               class="search-input" min="1" />
        <button (click)="chargerTickets()" class="btn-search" [disabled]="!clientId || loading">
          {{ loading ? 'Chargement...' : 'Voir mes tickets' }}
        </button>
      </div>

      <div *ngIf="erreur" class="error-msg">{{ erreur }}</div>

      <div *ngIf="tickets.length > 0">
        <div class="stats-bar">
          <span class="stat">{{ tickets.length }} ticket(s) au total</span>
          <span class="stat actif">{{ nbActifs() }} actif(s)</span>
          <span class="stat annule">{{ nbAnnules() }} annulé(s)</span>
        </div>

        <div class="tickets-list">
          <div class="ticket-card" *ngFor="let ticket of tickets"
               [class.annule]="ticket.status === 'ANNULE'"
               [class.utilise]="ticket.status === 'UTILISE'">

            <div class="ticket-left">
              <div class="ticket-status" [class]="'status-' + ticket.status.toLowerCase()">
                {{ ticket.status }}
              </div>
              <div class="ticket-numero">Place {{ ticket.numeroPlace }}</div>
            </div>

            <div class="ticket-center">
              <div class="ticket-concert">{{ ticket.concert ? ticket.concert.lieu : 'Concert inconnu' }}</div>
              <div class="ticket-date">
                📅 {{ ticket.concert.date | date:'dd/MM/yyyy HH:mm' }}
              </div>
              <div class="ticket-achat">
                Acheté le {{ ticket.date_achat | date:'dd/MM/yyyy' }}
              </div>
            </div>

            <div class="ticket-right">
              <div class="ticket-prix">{{ ticket.prixUnitaire }}€</div>
              <button *ngIf="ticket.status === 'ACTIVE'"
                      (click)="annuler(ticket)"
                      [disabled]="annulationEnCours === ticket.ticketId"
                      class="btn-annuler">
                {{ annulationEnCours === ticket.ticketId ? '...' : 'Annuler' }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <div *ngIf="!loading && rechercheFaite && tickets.length === 0 && !erreur" class="empty-state">
        Aucun ticket trouvé pour cet ID client.
      </div>
    </div>
  `,
  styles: [`
    .page-container { max-width: 900px; margin: 0 auto; padding: 2rem; }
    .page-header { text-align: center; margin-bottom: 2rem; }
    .page-header h1 { font-size: 2rem; color: #1a1a2e; margin-bottom: 0.5rem; }
    .subtitle { color: #666; }

    .search-bar { display: flex; gap: 1rem; margin-bottom: 2rem; max-width: 400px; margin-left: auto; margin-right: auto; }
    .search-input {
      flex: 1; padding: 0.7rem 1rem; border: 1.5px solid #ddd; border-radius: 8px;
      font-size: 1rem; outline: none; transition: border-color 0.2s;
    }
    .search-input:focus { border-color: #e94560; }
    .btn-search {
      background: #e94560; color: #fff; border: none; padding: 0.7rem 1.4rem;
      border-radius: 8px; cursor: pointer; font-weight: 600; white-space: nowrap;
      transition: background 0.2s;
    }
    .btn-search:hover:not(:disabled) { background: #c73652; }
    .btn-search:disabled { background: #ccc; cursor: not-allowed; }

    .stats-bar {
      display: flex; gap: 1.5rem; margin-bottom: 1.5rem;
      padding: 1rem; background: #f8f9fa; border-radius: 8px;
    }
    .stat { font-weight: 600; color: #444; }
    .stat.actif { color: #2e7d32; }
    .stat.annule { color: #c62828; }

    .tickets-list { display: flex; flex-direction: column; gap: 1rem; }
    .ticket-card {
      display: flex; align-items: center; gap: 1.5rem;
      background: #fff; border-radius: 12px; padding: 1.2rem 1.5rem;
      box-shadow: 0 2px 8px rgba(0,0,0,0.07); border: 1px solid #eee;
      border-left: 5px solid #4caf50; transition: box-shadow 0.2s;
    }
    .ticket-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
    .ticket-card.annule { border-left-color: #ef9a9a; opacity: 0.7; }
    .ticket-card.utilise { border-left-color: #bdbdbd; opacity: 0.6; }

    .ticket-left { display: flex; flex-direction: column; align-items: center; gap: 0.4rem; min-width: 80px; }
    .ticket-status {
      font-size: 0.7rem; font-weight: 700; padding: 0.2rem 0.5rem;
      border-radius: 20px; text-transform: uppercase; letter-spacing: 0.05em;
    }
    .status-active { background: #e8f5e9; color: #2e7d32; }
    .status-annule { background: #fce4ec; color: #c62828; }
    .status-utilise { background: #f5f5f5; color: #757575; }

    .ticket-numero { font-weight: 700; color: #1a1a2e; font-size: 0.95rem; }

    .ticket-center { flex: 1; }
    .ticket-concert { font-size: 1.05rem; font-weight: 600; color: #1a1a2e; margin-bottom: 0.3rem; }
    .ticket-date, .ticket-achat { color: #777; font-size: 0.85rem; }

    .ticket-right { display: flex; flex-direction: column; align-items: flex-end; gap: 0.7rem; }
    .ticket-prix { font-size: 1.3rem; font-weight: 700; color: #e94560; }
    .btn-annuler {
      background: transparent; color: #e94560; border: 1.5px solid #e94560;
      padding: 0.3rem 0.8rem; border-radius: 6px; cursor: pointer; font-size: 0.85rem;
      font-weight: 600; transition: all 0.2s;
    }
    .btn-annuler:hover:not(:disabled) { background: #e94560; color: #fff; }
    .btn-annuler:disabled { opacity: 0.5; cursor: not-allowed; }

    .error-msg { color: #c62828; background: #fce4ec; padding: 0.8rem; border-radius: 8px; margin-bottom: 1rem; }
    .empty-state { text-align: center; padding: 3rem; color: #999; }
  `]
})
export class MesTicketsComponent {
  clientId: number | null = null;
  tickets: Ticket[] = [];
  loading = false;
  erreur: string | null = null;
  rechercheFaite = false;
  annulationEnCours: number | null = null;

  constructor(private ticketService: TicketService) {}

  chargerTickets(): void {
    this.loading = true;
    this.erreur = null;
    this.rechercheFaite = true;

    // On charge tous les tickets puis on filtre côté client
    // (amélioration future : endpoint GET /tickets?clientId=X)
    this.ticketService.getAll().subscribe({
      next: (data) => {
        this.tickets = data;
        this.loading = false;
      },
      error: () => {
        this.erreur = 'Impossible de charger les tickets.';
        this.loading = false;
      }
    });
  }

  annuler(ticket: Ticket): void {
    if (!confirm(`Annuler le ticket place ${ticket.numeroPlace} ?`)) return;
    this.annulationEnCours = ticket.ticketId;

    this.ticketService.annuler(ticket.ticketId).subscribe({
      next: () => {
        ticket.status = 'ANNULE';
        this.annulationEnCours = null;
      },
      error: (err) => {
        this.erreur = err.error?.message || 'Erreur lors de l\'annulation.';
        this.annulationEnCours = null;
      }
    });
  }

  nbActifs(): number { return this.tickets.filter(t => t.status === 'ACTIVE').length; }
  nbAnnules(): number { return this.tickets.filter(t => t.status === 'ANNULE').length; }
}
