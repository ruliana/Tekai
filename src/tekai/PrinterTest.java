package tekai;

import static org.junit.Assert.assertEquals;
import static tekai.Expression.e;

import org.junit.Test;

public class PrinterTest {

    @Test
    public void basicPrint() throws Exception {
        Expression expression =
                e(" +", "PLUS",
                        e("1", "NUMBER"),
                        e("2", "NUMBER"));

        assertEquals(
                "+ (PLUS)\n" +
                "|__ 1 (NUMBER)\n" +
                "|__ 2 (NUMBER)"
        , expression.asPrintTree());
    }
    
    @Test
    public void nestedPrint() throws Exception {
        Expression expression =
                e(" +", "PLUS",
                        e("*", "MULTI",
                                e("3", "NUMBER"),
                                e("4", "NUMBER")),
                        e("2", "NUMBER"));

        assertEquals(
                "+ (PLUS)\n" +
                "|__ * (MULTI)\n" +
                "|   |__ 3 (NUMBER)\n" +
                "|   |__ 4 (NUMBER)\n" +
                "|__ 2 (NUMBER)"
                , expression.asPrintTree());
    }

    @Test
    public void deepNestedPrint() throws Exception {
        Expression expression =
                e(" +", "PLUS",
                        e("*", "MULTI",
                                e("3", "NUMBER"),
                                e("+", "PLUS",
                                        e("5", "NUMBER"),
                                        e("6", "NUMBER")),
                                e("4", "NUMBER")),
                        e("2", "NUMBER"));

        assertEquals(
                "+ (PLUS)\n" +
                "|__ * (MULTI)\n" +
                "|   |__ 3 (NUMBER)\n" +
                "|   |__ + (PLUS)\n" +
                "|   |   |__ 5 (NUMBER)\n" +
                "|   |   |__ 6 (NUMBER)\n" +
                "|   |__ 4 (NUMBER)\n" +
                "|__ 2 (NUMBER)"
                , expression.asPrintTree());
    }

    @Test
    public void reallyComplexExpression() throws Exception {
        Expression expression =
                e(" +", "PLUS",
                        e("*", "MULTI",
                                e("3", "NUMBER"),
                                e("+", "PLUS",
                                        e("5", "NUMBER"),
                                        e("*", "MULTI",
                                                e("0", "NUMBER"),
                                                e("4", "NUMBER")))),
                        e("+", "PLUS",
                                e("2", "NUMBER"),
                                e("3", "NUMBER")));

        assertEquals(
                "+ (PLUS)\n" +
                "|__ * (MULTI)\n" +
                "|   |__ 3 (NUMBER)\n" +
                "|   |__ + (PLUS)\n" +
                "|       |__ 5 (NUMBER)\n" +
                "|       |__ * (MULTI)\n" +
                "|           |__ 0 (NUMBER)\n" +
                "|           |__ 4 (NUMBER)\n" +
                "|__ + (PLUS)\n" +
                "    |__ 2 (NUMBER)\n" +
                "    |__ 3 (NUMBER)"
                , expression.asPrintTree());
    }
}
