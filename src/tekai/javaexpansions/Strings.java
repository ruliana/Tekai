package tekai.javaexpansions;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strings {

    private Strings(){}

    public static String coalesce(String... strings) {
        for (String string : strings) {
            if (string != null) return string;
        }
        return null;
    }

    public static String matches(String string, String regularExpression) {
        Pattern pattern = Pattern.compile(regularExpression);
        Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            if (matcher.groupCount() > 0) {
                return matcher.group(1);
            } else {
                return matcher.group();
            }
        } else {
            return "";
        }
    }

    public static String join(String separator, Collection<?> elements) {
        if (elements == null || elements.isEmpty()) return "";
        
        StringBuilder result = new StringBuilder();
        Iterator<?> iterator = elements.iterator();
        
        if (iterator.hasNext())
            result.append(String.valueOf(iterator.next()));
        
        while (iterator.hasNext()) {
            result.append(separator);
            result.append(String.valueOf(iterator.next()));
        }
        
        return result.toString();
    }
}
