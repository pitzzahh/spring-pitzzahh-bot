package tech.araopj.springpitzzahhbot.config.service;

import tech.araopj.springpitzzahhbot.config.DiscordBotConfig;
import org.springframework.stereotype.Service;

@Service
public record DiscordBotConfigService(DiscordBotConfig discordBotConfig) {
    public String getToken() {
        return discordBotConfig.getToken();
    }

}
