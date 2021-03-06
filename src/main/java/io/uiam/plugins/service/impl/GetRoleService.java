/*
 * uIAM - the IAM for microservices
 * 
 * Copyright (C) SoftInstigate Srl
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.uiam.plugins.service.impl;

import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.uiam.handlers.exchange.JsonRequest;
import io.uiam.handlers.PipedHttpHandler;
import static io.uiam.plugins.authentication.PluggableTokenManager.AUTH_TOKEN_HEADER;
import static io.uiam.plugins.authentication.PluggableTokenManager.AUTH_TOKEN_LOCATION_HEADER;
import static io.uiam.plugins.authentication.PluggableTokenManager.AUTH_TOKEN_VALID_HEADER;
import io.uiam.plugins.service.PluggableService;
import io.uiam.utils.HttpStatus;
import io.uiam.utils.URLUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

/**
 *
 * @author Andrea Di Cesare {@literal <andrea@softinstigate.com>}
 */
public class GetRoleService extends PluggableService {
    /**
     * Creates a new instance of GetRoleService
     *
     * @param next
     * @param args
     * @throws Exception
     */
    public GetRoleService(PipedHttpHandler next, 
            String name, 
            String uri, 
            Boolean secured, 
            Map<String, Object> args)
            throws Exception {
        super(next, name, uri, secured, args);

        if (args == null) {
            throw new IllegalArgumentException("args cannot be null");
        }
    }

    /**
     * Handles the request.
     *
     * @param exchange
     * @throws Exception
     */
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        var request = JsonRequest.wrap(exchange);

        if (request.isOptions()) {
            exchange.getResponseHeaders().put(HttpString.tryFromString("Access-Control-Allow-Methods"), "GET");
            exchange.getResponseHeaders().put(HttpString.tryFromString("Access-Control-Allow-Headers"),
                    "Accept, Accept-Encoding, Authorization, Content-Length, Content-Type, Host, Origin, X-Requested-With, User-Agent, No-Auth-Challenge, "
                    + AUTH_TOKEN_HEADER
                    + ", " + AUTH_TOKEN_VALID_HEADER
                    + ", " + AUTH_TOKEN_LOCATION_HEADER);
            exchange.setStatusCode(HttpStatus.SC_OK);
            exchange.endExchange();
        } else if (request.isGet()) {
            exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "application/json");

            if ((exchange.getSecurityContext() == null
                    || exchange.getSecurityContext().getAuthenticatedAccount() == null
                    || exchange.getSecurityContext().getAuthenticatedAccount().getPrincipal() == null)
                    || !(exchange.getRequestURI().equals(URLUtils.removeTrailingSlashes(getUri()) + "/"
                            + exchange.getSecurityContext().getAuthenticatedAccount().getPrincipal().getName()))) {
                {
                    exchange.setStatusCode(HttpStatus.SC_FORBIDDEN);

                    // REMOVE THE AUTH TOKEN HEADERS!!!!!!!!!!!
                    exchange.getResponseHeaders().remove(AUTH_TOKEN_HEADER);
                    exchange.getResponseHeaders().remove(AUTH_TOKEN_VALID_HEADER);
                    exchange.getResponseHeaders().remove(AUTH_TOKEN_LOCATION_HEADER);

                    exchange.endExchange();
                    return;
                }

            } else {
                JsonObject root = new JsonObject();

                Set<String> _roles = exchange.getSecurityContext().getAuthenticatedAccount().getRoles();

                JsonArray roles = new JsonArray();

                _roles.forEach((role) -> {
                    roles.add(new JsonPrimitive(role));
                });

                root.add("authenticated", new JsonPrimitive(true));
                root.add("roles", roles);

                exchange.getResponseSender().send(root.toString());
            }

            exchange.endExchange();
        } else {
            exchange.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
            exchange.endExchange();
        }
    }
}
