package wisewires.agent;

import java.util.Map;

public abstract class Names {
        static Map<String, String> ADDRESSES = Map.of(
                        "customer address", "",
                        "shipping address", "customer address",
                        "delivery address", "customer address",
                        "billing address", "");

        static Map<String, String> CHECKABLES = Maps.merge(
                        Maps.keyPrefix(ADDRESSES, "save "));

        static Map<String, String> CLICKABLES = Maps.merge(
                        Map.of("continue shopping", ""),
                        Map.of("view orders", ""));

        static Map<String, String> EDITABLES = Maps.merge(
                        Map.of("customer info", ""),
                        ADDRESSES,
                        Map.of("delivery", ""));

        static Map<String, String> SELECTABLES = Maps.merge(
                        Map.of("different billing address", ""),
                        Maps.keyPrefix(ADDRESSES, "new "),
                        Maps.keyPrefix(ADDRESSES, "saved "),
                        Map.of("individual order", ""),
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
}
