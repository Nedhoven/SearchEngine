
package engine.index.inverted;

import engine.analysis.*;
import engine.index.InvertedIndexManager;
import engine.index.PageFileChannel;
import engine.storage.Document;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class OrSearchTest {

    private final String FOLDER = "./index/OrSearchTest/Part1";
    private InvertedIndexManager manager;
    private Document doc1 = new Document("cat dog cat dog");
    private Document doc2 = new Document("apple dog");
    private Document doc3 = new Document("cat smile");
    private String path = "./index/OrSearchTest/Part2";
    private InvertedIndexManager invertedList;

    @Before
    public void before() {
        Analyzer analyzer = new NaiveAnalyzer();
        this.manager = InvertedIndexManager.createOrOpen(FOLDER, analyzer);
        manager.addDocument(doc1);
        manager.addDocument(doc2);
        manager.addDocument(doc3);
        manager.flush();
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        Analyzer an = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
        invertedList = InvertedIndexManager.createOrOpen(path, an);
        invertedList.addDocument(new Document("cat dog toy"));
        invertedList.flush();
        invertedList.addDocument(new Document("cat Dot"));
        invertedList.flush();
        invertedList.addDocument(new Document("cat dot toy"));
        invertedList.flush();
        invertedList.addDocument(new Document("cat toy Dog"));
        invertedList.flush();
        invertedList.addDocument(new Document("toy dog cat"));
        invertedList.flush();
        invertedList.addDocument(new Document("cat Dog"));
        invertedList.flush();
    }

    @Test
    public void test1() {
        List<Document> expected = Arrays.asList(doc1, doc2, doc3);
        List<String> keywords = Arrays.asList("cat", "apple");
        Iterator<Document> results = manager.searchOrQuery(keywords);
        for (int i = 0; results.hasNext(); i++) {
            assertEquals(results.next().getText(), expected.get(i).getText());
        }
    }

    @Test
    public void test2() {
        List<String> keywords = Arrays.asList("");
        Iterator<Document> results = manager.searchOrQuery(keywords);
        assertFalse(results.hasNext());
    }

    @Test
    public void test3() {
        List<String> keywords = Arrays.asList(",", ":./");
        Iterator<Document> results = manager.searchOrQuery(keywords);
        assertFalse(results.hasNext());
    }

    @Test
    public void test4() {
        List<String> words = new ArrayList<>();
        words.add("cat");
        words.add("dog");
        Iterator<Document> iterator = invertedList.searchOrQuery(words);
        int counter = 0;
        while (iterator.hasNext()) {
            String text = iterator.next().getText();
            assertEquals(true, text.contains("dog") || text.contains("cat"));
            counter++;
        }
        assertEquals(6, counter);
        assertTrue(PageFileChannel.readCounter >= 5 && PageFileChannel.writeCounter >= 5);
        words.clear();

    }

    @Test
    public void test5() {
        List<String> words = new ArrayList<>();
        words.add("dog");
        Iterator<Document> iterator = invertedList.searchOrQuery(words);
        int counter = 0;
        while (iterator.hasNext()) {
            String text = iterator.next().getText();
            assertEquals(true, text.toLowerCase().contains("dog"));
            counter++;
        }
        assertEquals(4, counter);
        assertTrue(PageFileChannel.readCounter >= 5 && PageFileChannel.writeCounter >= 5);
        words.clear();

    }

    @Test
    public void test6() {
        List<String> words = new ArrayList<>();
        words.add("sdasjdlslsah");
        words.add("*7&");
        Iterator<Document> iterator = invertedList.searchOrQuery(words);
        int counter = 0;
        while (iterator.hasNext()) {
            String text = iterator.next().getText();
            assertEquals(true, text.contains("sdasjdlslsah") || text.contains("*7&"));
            counter++;
        }
        assertEquals(0, counter);
        assertTrue(PageFileChannel.readCounter >= 5 && PageFileChannel.writeCounter >= 5);
        words.clear();
    }

    @Test
    public void test7() {
        List<String> words = new ArrayList<>();
        words.add("toy");
        words.add("dog");
        Iterator<Document> iterator = invertedList.searchOrQuery(words);
        int counter = 0;
        while (iterator.hasNext()) {
            String text = iterator.next().getText();
            assertEquals(true,
                    text.toLowerCase().contains("dog") || text.toLowerCase().contains("toy"));
            counter++;
        }
        assertEquals(5, counter);
        assertTrue(PageFileChannel.readCounter >= 5 && PageFileChannel.writeCounter >= 5);
        words.clear();
    }

    @Test
    public void test8() {
        List<String> words = new ArrayList<>();
        Iterator<Document> iterator = invertedList.searchOrQuery(words);
        assertFalse(iterator.hasNext());
    }

    @After
    public void after() {
        File cacheFolder = new File(FOLDER);
        for (File file : cacheFolder.listFiles()) {
            try {
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cacheFolder.delete();
        PageFileChannel.resetCounters();
        File f = new File(path);
        File[] files = f.listFiles();
        for (File file : files) {
            file.delete();
        }
        f.delete();
    }

}
