package wisewires.agent;

import java.util.List;
import java.util.Map;

public class Attachment {
    private String color;
    private String text;
    private List<Map<String, Object>> actions;

    Attachment(String color, String text) {
        this.color = color;
        this.text = text;
    }

    Attachment(String text) {
        this.text = text;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Map<String, Object>> getActions() {
        return actions;
    }

    public void setActions(List<Map<String, Object>> actions) {
        this.actions = actions;
    }
}
