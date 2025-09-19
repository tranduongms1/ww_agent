package wisewires.agent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public static Function<String, String> normalizeFunc = s -> s.toLowerCase().replaceFirst("[:,;]$", "");

    public static boolean contains(List<String> list, String s) {
        for (String item : list) {
            if (item.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsAny(List<String> tokens, String... values) {
        Set<String> set = new HashSet<>(tokens.stream().map(normalizeFunc).toList());
        for (String value : values) {
            if (set.contains(value.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsAll(List<String> tokens, String... values) {
        Set<String> set = new HashSet<>(tokens.stream().map(normalizeFunc).toList());
        for (String value : values) {
            if (!set.contains(value.toLowerCase()))
                return false;
        }
        return true;
    }

    static List<String> removeLeading(List<String> tokens, String... leadingWords) {
        return removeLeading(tokens, List.of(leadingWords));
    }

    static List<String> removeLeading(List<String> tokens, List<String> leadingList) {
        List<String> result = new ArrayList<>();
        while (!tokens.isEmpty() && leadingList.contains(normalizeFunc.apply(tokens.get(0)))) {
            result.add(normalizeFunc.apply(tokens.remove(0)));
        }
        return result;
    }

    static int getOrdinal(List<String> tokens) {
        if (tokens.isEmpty()) {
            return -1;
        }
        Map<String, Integer> map = Map.of(
                "first", 1,
                "1st", 1,
                "second", 2,
                "2nd", 2,
                "third", 3,
                "3rd", 3,
                "fourth", 4,
                "4th", 4,
                "fifth", 5,
                "5th", 5);
        String token = tokens.get(0).toLowerCase();
        if (map.containsKey(token)) {
            tokens.remove(0);
            return map.get(token);
        }
        return 0;
    }

    private static int indexOfAll(List<String> tokens, List<String> keywords) {
        for (int i = 0; i <= tokens.size() - keywords.size(); i++) {
            boolean match = true;
            for (int j = 0; j < keywords.size(); j++) {
                if (!tokens.get(i + j).equalsIgnoreCase(keywords.get(j))) {
                    match = false;
                    break;
                }
            }
            if (match)
                return i;
        }
        return -1;
    }

    static TokenSingleMatch getBestMatch(List<String> tokens, Map<String, String> canonicalMap) {
        List<String> lowerTokens = tokens.stream().map(normalizeFunc).collect(Collectors.toList());

        String bestMatch = null;
        int bestIndex = -1;
        int bestLength = 0;
        for (Map.Entry<String, String> entry : canonicalMap.entrySet()) {
            String synonym = entry.getKey();
            List<String> keywords = List.of(synonym.split(" "));
            int index = indexOfAll(lowerTokens, keywords);
            if (index != -1) {
                if (keywords.size() > bestLength || (keywords.size() == bestLength && index < bestIndex)) {
                    bestMatch = synonym;
                    bestIndex = index;
                    bestLength = keywords.size();
                }
            }
        }
        if (bestMatch != null) {
            List<String> leading = new ArrayList<>(tokens.subList(0, bestIndex));
            tokens.subList(0, bestIndex + bestLength).clear();
            String value = canonicalMap.get(bestMatch).isEmpty() ? bestMatch : canonicalMap.get(bestMatch);
            return new TokenSingleMatch(value, leading);
        }
        return new TokenSingleMatch("", new ArrayList<>());
    }
}
