
package engine.index.inverted;

import engine.analysis.Analyzer;
import engine.analysis.NaiveAnalyzer;
import engine.index.InvertedIndexManager;
import engine.index.InvertedIndexSegmentForTest;
import engine.storage.Document;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class FlushTest {

    private Analyzer an = new NaiveAnalyzer();
    private String file = "./index/FlushTest";
    private InvertedIndexManager iim = InvertedIndexManager.createOrOpen(file, an);

    @Before
    public void setup() {
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 3;
        File directory = new File(file);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Test
    public void test1() {
        iim.addDocument(new Document("cat dog"));
        iim.addDocument(new Document("cat elephant"));
        iim.flush();
        iim.addDocument(new Document("cat dog"));
        iim.addDocument(new Document("wolf dog"));
        iim.flush();
        assertEquals(2, iim.getNumSegments());
        Map<String, List<Integer>> PostingList = new HashMap<>();
        List<Integer> l1 = new LinkedList<>();
        l1.add(0);
        l1.add(1);
        PostingList.put("cat", l1);
        List<Integer> l2 = new LinkedList<>();
        l2.add(0);
        PostingList.put("dog", l2);
        List<Integer> l3 = new LinkedList<>();
        l3.add(1);
        PostingList.put("eleph", l3);
        Map<Integer, Document> DocStore = new HashMap<>();
        DocStore.put(0, new Document("cat dog"));
        DocStore.put(1, new Document("cat elephant"));
        InvertedIndexSegmentForTest test = iim.getIndexSegment(0);
        assertEquals(PostingList, test.getInvertedLists());
        assertEquals(DocStore, test.getDocuments());
    }

    @Test
    public void test2() {
        Document d1 = new Document("Information retrieval");
        Document d2 = new Document("Searches");
        Document d3 = new Document("The process user");
        Document d4 = new Document("Web search");
        iim.addDocument(d1);
        iim.addDocument(d2);
        iim.addDocument(d3);
        iim.addDocument(d4);
        iim.flush();
        assertEquals(2, iim.getNumSegments());
        List<Document> result = new ArrayList<>();
        List<Document> expected = new ArrayList<>();
        expected.add(d1);
        expected.add(d2);
        expected.add(d3);
        expected.add(d4);
        while(iim.documentIterator().hasNext()) {
            result.add(iim.documentIterator().next());
        }
        assertEquals(result, expected);
    }

    @Test
    public void test3() {
        String s1 = "fox lion tiger puma panther";
        String s2 = "lion dog eagle giraffe fox";
        String s3 = "budget director film movie actor";
        String s4 = "theater actor director budget popcorn";
        Map<String, List<Integer>> postingList = new HashMap<>();
        List<Integer> l = new LinkedList<>();
        l.add(0);
        l.add(1);
        postingList.put("actor", l);
        postingList.put("budget", l);
        postingList.put("director", l);
        l = new LinkedList<>();
        l.add(0);
        postingList.put("film", l);
        postingList.put("movie", l);
        l = new LinkedList<>();
        l.add(1);
        postingList.put("popcorn", l);
        postingList.put("theater", l);
        String s5 = "pizza burger pasta salad sandwich";
        String s6 = "salad sandwich noodle rice pasta";
        Document d1 = new Document(s1);
        Document d2 = new Document(s2);
        Document d3 = new Document(s3);
        Document d4 = new Document(s4);
        Document d5 = new Document(s5);
        Document d6 = new Document(s6);
        iim.addDocument(d1);
        iim.addDocument(d2);
        iim.flush();
        iim.addDocument(d3);
        iim.addDocument(d4);
        iim.flush();
        iim.addDocument(d5);
        iim.addDocument(d6);
        iim.flush();
        assertEquals(3, iim.getNumSegments());
        InvertedIndexSegmentForTest num = iim.getIndexSegment(1);
        assertEquals(postingList, num.getInvertedLists());
        Set<Document> result = new HashSet<>();
        Set<Document> expected = new HashSet<>();
        expected.add(d1);
        expected.add(d2);
        expected.add(d3);
        expected.add(d4);
        expected.add(d5);
        expected.add(d6);
        while(iim.documentIterator().hasNext()) {
            result.add(iim.documentIterator().next());
        }
        assertEquals(result, expected);
    }

    @After
    public void cleanup() throws Exception {
        File cacheFolder = new File(file);
        for (File file : cacheFolder.listFiles()) {
            try {
                file.delete();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        cacheFolder.delete();
        Files.deleteIfExists(Paths.get(file));
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
    }

}
