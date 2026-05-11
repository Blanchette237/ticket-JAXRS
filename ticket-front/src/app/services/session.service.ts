import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { SessionUser } from '../models/user.model';

const SESSION_KEY = 'ticketapp_user';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private userSubject = new BehaviorSubject<SessionUser | null>(this.load());
  user$ = this.userSubject.asObservable();

  get user(): SessionUser | null {
    return this.userSubject.value;
  }

  login(user: SessionUser): void {
    localStorage.setItem(SESSION_KEY, JSON.stringify(user));
    this.userSubject.next(user);
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
      return null;
    }
  }
}
