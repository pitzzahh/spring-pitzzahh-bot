package tech.araopj.springpitzzahhbot;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import tech.araopj.springpitzzahhbot.commands.chat_command.CommandManager;
import tech.araopj.springpitzzahhbot.commands.service.CommandsService;
import tech.araopj.springpitzzahhbot.commands.slash_command.SlashCommandManager;
import tech.araopj.springpitzzahhbot.commands.slash_command.commands.confessions.Confession;
import tech.araopj.springpitzzahhbot.commands.slash_command.commands.confessions.service.ConfessionService;
import tech.araopj.springpitzzahhbot.commands.slash_command.commands.game.service.GameService;
import tech.araopj.springpitzzahhbot.config.DiscordBotConfig;
import tech.araopj.springpitzzahhbot.config.category.service.CategoryService;
import tech.araopj.springpitzzahhbot.config.channels.ChannelsConfig;
import tech.araopj.springpitzzahhbot.config.channels.service.ChannelService;
import tech.araopj.springpitzzahhbot.config.moderation.service.MessageCheckerService;
import tech.araopj.springpitzzahhbot.config.moderation.service.ViolationService;
import tech.araopj.springpitzzahhbot.listeners.ButtonListener;
import tech.araopj.springpitzzahhbot.listeners.MemberLogger;
import tech.araopj.springpitzzahhbot.listeners.MessageListener;
import tech.araopj.springpitzzahhbot.listeners.SlashCommandListener;
import tech.araopj.springpitzzahhbot.utilities.MessageUtil;
import javax.security.auth.login.LoginException;
import java.io.IOException;

@Slf4j
@AllArgsConstructor
@SpringBootApplication
public class SpringPitzzahhBotApplication {

    private final MessageCheckerService messageCheckerService;
    private final ConfessionService confessionService;
    private final DiscordBotConfig discordBotConfig;
    private final ViolationService violationService;
    private final CommandsService commandsService;
    private final CategoryService categoryService;
    private final CommandManager commandManager;
    private final ChannelService channelService;
    private final GameService gameService;
    private final MessageUtil messageUtil;
    private final Confession confession;

    public static void main(String[] args) {
        SpringApplication.run(SpringPitzzahhBotApplication.class, args);
    }

    @Bean
    public ShardManager shardManager() {
        log.info("Initializing DiscordBotConfig");
        var builder = DefaultShardManagerBuilder.createDefault(discordBotConfig.getToken());

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
                        confessionService,
                        violationService,
                        commandsService,
                        categoryService,
                        channelService,
                        commandManager,
                        messageUtil,
                        gameService,
                        confession
                ),
                new ButtonListener(messageUtil),
                new SlashCommandListener(
                        new SlashCommandManager(
                                confessionService,
                                commandsService,
                                channelService,
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
            return builder.build();
        } catch (LoginException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
