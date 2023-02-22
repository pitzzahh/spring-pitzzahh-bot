package tech.araopj.springpitzzahhbot.config.service;

import org.springframework.beans.factory.annotation.Autowired;
import tech.araopj.springpitzzahhbot.config.DiscordBotConfig;
import org.springframework.stereotype.Service;

@Service
public final class DiscordBotConfigService {

    private final DiscordBotConfig discordBotConfig;

    @Autowired
    public DiscordBotConfigService(DiscordBotConfig discordBotConfig) {
        this.discordBotConfig = discordBotConfig;
    }

    public String getToken() {
        return discordBotConfig.getToken();
    }

}
