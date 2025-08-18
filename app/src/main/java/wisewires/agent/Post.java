package wisewires.agent;

import java.util.List;
import com.google.gson.Gson;

public class Post {
    private String id;
    private String channel_id;
    private String message;
    private List<String> file_ids;
    private PostProps props;
    private String root_id;
    private String type;

    Post(String channelId, String message) {
        this.channel_id = channelId;
        this.message = message;
    }

    Post(String channelId, Attachment attachment) {
        this.channel_id = channelId;
        this.props = new PostProps();
        this.props.getAttachments().add(attachment);
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

    public PostProps getProps() {
        if (props == null) {
            props = new PostProps();
        }
        return props;
    }

    public void setProps(PostProps props) {
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

    Attachment firstAttachment() {
        return getProps().getAttachments().get(0);
    }

    public String getScript() {
        if (getProps().getScript() != null) {
            return getProps().getScript();
        }
        return getMessage();
    }

    public void preRun() {
        if (getProps().getAttachments().isEmpty()) {
            Attachment attachment = new Attachment(getMessage());
            getProps().getAttachments().add(attachment);
            if (getType().isEmpty()) {
                getProps().setScript(getMessage());
            }
        }
        firstAttachment().setColor("default");
        firstAttachment().setActions(null);
    }
}
