package tekai;

import static org.junit.Assert.assertEquals;
import static tekai.Expression.e;

import org.junit.Test;

public class PrinterTest {

    @Test
    public void basicPrint() throws Exception {
        assertEquals("1 + 2", print(e(" +", "AR", e("1", "NUMBER"), e(" 2", "NUMBER"))));
    }

    private String print(Expression e) {
        if (e.isType("AR")) {
            return print(e.getChild(0)) + e.printValue() + print(e.getChild(1));
        } else if (e.isType("NUMBER")) {
            return e.printValue();
        }
        return "";
    }
}
