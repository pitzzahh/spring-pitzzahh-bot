package tech.araopj.springpitzzahhbot.service;

import tech.araopj.springpitzzahhbot.utilities.MessageUtil;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public record ViolationService(MessageUtil messageUtil) {

    /**
     * Adds violation to anyone who says a bad words.
     * @param username the username of the user who violated.
     */
    public void addViolation(final String username) {
        messageUtil.violations().put(username, getViolationCount(username) + 1);
    }

    private Integer getViolationCount(String username) {
        return messageUtil.violations()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().equals(username))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(0);
    }

    public boolean violatedThreeTimes(String username) {
        var violated = getViolationCount(username) >= 3;
        if (violated) messageUtil.violations().remove(username);
        return violated;
    }

}
