package md.ctif.recipes_app.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

public class KeycloakJwtRolesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    public static final String PREFIX_REALM_ROLE = "ROLE_realm_";
    public static final String PREFIX_RESOURCE_ROLE = "ROLE_";

    private static final String CLAIM_REALM_ACCESS = "realm_access";
    private static final String CLAIM_RESOURCE_ACCESS = "resource_access";
    private static final String CLAIM_ROLES = "roles";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Extract realm roles
        Map<String, Object> realmAccess = jwt.getClaimAsMap(CLAIM_REALM_ACCESS);
        if (realmAccess != null) {
            List<String> realmRoles = getRolesFromClaim(realmAccess);
            authorities.addAll(realmRoles.stream()
                    .map(role -> new SimpleGrantedAuthority(PREFIX_REALM_ROLE + role))
                    .collect(Collectors.toList()));
        }

        // Extract resource roles
        Map<String, Object> resourceAccess = jwt.getClaimAsMap(CLAIM_RESOURCE_ACCESS);
        if (resourceAccess != null) {
            resourceAccess.forEach((client, value) -> {
                if (value instanceof Map) {
                    Map<?, ?> resourceMap = (Map<?, ?>) value;
                    Object roles = resourceMap.get(CLAIM_ROLES);
                    if (roles instanceof Collection<?>) {
                        ((Collection<?>) roles).forEach(role -> {
                            if (role instanceof String) {
                                authorities.add(new SimpleGrantedAuthority(PREFIX_RESOURCE_ROLE + client + "_" + role));
                            }
                        });
                    }
                }
            });
        }

        return authorities;
    }

    private List<String> getRolesFromClaim(Map<String, Object> claim) {
        Object roles = claim.get(CLAIM_ROLES);
        if (roles instanceof Collection<?>) {
            return ((Collection<?>) roles).stream()
                    .filter(role -> role instanceof String)
                    .map(String.class::cast)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
