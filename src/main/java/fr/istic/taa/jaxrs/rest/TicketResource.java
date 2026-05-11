package fr.istic.taa.jaxrs.rest;

import fr.istic.taa.jaxr.dto.TicketCreateDTO;
import fr.istic.taa.jaxr.services.TicketService;
import fr.istic.taa.jaxrs.domain.Ticket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/tickets")
@Produces({"application/json"})
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Tickets" , description = "Gestion des tickets")
public class TicketResource {

	private final TicketService service = new TicketService();



	@GET
	@Operation(summary = "Récupérer tous les tickets")
	@ApiResponse(responseCode = "200", description = "Listes des tickets")
	public Response getAllTickets(){
		List<Ticket> tickets = service.findAll();
		return Response.ok(tickets).build();
	}
	@GET
	@Path("/{ticketId}")
	@Operation(summary = "Récupérer un ticket par l'id")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Ticket trouvé",
					content = @Content(schema = @Schema(implementation = Ticket.class))),
			@ApiResponse(responseCode = "404", description = "Ticket non trouvé")
	})
	public Ticket getTicketById(@PathParam("ticketId") Long ticketId) {

		return service.findOne(ticketId);
	}


	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary = "Créer un ticket")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Ticket crée",
					content = @Content(schema = @Schema(implementation = Ticket.class))),
			@ApiResponse(responseCode = "404", description = "Ticket invalide")
	})
	public Response createTicket(TicketCreateDTO dto){
		long id = service.create(dto);
		return Response.created(URI.create("/tickets/" +id)).build();
	}


	@DELETE
	@Path("/{id}/annuler")
	@Operation(summary = "Annuler un ticket")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Ticket annulé",
					content = @Content(schema = @Schema(implementation = Ticket.class))),
			@ApiResponse(responseCode = "404", description = "Ticket non trouvé")
	})
	public Response annulerTicket(@PathParam("id") Long id){
		service.annuler(id);
		return Response.noContent().build();
	}

}
