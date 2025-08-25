package wisewires.agent;

import java.util.ArrayList;

public class PostProps {
    private ArrayList<Attachment> attachments;
    private String card;
    private String script;
    private boolean activate_ai;

    private String currentUrl;

    public ArrayList<Attachment> getAttachments() {
        if (attachments == null) {
            attachments = new ArrayList<>();
        }
        return attachments;
    }

    public void setAttachments(ArrayList<Attachment> attachments) {
        this.attachments = attachments;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public boolean getActivateAI() {
        return activate_ai;
    }

    public void setActivateAI(boolean activate) {
        this.activate_ai = activate;
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    public void setCurrentUrl(String currentUrl) {
        this.currentUrl = currentUrl;
    }
}
