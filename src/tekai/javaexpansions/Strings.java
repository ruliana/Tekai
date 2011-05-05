package tekai.javaexpansions;

public class Strings {

    private Strings(){}

    public static String coalesce(String... strings) {
        for (String string : strings) {
            if (string != null) return string;
        }
        return null;
    }
}
