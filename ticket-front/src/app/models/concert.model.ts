export interface Concert {
  concertId: number;
  lieu: string;
  description: string;
  date: string;         // ISO string ex: "2025-12-01T20:00:00"
  popularite: number;
  capaciteMax: number;
  capacite: number;     // places disponibles
}

export interface ConcertCreateDTO {
  organiserId: number;
  lieu: string;
  description: string;
  dateTime: string;
  capacite: number;
  popularite: number;
}
