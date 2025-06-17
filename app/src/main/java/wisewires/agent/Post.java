package wisewires.agent;

import com.google.gson.Gson;

public class Post {
    private String id;
    private String message;

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
}
