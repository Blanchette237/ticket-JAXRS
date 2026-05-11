export type TicketStatus = 'ACTIVE' | 'ANNULE' | 'UTILISE';

export interface Ticket {
  ticketId: number;
  numeroPlace: string;
  prixUnitaire: number;
  date_achat: string;
  status: TicketStatus;
  concert: {
    concertId: number;
    lieu: string;
    date: string;
  };
}

export interface TicketCreateDTO {
  utilisateurId: number;
  concertId: number;
  numeroPlace: string;
}
