package io.protostuff.jetbrains.plugin.util;

import com.google.common.base.Strings;

import java.util.LinkedList;
import java.util.List;

public final class StringUtil {

    static final int LOWERCASE_UPPERCASE_GAP = 32; //'a' - 'A'

    private StringUtil() {
        throw new UnsupportedOperationException("Utility class can not be instantiated");
    }

    private static String convertCamelName2UnderLine(String camelName) {
        if (Strings.isNullOrEmpty(camelName)) {
            return null;
        }
        if (camelName.length() == 1) {
            return camelName.charAt(0) >= 'a' && camelName.charAt(0) <= 'z' ? camelName : camelName.toLowerCase();
        }
        char[] chars = camelName.toCharArray();
        List<Character> resultChars = new LinkedList<>();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (isUpperCase(chars[i])) {
                c = (char) (chars[i] + LOWERCASE_UPPERCASE_GAP);
                if (i != 0 && isLowerCase(chars[i - 1])) {
                    resultChars.add('_');
                } else if (i != chars.length - 1 && isLowerCase(chars[i + 1])) {
                    resultChars.add('_');
                }
            }
            resultChars.add(c);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Character resultChar : resultChars) {
            stringBuilder.append(resultChar);
        }
        return stringBuilder.toString();
    }

    private static boolean isLowerCase(char a) {
        return a >= 'a' && a <= 'z';
    }

    private static boolean isUpperCase(char a) {
        return a >= 'A' && a <= 'Z';
    }

    public static List<String> getAllUnderLineFormatString(String camelName) {
        List<String> results = new LinkedList<>();
        String underLine = convertCamelName2UnderLine(camelName);
        assert underLine != null;
        results.add(underLine);
        int startIndex = 0;
        while (startIndex != -1) {
            int lastStartIndex = startIndex;
            startIndex = underLine.indexOf("_", startIndex);
            if (startIndex != -1) {
                results.add(underLine.substring(++startIndex));
            }
        }
        if (results.get(0).startsWith("_")) {
            results.remove(0);
        }
        return results;
    }
}
