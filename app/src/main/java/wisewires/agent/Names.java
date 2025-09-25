package wisewires.agent;

import java.util.Map;
import java.util.stream.Collectors;

public abstract class Names {
        static Map<String, String> ADDRESSES = Map.of(
                        "customer address", "",
                        "shipping address", "customer address",
                        "delivery address", "customer address",
                        "billing address", "");

        static Map<String, String> CHECKABLES = Maps.merge(
                        withPrefix(ADDRESSES, "save "));

        static Map<String, String> CLICKABLES = Maps.merge(
                        Map.of("continue shopping", ""),
                        Map.of("continue to checkout", ""),
                        Map.of("view orders", ""),
                        Map.of("samsung logo", ""),
                        Map.of("remove icon", ""));

        static Map<String, String> EDITABLES = Maps.merge(
                        Map.of("customer info", ""),
                        ADDRESSES,
                        Map.of("delivery", ""),
                        Map.of("cart", ""));

        static Map<String, String> SELECTABLES = Maps.merge(
                        Map.of("same shipping address", ""),
                        Map.of("different billing address", ""),
                        withPrefix(ADDRESSES, "new "),
                        withPrefix(ADDRESSES, "saved "),
                        Map.of("personal order", ""),
                        Map.of("individual order", "personal order"),
                        Map.of("personal company order", ""),
                        Map.of("company order", ""));

        static Map<String, String> FORMS = Maps.merge(
                        Map.of("customer info", ""),
                        ADDRESSES,
                        Map.of("sim info", ""));

        static Map<String, String> PROCESS_UNTIL = Maps.merge(
                        Map.of("customer info", ""),
                        ADDRESSES,
                        Map.of("sim step", ""),
                        Map.of("delivery", ""));

        static Map<String, String> withPrefix(Map<String, String> map, String prefix) {
                return map.entrySet().stream().collect(Collectors.toMap(
                                e -> prefix + e.getKey(),
                                e -> e.getValue().isEmpty() ? prefix + e.getKey() : prefix + e.getValue()));
        }
}
