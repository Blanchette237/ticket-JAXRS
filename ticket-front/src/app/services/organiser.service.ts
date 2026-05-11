import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OrganiserCreateDTO } from '../models/user.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class OrganiserService {
  private readonly url = `${environment.apiUrl}/organisateurs`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<any[]> {
    return this.http.get<any[]>(this.url);
  }

  create(dto: OrganiserCreateDTO): Observable<number> {
    return this.http.post<number>(this.url, dto);
  }
}
