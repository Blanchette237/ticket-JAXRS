import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { SessionUser } from '../models/user.model';

const SESSION_KEY = 'ticketapp_user';

/**
 * Gère la session de l'utilisateur connecté.
 *
 * Pourquoi BehaviorSubject ?
 * Un BehaviorSubject est un Observable qui :
 *   - conserve toujours sa dernière valeur (contrairement à un Subject simple)
 *   - notifie instantanément tout composant abonné quand la valeur change
 * Ici, quand un utilisateur se connecte, la navbar se met à jour immédiatement
 * sans rechargement, car elle est abonnée à user$ via ngOnInit.
 *
 * Pourquoi localStorage ?
 * localStorage persiste les données entre les rechargements de page et
 * les fermetures d'onglet. Sans lui, la session serait perdue à chaque F5.
 * La méthode load() relit le localStorage au démarrage pour restaurer la session.
 */
@Injectable({ providedIn: 'root' })
export class SessionService {
  // Valeur initiale : lire le localStorage au démarrage (restaure la session après F5)
  private userSubject = new BehaviorSubject<SessionUser | null>(this.load());

  // Observable public — les composants s'y abonnent pour réagir aux changements
  user$ = this.userSubject.asObservable();

  // Accès synchrone à la valeur courante (utile dans ngOnInit sans subscribe)
  get user(): SessionUser | null {
    return this.userSubject.value;
  }

  login(user: SessionUser): void {
    localStorage.setItem(SESSION_KEY, JSON.stringify(user));
    this.userSubject.next(user); // notifie tous les composants abonnés
  }

  logout(): void {
    localStorage.removeItem(SESSION_KEY);
    this.userSubject.next(null);
  }

  private load(): SessionUser | null {
    try {
      const raw = localStorage.getItem(SESSION_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      // En cas de données corrompues dans le localStorage, on ignore silencieusement
      return null;
    }
  }
}
