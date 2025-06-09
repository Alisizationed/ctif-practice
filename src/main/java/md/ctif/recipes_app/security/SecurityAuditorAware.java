package md.ctif.recipes_app.security;

import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

@Component
@EnableR2dbcAuditing(auditorAwareRef = "securityAuditorAware")
public class SecurityAuditorAware implements ReactiveAuditorAware<String> {
    @Override
    @NonNull
    public Mono<String> getCurrentAuditor() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(auth -> {
                    if (auth.getPrincipal() instanceof Jwt jwt) {
                        return jwt.getClaimAsString("sub");
                    }
                    return auth.getName();
                });
    }
}

