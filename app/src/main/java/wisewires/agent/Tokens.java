package wisewires.agent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public static boolean contains(List<String> list, String s) {
        for (String item : list) {
            if (item.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsAny(List<String> tokens, String... values) {
        Set<String> set = new HashSet<>(tokens.stream().map(String::toLowerCase).toList());
        for (String value : values) {
            if (set.contains(value.toLowerCase()) || set.contains(value.toLowerCase() + ":"))
                return true;
        }
        return false;
    }

    public static boolean containsAll(List<String> tokens, String... values) {
        Set<String> set = new HashSet<>(tokens.stream().map(String::toLowerCase).toList());
        for (String value : values) {
            if (!set.contains(value.toLowerCase()) && !set.contains(value.toLowerCase() + ":"))
                return false;
        }
        return true;
    }

    static List<String> removeLeading(List<String> tokens, String... leadingWords) {
        return removeLeading(tokens, List.of(leadingWords));
    }

    static List<String> removeLeading(List<String> tokens, List<String> leadingList) {
        List<String> result = new ArrayList<>();
        while (!tokens.isEmpty() && leadingList.contains(tokens.get(0).toLowerCase().replaceFirst(":$", ""))) {
            result.add(tokens.remove(0).toLowerCase().replaceFirst(":$", ""));
        }
        return result;
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

    static TokenSingleMatch getFormName(List<String> tokens) {
        Map<String, String> canonicalMap = Map.of(
                "customer info", "customer info",
                "customer address", "customer address",
                "billing address", "billing address",
                "shipping address", "customer address",
                "delivery address", "customer address",
                "delivery", "delivery",
                "sim", "sim");

        List<String> lowerTokens = tokens.stream().map(String::toLowerCase).collect(Collectors.toList());

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
            return new TokenSingleMatch(canonicalMap.get(bestMatch), leading);
        }
        return new TokenSingleMatch("", new ArrayList<>());
    }
}
