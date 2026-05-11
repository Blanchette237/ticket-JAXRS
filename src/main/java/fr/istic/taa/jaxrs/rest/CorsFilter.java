package fr.istic.taa.jaxrs.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * Filtre CORS (Cross-Origin Resource Sharing).
 *
 * Pourquoi ce filtre est indispensable :
 * Le frontend Angular tourne sur http://localhost:4200
 * et le backend sur http://localhost:8080.
 * Par sécurité, les navigateurs bloquent par défaut les requêtes HTTP
 * vers un domaine ou port différent de celui de la page courante.
 * Ce filtre ajoute les en-têtes HTTP qui autorisent ces appels cross-port.
 *
 * Sans ce filtre : le navigateur affiche "CORS policy: No 'Access-Control-Allow-Origin'"
 * et l'application Angular ne peut pas appeler l'API.
 *
 * En production, remplacer "*" par l'URL exacte du frontend
 * (ex: "https://mon-app.example.com") pour des raisons de sécurité.
 *
 * @Provider indique à RESTEasy d'enregistrer automatiquement ce filtre.
 */
@Provider
public class CorsFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext request, ContainerResponseContext response) {
		// Autorise les requêtes depuis n'importe quelle origine
		response.getHeaders().add("Access-Control-Allow-Origin", "*");
		// Méthodes HTTP autorisées
		response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		// En-têtes que le frontend peut envoyer
		response.getHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
		// Durée de mise en cache de la vérification CORS (en secondes)
		response.getHeaders().add("Access-Control-Max-Age", "86400");
	}
}
