import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ClientService } from '../../services/client.service';
import { TicketService } from '../../services/ticket.service';
import { SessionService } from '../../services/session.service';
import { Ticket } from '../../models/ticket.model';
import { SessionUser } from '../../models/user.model';

@Component({
  selector: 'app-mes-tickets',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="page-container">
      <div class="page-header">
        <h1>Mes Tickets</h1>
        <p class="subtitle" *ngIf="user">
          {{ user.firstname }} {{ user.name }} · ID client : {{ user.userId }}
        </p>
      </div>

      <!-- Pas connecté -->
      <div *ngIf="!user" class="no-session">
        <p>Connectez-vous pour voir vos tickets.</p>
        <a routerLink="/inscription" class="btn-primary">Créer un compte</a>
      </div>

      <!-- Mauvais rôle -->
      <div *ngIf="user && user.role !== 'CLIENT'" class="no-session">
        <p>Cette page est réservée aux clients.</p>
        <a routerLink="/organisateur" class="btn-primary">Mon tableau de bord</a>
      </div>

      <ng-container *ngIf="user && user.role === 'CLIENT'">
        <div *ngIf="loading" class="loading">Chargement de vos tickets...</div>
        <div *ngIf="erreur" class="error-msg">{{ erreur }}</div>

        <div *ngIf="!loading && tickets.length > 0">
          <div class="stats-bar">
            <span class="stat">{{ tickets.length }} ticket(s)</span>
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
                <div class="ticket-concert">{{ ticket.concert ? ticket.concert.lieu : 'Concert' }}</div>
                <div class="ticket-date">📅 {{ ticket.concert ? (ticket.concert.date | date:'dd/MM/yyyy HH:mm') : '' }}</div>
                <div class="ticket-achat">Acheté le {{ ticket.date_achat | date:'dd/MM/yyyy' }}</div>
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

        <div *ngIf="!loading && tickets.length === 0 && !erreur" class="empty-state">
          <p>Vous n'avez pas encore de ticket.</p>
          <a routerLink="/concerts" class="btn-primary">Voir les concerts</a>
        </div>
      </ng-container>
    </div>
  `,
  styles: [`
    .page-container { max-width: 900px; margin: 0 auto; padding: 2rem; }
    .page-header { margin-bottom: 2rem; }
    .page-header h1 { font-size: 2rem; color: #1a1a2e; margin-bottom: 0.3rem; }
    .subtitle { color: #888; font-size: 0.9rem; }

    .no-session { text-align: center; padding: 3rem; }
    .no-session p { color: #666; margin-bottom: 1.5rem; font-size: 1rem; }

    .stats-bar { display: flex; gap: 1.5rem; margin-bottom: 1.5rem; padding: 1rem; background: #f8f9fa; border-radius: 8px; flex-wrap: wrap; }
    .stat { font-weight: 600; color: #444; }
    .stat.actif { color: #2e7d32; }
    .stat.annule { color: #c62828; }

    .tickets-list { display: flex; flex-direction: column; gap: 1rem; }
    .ticket-card {
      display: flex; align-items: center; gap: 1.5rem; flex-wrap: wrap;
      background: #fff; border-radius: 12px; padding: 1.2rem 1.5rem;
      box-shadow: 0 2px 8px rgba(0,0,0,0.07); border: 1px solid #eee;
      border-left: 5px solid #4caf50;
    }
    .ticket-card.annule { border-left-color: #ef9a9a; opacity: 0.7; }
    .ticket-card.utilise { border-left-color: #bdbdbd; opacity: 0.6; }

    .ticket-left { display: flex; flex-direction: column; align-items: center; gap: 0.4rem; min-width: 80px; }
    .ticket-status { font-size: 0.7rem; font-weight: 700; padding: 0.2rem 0.5rem; border-radius: 20px; text-transform: uppercase; }
    .status-active { background: #e8f5e9; color: #2e7d32; }
    .status-annule { background: #fce4ec; color: #c62828; }
    .status-utilise { background: #f5f5f5; color: #757575; }
    .ticket-numero { font-weight: 700; color: #1a1a2e; font-size: 0.95rem; }

    .ticket-center { flex: 1; min-width: 200px; }
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

    .btn-primary {
      display: inline-block; text-decoration: none; text-align: center;
      background: #e94560; color: #fff; border: none; padding: 0.7rem 1.5rem;
      border-radius: 8px; cursor: pointer; font-weight: 700; font-size: 0.95rem;
    }
    .error-msg { color: #c62828; background: #fce4ec; padding: 0.8rem; border-radius: 8px; margin-bottom: 1rem; }
    .loading, .empty-state { text-align: center; padding: 3rem; color: #999; }
    .empty-state p { margin-bottom: 1.5rem; }
  `]
})
export class MesTicketsComponent implements OnInit {
  user: SessionUser | null = null;
  tickets: Ticket[] = [];
  loading = false;
  erreur: string | null = null;
  annulationEnCours: number | null = null;

  constructor(
    private clientService: ClientService,
    private ticketService: TicketService,
    private sessionService: SessionService
  ) {}

  ngOnInit(): void {
    this.user = this.sessionService.user;
    if (this.user?.role === 'CLIENT') {
      this.chargerTickets();
    }
  }

  chargerTickets(): void {
    if (!this.user) return;
    this.loading = true;
    this.clientService.getTickets(this.user.userId).subscribe({
      next: (data) => { this.tickets = data; this.loading = false; },
      error: () => { this.erreur = 'Impossible de charger vos tickets.'; this.loading = false; }
    });
  }

  annuler(ticket: Ticket): void {
    if (!confirm(`Annuler le ticket place ${ticket.numeroPlace} ?`)) return;
    this.annulationEnCours = ticket.ticketId;
    this.ticketService.annuler(ticket.ticketId).subscribe({
      next: () => { ticket.status = 'ANNULE'; this.annulationEnCours = null; },
      error: (err) => {
        this.erreur = err.error?.message || 'Erreur lors de l\'annulation.';
        this.annulationEnCours = null;
      }
    });
  }

  nbActifs(): number { return this.tickets.filter(t => t.status === 'ACTIVE').length; }
  nbAnnules(): number { return this.tickets.filter(t => t.status === 'ANNULE').length; }
}
