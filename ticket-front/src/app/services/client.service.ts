import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClientCreateDTO } from '../models/user.model';
import { Ticket } from '../models/ticket.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ClientService {
  private readonly url = `${environment.apiUrl}/clients`;

  constructor(private http: HttpClient) {}

  create(dto: ClientCreateDTO): Observable<number> {
    return this.http.post<number>(this.url, dto);
  }

  getTickets(clientId: number): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.url}/${clientId}/tickets`);
  }
}
