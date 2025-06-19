package md.ctif.recipes_app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AccountsService {
    @Value("${accounts.favourites-endpoint}")
    private String favouritesEndpoint;
    @Value("${accounts.favourites-endpoint-v2}")
    private String favouritesEndpointV2;
    @Value("${accounts.picture-endpoint}")
    private String pictureEndpoint;
    @Value("${server.port}")
    private String serverPort;
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

    public Flux<Long> getFavouriteRecipesPageable(String id, Integer page, Integer size) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(JwtAuthenticationToken.class)
                .map(jwtAuth -> jwtAuth.getToken().getTokenValue())
                .flatMapMany(token -> webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(favouritesEndpointV2)
                                .queryParam("offset", page * size)
                                .queryParam("limit", size)
                                .build(id)
                        )
                        .headers(headers -> headers.setBearerAuth(token))
                        .retrieve()
                        .bodyToFlux(Long.class)
                );
    }

    public Mono<String> setProfilePicture(String id, String profilePicture) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(JwtAuthenticationToken.class)
                .map(jwtAuth -> jwtAuth.getToken().getTokenValue())
                .flatMap(token -> webClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path(pictureEndpoint)
                                .build(id)
                        )
                        .headers(headers -> headers.setBearerAuth(token))
                        .bodyValue("https://localhost:" + serverPort + "/api/recipe/images/v2/" + profilePicture)
                        .retrieve()
                        .bodyToMono(String.class)
                );
    }
}
