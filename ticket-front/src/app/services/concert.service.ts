import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Concert, ConcertCreateDTO } from '../models/concert.model';
import { environment } from '../../environments/environment';

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
