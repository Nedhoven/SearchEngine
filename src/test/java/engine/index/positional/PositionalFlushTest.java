
package engine.index.positional;

import com.google.common.collect.Table;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.TreeBasedTable;

import engine.analysis.*;
import engine.index.*;
import engine.index.inverted.*;
import engine.storage.Document;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PositionalFlushTest {

    private Analyzer an = new NaiveAnalyzer();
    private Compressor cp = new NaiveCompressor();
    private InvertedIndexManager iim;
    private String file = "./index/PositionalFlushTest";
    private Document d1 = new Document("cat dog bird");
    private Document d2 = new Document("dog wolf tiger");
    private Document d3 = new Document("cat bird elephant");
    private Document d4 = new Document("wolf tiger mouse");
    private Document d5 = new Document("cat puma mouse");
    private Document d6 = new Document("elephant cat puma");

    @Before
    public void setup() throws Exception {
        Path path = Paths.get(file);
        Files.deleteIfExists(path);
        iim = InvertedIndexManager.createOrOpenPositional(file, an, cp);
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 3;
    }

    @Test
    public void test1() {
        iim.addDocument(new Document("cat dog"));
        iim.addDocument(new Document("cat elephant"));
        iim.addDocument(new Document("wolf dog dog"));
        iim.addDocument(new Document("cat dog"));
        iim.flush();
        assertEquals(2, iim.getNumSegments());
        Map<String, List<Integer>> PostingList = new HashMap<>();
        PostingList.put("cat", Arrays.asList(0, 1));
        PostingList.put("dog", Arrays.asList(0, 2));
        PostingList.put("elephant", Arrays.asList(1));
        PostingList.put("wolf", Arrays.asList(2));
        Map<Integer, Document> DocStore = new HashMap<>();
        DocStore.put(0, new Document("cat dog"));
        DocStore.put(1, new Document("cat elephant"));
        DocStore.put(2, new Document("wolf dog dog"));
        Table<String, Integer, List<Integer>> Positions = HashBasedTable.create();
        Positions.put("cat", 0, Arrays.asList(0));
        Positions.put("cat", 1, Arrays.asList(0));
        Positions.put("dog", 0, Arrays.asList(1));
        Positions.put("dog", 2, Arrays.asList(1, 2));
        Positions.put("elephant", 1, Arrays.asList(1));
        Positions.put("wolf", 2, Arrays.asList(0));
        PositionalIndexSegmentForTest test = iim.getIndexSegmentPositional(0);
        assertEquals(PostingList, test.getInvertedLists());
        assertEquals(DocStore, test.getDocuments());
        assertEquals(Positions, test.getPositions());
    }

    @Test
    public void test2(){
        iim.flush();
        assertEquals(0, iim.getNumSegments());
        assertEquals(null, iim.getIndexSegmentPositional(0));
    }

    @Test
    public void test3() {
        assertEquals(0, iim.getNumSegments());
        iim.addDocument(d1);
        iim.addDocument(d2);
        iim.flush();
        assertEquals(1, iim.getNumSegments());
        PositionalIndexSegmentForTest inCase = iim.getIndexSegmentPositional(0);
        Map<Integer, Document> docs = inCase.getDocuments();
        assertFalse(docs.isEmpty());
        assertTrue(docs.containsKey(0) && docs.containsKey(1));
        assertTrue(docs.containsValue(d1) && docs.containsValue(d2));
        assertFalse(docs.size() > 2);
    }

    @Test
    public void test4() {
        String target = "cat";
        iim.addDocument(d1);
        iim.addDocument(d2);
        iim.flush();
        iim.addDocument(d3);
        iim.addDocument(d4);
        iim.flush();
        assertEquals(2, iim.getNumSegments());
        PositionalIndexSegmentForTest inCase = iim.getIndexSegmentPositional(1);
        List<Integer> list = inCase.getInvertedLists().get(target);
        assertFalse(list.isEmpty());
        assertFalse(list.size() > 1);
        assertEquals(0, (int) list.get(0));
        list = inCase.getPositions().get(target, 0);
        assertFalse(list.isEmpty());
        assertFalse(list.size() > 1);
        assertEquals(0, (int) list.get(0));
    }

    @Test
    public void test5() {
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
        PositionalIndexSegmentForTest inCase = iim.getIndexSegmentPositional(2);
        Collection<Document> docs = inCase.getDocuments().values();
        assertTrue(docs.contains(d5) && docs.contains(d6));
        String target = "cat";
        List<Integer> list = inCase.getPositions().get(target, 0);
        assertFalse(list.isEmpty());
        assertFalse(list.size() > 1);
        assertTrue(list.contains(0));
        list = inCase.getPositions().get(target, 1);
        assertFalse(list.isEmpty());
        assertFalse(list.size() > 1);
        assertTrue(list.contains(1));
    }

    @Test
    public void test6() {
        Document d1 = new Document("rate roll rate sing roll");
        Document d2 = new Document("rate sky rate rate");
        Map<String, List<Integer>> expectedPostingList = new HashMap<>();
        expectedPostingList.put("rate", Arrays.asList(0, 1));
        expectedPostingList.put("roll", Arrays.asList(0));
        expectedPostingList.put("sing", Arrays.asList(0));
        expectedPostingList.put("sky", Arrays.asList(1));
        Map<Integer, Document> expectedDocStore = new HashMap<>();
        expectedDocStore.put(0, d1);
        expectedDocStore.put(1, d2);
        Table<String, Integer, List<Integer>> expectedPositions = TreeBasedTable.create();
        expectedPositions.put("rate", 0, Arrays.asList(0, 2));
        expectedPositions.put("roll", 0, Arrays.asList(1, 4));
        expectedPositions.put("sing", 0, Arrays.asList(3) );
        expectedPositions.put("rate", 1, Arrays.asList(0, 2, 3) );
        expectedPositions.put("sky", 1, Arrays.asList(1) );
        ComposableAnalyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
        DeltaVarLenCompressor compressor = new DeltaVarLenCompressor();
        InvertedIndexManager ii = InvertedIndexManager.createOrOpenPositional(file, analyzer, compressor);
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 2;
        ii.addDocument(d1);
        ii.addDocument(d2);
        assertEquals(1, ii.getNumSegments());
        PositionalIndexSegmentForTest segment = ii.getIndexSegmentPositional(0);
        assertEquals(expectedPostingList, segment.getInvertedLists());
        assertEquals(expectedDocStore, segment.getDocuments());
        assertEquals(expectedPositions, segment.getPositions());
    }

    @Test
    public void test7() {
        Document d1 = new Document("bled cat dog feed bled feed rate roll rate sing roll sing");
        Document d2 = new Document("bled rate sky rate rat bled rate rate rate");
        Map<String, List<Integer>> expectedPostingList = new HashMap<>();
        expectedPostingList.put("bled", Arrays.asList(0, 1));
        expectedPostingList.put("cat", Arrays.asList(0));
        expectedPostingList.put("dog", Arrays.asList(0));
        expectedPostingList.put("feed", Arrays.asList(0));
        expectedPostingList.put("rat", Arrays.asList(1));
        expectedPostingList.put("rate", Arrays.asList(0, 1));
        expectedPostingList.put("roll", Arrays.asList(0));
        expectedPostingList.put("sing", Arrays.asList(0));
        expectedPostingList.put("sky", Arrays.asList(1));
        Map<Integer, Document> expectedDocStore = new HashMap<>();
        expectedDocStore.put(0, d1);
        expectedDocStore.put(1, d2);
        Table<String, Integer, List<Integer>> expectedPositions = TreeBasedTable.create();
        expectedPositions.put("bled", 0, Arrays.asList(0, 4));
        expectedPositions.put("cat", 0, Arrays.asList(1));
        expectedPositions.put("dog", 0, Arrays.asList(2));
        expectedPositions.put("feed", 0, Arrays.asList(3, 5));
        expectedPositions.put("rate", 0, Arrays.asList(6, 8));
        expectedPositions.put("roll", 0, Arrays.asList(7, 10));
        expectedPositions.put("sing", 0, Arrays.asList(9, 11));
        expectedPositions.put("bled", 1, Arrays.asList(0, 5));
        expectedPositions.put("rat", 1, Arrays.asList(4));
        expectedPositions.put("rate", 1, Arrays.asList(1, 3, 6, 7, 8));
        expectedPositions.put("sky", 1, Arrays.asList(2));
        ComposableAnalyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
        DeltaVarLenCompressor compressor = new DeltaVarLenCompressor();
        InvertedIndexManager ii = InvertedIndexManager.createOrOpenPositional(file, analyzer, compressor);
        PageFileChannel.PAGE_SIZE = 8;
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 2;
        ii.addDocument(d1);
        ii.addDocument(d2);
        assertEquals(1, ii.getNumSegments());
        PositionalIndexSegmentForTest segment = ii.getIndexSegmentPositional(0);
        assertEquals(expectedPostingList, segment.getInvertedLists());
        assertEquals(expectedDocStore, segment.getDocuments());
        assertEquals(expectedPositions, segment.getPositions());
    }

    @Test
    public void test8() {
        Document d1 = new Document("rate roll rate rate roll");
        Document d2 = new Document("rate sky rate sky sky");
        Document d3 = new Document("feed bled feed bled cat dog cat");
        Document d4 = new Document("sing feed dog dog cat dog feed");
        Map<String, List<Integer>> expectedPostingList0 = new HashMap<>();
        expectedPostingList0.put("rate", Arrays.asList(0, 1));
        expectedPostingList0.put("roll", Arrays.asList(0));
        expectedPostingList0.put("sky", Arrays.asList(1));
        Map<Integer, Document> expectedDocStore0 = new HashMap<>();
        expectedDocStore0.put(0, d1);
        expectedDocStore0.put(1, d2);
        Table<String, Integer, List<Integer>> expectedPositions0 = TreeBasedTable.create();
        expectedPositions0.put("rate", 0, Arrays.asList(0, 2, 3));
        expectedPositions0.put("roll", 0, Arrays.asList(1, 4));
        expectedPositions0.put("rate", 1, Arrays.asList(0, 2));
        expectedPositions0.put("sky", 1, Arrays.asList(1, 3, 4));
        Map<String, List<Integer>> expectedPostingList1 = new HashMap<>();
        expectedPostingList1.put("feed", Arrays.asList(0, 1));
        expectedPostingList1.put("bled", Arrays.asList(0));
        expectedPostingList1.put("cat", Arrays.asList(0, 1));
        expectedPostingList1.put("dog", Arrays.asList(0, 1));
        expectedPostingList1.put("sing", Arrays.asList(1));
        Map<Integer, Document> expectedDocStore1 = new HashMap<>();
        expectedDocStore1.put(0, d3);
        expectedDocStore1.put(1, d4);
        Table<String, Integer, List<Integer>> expectedPositions1 = TreeBasedTable.create();
        expectedPositions1.put("feed", 0, Arrays.asList(0, 2));
        expectedPositions1.put("bled", 0, Arrays.asList(1, 3));
        expectedPositions1.put("cat", 0, Arrays.asList(4, 6));
        expectedPositions1.put("dog", 0, Arrays.asList(5));
        expectedPositions1.put("feed", 1, Arrays.asList(1, 6));
        expectedPositions1.put("sing", 1, Arrays.asList(0));
        expectedPositions1.put("cat", 1, Arrays.asList(4));
        expectedPositions1.put("dog", 1, Arrays.asList(2, 3, 5));
        ComposableAnalyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
        InvertedIndexManager ii = InvertedIndexManager.createOrOpen(file, analyzer);
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 2;
        ii.addDocument(d1);
        ii.addDocument(d2);
        ii.addDocument(d3);
        ii.addDocument(d4);
        assertEquals(2, ii.getNumSegments());
        PositionalIndexSegmentForTest segment0 = ii.getIndexSegmentPositional(0);
        assertEquals(expectedPostingList0, segment0.getInvertedLists());
        assertEquals(expectedDocStore0, segment0.getDocuments());
        assertEquals(expectedPositions0, segment0.getPositions());
        PositionalIndexSegmentForTest segment1 = ii.getIndexSegmentPositional(1);
        assertEquals(expectedPostingList1, segment1.getInvertedLists());
        assertEquals(expectedDocStore1, segment1.getDocuments());
        assertEquals(expectedPositions1, segment1.getPositions());
    }

    @After
    public void cleanup() throws Exception {
        try {
            File index = new File(file);
            String[] f = index.list();
            for(String s: f){
                File currentFile = new File(index.getPath(),s);
                currentFile.delete();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Files.deleteIfExists(Paths.get(file));
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
        PageFileChannel.PAGE_SIZE = 4096;
    }

}
