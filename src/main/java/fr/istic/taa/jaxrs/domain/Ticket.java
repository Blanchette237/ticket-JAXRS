package fr.istic.taa.jaxrs.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name ="ticket")
public class Ticket implements Serializable{
	@Id
	@GeneratedValue
	private Long TicketId;
	
	@ManyToOne
	@JoinColumn(name = "UserId")
	@JsonBackReference
	private Client client;
	
	@ManyToOne
    @JoinColumn(name = "ConcertId", nullable = false)
    private Concert concert;
	
	@Enumerated(EnumType.STRING)
	private TicketStatus status;
	
	private String NumeroPlace;
	
	private double prixUnitaire;
	
	private LocalDateTime Date_achat;

	public Ticket() {}

	public Long getTicketId() {
		return TicketId;
	}
	public Ticket(Long ticketId, Client client, Concert concert, TicketStatus status) {
		super();
		TicketId = ticketId;
		this.client = client;
		this.concert = concert;
		this.status = status;
	}
	public void setTicketId(Long ticketId) {
		TicketId = ticketId;
	}
	public Concert getConcert() {
		return concert;
	}
	public void setConcert(Concert concert) {
		this.concert = concert;
	}
	public Client getClient() {
		return client;
	}
	public void setClient(Client client) {
		this.client = client;
	}
	public TicketStatus getStatus() {
		return status;
	}
	public void setStatus(TicketStatus status) {
		this.status = status;
	}
	public LocalDateTime getDate_achat() {
		return Date_achat;
	}
	public void setDate_achat(LocalDateTime date_achat) {
		Date_achat = date_achat;
	}
	public String getNumeroPlace() {
		return NumeroPlace;
	}
	public void setNumeroPlace(String numeroPlace) {
		NumeroPlace = numeroPlace;
	}
	
	public double getPrixUnitaire() {
		return prixUnitaire;
	}
	public void setPrixUnitaire(double prixUnitaire) {
		this.prixUnitaire = prixUnitaire;
	}
	public void setPrixUnitaire(Double prixUnitaire) {
		this.prixUnitaire = prixUnitaire;
	}

}
