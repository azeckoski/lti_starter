/**
 * Copyright 2014 Unicon (R)
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
package ltistarter.oauth;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth.provider.ConsumerAuthentication;
import org.springframework.security.oauth.provider.ConsumerCredentials;
import org.springframework.security.oauth.provider.OAuthAuthenticationHandler;
import org.springframework.security.oauth.provider.token.OAuthAccessProviderToken;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;

@Component
public class MyOAuthAuthenticationHandler implements OAuthAuthenticationHandler {

    static final Logger log = LoggerFactory.getLogger(MyOAuthAuthenticationHandler.class);

    public static SimpleGrantedAuthority userGA = new SimpleGrantedAuthority("ROLE_USER");
    public static SimpleGrantedAuthority adminGA = new SimpleGrantedAuthority("ROLE_ADMIN");

    @PostConstruct
    public void init() {
        log.info("INIT");
    }

    @Override
    public Authentication createAuthentication(HttpServletRequest request, ConsumerAuthentication authentication, OAuthAccessProviderToken authToken) {
        Collection<GrantedAuthority> authorities = new HashSet<>(authentication.getAuthorities());
        // attempt to create a user Authority
        String username = request.getParameter("username");
        if (StringUtils.isBlank(username)) {
            username = authentication.getName();
        }

        // NOTE: you should replace this block with your real rules for determining OAUTH ADMIN roles
        if (username.equals("admin")) {
            authorities.add(userGA);
            authorities.add(adminGA);
        } else {
            authorities.add(userGA);
        }

        Principal principal = new NamedOAuthPrincipal(username, authorities,
                authentication.getConsumerCredentials().getConsumerKey(),
                authentication.getConsumerCredentials().getSignature(),
                authentication.getConsumerCredentials().getSignatureMethod(),
                authentication.getConsumerCredentials().getSignatureBaseString(),
                authentication.getConsumerCredentials().getToken()
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        log.info("createAuthentication generated auth principal (" + principal + "): req=" + request);
        return auth;
    }

    public static class NamedOAuthPrincipal extends ConsumerCredentials implements Principal {
        public String name;
        public Collection<GrantedAuthority> authorities;

        public NamedOAuthPrincipal(String name, Collection<GrantedAuthority> authorities, String consumerKey, String signature, String signatureMethod, String signatureBaseString, String token) {
            super(consumerKey, signature, signatureMethod, signatureBaseString, token);
            this.name = name;
            this.authorities = authorities;
        }

        @Override
        public String getName() {
            return name;
        }

        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String toString() {
            return "NamedOAuthPrincipal{" +
                    "name='" + name + '\'' +
                    ", key='" + getConsumerKey() + '\'' +
                    ", base='" + getSignatureBaseString() + '\'' +
                    "}";
        }
    }

}
