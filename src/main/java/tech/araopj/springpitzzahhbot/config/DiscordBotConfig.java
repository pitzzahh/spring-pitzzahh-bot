package tech.araopj.springpitzzahhbot.config;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import tech.araopj.springpitzzahhbot.listeners.MemberLogger;
import tech.araopj.springpitzzahhbot.listeners.SlashCommandListener;
import tech.araopj.springpitzzahhbot.moderation.MessageChecker;
import tech.araopj.springpitzzahhbot.service.ChannelService;
import tech.araopj.springpitzzahhbot.utilities.MessageUtil;

@Configuration
@AllArgsConstructor
public class DiscordBotConfig {

    @Value("${bot.token}")
    private String token;

    @Value("${bot.shardTotal}")
    private int shardTotal;

    @Value("${bot.shardId}")
    private int shardId;

    private final ShardManager shardManager;

    @SneakyThrows
    public DiscordBotConfig(MessageChecker messageChecker) {
        var builder = DefaultShardManagerBuilder.createDefault(token)
                .setStatus(OnlineStatus.ONLINE)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                .setActivity(Activity.listening("your messages 📩"))
                .setShardsTotal(shardTotal)
                .setShards(shardId);
        messageChecker.loadSwearWords();

        builder.addEventListeners(
                new MessageListener(),
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
