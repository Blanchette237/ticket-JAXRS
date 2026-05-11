package fr.istic.taa.jaxrs.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "concert")
@NamedQuery(name = "Concert.findByLieu", query = "SELECT c from Concert c where c.lieu = :lieu")
public class Concert implements Serializable {
	@Id
	@GeneratedValue
	private Long ConcertId;
	private String lieu;
	private String Description;
	private LocalDateTime date;
	private Double popularite;

	private double capaciteMax; // nombre total de places (immuable après création)
	private double capacite;    // places disponibles (décrémentées à chaque vente)

	@ManyToOne
	@JoinColumn(name = "UserId", nullable = false)
	private Organiser organiser;

	@OneToMany(mappedBy = "concert", cascade = CascadeType.ALL)
	@JsonBackReference
	private List<Ticket> ticketsVendus = new ArrayList<>();

	public Long getConcertId() {
		return ConcertId;
	}
	public void setConcertId(Long concertId) {
		this.ConcertId = concertId;
	}
	public List<Ticket> getTicketsVendus() {
		return ticketsVendus;
	}
	public void setTicketsVendus(List<Ticket> ticketsVendus) {
		this.ticketsVendus = ticketsVendus;
	}
	public Organiser getOrganiser() {
		return organiser;
	}
	public void setOrganiser(Organiser organiser) {
		this.organiser = organiser;
	}
	public double getCapacite() {
		return capacite;
	}
	public void setCapacite(double capacite) {
		this.capacite = capacite;
	}
	public String getLieu() {
		return lieu;
	}
	public void setLieu(String lieu) {
		this.lieu = lieu;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	public double getCapaciteMax() {
		return capaciteMax;
	}
	public void setCapaciteMax(double capaciteMax) {
		this.capaciteMax = capaciteMax;
	}
	public LocalDateTime getDate() {
		return date;
	}
	public void setDate(LocalDateTime date) {
		this.date = date;
	}
	public Double getPopularite() {
		return popularite;
	}
	public void setPopularite(Double popularite) {
		this.popularite = popularite;
	}
}
