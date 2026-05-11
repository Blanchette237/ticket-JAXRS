import { Routes } from '@angular/router';
import { ConcertListComponent } from './components/concert-list/concert-list.component';
import { ConcertDetailComponent } from './components/concert-detail/concert-detail.component';
import { MesTicketsComponent } from './components/mes-tickets/mes-tickets.component';
import { InscriptionComponent } from './components/inscription/inscription.component';
import { OrganisateurDashboardComponent } from './components/organisateur-dashboard/organisateur-dashboard.component';

export const routes: Routes = [
  { path: '', redirectTo: 'concerts', pathMatch: 'full' },
  { path: 'concerts', component: ConcertListComponent },
  { path: 'concerts/:id', component: ConcertDetailComponent },
  { path: 'mes-tickets', component: MesTicketsComponent },
  { path: 'inscription', component: InscriptionComponent },
  { path: 'organisateur', component: OrganisateurDashboardComponent },
  { path: '**', redirectTo: 'concerts' }
];
