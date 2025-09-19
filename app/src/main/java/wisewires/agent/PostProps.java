package wisewires.agent;

import java.util.ArrayList;
import java.util.HashMap;

public class PostProps {
    private ArrayList<Attachment> attachments;
    private String card;
    private String script;
    private boolean activate_ai;

    private String currentUrl;
    private HashMap<String, Object> testData;
    private int lastCommandIndex = -1;
    private boolean retry = false;

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

    public HashMap<String, Object> getTestData() {
        return testData;
    }

    public void setTestData(HashMap<String, Object> testData) {
        this.testData = testData;
    }

    public int getLastCommandIndex() {
        return lastCommandIndex;
    }

    public void setLastCommandIndex(int index) {
        this.lastCommandIndex = index;
    }

    public void increaseLastCommandIndex() {
        this.lastCommandIndex++;
    }

    public boolean getRetry() {
        return retry;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }
}
