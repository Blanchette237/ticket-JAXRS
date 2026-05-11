package fr.istic.taa.jaxrs.rest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "API Ticket",
                version = "1.0",
                description = "Documentation de l'API de gestion des tickets",
                contact = @Contact(name = "Blanche"),
                license = @License(name = "Apache 2.0")
        ),
        servers = {
                @Server(url= "http://localhost/8080", description = "Serveur local")
        }
)

public class config {
}
