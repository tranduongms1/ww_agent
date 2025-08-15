package wisewires.agent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class Post {
    private String id;
    private String channel_id;
    private String message;
    private List<String> file_ids;
    private HashMap<String, Object> props;
    private String root_id;
    private String type;

    Post(String channelId, String message) {
        this.channel_id = channelId;
        this.message = message;
    }

    Post(String channelId, Attachment attachment) {
        this.channel_id = channelId;
        this.props = new HashMap<>(Map.of("attachments", List.of(attachment)));
    }

    public static Post fromJson(String json) {
        return new Gson().fromJson(json, Post.class);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannelId() {
        return channel_id;
    }

    public void setChannelId(String channelId) {
        this.channel_id = channelId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getFileIds() {
        return file_ids;
    }

    public void setFileIds(List<String> fileIds) {
        this.file_ids = fileIds;
    }

    public HashMap<String, Object> getProps() {
        if (this.props == null) {
            this.props = new HashMap<>();
        }
        return this.props;
    }

    public void setProps(HashMap<String, Object> props) {
        this.props = props;
    }

    public String getRootId() {
        return root_id;
    }

    public void setRootId(String rootId) {
        this.root_id = rootId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStringProp(String key) {
        return (String) getProps().get(key);
    }
}
