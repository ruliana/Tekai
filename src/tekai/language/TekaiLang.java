package tekai.language;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import tekai.Expression;
import tekai.Parselet;
import tekai.Parser;
import tekai.standard.AtomParselet;
import tekai.standard.BeforeMiddleAfterParselet;

public class TekaiLang {

    private Parser contextParser;
    private LinkedList<LangExpression> contextParsed = new LinkedList<LangExpression>();

    private Parser parser;
    private int precedence = 1;
    private final int ATOM = precedence++;
    private final int EXPRESSION = precedence++;
    private final int REGULAR_EXPRESSION = precedence++;
    private final int DEFINITION = precedence++;

    private LinkedList<LangExpression> parsed = new LinkedList<LangExpression>();
    private Parser language;

    public TekaiLang() {
        contextParser = new Parser();
        contextParser.register(new BeforeMiddleAfterParselet(0, "Context +(\\w+)\r?\n", "(\r?\n)+", null , "Context"));
        contextParser.register(new AtomParselet(1, "[^\\r\\n]+", "Text"));

        parser = new Parser();
        parser.register(new BeforeMiddleAfterParselet(DEFINITION, "([\\w\\-]+) *?=", " +", null, "Definition"));
        parser.register(new AtomParselet(REGULAR_EXPRESSION, "\\/(.*?)\\/", "RegularExpression"));
        parser.register(new AtomParselet(EXPRESSION, "<([\\w\\-]+)>", "Expression"));
        parser.register(new AtomParselet(ATOM, "([^ ]+)\\.{3}", "MultipleSymbols"));
        parser.register(new AtomParselet(ATOM, "[^ ]+", "Symbol"));

        language = new Parser();
    }

    public void define(String string) {
        contextParsed.add(new LangExpression(contextParser.parse(string)));
    }

    protected void defineExpression(String string) {
        parsed.add(new LangExpression(parser.parse(string)));
    }

    public Expression parse(String string) {
        Iterator<LangExpression> iter = contextParsed.descendingIterator();

        while (iter.hasNext())
            buildContext(iter.next());

        return parseExpression(string);
    }

    private Expression parseExpression(String string) {
        Iterator<LangExpression> iter = parsed.descendingIterator();

        int x = parsed.size() - 1;
        while (iter.hasNext())
            build(x++, iter.next());

        return language.parse(string);
    }

    public LangExpression debugLastExpression() {
        return parsed.getLast();
    }

    private void buildContext(LangExpression expression) {
        for (LangExpression exp : expression.getChildren())
            defineExpression(exp.getValue());
    }

    private void build(int precedence, LangExpression expression) {
        if (expression.isDefinition())
            buildDefinition(precedence, expression);
        else
            throw new RuntimeException("No build rule for \"" + expression + "\"");
    }

    private void buildDefinition(final int precedence, final LangExpression expression) {
        assert precedence >= 0;
        assert expression != null;

        final String type = expression.getValue();
        MarkAndRest markAndRest = new MarkAndRest(expression.getChildren());
        final boolean startsWithMark = markAndRest.startsWithMark;
        final LangExpression firstMark = markAndRest.mark;
        final List<LangExpression> expressions = markAndRest.rest;

        language.register(new Parselet() {

            @Override
            public int getPrecedence() {
                return precedence;
            }

            @Override
            public boolean isPrefixParselet() {
                return startsWithMark;
            }

            @Override
            public String startingRegularExpression() {
                return firstMark.getValue();
            }

            @Override
            protected Expression parse() {
                Expression result = new Expression(type, lastMatch());

                Iterator<LangExpression> exps = expressions.iterator();

                if (notPrefixParselet() && exps.hasNext() && exps.next().isExpression())
                    result.addChildren(left());

                if (notPrefixParselet() && firstMark.isMultiple()) {
                    do {
                        result.addChildren(nextExpression(getPrecedence() + 1));
                    } while (canConsume(firstMark.getValue()));
                }

                while (exps.hasNext()) {
                    LangExpression exp = exps.next();
                    if (exp.isExpression())
                        result.addChildren(nextExpression());
                    else if (exp.isMultiple())
                        while (canConsume(exp.getValue()))
                            result.addChildren(nextExpression());
                    else if (exp.isMark())
                        consumeIf(exp.getValue());
                    else
                        throw new RuntimeException("Not a recognizable expression:" + expression);
                }

                return result;
            }
        });
    }

    /**
     * Helps {@link TekaiLang#buildDefinition(int, LangExpression)} to separate the first
     * part of the expression that can be used as "anchor".
     */
    static class MarkAndRest {
        public boolean startsWithMark = false;
        public LangExpression mark = null;
        public List<LangExpression> rest = new LinkedList<LangExpression>();

        public MarkAndRest(List<LangExpression> all) {
            boolean found = false;
            for(LangExpression exp : all) {
                if (!found && exp.isMark()) {
                    mark = exp;
                    if (rest.isEmpty()) startsWithMark = true;
                    found = true;
                } else {
                    rest.add(exp);
                }
            }
        }
    }

    /**
     * Wrapper for {@link Expression}. It adds useful meyhods for this context.
     */
    static class LangExpression {
        private final Expression expression;

        public LangExpression(Expression expression) {
            assert expression != null;
            this.expression = expression;
        }

        public String getValue() {
            if (expression.isType("Symbol"))
                return Pattern.quote(expression.getValue());
            if (expression.isType("MultipleSymbols"))
                return Pattern.quote(expression.getValue());
            if (expression.isType("RegularExpression"))
                return expression.getValue();
            else
                return expression.getValue();
        }

        public boolean isContext() {
            return expression.isType("Context");
        }

        public boolean isDefinition() {
            return expression.isType("Definition");
        }

        public boolean isMark() {
            return expression.isType("Symbol")
                || expression.isType("MultipleSymbols")
                || expression.isType("RegularExpression");
        }

        public boolean isExpression() {
            return expression.isType("Expression");
        }

        public boolean isMultiple() {
            return expression.isType("MultipleSymbols");
        }

        public List<LangExpression> getChildren() {
            List<LangExpression> result = new LinkedList<LangExpression>();
            for (Expression exp : expression.getChildren()) {
                result.add(new LangExpression(exp));
            }
            return result;
        }

        @Override
        public String toString() {
            return expression.toString();
        }
    }
}
