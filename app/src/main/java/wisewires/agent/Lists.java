package wisewires.agent;

import java.util.List;

public abstract class Lists {
    public static <E> E last(List<E> list) {
        return list.get(list.size() - 1);
    }
}
