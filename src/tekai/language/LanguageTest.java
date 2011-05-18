package tekai.language;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class LanguageTest {

    private TekaiLang lang;

    @Before
    public void newTekaiLang() {
        lang = new TekaiLang();
    }

    @Test
    public void atom() {
        lang.defineExpression("Number = /\\d+/");
        assertEquals("[4]:Number", lang.parse("4").toString());
    }

    @Test
    public void prefix() {
        lang.defineExpression("Negative = - <Expression>");
        lang.defineExpression("Number = /\\d+/");
        assertEquals("([-]:Negative [1]:Number)", lang.parse("-1").toString());
    }

    @Test
    public void infix() {
        lang.defineExpression("Sum = <Expression> + <Expression>");
        lang.defineExpression("Number = /\\d+/");
        assertEquals("([+]:Sum [1]:Number [2]:Number)", lang.parse("1 + 2").toString());
        assertEquals("([+]:Sum [1]:Number ([+]:Sum [2]:Number [3]:Number))", lang.parse("1 + 2 + 3").toString());
    }

    @Test
    public void suffix() {
        lang.defineExpression("AddAdd = <Expression> ++");
        lang.defineExpression("Number = /\\d+/");
        assertEquals("([++]:AddAdd [1]:Number)", lang.parse("1++").toString());
    }

    @Test
    public void infixWithRegularExpression() {
        lang.defineExpression("AddSub = <Expression> /\\+|\\-/ <Expression>");
        lang.defineExpression("Number = /\\d+/");
        assertEquals("([+]:AddSub [1]:Number [2]:Number)", lang.parse("1 + 2").toString());
        assertEquals("([-]:AddSub [1]:Number [2]:Number)", lang.parse("1 - 2").toString());
    }

    @Test
    public void multipleInfix() {
        lang.defineExpression("AddAndSub = <Expression> + <Expression> - <Expression>");
        lang.defineExpression("Number = /\\d+/");
        assertEquals("([+]:AddAndSub [1]:Number [2]:Number [3]:Number)", lang.parse("1 + 2 - 3").toString());
    }

    @Test
    public void around() {
        lang.defineExpression("Parenthesis = ( <Expression> )");
        lang.defineExpression("Number = /\\d+/");
        assertEquals("([(]:Parenthesis [1]:Number)", lang.parse("(1)").toString());
    }

    @Test
    public void infiniteMiddle() {
        lang.defineExpression("List = <Expression> ,...");
        lang.defineExpression("Number = /\\d+/");
        assertEquals("([,]:List [1]:Number [2]:Number [3]:Number)", lang.parse("1, 2, 3").toString());
    }

    @Test
    public void likeFunctions() {
        lang.defineExpression("Function = /(\\w+)/ ( <Expression> ,... )");
        lang.defineExpression("Number = /\\d+/");
        assertEquals("([average]:Function [1]:Number [2]:Number [3]:Number)", lang.parse("average(1, 2, 3)").toString());
        assertEquals("([average]:Function [1]:Number)", lang.parse("average(1)").toString());
        assertEquals("[average]:Function", lang.parse("average()").toString());
    }

    @Test
    public void context() {
        lang.define("Context Expression\n" +
                    "Sum = <Expression> + <Expression>\n" +
                    "Number = /\\d+/");
        assertEquals("([+]:Sum [1]:Number [2]:Number)", lang.parse("1 + 2").toString());
    }

    //TODO Left associativity
    //TODO "Typed" expressions
    //TODO Nested contexts (types)
}
