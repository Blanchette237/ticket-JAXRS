import { Routes } from '@angular/router';
import { ConcertListComponent } from './components/concert-list/concert-list.component';
import { ConcertDetailComponent } from './components/concert-detail/concert-detail.component';
import { MesTicketsComponent } from './components/mes-tickets/mes-tickets.component';

export const routes: Routes = [
  { path: '', redirectTo: 'concerts', pathMatch: 'full' },
  { path: 'concerts', component: ConcertListComponent },
  { path: 'concerts/:id', component: ConcertDetailComponent },
  { path: 'mes-tickets', component: MesTicketsComponent },
  { path: '**', redirectTo: 'concerts' }
];
