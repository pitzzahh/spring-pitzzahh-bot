/*
 * MIT License
 *
 * Copyright (c) 2022 pitzzahh
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package tech.araopj.springpitzzahhbot.listeners;

import static io.github.pitzzahh.util.utilities.validation.Validator.isDecimalNumber;
import static io.github.pitzzahh.util.utilities.validation.Validator.isWholeNumber;
import static net.dv8tion.jda.api.interactions.components.buttons.Button.primary;
import static net.dv8tion.jda.api.interactions.components.ActionRow.of;
import static java.time.format.DateTimeFormatter.ofLocalizedTime;
import static java.time.format.FormatStyle.SHORT;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import tech.araopj.springpitzzahhbot.commands.chat_command.CommandManager;
import tech.araopj.springpitzzahhbot.config.ChannelsConfiguration;
import tech.araopj.springpitzzahhbot.config.CommandsConfiguration;
import tech.araopj.springpitzzahhbot.games.service.GameService;
import tech.araopj.springpitzzahhbot.moderation.service.MessageCheckerService;
import tech.araopj.springpitzzahhbot.service.ViolationService;
import tech.araopj.springpitzzahhbot.utilities.MessageUtil;
import java.util.Objects;
import static java.awt.Color.*;
import static java.lang.String.format;
import static java.time.Clock.systemDefaultZone;
import static java.time.LocalDateTime.now;
import static java.time.ZoneId.of;

/**
 * Class that listens to messages on text channels.
 */
@Slf4j
@Component
@AllArgsConstructor
public class MessageListener extends ListenerAdapter {

    private final CommandManager MANAGER;
    private final CommandsConfiguration commandsConfiguration;
    private final ChannelsConfiguration channelsConfiguration;
    private final MessageCheckerService messageCheckerService;
    private final ViolationService violationService;
    private final GameService gameService;
    private final MessageUtil messageUtil;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        final var AUTHOR = event.getAuthor();
        final var PREFIX = commandsConfiguration.getPrefix();
        final var MESSAGE = event.getMessage().getContentRaw();
        if (MESSAGE.startsWith(PREFIX)) MANAGER.handle(event);
        else {
            if (MESSAGE.equals(commandsConfiguration.getVerifyCommand())) {
                final var BUTTON = primary("verify-button", "Verify");
                messageUtil.getEmbedBuilder().clear()
                        .clearFields()
                        .setColor(BLUE)
                        .setTitle("Verify yourself")
                        .appendDescription("Click the verify button to verify")
                        .setTimestamp(now(of("UTC")))
                        .setFooter(
                                format("Created by %s", event.getJDA().getSelfUser().getAsTag()),
                                event.getJDA().getSelfUser().getAvatarUrl()
                        );
                messageUtil.getMessageBuilder().clear()
                        .setActionRows(of(BUTTON))
                        .setEmbeds(messageUtil.getEmbedBuilder().build());
                event.getGuild()
                        .createTextChannel(channelsConfiguration.getVerifyChannelName())
                        .queue(c -> c.sendMessage(messageUtil.getMessageBuilder().build()).queue());
            }
            else {
                var sentSecretChannel = channelsConfiguration.getSentSecretChannel();
                if (MESSAGE.equals(commandsConfiguration.getSecretsCommand()) && Objects.requireNonNull(event.getMember()).isOwner()){
                    event.getGuild().createCategory(channelsConfiguration.getCreateSecretCategory())
                            .syncPermissionOverrides()
                            .queue(
                                    category -> {
                                        messageUtil.getEmbedBuilder().clear()
                                                .clearFields()
                                                .setColor(CYAN)
                                                .setTitle("Write your secret here")
                                                .setDescription("your secret will be anonymous")
                                                .appendDescription(", use `/secret` to tell a secret")
                                                .setFooter(
                                                        format("Created by %s", event.getJDA().getSelfUser().getAsTag()),
                                                        category.getJDA().getSelfUser().getAvatarUrl()
                                                );
                                        category.createTextChannel(channelsConfiguration.getEnterSecretChannel())
                                                .queue(c -> c.sendMessageEmbeds(messageUtil.getEmbedBuilder().build()).queue());
                                        category.createTextChannel(sentSecretChannel)
                                                .queue();
                            }
                    );
                } else {
                    if (event.getChannel().getName().equals(sentSecretChannel) && !event.getAuthor().isBot()) {
                        messageUtil.getEmbedBuilder().clear()
                                .clearFields()
                                .setColor(RED)
                                .appendDescription("Please use `/secret` to tell a secret")
                                .setTimestamp(now(of("UTC")).plusSeconds(10))
                                .setFooter("This message will be automatically deleted on");
                        event.getMessage()
                                .replyEmbeds(messageUtil.getEmbedBuilder().build())
                                .queue(e -> e.delete().queueAfter(5, SECONDS));
                        event.getMessage().delete().queue();
                    }

                    // TODO: refactor embedded messages, remove code and effort duplication
                    else if (!AUTHOR.isBot()){
                        var contains = messageCheckerService.searchForBadWord(event.getMessage().getContentRaw());
                        log.debug("is bad word = " + contains);
                        if (contains && !AUTHOR.isBot()) {
                            violationService.addViolation(AUTHOR.getName());
                            var isVeryBad = violationService.violatedThreeTimes(AUTHOR.getName());
                            if (isVeryBad) {
                                messageUtil.getEmbedBuilder().clear()
                                        .clearFields()
                                        .setColor(RED)
                                        .setTitle("Violated Three Times")
                                        .appendDescription(
                                                format(
                                                        AUTHOR.getAsMention().concat(" Cannot send messages until %s"),
                                                        now(systemDefaultZone())
                                                                .plusMinutes(5)
                                                                .format(ofLocalizedTime(SHORT))
                                                )
                                        )
                                        .setFooter(
                                                format("Scanned by %s", event.getJDA().getSelfUser().getAsTag()),
                                                event.getJDA().getSelfUser().getAvatarUrl()
                                        );
                                event.getChannel()
                                        .sendMessageEmbeds(messageUtil.getEmbedBuilder().build())
                                        .queue();
                                AUTHOR.retrieveProfile()
                                        .timeout(5, MINUTES)
                                        .queue();
                                event.getMessage().delete().queueAfter(2, SECONDS);
                            }
                            else {
                                messageUtil.getEmbedBuilder().clear()
                                        .clearFields()
                                        .setColor(RED)
                                        .setTitle("Bad Word Detected")
                                        .appendDescription(
                                                format(
                                                        "This message will be deleted on %s",
                                                        now(systemDefaultZone())
                                                                .plusMinutes(1)
                                                                .format(
                                                                        ofLocalizedTime(SHORT)
                                                                )
                                                )
                                        )
                                        .setFooter(
                                                format("Scanned by %s", event.getJDA().getSelfUser().getAsTag()),
                                                event.getJDA().getSelfUser().getAvatarUrl()
                                        );
                                event.getMessage()
                                        .replyEmbeds(messageUtil.getEmbedBuilder().build())
                                        .mentionRepliedUser(true)
                                        .queue();
                                event.getMessage().delete().queueAfter(5, SECONDS);
                            }
                        }

                        if (gameService.isTheOneWhoPlays(AUTHOR.getName())) {
                            final var IS_CORRECT = gameService.processAnswer(AUTHOR.getName(), MESSAGE);
                            if (isWholeNumber().or(isDecimalNumber()).test(MESSAGE)) {
                                if (IS_CORRECT) {
                                    messageUtil.getEmbedBuilder().clear()
                                            .clearFields()
                                            .setColor(BLUE)
                                            .setTitle("Correct!")
                                            .setFooter(
                                                    format("Checked by %s", event.getJDA().getSelfUser().getAsTag()),
                                                    event.getJDA().getSelfUser().getAvatarUrl()
                                            );
                                    event.getMessage()
                                            .replyEmbeds(messageUtil.getEmbedBuilder().build())
                                            .queue();
                                }
                                else {
                                    messageUtil.getEmbedBuilder().clear()
                                            .clearFields()
                                            .setColor(RED)
                                            .setTitle("WRONG ANSWER")
                                            .setFooter(
                                                    format("Checked by %s", event.getJDA().getSelfUser().getAsTag()),
                                                    event.getJDA().getSelfUser().getAvatarUrl()
                                            );
                                    event.getMessage()
                                            .replyEmbeds(messageUtil.getEmbedBuilder().build())
                                            .queue();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
