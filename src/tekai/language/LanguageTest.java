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
        lang.define("Number = /\\d+/");
        assertEquals("[4]:Number", lang.parse("4").toString());
    }

    @Test
    public void prefix() {
        lang.define("Negative = - <Expression>");
        lang.define("Number = /\\d+/");
        assertEquals("([-]:Negative [1]:Number)", lang.parse("-1").toString());
    }

    @Test
    public void infix() {
        lang.define("Sum = <Expression> + <Expression>");
        lang.define("Number = /\\d+/");
        assertEquals("([+]:Sum [1]:Number [2]:Number)", lang.parse("1 + 2").toString());
    }

    @Test
    public void suffix() {
        lang.define("AddAdd = <Expression> ++");
        lang.define("Number = /\\d+/");
        assertEquals("([++]:AddAdd [1]:Number)", lang.parse("1++").toString());
    }

    @Test
    public void infixWithRegularExpression() {
        lang.define("AddSub = <Expression> /\\+|\\-/ <Expression>");
        lang.define("Number = /\\d+/");
        assertEquals("([+]:AddSub [1]:Number [2]:Number)", lang.parse("1 + 2").toString());
        assertEquals("([-]:AddSub [1]:Number [2]:Number)", lang.parse("1 - 2").toString());
    }

    @Test
    public void multipleInfix() {
        lang.define("AddAndSub = <Expression> + <Expression> - <Expression>");
        lang.define("Number = /\\d+/");
        assertEquals("([+]:AddAndSub [1]:Number [2]:Number [3]:Number)", lang.parse("1 + 2 - 3").toString());
    }

    @Test
    public void around() {
        lang.define("Parenthesis = ( <Expression> )");
        lang.define("Number = /\\d+/");
        assertEquals("([(]:Parenthesis [1]:Number)", lang.parse("(1)").toString());
    }

    @Test
    public void infiniteMiddle() {
        lang.define("List = <Expression> ,...");
        lang.define("Number = /\\d+/");
        assertEquals("([,]:List [1]:Number [2]:Number [3]:Number)", lang.parse("1, 2, 3").toString());
    }

    @Test
    public void likeFunctions() {
        lang.define("Function = /(\\w+)/ ( <Expression> ,... )");
        lang.define("Number = /\\d+/");
        assertEquals("([average]:Function [1]:Number [2]:Number [3]:Number)", lang.parse("average(1, 2, 3)").toString());
        assertEquals("([average]:Function [1]:Number)", lang.parse("average(1)").toString());
        assertEquals("[average]:Function", lang.parse("average()").toString());
    }

    //TODO "Typed" expressions
}
