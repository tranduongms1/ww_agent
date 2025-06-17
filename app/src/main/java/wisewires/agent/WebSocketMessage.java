package wisewires.agent;

import java.util.Map;

import com.google.gson.Gson;

public class WebSocketMessage {
    private String event;
    private Map<String, Object> data;
    private int seq;

    public static WebSocketMessage fromJson(String json) {
        return new Gson().fromJson(json, WebSocketMessage.class);
    }

    public String getEvent() {
        return event != null ? event : "";
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getString(String key) {
        return data.get(key).toString();
    }
}
