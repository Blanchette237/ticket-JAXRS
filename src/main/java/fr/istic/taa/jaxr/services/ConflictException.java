package fr.istic.taa.jaxr.services;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class ConflictException extends WebApplicationException {
    public ConflictException(String message) {
        super(Response.status(Response.Status.CONFLICT)
                .entity(message)
                .type(MediaType.TEXT_PLAIN_TYPE)
                .build());
    }
}
