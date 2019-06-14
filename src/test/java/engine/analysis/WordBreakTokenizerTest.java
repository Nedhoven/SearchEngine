
package engine.analysis;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WordBreakTokenizerTest {

    @Test
    public void test1() {
        String text = "catdog";
        List<String> expected = Arrays.asList("cat", "dog");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test(timeout=20000)
    public void test2() {
        String text = "tosherlockholmessheisalwaysthewomanihaveseldomheardhimmentionherunderanyothernameinhiseyessheeclipsesandpredominatesthewholeofhersexitwasnotthathefeltanyemotionakintoloveforireneadlerallemotionsandthatoneparticularlywereabhorrenttohiscoldprecisebutadmirablybalancedmindhewasitakeitthemostperfectreasoningandobservingmachinethattheworldhasseenbutasaloverhewouldhaveplacedhimselfinafalsepositionheneverspokeofthesofterpassionssavewithagibeandasneertheywereadmirablethingsfortheobserverexcellentfordrawingtheveilfrommenmotivesandactionsbutforthetrainedreasonertoadmitsuchintrusionsintohisowndelicateandfinelyadjustedtemperamentwastointroduceadistractingfactorwhichmightthrowadoubtuponallhismentalresultsgritinasensitiveinstrumentoracrackinoneofhisownhighpowerlenseswouldnotbemoredisturbingthanastrongemotioninanaturesuchashisandyettherewasbutonewomantohimandthatwomanwasthelateireneadlerofdubiousandquestionablememory";
        String expectedStr = "sherlock holmes always woman seldom heard mention name eyes eclipses predominates whole sex felt emotion akin love irene adler emotions one particularly abhorrent cold precise admirably balanced mind take perfect reasoning observing machine world seen lover would placed false position never spoke softer passions save gibe sneer admirable things observer excellent drawing veil men motives actions trained reasoner admit intrusions delicate finely adjusted temperament introduce distracting factor might throw doubt upon mental results grit sensitive instrument crack one high power lenses would disturbing strong emotion nature yet one woman woman late irene adler dubious questionable memory";
        List<String> expected = Arrays.asList(expectedStr.split(" "));
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test(timeout=20000)
    public void test3() {
        String text = "ihadseenlittleofholmeslatelymymarriagehaddriftedusawayfromeachothermyowncompletehappinessandthehomecentredinterestswhichriseuparoundthemanwhofirstfindshimselfmasterofhisownestablishmentweresufficienttoabsorballmyattentionwhileholmeswholoathedeveryformofsocietywithhiswholesoulremainedinourlodgingsinbakerstreetburiedamonghisoldbooksandalternatingfromweektoweekbetweencocaineandambitionthedrowsinessofthedrugandthefierceenergyofhisownkeennaturehewasstillaseverdeeplyattractedbythestudyofcrimeandoccupiedhisimmensefacultiesandextraordinarypowersofobservationinfollowingoutthosecluesandclearingupthosemysterieswhichhadbeenabandonedashopelessbytheofficialpolicefromtimetotimeiheardsomevagueaccountofhisdoingsofhissummonstoodessainthecaseofthemurderofhisclearingupofthesingulartragedyoftheatkinsonbrothersattrincomaleeandfinallyofthemissionwhichhehadaccomplishedsodelicatelyandsuccessfullyforthereigningfamilyofhollandbeyondthesesignsofhisactivityhoweverwhichimerelysharedwithallthereadersofthedailypressiknewlittleofmyformerfriendandcompanion";
        String expectedStr = "seen little holmes lately marriage drifted us away complete happiness home centred interests rise around man first finds master establishment sufficient absorb attention holmes loathed every form society whole soul remained lodgings baker street buried among old books alternating week week cocaine ambition drowsiness drug fierce energy keen nature still ever deeply attracted study crime occupied immense faculties extraordinary powers observation following clues clearing mysteries abandoned hopeless official police time time heard vague account doings summons odessa case murder clearing singular tragedy atkinson brothers trincomalee finally mission accomplished delicately successfully reigning family holland beyond signs activity however merely shared readers daily press knew little former friend companion";
        List<String> expected = Arrays.asList(expectedStr.split(" "));
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test4() {
        String text = "Itisnotourgoal";
        List<String> expected = Arrays.asList("goal");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test5() {
        String text = "FindthelongestpalindromicstringYoumayassumethatthemaximumlengthisonehundred";
        List<String> expected = Arrays.asList("find", "longest", "palindromic", "string", "may",
                "assume", "maximum", "length", "one", "hundred");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test6() {
        String text = "";
        List<String> expected = Arrays.asList();
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test7() {
        String text = "ILIKEINFORMATIONRETRIEVAL";
        List<String> expected = Arrays.asList("like", "information", "retrieval");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test8() {
        String text = "thereareelevenpineapples";
        List<String> expected = Arrays.asList("eleven", "pineapples");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test(expected = RuntimeException.class)
    public void test9() {
        String text = "abc123";
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        tokenizer.tokenize(text);
    }

    @Test
    public void test10() {
        String text = "THISiswhATItoldyourI'llFRIendandI'llgoonlinecontactcan'tforget";
        List<String> expected = Arrays.asList("old", "i'll", "friend", "i'll","go","online","contact","can't","forget");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));

    }

    @Test
    public void test11(){
        String text = "informationinforTHOUGHTFULLYcopyrightwhatevercontactablewhatevergreen";
        List<String> expected = Arrays.asList("information", "thoughtfully", "copyright", "whatever", "contact", "able","whatever", "green" );
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test(expected = RuntimeException.class)
    public void test12(){
        String text = "$reLLL(  ghn)iog*";
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        tokenizer.tokenize(text);
    }

    @Test
    public void test13() {
        String text = "IlOveSAnFrancIsCo";
        List<String> expected = Arrays.asList("love", "san", "francisco");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test14() {
        String text = "";
        List<String> expected = Arrays.asList();
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test(expected = RuntimeException.class)
    public void test15() {
        String text = "mother-in-law";
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        tokenizer.tokenize(text);
    }

    @Test
    public void test16() {
        String text = "hellorange";
        List<String> expected = Arrays.asList("hello","range");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text)); //must expect an exception
    }

    @Test(expected = RuntimeException.class)
    public void test17() {
        String text = "fralprtnqela";
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        tokenizer.tokenize(text);
    }

    @Test
    public void test18() {
        String text = "WEhaveaCOOLTaskinFrontOfUSANDwEShouldbehavingAgoodTIme";
        List<String> expected = Arrays.asList("cool","task","front","us","behaving","good","time");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test(expected = RuntimeException.class)
    public void test19() {
        String text = "WhatHappensWhenWeaddAperiod.";
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        tokenizer.tokenize(text);
    }

    @Test(expected = RuntimeException.class)
    public void test20() {
        String text = "This is too check if an exception is thrown when there are spaces";
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        tokenizer.tokenize(text);
    }

    @Test
    public void test21() {
        String text = "thelordofthering";
        List<String> expected = Arrays.asList("lord", "ring");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));

    }

    @Test
    public void test22() {
        String text = "IWANTtohavepeanutbuttersandwich";
        List<String> expected = Arrays.asList("want", "peanut", "butter", "sandwich");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test(expected = RuntimeException.class)
    public void test23() {
        String text = "Where did Ghada go?";
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        tokenizer.tokenize(text);
    }

    @Test
    public void test24() {
        String text = "tobeornottobe";
        List<String> expected = Arrays.asList();
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test25() {
        String text = "";
        List<String> expected = Arrays.asList();
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();

        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test(expected = RuntimeException.class)
    public void test26() {
        String text = "b";
        List<String> expected = Arrays.asList();
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        tokenizer.tokenize(text);
        assert(false);
    }

    @Test
    public void test27() {
        String text = "searchnewtimeuse";
        List<String> expected = Arrays.asList("search", "new", "time", "use");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test28() {
        String text = "seaRchneWtiMeuSe";
        List<String> expected = Arrays.asList("search", "new", "time", "use");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test29() {
        String text = "SEARCHNEWTIMEUSE";
        List<String> expected = Arrays.asList("search", "new", "time", "use");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test30() {
        String text = "thesearchnewtimeuse";
        List<String> expected = Arrays.asList("search", "new", "time", "use");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test31() {
        String text = "searchthenewtimeuse";
        List<String> expected = Arrays.asList("search", "new", "time", "use");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test32() {
        String text = "searchnewtimeusethe";
        List<String> expected = Arrays.asList("search", "new", "time", "use");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test33() {
        String text = "nedishere";
        List<String> expected = Arrays.asList("ned");
        WordBreakTokenizer tokenizer = new WordBreakTokenizer();
        assertEquals(expected, tokenizer.tokenize(text));
    }

}
