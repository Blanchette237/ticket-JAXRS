import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="navbar">
      <div class="navbar-brand">
        <span class="logo">🎵</span>
        <span class="brand-name">TicketApp</span>
      </div>
      <ul class="navbar-links">
        <li><a routerLink="/concerts" routerLinkActive="active">Concerts</a></li>
        <li><a routerLink="/mes-tickets" routerLinkActive="active">Mes tickets</a></li>
      </ul>
    </nav>
  `,
  styles: [`
    .navbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      background: #1a1a2e;
      padding: 0 2rem;
      height: 64px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.3);
    }
    .navbar-brand {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: #fff;
      font-size: 1.3rem;
      font-weight: 700;
    }
    .logo { font-size: 1.5rem; }
    .navbar-links {
      list-style: none;
      display: flex;
      gap: 2rem;
      margin: 0;
      padding: 0;
    }
    .navbar-links a {
      color: #ccc;
      text-decoration: none;
      font-weight: 500;
      padding: 0.3rem 0.6rem;
      border-radius: 6px;
      transition: all 0.2s;
    }
    .navbar-links a:hover,
    .navbar-links a.active {
      color: #e94560;
      background: rgba(233,69,96,0.1);
    }
  `]
})
export class NavbarComponent {}
