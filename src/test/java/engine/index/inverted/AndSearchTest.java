
package engine.index.inverted;

import engine.analysis.*;
import engine.index.InvertedIndexManager;
import engine.storage.Document;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AndSearchTest {

    private final String path = "./index/AndSearchTest/Part1";
    private InvertedIndexManager manager;
    private Document doc1 = new Document("dog, bone and fishes");
    private Document doc2 = new Document("cats, fishes and dogs");
    private Document doc3 = new Document("fishes, birds and sky");
    private Document doc4 = new Document("cats, bones and something");
    private Document doc5 = new Document("Apple is the name of a dog and it is also the name of a tree.");
    private Document doc6 = new Document("The name of a dog is apple which is also a name of a tree.");
    private Document doc7 = new Document("Apple trees will have fruit once a year.");
    private Document doc8 = new Document("What is the name of that dog. Is it apple?");

    private final String PATH = "./index/AndSearchTest/Part2";
    private InvertedIndexManager iim;
    private static Document Doc = new Document("cat dog monkey");
    private static Document Doc1 = new Document("hello world");
    private static Document Doc2 = new Document("cat dog ");

    @Before
    public void before() {
        Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        this.manager = InvertedIndexManager.createOrOpen(path, analyzer);
        manager.addDocument(doc1);
        manager.addDocument(doc2);
        manager.addDocument(doc3);
        manager.addDocument(doc4);
        manager.addDocument(doc5);
        manager.addDocument(doc6);
        manager.addDocument(doc7);
        manager.addDocument(doc8);
        manager.flush();
        Analyzer an = new NaiveAnalyzer();
        File INDEX = new File(PATH);
        if (!INDEX.exists()) {
            INDEX.mkdirs();
        }
        iim = InvertedIndexManager.createOrOpen(PATH, an);
        iim.addDocument(Doc);
        iim.addDocument(Doc1);
        iim.addDocument(Doc2);
        iim.flush();
    }

    @Test
    public void test1() {
        Set<Document> expectedDocs = new HashSet<>();
        expectedDocs.add(doc4);
        List<String> keyWords = Arrays.asList("cat", "bone");
        Iterator<Document> ite = manager.searchAndQuery(keyWords);
        while(ite.hasNext()){
            Document cur = ite.next();
            if(expectedDocs.contains(cur)) {
                expectedDocs.remove(cur);
            }
            else {
                break;
            }
        }
        assertEquals(true, expectedDocs.size() == 0);
    }

    @Test
    public void test2() {
        List<String> keyWords = Arrays.asList("");
        Iterator<Document> ite = manager.searchAndQuery(keyWords);
        assertEquals(false, ite.hasNext());
    }

    @Test
    public void test3() {
        Set<Document> expectedDocs = new HashSet<>();
        expectedDocs.add(doc5);
        expectedDocs.add(doc6);
        List<String> keyWords = Arrays.asList("dog", "tree", "apple");
        Iterator<Document> ite = manager.searchAndQuery(keyWords);
        while (ite.hasNext()) {
            Document cur = ite.next();
            if (expectedDocs.contains(cur)) {
                expectedDocs.remove(cur);
            }
            else {
                break;
            }
        }
        assertEquals(true, expectedDocs.size() == 0);

    }

    @Test(expected = NoSuchElementException.class)
    public void test4() {
        List<String> keyWords = Arrays.asList("cat", "tree", "apple");
        Iterator<Document> ite = manager.searchAndQuery(keyWords);
        ite.next();
    }

    @Test
    public void test5() {
        List<Document> expected = Arrays.asList(Doc1);
        List<String> input = Arrays.asList("hello", "world");
        Iterator<Document> results = iim.searchAndQuery(input);
        int count = 0;
        while (results.hasNext()) {
            assertEquals(results.next().getText(), expected.get(count++).getText());
        }
    }

    @Test
    public void test6() {
        List<Document> expected = Arrays.asList(Doc, Doc2);
        List<String> input = Arrays.asList("cat", "dog");
        Iterator<Document> results = iim.searchAndQuery(input);
        int count = 0;
        while (results.hasNext()) {
            assertEquals(results.next().getText(), expected.get(count++).getText());
        }
    }

    @Test
    public void test7() {
        List<String> input = Arrays.asList("g", "g");
        Iterator<Document> results = iim.searchAndQuery(input);
        assertFalse(results.hasNext());
    }

    @After
    public void after() {
        try {
            File index = new File(PATH);
            String[] entries = index.list();
            for (String s : entries) {
                File currentFile = new File(index.getPath(), s);
                currentFile.delete();
            }
            index.delete();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        File cacheFolder = new File(path);
        for (File file : cacheFolder.listFiles()) {
            try {
                file.delete();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        cacheFolder.delete();
    }

}
