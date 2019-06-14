
package engine.index.inverted;

import engine.index.Compressor;
import engine.index.DeltaVarLenCompressor;
import engine.index.InvertedIndexManager;
import engine.index.PositionalIndexSegmentForTest;
import engine.storage.Document;

import engine.analysis.Analyzer;
import engine.analysis.NaiveAnalyzer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.*;

public class DeletionTest {

    private String folder = "./index/DeletionTest";
    private InvertedIndexManager iim;
    private Document d1 = new Document("cat dog bird");
    private Document d2 = new Document("dog wolf tiger");
    private Document d3 = new Document("cat bird elephant");
    private Document d4 = new Document("wolf tiger mouse");
    private Document d5 = new Document("cat puma mouse");
    private Document d6 = new Document("elephant cat puma");

    @Before
    public void setUp() throws Exception {
        Analyzer analyze = new NaiveAnalyzer();
        Compressor compress = new DeltaVarLenCompressor();
        Path path = Paths.get(folder);
        Files.deleteIfExists(path);
        iim = InvertedIndexManager.createOrOpenPositional(folder, analyze, compress);
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 3;
    }

    @Test
    public void test1() {
        String target = "cat";
        iim.addDocument(d1);
        iim.addDocument(d2);
        iim.addDocument(d3);
        iim.deleteDocuments(target);
        assertEquals(1, iim.getNumSegments());
        PositionalIndexSegmentForTest inCase = iim.getIndexSegmentPositional(0);
        Map<Integer, Document> docs = inCase.getDocuments();
        assertFalse(docs.isEmpty());
        assertTrue(docs.containsKey(0) && docs.containsKey(1));
        assertTrue(docs.containsValue(d1) && docs.containsValue(d2));
        assertFalse(docs.size() > 2);
    }

    @Test
    public void test2() {
    }

    @After
    public void cleanUp() {
        File local = new File(folder);
        for (File file : local.listFiles()) {
            file.delete();
        }
        local.delete();
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 1000;
    }

}
