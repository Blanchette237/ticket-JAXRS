package fr.istic.taa.jaxrs.rest;

import fr.istic.taa.jaxr.dto.ConcertCreateDTO;
import fr.istic.taa.jaxr.services.ConcertService;
import fr.istic.taa.jaxrs.domain.Concert;
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

@Path("/concerts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Concerts", description = "Gestion des concerts")
public class ConcertRessource {

	private final ConcertService service = new ConcertService();

	@GET
	@Operation(summary = "Récupérer tous les concerts")
	@ApiResponse(responseCode = "200", description = "Liste des concerts")
	public Response getAllConcerts() {
		List<Concert> concerts = service.findAll();
		return Response.ok(concerts).build();
	}

	@GET
	@Path("/{concertId}")
	@Operation(summary = "Récupérer un concert par son id")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Concert trouvé",
					content = @Content(schema = @Schema(implementation = Concert.class))),
			@ApiResponse(responseCode = "404", description = "Concert non trouvé")
	})
	public Response getConcertById(@PathParam("concertId") Long concertId) {
		Concert concert = service.findOne(concertId);
		if (concert == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		return Response.ok(concert).build();
	}

	@POST
	@Operation(summary = "Créer un concert")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Concert créé"),
			@ApiResponse(responseCode = "400", description = "Données invalides")
	})
	public Response addConcert(ConcertCreateDTO dto) {
		long id = service.create(dto);
		return Response.created(URI.create("/concerts/" + id)).build();
	}

	@DELETE
	@Path("/{concertId}")
	@Operation(summary = "Supprimer un concert")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Concert supprimé"),
			@ApiResponse(responseCode = "404", description = "Concert non trouvé")
	})
	public Response deleteConcert(@PathParam("concertId") Long concertId) {
		service.delete(concertId);
		return Response.noContent().build();
	}
}
