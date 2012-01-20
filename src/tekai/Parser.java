package tekai;

import tekai.standard.AtomParselet;
import tekai.standard.InfixParselet;
import tekai.standard.PrefixParselet;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


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

    public Rule rule(String type) {
        return new Rule(type);
    }
    
    public void addRule(String type, String tokenExpression) {
        register(new AtomParselet(0, tokenExpression, type));
    }

    public void addRule(String type, String tokenExpression, Parser parser) {
        register(new PrefixParselet(0, tokenExpression, type, parser));
    }

    public void addRule(String type, Parser leftParser, String tokenExpression, Parser rightParser) {
        register(new InfixParselet(1, tokenExpression, type, leftParser, rightParser));
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

        Parselet currentParselet = findPrefixParselet();

        if (currentParselet == null)
            if ("".equals(getSource().sample()))
                throw new UnparseableException("Expected something to parse, but found end of source");
            else
                throw new UnparseableException("Could not find a then to parse \"" + getSource().sample() + "\"");


        consumeLastMatch();
        Expression left = currentParselet.executeParsing(this);

        Parselet nextParselet = findParselet();

        while (nextParselet != null && currentPrecedence < nextParselet.getPrecedence()) {
            consumeLastMatch();
            left = nextParselet.executeParsing(this, left);
            nextParselet = findParselet();
        }

        return left;
    }

    // == Helpers to Parse Engine ==

    private Parselet findParselet() {
        for (Parselet parselet : parselets)
            if (sourceMatches(parselet))
                return parselet;
        return null;
    }

    private Parselet findPrefixParselet() {
        return findPrefixParselet(new HashSet<Parser>());
    }
    
    private Parselet findPrefixParselet(Set<Parser> usedParsers) {
        for (Parselet parselet : prefixParselets)
            if (sourceMatches(parselet))
                return parselet;

        for (Parselet parselet : parselets) {
            Parser leftParser = parselet.getLeftParser();
            if (leftParser == null) break;
            if (usedParsers.contains(leftParser)) break;
            usedParsers.add(leftParser);

            leftParser.setSource(getSource());
            Parselet prefixParselet = leftParser.findPrefixParselet(usedParsers);

            if (prefixParselet != null)
                return prefixParselet;
        }

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
    
    // == Helpers to rule
    
    public class Rule {
        private String type;
        private LinkedList<Element> elements;

        private Rule(String type) {
            this.type = type;
            this.elements = new LinkedList<Element>();
        }
        
        public Rule is(String marker) {
            elements.add(new Element(marker));
            return this;
        }

        public Rule is(Parser parser) {
            elements.add(new Element(parser));
            return this;
        }
        
        public Rule then(String marker) {
            return is(marker);
        }

        public Rule then(Parser parser) {
            return is(parser);
        }
        
        public void end() {
            if (elements.isEmpty())
                throw new RuntimeException("Rule \"" + type + "\" has no markers or parsers");
            
            if (elements.size() == 1 && elements.getFirst().isMarker())
                register(new AtomParselet(0, elements.getFirst().getMarker(), type));
            else if (elements.getFirst().isMarker() && elements.get(1).isParser())
                register(new PrefixParselet(0, elements.getFirst().getMarker(), type, elements.get(1).getParser()));
            else if (elements.getFirst().isParser() && elements.get(1).isMarker() && elements.get(2).isParser())
                register(new InfixParselet(1, elements.get(1).getMarker(), type, elements.getFirst().getParser(), elements.get(2).getParser()));
            else
                throw new RuntimeException("No parselet for rule \"" + type + "\"");
        }
    }

    private static class Element<T> {

        private String marker;
        private Parser parser;

        public Element(String marker) {
            this.marker = marker;
        }

        public Element(Parser parser) {
            this.parser = parser;
        }

        public boolean isMarker() {
            return marker != null;
        }

        public String getMarker() {
            return marker;
        }

        public boolean isParser() {
            return parser != null;
        }

        public Parser getParser() {
            return parser;
        }
    }
}
