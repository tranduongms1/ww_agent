package wisewires.agent;

import java.util.List;

public abstract class Lists {
    public static <E> E last(List<E> list) {
        return list.get(list.size() - 1);
    }

    public static <E> E first(List<E> list) {
        return list.get(0);
    }

}
