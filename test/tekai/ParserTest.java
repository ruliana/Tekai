package tekai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static tekai.Helpers.word;

import org.junit.Test;

import tekai.Expression;
import tekai.Parselet;
import tekai.Parser;
import tekai.UnparseableException;
import tekai.standard.AtomParselet;
import tekai.standard.BeforeMiddleAfterParselet;
import tekai.standard.GroupingParselet;
import tekai.standard.InfixParselet;
import tekai.standard.PostfixParselet;
import tekai.standard.PrefixParselet;

public class ParserTest {

    @Test
    public void noSourceNoExpression() {
        Parser parser = new Parser();
        assertNull(parser.parse());
        assertNull(parser.parse(""));
    }

    @Test
    public void testAtomParser() throws Exception {
        Parser number = new Parser();
        number.rule("NUMBER").is("\\d+").end();

        Expression expression = number.parse("1");
        assertEquals("1 (NUMBER)", expression.asPrintTree());
    }

    @Test
     public void testPrefixParser() throws Exception {
        Parser anyExpression = new Parser();
        anyExpression.rule("MINUS").is("--").then(anyExpression).end();
        anyExpression.rule("NUMBER").is("\\d+").end();

        Expression expression = anyExpression.parse("--1");
        assertEquals(
                "-- (MINUS)\n" +
                "|__ 1 (NUMBER)"
                , expression.asPrintTree());

        Expression expression2 = anyExpression.parse("----2");
        assertEquals(
                "-- (MINUS)\n" +
                "|__ -- (MINUS)\n" +
                "    |__ 2 (NUMBER)"
                , expression2.asPrintTree());

        Expression expression3 = anyExpression.parse("-- -- 3");
        assertEquals(
                "-- (MINUS)\n" +
                "|__ -- (MINUS)\n" +
                "    |__ 3 (NUMBER)"
                , expression3.asPrintTree());
    }
    
    @Test
    public void testPrefixSelectiveParser() throws Exception {
        Parser minus = new Parser();
        Parser anyNumber = new Parser();

        minus.rule("MINUS").is("--").then(anyNumber).end();
        anyNumber.rule("NUMBER").is("\\d+").end();

        Expression expression = minus.parse("--1");
        assertEquals(
                "-- (MINUS)\n" +
                "|__ 1 (NUMBER)"
                , expression.asPrintTree());
    }

    @Test
    public void testInfixParser() throws Exception {
        Parser anyExpression = new Parser();
        anyExpression.rule("PLUS").is(anyExpression).then("\\+").then(anyExpression).end();
        anyExpression.rule("NUMBER").is("\\d+").end();

        Expression expression = anyExpression.parse("1 + 2");
        assertEquals(
                "+ (PLUS)\n" +
                "|__ 1 (NUMBER)\n" +
                "|__ 2 (NUMBER)"
                , expression.asPrintTree());
    }

    @Test
    public void testInfixSelectiveParser() throws Exception {
        Parser plus = new Parser();
        Parser anyABC = new Parser();
        Parser anyNumber = new Parser();

        plus.rule("PLUS").is(anyABC).then("\\+").then(anyNumber).end();
        anyABC.rule("ABC").is("[ABC]").end();
        anyNumber.rule("NUMBER").is("\\d+").end();

        Expression expression = plus.parse("A + 2");
        assertEquals(
                "+ (PLUS)\n" +
                "|__ A (ABC)\n" +
                "|__ 2 (NUMBER)"
                , expression.asPrintTree());
    }

    @Test(expected = UnparseableException.class)
    public void fixInfiniteRecursionInPrefixParser() throws Exception {
        Parser anyExpression = new Parser();
        anyExpression.rule("PLUS").is(anyExpression).then("\\+").then(anyExpression).end();
        anyExpression.rule("NUMBER").is("\\d+").end();

        Expression expression = anyExpression.parse("A + 2");
    }

    // TODO test multipart prefix parser
    // TODO test multipart infix parser
    // TODO test precedence
    // TODO separate parsers tests above in individual tests files

    @Test
    public void arithmeticParser() {
        int x = 1;
        int ATOM_PRECEDENCE = x++;
        int SUM_PRECEDENCE = x++;
        int MULT_PRECEDENCE = x++;
        int POSTFIX_PRECEDENCE = x++;

        Parser parser = new Parser();
        parser.register(new PostfixParselet(POSTFIX_PRECEDENCE, "\\+{2}", "PLUSONE"));
        parser.register(new InfixParselet(MULT_PRECEDENCE, "\\*", "MULT"));
        parser.register(new InfixParselet(SUM_PRECEDENCE, "\\+", "PLUS"));
        parser.register(new AtomParselet(ATOM_PRECEDENCE, "\\d+", "NUMBER"));

        assertEquals("([+]:PLUS [1]:NUMBER ([*]:MULT ([++]:PLUSONE [2]:NUMBER) [3]:NUMBER))", parser.parse("1 + 2++ * 3").toString());
    }

    @Test
    public void justAnAtom() {
        assertParsing("Just a number", "[1]:NUMBER", "1");
        assertParsing("Just an identifier", "[abc]:IDENTIFIER", "abc");
    }

    @Test
    public void simpleExpression() {
        assertParsing("Simple infix", "([+]:ARITHMETIC [1]:NUMBER [2]:NUMBER)", "1 + 2");
        assertParsing("Double infix (left associativity)", "([+]:ARITHMETIC ([+]:ARITHMETIC [1]:NUMBER [2]:NUMBER) [3]:NUMBER)", "1 + 2 + 3");
        assertParsing("Double infix with parenthesis", "([+]:ARITHMETIC [1]:NUMBER ([+]:ARITHMETIC [2]:NUMBER [3]:NUMBER))", "1 + (2 + 3)");
    }

    @Test
    public void functions() {
        assertParsing("[abc]:FUNCTION", "abc()");
        assertParsing("([abc]:FUNCTION [1]:NUMBER)", "abc(1)");
        assertParsing("([abc]:FUNCTION [1]:NUMBER [2]:NUMBER)", "abc(1, 2)");
        assertParsing("([abc]:FUNCTION [1]:NUMBER [2]:NUMBER [3]:NUMBER)", "abc(1, 2, 3)");
        assertParsing("([+]:ARITHMETIC ([abc]:FUNCTION [4]:NUMBER) ([def]:FUNCTION [3]:NUMBER [2]:NUMBER))", "abc(4) + def(3, 2)");
        assertParsing("([abc]:FUNCTION ([+]:ARITHMETIC ([+]:ARITHMETIC [2]:NUMBER [1]:NUMBER) [3]:NUMBER))", "abc((2 + 1) + 3)");
        assertParsing("([+]:ARITHMETIC ([+]:ARITHMETIC ([+]:ARITHMETIC [1]:NUMBER ([abc]:FUNCTION ([+]:ARITHMETIC [2]:NUMBER [3]:NUMBER) [4]:NUMBER)) [5]:NUMBER) [6]:NUMBER)", "(1 + abc(2 + 3, 4) + 5) + 6");
        assertParsing("([abc]:FUNCTION ([def]:FUNCTION [1]:NUMBER) ([ghi]:FUNCTION [2]:NUMBER))", "abc(def(1), ghi(2))");
    }

    @Test
    public void preserveSpaces() {
        Expression expression = parse("  1 +   2");
        assertEquals(" ", expression.getSpacing());
        assertEquals("  ", expression.getChild(0).getSpacing());
        assertEquals("   ", expression.getChild(1).getSpacing());
    }

    @Test
    public void preserveTabsAndLineBreaks() {
        Expression expression = parse("1\t + \n2");
        assertEquals("\t ", expression.getSpacing());
        assertEquals("", expression.getChild(0).getSpacing());
        assertEquals(" \n", expression.getChild(1).getSpacing());
    }

    @Test
    public void selectFrom() {
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [campo1]:IDENTIFIER [campo2]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER))", "SELECT campo1, campo2 FROM tabela");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER))", "SELECT * FROM tabela");
        assertParsing("([SQL]:SQL ([SELECT]:SELECT [*]:IDENTIFIER) ([FROM]:FROM [tabela]:IDENTIFIER ([INNER JOIN]:JOIN [outra_tabela]:IDENTIFIER [xxx]:IDENTIFIER)))", "SELECT * FROM tabela INNER JOIN outra_tabela ON xxx");
    }

    @Test
    public void exceptions() {
        try {
            parse("1 +");
            fail("Expected not able to parse an incomplete then \"1 +\"");
        } catch (Exception e) {
            // success
        }
    }

    // == Helpers ==

    private void assertParsing(String expected, String source) {
        assertParsing(null, expected, source);
    }

    private void assertParsing(String message, String expected, String source) {
        Expression expression = parse(source);
        assertEquals(message, expected, expression.toString());
    }

    private Expression parse(String source) {
        Parser parser = new Parser(source);
        configureParser(parser);
        return parser.parse();
    }

    private void configureParser(Parser parser) {
        // PRECEDENCE (What to parse first. Higher numbers means more precedence)
        int x = 1;
        final int ATOM = x++;
        final int OR = x++;
        final int AND = x++;
        final int NOT = x++;
        final int EQUALS = x++;
        final int MULTIPLY = x++;
        final int SUM = x++;
        final int GROUPING = x++;
        final int FUNCTION = x++;
        final int SELECT = x++;

        // SQL
        parser.register(new Parselet(SELECT) {
            @Override
            public boolean isPrefixParselet() {
                return true;
            }

            @Override
            public String startingRegularExpression() {
                return "SELECT";
            }

            @Override
            public Expression parse() {
                Expression fields = new Expression("SELECT", "SELECT");
                do {
                    fields.addChildren(nextExpression());
                } while (canConsume(","));

                consumeIf("FROM");

                Expression from = new Expression("FROM", "FROM");
                from.addChildren(nextExpression());

                if (canConsume("INNER( OUTER|RIGHT)? JOIN")) {
                    Expression join = new Expression("JOIN", lastMatch());
                    join.addChildren(nextExpression());
                    consumeIf("ON");
                    join.addChildren(nextExpression());
                    from.addChildren(join);
                }

                Expression result = new Expression("SQL", "SQL");
                result.addChildren(fields, from);
                return result;
            }
        });

        // BOOLEAN
        parser.register(new InfixParselet(OR, word("OR"), "BOOLEAN"));
        parser.register(new InfixParselet(AND, word("AND"), "BOOLEAN"));
        parser.register(new PrefixParselet(NOT, word("NOT"), "BOOLEAN"));

        // ARITHMETIC
        parser.register(new InfixParselet(MULTIPLY, "(\\*|/|%)", "ARITHMETIC"));
        parser.register(new InfixParselet(SUM, "(\\+|-)", "ARITHMETIC"));

        //EQUALS (OPERATOR)
        parser.register(new InfixParselet(EQUALS, "=", "OPERATOR"));

        // GROUPING (parenthesis)
        parser.register(new GroupingParselet(GROUPING, "\\(", "\\)"));

        // FUNCTION
        parser.register(new BeforeMiddleAfterParselet(FUNCTION, "(\\w+)\\s*\\(", ",", "\\)", "FUNCTION"));

        //NUMBER
        parser.register(new AtomParselet(ATOM, "\\d+(?:\\.\\d+)?", "NUMBER"));

        //STRING
        parser.register(new AtomParselet(ATOM, "\\'[^\\']*?\\'", "STRING"));

        //IDENTIFIER
        parser.register(new AtomParselet(ATOM, "(\\w+\\.\\w+|\\w+|\\*)", "IDENTIFIER"));
    }
}