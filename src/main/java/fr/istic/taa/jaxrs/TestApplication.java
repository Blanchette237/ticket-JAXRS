/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.istic.taa.jaxrs;

import fr.istic.taa.jaxrs.rest.ClientResource;
import fr.istic.taa.jaxrs.rest.ConcertRessource;
import fr.istic.taa.jaxrs.rest.CorsFilter;
import fr.istic.taa.jaxrs.rest.OrganisateurResource;
import fr.istic.taa.jaxrs.rest.TicketResource;
import fr.istic.taa.jaxrs.rest.config;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class TestApplication extends Application {
	

    @Override
    public Set<Class<?>> getClasses() {

        final Set<Class<?>> clazzes = new HashSet<Class<?>>();

        clazzes.add(config.class);
        clazzes.add(CorsFilter.class);
        clazzes.add(OpenApiResource.class);
        clazzes.add(ClientResource.class);
        clazzes.add(OrganisateurResource.class);
        clazzes.add(ConcertRessource.class);
        clazzes.add(TicketResource.class);
//        clazzes.add(AcceptHeaderOpenApiResource.class);
         

        return clazzes;
    }

}
