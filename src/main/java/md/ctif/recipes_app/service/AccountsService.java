package md.ctif.recipes_app.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
public class AccountsService {
    @Value("${accounts.favourites-endpoint}")
    private String favouritesEndpoint;
    @Autowired
    private WebClient webClient;

    public Flux<Long> getFavouriteRecipes(String id) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(JwtAuthenticationToken.class)
                .map(jwtAuth -> jwtAuth.getToken().getTokenValue())
                .flatMapMany(token -> webClient.get()
                        .uri(favouritesEndpoint, id)
                        .headers(headers -> headers.setBearerAuth(token))
                        .retrieve()
                        .bodyToFlux(Long.class)
                );
    }
}
