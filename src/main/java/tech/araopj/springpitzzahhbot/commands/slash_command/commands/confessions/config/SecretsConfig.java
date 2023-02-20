package tech.araopj.springpitzzahhbot.commands.slash_command.commands.confessions.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import lombok.Getter;
@Getter
@Configuration
public class SecretsConfig {

    @Value("${bot.channel.enter-confession-channel}")
    private String enterSecretChannel;

    @Value("${bot.channel.sent-confessions-channel}")
    private String sentSecretChannel;

}
