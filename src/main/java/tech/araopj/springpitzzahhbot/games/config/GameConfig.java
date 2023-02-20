package tech.araopj.springpitzzahhbot.games.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class GameConfig {

    @Bean
    public Map<String, String> questions() {
        return new HashMap<>();
    }
}
