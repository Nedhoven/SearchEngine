
package engine.index.inverted;

import engine.analysis.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

import engine.index.InvertedIndexManager;
import engine.storage.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class KeywordSearchTest {

    private String pathname = "./index/KeywordSearchTest/Part1";
    private InvertedIndexManager indexManager;
    private Document doc1 = new Document("cat and dog");
    private Document doc2 = new Document("cat and fish");
    private Document doc3 = new Document("fish and dog");
    private String indexFolder = "./index/KeywordSearchTest/Part2";
    private InvertedIndexManager invertedIndex;
    private Document Doc1;
    private Document Doc2;
    private Document Doc3;
    private Document Doc4;

    @Before
    public void initial() {
        Analyzer analyzer = new NaiveAnalyzer();
        indexManager = InvertedIndexManager.createOrOpen(pathname, analyzer);
        indexManager.addDocument(doc1);
        indexManager.addDocument(doc2);
        indexManager.addDocument(doc3);
        indexManager.flush();
        ComposableAnalyzer an = new ComposableAnalyzer(new WordBreakTokenizer(), new PorterStemmer());
        invertedIndex = InvertedIndexManager.createOrOpen(indexFolder, an);
        Doc1 = new Document("catdog");
        Doc2 = new Document("dogbird");
        Doc3 = new Document("catpig");
        Doc4 = new Document("cat");
    }

    @Test
    public void test1() {
        String query = "cat";
        List<Document> expected = Arrays.asList(doc1, doc2);
        Iterator<Document> results = indexManager.searchQuery(query);
        int count = 0;
        while (results.hasNext()) {
            assertEquals(results.next().getText(), expected.get(count++).getText());
        }
        assertEquals(count, expected.size());
    }

    @Test
    public void test2() {
        String query = "";
        Iterator<Document> results = indexManager.searchQuery(query);
        assertFalse(results.hasNext());
    }

    @Test
    public void test3() {
        String query = "elephant";
        Iterator<Document> results = indexManager.searchQuery(query);
        assertFalse(results.hasNext());

    }

    @Test
    public void test4() {
        invertedIndex.addDocument(Doc1);
        invertedIndex.addDocument(Doc2);
        invertedIndex.addDocument(Doc3);
        invertedIndex.addDocument(Doc4);
        invertedIndex.flush();
        Iterator<Document> actualDoc = invertedIndex.searchQuery("cat");
        List<Document> expected = Arrays.asList(Doc1, Doc3, Doc4);
        for (Document expectedDoc : expected) {
            assertNotNull(actualDoc);
            assertEquals(expectedDoc.getText(), actualDoc.next().getText());
        }
        assertFalse(actualDoc.hasNext());
    }

    @Test
    public void test5() {
        invertedIndex.addDocument(Doc1);
        invertedIndex.addDocument(Doc2);
        invertedIndex.flush();
        invertedIndex.addDocument(Doc3);
        invertedIndex.addDocument(Doc4);
        invertedIndex.flush();
        invertedIndex.mergeAllSegments();
        Iterator<Document> actualDoc = invertedIndex.searchQuery("cat");
        List<Document> expected = Arrays.asList(Doc1, Doc3, Doc4);
        for (Document expectedDoc : expected) {
            assertNotNull(actualDoc);
            assertEquals(actualDoc.next().getText(), expectedDoc.getText());
        }
        assertFalse(actualDoc.hasNext());
    }

    @Test
    public void test6() {
        Iterator<Document> actualDoc = invertedIndex.searchQuery("cat");
        assertFalse(actualDoc.hasNext());
    }

    @After
    public void delete() {
        File files = new File(pathname);
        for (File file: files.listFiles()){
            if (!file.isDirectory()){
                file.delete();
            }
        }
        files.delete();

        File localStorageFolder = new File(indexFolder);
        for (File file : localStorageFolder.listFiles()) {
            file.delete();
        }
        localStorageFolder.delete();
    }

}
