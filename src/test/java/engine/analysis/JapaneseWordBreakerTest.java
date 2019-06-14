
package engine.analysis;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JapaneseWordBreakerTest {

    @Test
    public void test0() {
        String text = "にをで";
        List<String> expected = Arrays.asList();
        JapaneseWordBreaker tokenizer = new JapaneseWordBreaker();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test1() {
        String text = "いただくサイトあまり自由子";
        List<String> expected = Arrays.asList("いただく", "サイト", "あまり", "自由", "子");
        JapaneseWordBreaker tokenizer = new JapaneseWordBreaker();
        assertEquals(expected, tokenizer.tokenize(text));
    }

    @Test
    public void test2() {
        String text = "ものできる|ござる";
        List<String> expected = Arrays.asList("ござる");
        JapaneseWordBreaker tokenizer = new JapaneseWordBreaker();
        assertEquals(expected, tokenizer.tokenize(text));
    }
}
