package tekai;

public class Helpers {

    private Helpers() {}
    
    /**
     * Returns a regular expression marking word boundaries and with ignore case.
     * <p>
     * Example:<br />
     * "group\\s+by" becomes "\\b(?:(i?)group\\s+by)\\b"
     * </p>
     */
    public static String word(String word) {
        return "\\b((i?)" + word + ")\\b";
    }
}
