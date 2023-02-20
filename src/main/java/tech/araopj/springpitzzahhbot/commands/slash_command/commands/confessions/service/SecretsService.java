package tech.araopj.springpitzzahhbot.commands.slash_command.commands.confessions.service;

import tech.araopj.springpitzzahhbot.commands.slash_command.commands.confessions.config.SecretsConfig;
import org.springframework.stereotype.Service;

@Service
public record SecretsService(SecretsConfig secretsConfig) {

    public String enterSecretChannelName() {
        return secretsConfig.getEnterSecretChannel();
    }

    public String sentSecretChannelName() {
        return secretsConfig.getSentSecretChannel();
    }


}
