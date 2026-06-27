package at.ac.hcw.chat.common;

import java.util.Arrays;
import java.util.List;

public class Protocol {

    private static final String SEPARATOR = "|";
    private static final String USERLIST_PREFIX = "USERLIST:";

    public static String formatMessage(String sender, String text) {
        return sender + SEPARATOR + text;
    }

    public static String formatUserList(List<String> users) {
        return USERLIST_PREFIX + String.join(",", users);
    }

    public static Message parse(String raw) {
        if (raw.startsWith(USERLIST_PREFIX)) {
            return new Message("[USERLIST]", raw.substring(USERLIST_PREFIX.length()), false);
        }

        String[] parts = raw.split("\\|", 2);

        if (parts.length < 2) {
            return new Message("[System]", raw, false);
        }

        String sender = parts[0].trim();
        String text = parts[1].trim();
        boolean isPrivate = sender.startsWith("[Private");

        return new Message(sender, text, isPrivate);
    }

    public static List<String> parseUserList(String text) {
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .sorted()
                .toList();
    }

    public static boolean isPrivateCommand(String text) {
        return text.startsWith("@") && text.contains(":");
    }
}
