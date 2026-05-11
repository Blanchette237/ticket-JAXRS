package fr.istic.taa.jaxrs.rest;

import fr.istic.taa.jaxr.dto.OrganiserCreateDTO;
import fr.istic.taa.jaxr.services.OrganiserService;
import fr.istic.taa.jaxrs.domain.Organiser;
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

@Path("/organisateurs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Organisateurs", description = "Gestion des organisateurs de concerts")
public class OrganisateurResource {

    private final OrganiserService service = new OrganiserService();

    @GET
    @Operation(summary = "Lister tous les organisateurs")
    @ApiResponse(responseCode = "200", description = "Liste des organisateurs")
    public Response getAll() {
        List<Organiser> organisateurs = service.findAll();
        return Response.ok(organisateurs).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Récupérer un organisateur par son id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organisateur trouvé",
                    content = @Content(schema = @Schema(implementation = Organiser.class))),
            @ApiResponse(responseCode = "404", description = "Organisateur non trouvé")
    })
    public Response getById(@PathParam("id") Long id) {
        return Response.ok(service.findOne(id)).build();
    }

    @POST
    @Operation(summary = "Créer un organisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Organisateur créé"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public Response create(OrganiserCreateDTO dto) {
        long id = service.create(dto);
        return Response.created(URI.create("/organisateurs/" + id)).entity(id).build();
    }
}
