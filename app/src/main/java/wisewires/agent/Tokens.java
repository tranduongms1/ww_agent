package wisewires.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TokenSingleMatch {
    String value;
    List<String> leading;

    TokenSingleMatch(String value, List<String> leading) {
        this.value = value;
        this.leading = leading;
    }
}

public abstract class Tokens {
    public static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();

        // Match double-quoted, single-quoted, or unquoted words
        Pattern pattern = Pattern.compile("\"[^\"]*\"|'[^']*'|\\S+");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String token = matcher.group();
            // Remove surrounding quotes if present
            if ((token.startsWith("\"") && token.endsWith("\"")) ||
                    (token.startsWith("'") && token.endsWith("'"))) {
                token = token.substring(1, token.length() - 1);
            }
            tokens.add(token);
        }

        return tokens;
    }

    public static boolean containsAny(List<String> list, String... ss) {
        for (String s : ss) {
            if (list.contains(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsAll(List<String> list, String... ss) {
        for (String s : ss) {
            if (!list.contains(s)) {
                return false;
            }
        }
        return true;
    }

    static List<String> removeLeading(List<String> list, String... ss) {
        return removeLeading(list, List.of(ss));
    }

    static List<String> removeLeading(List<String> list, List<String> ss) {
        List<String> l = new ArrayList<>();
        while (!list.isEmpty()) {
            String first = list.get(0).toLowerCase();
            if (ss.contains(first)) {
                l.add(list.remove(0));
            } else {
                break;
            }
        }
        return l;
    }

    static TokenSingleMatch getFormName(List<String> tokens) {
        List<String> leading = Tokens.removeLeading(tokens, "all", "form",
                "customer", "info", "customer info",
                "customer", "delivery", "address", "customer address", "delivery address",
                "billing", "address", "billing address");
        if (Tokens.containsAll(leading, "customer", "info")
                || Tokens.containsAny(leading, "customer info")) {
            return new TokenSingleMatch("customer info", leading);
        } else if (Tokens.containsAll(leading, "customer", "address")
                || Tokens.containsAll(leading, "delivery", "address")
                || Tokens.containsAll(leading, "shipping", "address")
                || Tokens.containsAny(leading, "customer address", "delivery address")) {
            return new TokenSingleMatch("customer address", leading);
        } else if (Tokens.containsAll(leading, "billing", "address")
                || Tokens.containsAny(leading, "billing address")) {
            return new TokenSingleMatch("billing address", leading);
        }
        return new TokenSingleMatch("", leading);
    }
}
