package wisewires.agent;

import java.util.List;

public abstract class Lists {
    public static <E> E last(List<E> list) {
        return list.get(list.size() - 1);
    }

    public static <E> E first(List<E> list) {
        return list.get(0);
    }

    public static String firstStartsWith(List<String> list, String prefix) {
        return list.stream().filter(e -> e.startsWith(prefix)).findFirst().orElse(null);
    }
}
