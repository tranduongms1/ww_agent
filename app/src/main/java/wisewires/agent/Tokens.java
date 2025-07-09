package wisewires.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}
