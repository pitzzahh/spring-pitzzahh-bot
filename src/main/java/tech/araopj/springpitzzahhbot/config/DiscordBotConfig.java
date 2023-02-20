package tech.araopj.springpitzzahhbot.config;

import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import tech.araopj.springpitzzahhbot.commands.chat_command.CommandManager;
import tech.araopj.springpitzzahhbot.games.service.GameService;
import tech.araopj.springpitzzahhbot.listeners.MemberLogger;
import tech.araopj.springpitzzahhbot.moderation.service.MessageCheckerService;
import tech.araopj.springpitzzahhbot.service.ChannelService;
import tech.araopj.springpitzzahhbot.service.ViolationService;
import tech.araopj.springpitzzahhbot.utilities.MessageUtil;
import tech.araopj.springpitzzahhbot.listeners.*;

@Configuration
public class DiscordBotConfig {

    @Value("${bot.token}")
    private String token;

    @Value("${bot.shardTotal}")
    private int shardTotal;

    @Value("${bot.shardId}")
    private int shardId;

    private final ShardManager shardManager;
    private final CommandsConfiguration commandsConfiguration;
    private final ChannelsConfiguration channelsConfiguration;
    private final MessageCheckerService messageCheckerService;
    private final ViolationService violationService;
    private final CommandManager commandManager;
    private final ChannelService channelService;
    private final GameService gameService;
    private final MessageUtil messageUtil;

    @SneakyThrows
    public DiscordBotConfig(
            MessageCheckerService messageCheckerService,
            CommandManager commandManager,
            CommandsConfiguration commandsConfiguration,
            ChannelsConfiguration channelsConfiguration,
            MessageCheckerService messageCheckerService1,
            GameService gameService,
            ViolationService violationService,
            MessageUtil messageUtil,
            ChannelService channelService
    ) {
        this.commandManager = commandManager;
        this.commandsConfiguration = commandsConfiguration;
        this.channelsConfiguration = channelsConfiguration;
        this.messageCheckerService = messageCheckerService1;
        this.gameService = gameService;
        this.violationService = violationService;
        this.messageUtil = messageUtil;
        this.channelService = channelService;
        var builder = DefaultShardManagerBuilder.createDefault(token)
                .setStatus(OnlineStatus.ONLINE)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                .setActivity(Activity.listening("your messages 📩"))
                .setShardsTotal(shardTotal)
                .setShards(shardId);
        messageCheckerService.loadSwearWords();

        builder.addEventListeners(
                new MessageListener(commandManager, commandsConfiguration, channelsConfiguration, messageCheckerService, violationService, gameService, messageUtil),
                new ButtonListener(),
                new SlashCommandListener(),
                new MemberLogger(
                        new ChannelsConfiguration(),
                        new ChannelService(new ChannelsConfiguration(), this),
                        new MessageUtil()
                )
        );

        this.shardManager = builder.build();

    }

    @Bean
    public JDA jda() {
        return shardManager.getShardById(shardId);
    }
}
