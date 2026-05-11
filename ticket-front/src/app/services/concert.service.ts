import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Concert, ConcertCreateDTO } from '../models/concert.model';
import { environment } from '../../environments/environment';

/**
 * Service Angular de communication avec l'API /concerts.
 *
 * Pourquoi Injectable({ providedIn: 'root' }) ?
 * Cela crée une seule instance partagée par toute l'application (singleton).
 * Tous les composants qui injectent ConcertService reçoivent la même instance.
 *
 * Pourquoi retourner des Observable et non des Promises ?
 * Les Observables RxJS permettent d'annuler une requête en cours, de chaîner
 * des opérateurs (map, catchError...) et de gérer les flux de données réactifs.
 * Angular HTTP est construit sur RxJS — c'est le standard du framework.
 *
 * L'URL de base est lue depuis environment.ts : un seul endroit à changer
 * pour pointer vers un serveur de production.
 */
@Injectable({ providedIn: 'root' })
export class ConcertService {
  private readonly url = `${environment.apiUrl}/concerts`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Concert[]> {
    return this.http.get<Concert[]>(this.url);
  }

  getById(id: number): Observable<Concert> {
    return this.http.get<Concert>(`${this.url}/${id}`);
  }

  create(dto: ConcertCreateDTO): Observable<void> {
    return this.http.post<void>(this.url, dto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
