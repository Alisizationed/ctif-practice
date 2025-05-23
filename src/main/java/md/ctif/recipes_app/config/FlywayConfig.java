package md.ctif.recipes_app.config;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FlywayConfig {
    @Value("${spring.flyway.url}")
    private String url;
    @Value("${spring.flyway.user}")
    private String user;
    @Value("${spring.flyway.password}")
    private String password;

    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        return new Flyway(
                Flyway.configure()
                        .dataSource(url,user,password)
                        .baselineOnMigrate(true)
                        .locations("db/migration")
        );
    }
}

