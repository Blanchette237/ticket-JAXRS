package fr.istic.taa.jaxrs.rest;

import fr.istic.taa.jaxr.dto.ClientCreateDTO;
import fr.istic.taa.jaxr.services.ClientService;
import fr.istic.taa.jaxrs.domain.Client;
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

@Path("/clients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Clients", description = "Gestion des clients")
public class ClientResource {

    private final ClientService service = new ClientService();

    @GET
    @Operation(summary = "Lister tous les clients")
    @ApiResponse(responseCode = "200", description = "Liste des clients")
    public Response getAll() {
        List<Client> clients = service.findAll();
        return Response.ok(clients).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Récupérer un client par son id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client trouvé",
                    content = @Content(schema = @Schema(implementation = Client.class))),
            @ApiResponse(responseCode = "404", description = "Client non trouvé")
    })
    public Response getById(@PathParam("id") Long id) {
        try {
            return Response.ok(service.findOne(id)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Operation(summary = "Créer un client")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Client créé"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public Response create(ClientCreateDTO dto) {
        long id = service.create(dto);
        return Response.created(URI.create("/clients/" + id)).entity(id).build();
    }

    @GET
    @Path("/{id}/tickets")
    @Operation(summary = "Récupérer les tickets d'un client")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets du client"),
            @ApiResponse(responseCode = "404", description = "Client non trouvé")
    })
    public Response getTicketsByClient(@PathParam("id") Long id) {
        try {
            List<Ticket> tickets = service.findTicketsByClient(id);
            return Response.ok(tickets).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
