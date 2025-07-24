package wisewires.agent;

import java.util.HashMap;

import com.google.gson.Gson;

public class Post {
    private String id;
    private String message;
    private HashMap<String, Object> props;

    public static Post fromJson(String json) {
        return new Gson().fromJson(json, Post.class);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HashMap<String, Object> getProps() {
        return props;
    }

    public void setProps(HashMap<String, Object> props) {
        this.props = props;
    }
}
