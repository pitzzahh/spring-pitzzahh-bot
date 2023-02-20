package tech.araopj.springpitzzahhbot.games.service;

import tech.araopj.springpitzzahhbot.games.config.GameConfig;
import org.springframework.stereotype.Service;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Map;

@Service
public record GameService(GameConfig gameConfig) {

    public BiConsumer<String, String> addQuestion() {
        return (username, answer) -> gameConfig.questions().put(username, answer);
    }

    public boolean isTheOneWhoPlays(String username) {
        if (isDone(username)) return false;
        return gameConfig.questions()
                .keySet()
                .stream()
                .anyMatch(username::equals);
    }

    /**
     * Checks if the user answer is correct.
     * param player the user that is playing.
     * param guess the user guess on a question.
     * returns {@code true} if the user guessed the answer.
     */
    public boolean processAnswer(String player, String guess) {
        final var ANSWER = gameConfig.questions()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().equals(player))
                .map(Map.Entry::getValue)
                .collect(Collectors.joining());
        gameConfig.questions().replace(player, null);
        return guess.equals(ANSWER);
    }

    public boolean isDone(String player) {
        return gameConfig
                .questions()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().equals(player))
                .map(Map.Entry::getValue)
                .anyMatch(Objects::isNull);
    }
}
