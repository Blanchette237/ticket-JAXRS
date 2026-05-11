export type UserRole = 'CLIENT' | 'ORGANISATEUR';

export interface SessionUser {
  userId: number;
  name: string;
  firstname: string;
  email: string;
  role: UserRole;
}

export interface ClientCreateDTO {
  name: string;
  firstname: string;
  email: string;
  password: string;
}

export interface OrganiserCreateDTO {
  name: string;
  firstname: string;
  email: string;
  password: string;
}
