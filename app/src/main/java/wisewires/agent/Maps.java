package wisewires.agent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Maps {
    @SafeVarargs
    static <K, V> Map<K, V> merge(Map<K, V>... maps) {
        return List.of(maps)
                .stream()
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2));
    }

    static Map<String, String> keyPrefix(Map<String, String> map, String prefix) {
        return map.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> prefix + e.getKey(), Map.Entry::getValue));
    }
}
