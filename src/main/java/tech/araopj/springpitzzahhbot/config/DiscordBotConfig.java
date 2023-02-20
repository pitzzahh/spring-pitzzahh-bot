package tech.araopj.springpitzzahhbot.config;

import org.springframework.beans.factory.annotation.Value;
import tech.araopj.springpitzzahhbot.commands.slash_command.commands.confessions.service.SecretsService;
import tech.araopj.springpitzzahhbot.commands.slash_command.commands.game.service.GameService;
import tech.araopj.springpitzzahhbot.config.moderation.service.MessageCheckerService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import tech.araopj.springpitzzahhbot.commands.chat_command.CommandManager;
import tech.araopj.springpitzzahhbot.commands.service.CommandsService;
import tech.araopj.springpitzzahhbot.commands.slash_command.SlashCommandManager;
import tech.araopj.springpitzzahhbot.config.category.service.CategoryService;
import tech.araopj.springpitzzahhbot.config.channels.ChannelsConfig;
import tech.araopj.springpitzzahhbot.listeners.MemberLogger;
import tech.araopj.springpitzzahhbot.config.channels.service.ChannelService;
import tech.araopj.springpitzzahhbot.config.moderation.service.ViolationService;
import tech.araopj.springpitzzahhbot.utilities.MessageUtil;
import tech.araopj.springpitzzahhbot.listeners.*;
import javax.security.auth.login.LoginException;
import java.io.IOException;

@Slf4j
@Configuration
public class DiscordBotConfig {

    @Value("${bot.token}")
    private String token;

    private final ShardManager shardManager;

    public DiscordBotConfig(
            MessageCheckerService messageCheckerService,
            ViolationService violationService,
            CommandsService commandsService,
            CategoryService categoryService,
            CommandManager commandManager,
            ChannelService channelService,
            SecretsService secretsService,
            GameService gameService,
            MessageUtil messageUtil
    ) {
        log.debug("Initializing DiscordBotConfig");

        var builder = DefaultShardManagerBuilder.createDefault(token);

        builder.setStatus(OnlineStatus.ONLINE)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .setActivity(Activity.listening("your messages 📩"));

        try {
            messageCheckerService.loadSwearWords();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        builder.addEventListeners(
                new MessageListener(
                        messageCheckerService,
                        violationService,
                        commandsService,
                        categoryService,
                        channelService,
                        secretsService,
                        messageUtil,
                        gameService,
                        commandManager
                ),
                new ButtonListener(messageUtil),
                new SlashCommandListener(
                        new SlashCommandManager(
                                channelService,
                                secretsService,
                                gameService,
                                messageUtil
                        )
                ),
                new MemberLogger(
                        new ChannelsConfig(),
                        new ChannelService(new ChannelsConfig()),
                        new MessageUtil()
                )
        );

        try {
            this.shardManager = builder.build();
        } catch (LoginException e) {
            throw new RuntimeException(e);
        }

    }

    @Bean
    ShardManager shardManager() {
        return shardManager;
    }

}
