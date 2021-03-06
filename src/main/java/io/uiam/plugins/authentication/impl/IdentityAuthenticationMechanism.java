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
package io.uiam.plugins.authentication.impl;

import static io.uiam.plugins.ConfigurablePlugin.argValue;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Sets;

import io.uiam.plugins.PluginConfigurationException;
import io.uiam.plugins.authentication.PluggableAuthenticationMechanism;
import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.SecurityContext;
import io.undertow.security.idm.Account;
import io.undertow.server.HttpServerExchange;

/**
 * a simple IdentityAuthenticationMechanism to demonstrate how to plug a custom
 * AuthenticationMechanism
 *
 * it authenticates all requests against the configured IdentityManager using
 * the credentials specified in the configuration file
 *
 * @author Andrea Di Cesare <andrea@softinstigate.com>
 */
public class IdentityAuthenticationMechanism implements PluggableAuthenticationMechanism {
    private final String mechanismName;
    private final String username;
    private final List<String> roles;

    public IdentityAuthenticationMechanism(String mechanismName, Map<String, Object> args)
            throws PluginConfigurationException {
        this.mechanismName = mechanismName;
        this.username = argValue(args, "username");
        this.roles = argValue(args, "roles");

    }

    @Override
    public AuthenticationMechanism.AuthenticationMechanismOutcome authenticate(HttpServerExchange exchange,
            SecurityContext securityContext) {
        Account sa = new BaseAccount(username, Sets.newTreeSet(roles));

        securityContext.authenticationComplete(sa, "IdentityAuthenticationManager", true);
        return AuthenticationMechanism.AuthenticationMechanismOutcome.AUTHENTICATED;
    }

    @Override
    public AuthenticationMechanism.ChallengeResult sendChallenge(HttpServerExchange exchange,
            SecurityContext securityContext) {
        return new AuthenticationMechanism.ChallengeResult(true, 200);
    }

    /**
     * @return the mechanismName
     */
    public String getMechanismName() {
        return mechanismName;
    }
}
