package tech.araopj.springpitzzahhbot.moderation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class ModerationConfig {

    @Bean
    public Map<String, Integer> violations() {
        return new HashMap<>();
    }

    @Bean
    public List<String> warnings() {
        return  Collections.emptyList();
    }
}
