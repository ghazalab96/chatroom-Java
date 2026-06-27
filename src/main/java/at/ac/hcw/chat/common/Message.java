package at.ac.hcw.chat.common;

public record Message(String sender, String text, boolean isPrivate) {

    public boolean isSystem() {
        return sender.equals("[System]");
    }

    public boolean isUserList() {
        return sender.equals("[USERLIST]");
    }
}
