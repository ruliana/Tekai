package tekai;

import tekai.standard.AtomParselet;
import tekai.standard.PrefixParselet;

import java.util.LinkedList;
import java.util.List;


public class Parser {

    private Source source;

    private List<Parselet> prefixParselets = new LinkedList<Parselet>();
    private List<Parselet> parselets = new LinkedList<Parselet>();

    // == Construction

    public Parser() {
    }

    public Parser(CharSequence source) {
        this.setSource(new Source(source));
    }

    public void register(Parselet parselet) {
        if (parselet.isPrefixParselet()) {
            prefixParselets.add(parselet);
        } else {
            parselets.add(parselet);
        }
    }

    public void addRule(String type, String tokenExpression) {
        register(new AtomParselet(0, tokenExpression, type));
    }

    public void addRule(String type, String tokenExpression, Parser parser) {
        register(new PrefixParselet(0, tokenExpression, type, parser));
    }

    // == Parse Engine ==

    /**
     * @throws UnparseableException
     */
    public Expression parse(CharSequence source) {
        this.setSource(new Source(source));
        return parse();
    }

    /**
     * @throws UnparseableException
     */
    public Expression parse() {
        Expression result = parse(0);

        if (getSource() != null && !"".equals(getSource().sample().trim()))
            throw new UnparseableException("There are things to parse, but no rule for it: \"" + getSource().sample() + "\"");

        return result;
    }

    /**
     * @throws UnparseableException
     */
    protected Expression parse(int currentPrecedence) {

        if (getSource() == null) return null;
        if (getSource().isEmpty()) return null;

        Parselet currentParselet = findParseletIn(prefixParselets);

        if (currentParselet == null)
            if ("".equals(getSource().sample()))
                throw new UnparseableException("Expected something to parse, but found end of source");
            else
                throw new UnparseableException("Could not find a expression to parse \"" + getSource().sample() + "\"");


        consumeLastMatch();
        Expression left = currentParselet.executeParsing(this);

        Parselet nextParselet = findParseletIn(parselets);

        while (nextParselet != null && currentPrecedence < nextParselet.getPrecedence()) {
            consumeLastMatch();
            left = nextParselet.executeParsing(this, left);
            nextParselet = findParseletIn(parselets);
        }

        return left;
    }

    // == Helpers to Parse Engine ==

    private Parselet findParseletIn(List<Parselet> parselets) {
        for (Parselet parselet : parselets)
            if (sourceMatches(parselet))
                return parselet;
        return null;
    }

    private void consumeLastMatch() {
        getSource().consumeLastMatch();
    }

    private boolean sourceMatches(Parselet parselet) {
        return getSource().matches(parselet.startingRegularExpression());
    }

    // == Helpers to Parselets

    public String lastMatch() {
        return getSource().lastMatch();
    }

    public boolean couldConsume(String regularExpression) {
        return getSource().couldConsume(regularExpression);
    }

    public boolean canConsume(String regularExpression) {
        return getSource().canConsume(regularExpression);
    }

    public void consumeIf(String regularExpression) {
        getSource().consumeIf(regularExpression);
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }
}
