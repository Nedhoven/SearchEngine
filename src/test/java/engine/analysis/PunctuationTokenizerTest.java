
package engine.analysis;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PunctuationTokenizerTest {

    @Test
    public void test1() {
        String text = "I am Happy Today!";
        List<String> expected = Arrays.asList("happy", "today");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test2(){
        String text = "Good morning, Sara!";
        List<String> expected = Arrays.asList("good", "morning","sara");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test3(){
        String text = "Information Retrival is      the best course in UCI!";
        List<String> expected = Arrays.asList("information", "retrival","best","course","uci");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test4(){
        String text = "Information Retrival is \t \n the best course in UCI!";
        List<String> expected = Arrays.asList("information", "retrival","best","course","uci");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test5() {
        String text = " testcase\tgood example\nyes great example\n";
        List<String> expected = Arrays.asList("testcase", "good", "example",
                "yes", "great", "example");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test6() {
        String text = "\tgood example\nyes great example";
        List<String> expected = Arrays.asList("good", "example", "yes", "great", "example");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test7() {
        String text = "uci cs221\tinformation\nretrieval";
        List<String> expected = Arrays.asList("uci", "cs221", "information", "retrieval");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test8() {
        String text = "uci,cs221.information;retrieval?project!1";
        List<String> expected = Arrays.asList("uci", "cs221", "information", "retrieval", "project", "1");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test9() {
        String text = "uci~cs221/information>retrieval";
        List<String> expected = Arrays.asList("uci~cs221/information>retrieval");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test10() {
        String text = "UciCS221InformationRetrieval";
        List<String> expected = Arrays.asList("ucics221informationretrieval");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test11() {
        String text = "uci \tcs221\t\ninformation\n \tretrieval";
        List<String> expected = Arrays.asList("uci", "cs221", "information", "retrieval");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test12() {
        String text = "uci,.cs221.;information;?retrieval?!project!,.1";
        List<String> expected =
                Arrays.asList("uci", "cs221", "information", "retrieval", "project", "1");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test13() {
        String text = " \t\nucics221informationretrieval \t\n";
        List<String> expected = Arrays.asList("ucics221informationretrieval");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test14() {
        String text = ",.;?!ucics221informationretrieval,.;?!";
        List<String> expected = Arrays.asList("ucics221informationretrieval");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test15() {
        String text = " Do UCI CS221:\tInformation Retrieval, project 1 by yourself.\n";
        List<String> expected = Arrays.asList("uci", "cs221:", "information", "retrieval", "project", "1");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test16() {
        String text = "Word LOL means Laughing. WHO";
        List<String> expected = Arrays.asList("word", "lol", "means", "laughing");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test17() {
        String text = "He did not pass The Exam, did he?\n\r\t";
        List<String> expected = Arrays.asList("pass", "exam");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals("test ,? split, ignoring stop words and uppercase to lowercase transfer",
                expected, tokenizer.tokenize(text));
    }

    @Test
    public void test18() {
        String text = "Thanks God! I found my wallet there.";
        List<String> expected = Arrays.asList("thanks", "god","found","wallet");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals("test ! . split, ignoring stop words and uppercase to lowercase transfer",
                expected, tokenizer.tokenize(text));
    }

    @Test
    public void test19() {
        String text = "";
        List<String> expected = Arrays.asList();
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals("test empty input",
                expected, tokenizer.tokenize(text));
    }

    @Test
    public void test20() {
        String text = "         ";
        List<String> expected = Arrays.asList();
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals("test more  one spaces",
                expected, tokenizer.tokenize(text));
    }

    @Test
    public void test21() {
        String text =
                "herselF, me Own. ourS; tHiS? her thEirs were onLY; THese. Hidden oUrselVeS again, agaInsT hAs An? " +
                        "our have, he. oN. bEing aM CAn WiTh; So THRough? Them tHoSe. few. itS! Below! was? once Do Is! By of eACh. " +
                        "hImself; hiM; such? My; whO haViNg beEN haD She during! bEcAuse; other doEs; uNDeR oveR sHoUld JUSt! MoRe fOr Be " +
                        "into dID WHich thE, MySelf. hers; wHErE? They; now veRy aBouT NO information bUt tHemSeLVEs aRe hOw? tHeir NoT, bEFOrE? ANd wHat " +
                        "yourself; We froM? nor yOuR aboVe too wHY Or! yOurSelVeS theRE. DOn! dOwN; T. I sAme hERE uP; At. furThEr To; While; wILL; " +
                        "yours! bEtween? ThAt. you OfF theN as aLL both? uNTil; aNY Doing? tHAn iTsELf, ouT! WhEn IT whom; S, Some most A if. iN hIs! after.";
        List<String> expected = Arrays.asList("hidden", "information");
        PunctuationTokenizer tokenizer = new PunctuationTokenizer();
        assertEquals("test all stop words and punctuations: ", expected, tokenizer.tokenize(text));
    }

}
