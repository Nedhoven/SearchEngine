
package engine.index.positional;

import engine.analysis.Analyzer;
import engine.analysis.ComposableAnalyzer;
import engine.analysis.PorterStemmer;
import engine.analysis.PunctuationTokenizer;
import engine.index.DeltaVarLenCompressor;
import engine.index.InvertedIndexManager;
import engine.index.NaiveCompressor;
import engine.index.PageFileChannel;
import engine.storage.Document;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class IndexCompressionTest {

    private Document doc1 = new Document("dog");
    private Document doc2 = new Document(String.join(" ", Collections.nCopies(4096, "cat")));
    private Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
    private InvertedIndexManager naiveIndexManager;
    private InvertedIndexManager dvlIndexManager;
    private DeltaVarLenCompressor dvlCompressor = new DeltaVarLenCompressor();
    private NaiveCompressor naiveCompressor = new NaiveCompressor();
    private static final String indexFolder = "./index/IndexCompressionTest";
    private static final String path1 =  indexFolder + "/naive_compress";
    private static final String path2 = indexFolder + "/delta_compress";
    private Document sampleDoc = generateDoc();
    private Document emptyDoc = new Document(" ");
    private double nonCompressWriteCounter = 0;
    private double compressWriteCounter = 0;

    @Before
    public void init() {
        File directory1 = new File(path1);
        if (!directory1.exists()) {
            directory1.mkdirs();
        }
        File directory2 = new File(path2);
        if (!directory2.exists()) {
            directory2.mkdirs();
        }
        PageFileChannel.resetCounters();
        naiveIndexManager = InvertedIndexManager.createOrOpenPositional(path1, analyzer, naiveCompressor);
        dvlIndexManager = InvertedIndexManager.createOrOpenPositional(path2, analyzer, dvlCompressor);
    }

    // helper method

    private Document generateDoc() {
        String docPath = "https://grape.ics.uci.edu/wiki/public/raw-attachment/wiki/cs221-2019-spring-project2/Team2StressTest.txt";
        String text = "";
        try {
            URL docURL = new URL(docPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(docURL.openStream()));
            StringBuilder sb = new StringBuilder();
            String inputStr;
            while ((inputStr = reader.readLine()) != null) {
                sb.append(inputStr + "\n");
            }
            reader.close();
            text = sb.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new Document(text);
    }

    @Test
    public void test1(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 4096;
        PageFileChannel.resetCounters();
        for(int i = 0; i < 4096; i++) {
            this.naiveIndexManager.addDocument(doc1);
        }
        this.naiveIndexManager.flush();
        int naiveCount = PageFileChannel.writeCounter;
        PageFileChannel.resetCounters();
        for(int i = 0; i < 4096; i++) {
            this.dvlIndexManager.addDocument(doc1);
        }
        this.dvlIndexManager.flush();
        int dvlCount = PageFileChannel.writeCounter;
        assertTrue(naiveCount/(double)dvlCount > 2);
    }

    @Test
    public void test2(){
        PageFileChannel.resetCounters();
        this.naiveIndexManager.addDocument(doc2);
        this.naiveIndexManager.flush();
        int naiveCount = PageFileChannel.writeCounter;
        PageFileChannel.resetCounters();
        this.dvlIndexManager.addDocument(doc2);
        this.dvlIndexManager.flush();
        int dvlCount = PageFileChannel.writeCounter;
        assertTrue(naiveCount/(double)dvlCount < 4);
        assertTrue(naiveCount/(double)dvlCount > 1.5);
    }

    @Test
    public void test3() {
        Assert.assertEquals(0, PageFileChannel.readCounter);
        Assert.assertEquals(0, PageFileChannel.writeCounter);
        for (int i = 0; i < 10000; i++) {
            naiveIndexManager.addDocument(new Document("cat Dot"));
        }
        naiveIndexManager.flush();
        for (int i = 0; i < naiveIndexManager.getNumSegments(); i++) {
            naiveIndexManager.getIndexSegmentPositional(i);
        }
        int naive_wc = PageFileChannel.writeCounter;
        int naive_rc = PageFileChannel.readCounter;
        PageFileChannel.resetCounters();
        for (int i = 0; i < 10000; i++) dvlIndexManager.addDocument(new Document("cat Dot"));
        dvlIndexManager.flush();
        for (int i = 0; i < dvlIndexManager.getNumSegments(); i++) {
            dvlIndexManager.getIndexSegmentPositional(i);
        }
        int compress_wc = PageFileChannel.writeCounter;
        int compress_rc = PageFileChannel.readCounter;
        System.out.println();
        Assert.assertTrue(naive_rc > 1.5 * compress_rc);
        Assert.assertTrue(naive_wc > 1.5 * compress_wc);
        System.out.println("\033[0;32m");
        System.out.println("Naive compress write: " + naive_wc + " pages");
        System.out.println("Naive compress read: " + naive_rc + " pages");
        System.out.println("Your compress write: " + compress_wc + " pages");
        System.out.println("Your compress read: " + compress_rc + " pages");
        System.out.println("\033[0m");
    }

    @Test
    public void test4() {
        Assert.assertEquals(0, PageFileChannel.readCounter);
        Assert.assertEquals(0, PageFileChannel.writeCounter);
        for (int i = 0; i < 3000; i++) {
            naiveIndexManager.addDocument(new Document("cat Dot cat Dog I can not tell the difference between cat and Dog"));
            naiveIndexManager.addDocument(new Document("cat and dog have a lot of difference"));
            naiveIndexManager.addDocument(new Document("Dog can be very different from cat"));
        }
        naiveIndexManager.flush();
        for (int i = 0; i < naiveIndexManager.getNumSegments(); i++) {
            naiveIndexManager.getIndexSegmentPositional(i);
        }
        int naive_wc = PageFileChannel.writeCounter;
        int naive_rc = PageFileChannel.readCounter;
        PageFileChannel.resetCounters();
        for (int i = 0; i < 3000; i++) {
            dvlIndexManager.addDocument(new Document("cat Dot cat Dog I can not tell the difference between cat and Dog"));
            dvlIndexManager.addDocument(new Document("cat and dog have a lot of difference"));
            dvlIndexManager.addDocument(new Document("Dog can be very different from cat"));
        }
        dvlIndexManager.flush();
        for (int i = 0; i < dvlIndexManager.getNumSegments(); i++) {
            dvlIndexManager.getIndexSegmentPositional(i);
        }
        int compress_wc = PageFileChannel.writeCounter;
        int compress_rc = PageFileChannel.readCounter;
        Assert.assertTrue(naive_rc > 1.5 * compress_rc);
        Assert.assertTrue(naive_wc > 1.5 * compress_wc);
        System.out.println("\033[0;32m");
        System.out.println("Naive compress write: " + naive_wc + " pages");
        System.out.println("Naive compress read: " + naive_rc + " pages");
        System.out.println("Your compress write: " + compress_wc + " pages");
        System.out.println("Your compress read: " + compress_rc + " pages");
        System.out.println("\033[0m");
    }

    @Test
    public void test5() {
        Assert.assertEquals(0, PageFileChannel.readCounter);
        Assert.assertEquals(0, PageFileChannel.writeCounter);
        for (int i = 0; i < 3000; i++) {
            naiveIndexManager.addDocument(new Document("cat" + " cat" + " cat" + " and dog" + " dog" + " dog"));
            naiveIndexManager.addDocument(new Document("pepsi" + " pepsi" + " pepsi" + " or coke" + " coke" + " coke"));
            naiveIndexManager.addDocument(new Document("microsoft" + " microsoft" + i + " microsoft" + " vs apple" + " apple" + " apple" + i));
        }
        naiveIndexManager.flush();
        for (int i = 0; i < naiveIndexManager.getNumSegments(); i++) {
            naiveIndexManager.getIndexSegmentPositional(i);
        }
        int naive_wc = PageFileChannel.writeCounter;
        int naive_rc = PageFileChannel.readCounter;
        PageFileChannel.resetCounters();

        for (int i = 0; i < 3000; i++) {
            dvlIndexManager.addDocument(new Document("cat" + " cat" + " cat" + " and dog" + " dog" + " dog"));
            dvlIndexManager.addDocument(new Document("pepsi" + " pepsi" + " pepsi" + " or coke" + " coke" + " coke"));
            dvlIndexManager.addDocument(new Document("microsoft" + " microsoft" + i + " microsoft" + " vs apple" + " apple" + " apple" + i));
        }
        dvlIndexManager.flush();
        for (int i = 0; i < dvlIndexManager.getNumSegments(); i++) {
            dvlIndexManager.getIndexSegmentPositional(i);
        }
        int compress_wc = PageFileChannel.writeCounter;
        int compress_rc = PageFileChannel.readCounter;
        Assert.assertTrue("naive write counter > 1.5 delta compress write count  \n Actual  naive write: " + naive_wc + " delta write count: " + compress_wc, naive_wc > 1.5 * compress_wc);
        Assert.assertTrue("naive write counter > 1.5 delta compress read count, \n Actual naive write: " + naive_rc + " delta write count: " + compress_rc, naive_rc > 1.5 * compress_rc);
        System.out.println("\033[0;32m");
        System.out.println("Naive compress write: " + naive_wc + " pages");
        System.out.println("Naive compress read: " + naive_rc + " pages");
        System.out.println("Your compress write: " + compress_wc + " pages");
        System.out.println("Your compress read: " + compress_rc + " pages");
        System.out.println("\033[0m");
    }

    @Test
    public void test6() {
        Assert.assertEquals(0, PageFileChannel.readCounter);
        Assert.assertEquals(0, PageFileChannel.writeCounter);
        StringBuilder doc1 = new StringBuilder("cat Dot cat Dog I can not tell the difference between cat and Dog");
        StringBuilder doc2 = new StringBuilder("cat and dog have a lot of difference");
        StringBuilder doc3 = new StringBuilder("Dog can be very different from cat");
        for (int i = 0; i < 1000; i++) {
            doc1.append(" cat Dot cat Dog I can not tell the difference between cat and Dog");
            doc2.append(" cat and dog have a lot of difference");
            doc3.append(" Dog can be very different from cat");
        }
        Document document1 = new Document(doc1.toString());
        Document document2 = new Document(doc2.toString());
        Document document3 = new Document(doc3.toString());
        for (int i = 0; i < 30; i++) {
            naiveIndexManager.addDocument(document1);
            naiveIndexManager.addDocument(document2);
            naiveIndexManager.addDocument(document3);
        }
        naiveIndexManager.flush();
        for (int i = 0; i < naiveIndexManager.getNumSegments(); i++) {
            naiveIndexManager.getIndexSegmentPositional(i);
        }
        int naive_wc = PageFileChannel.writeCounter;
        int naive_rc = PageFileChannel.readCounter;
        PageFileChannel.resetCounters();
        for (int i = 0; i < 30; i++) {
            dvlIndexManager.addDocument(document1);
            dvlIndexManager.addDocument(document2);
            dvlIndexManager.addDocument(document3);
        }
        dvlIndexManager.flush();
        for (int i = 0; i < dvlIndexManager.getNumSegments(); i++) {
            dvlIndexManager.getIndexSegmentPositional(i);
        }
        int compress_wc = PageFileChannel.writeCounter;
        int compress_rc = PageFileChannel.readCounter;
        Assert.assertTrue(naive_rc > 1.5 * compress_rc);
        Assert.assertTrue(naive_wc > 1.5 * compress_wc);
        System.out.println("\033[0;32m");
        System.out.println("Naive compress write: " + naive_wc + " pages");
        System.out.println("Naive compress read: " + naive_rc + " pages");
        System.out.println("Your compress write: " + compress_wc + " pages");
        System.out.println("Your compress read: " + compress_rc + " pages");
        System.out.println("\033[0m");
    }

    @Test
    public void test7() {
        for(int i = 0; i < 100; i++) {
            naiveIndexManager.addDocument(sampleDoc);
        }
        naiveIndexManager.flush();
        nonCompressWriteCounter = PageFileChannel.writeCounter;
        PageFileChannel.resetCounters();
        for (int i = 0; i < 100; i++) {
            dvlIndexManager.addDocument(sampleDoc);
        }
        dvlIndexManager.flush();
        compressWriteCounter = PageFileChannel.writeCounter;
        PageFileChannel.resetCounters();
        List<String> keywords = new ArrayList<>();
        keywords.add("Pride ");
        keywords.add("Prejudice");
        for(int i = 0; i < 10; i++) {
            naiveIndexManager.searchPhraseQuery(keywords);
        }
        double nonCompressReadCounter = PageFileChannel.readCounter;
        PageFileChannel.resetCounters();
        for (int i = 0; i < 10; i++) {
            dvlIndexManager.searchPhraseQuery(keywords);
        }
        double compressReadCounter = PageFileChannel.readCounter;
        PageFileChannel.resetCounters();
        assertEquals(true, compressReadCounter / nonCompressReadCounter < (double)2/3);
        assertEquals(true, compressWriteCounter/nonCompressWriteCounter < (double)2/3);
    }

    @Test
    public void test8() {
        naiveIndexManager.addDocument(emptyDoc);
        naiveIndexManager.flush();
        nonCompressWriteCounter = PageFileChannel.writeCounter;
        PageFileChannel.resetCounters();
        dvlIndexManager.addDocument(emptyDoc);
        dvlIndexManager.flush();
        compressWriteCounter = PageFileChannel.writeCounter;
        PageFileChannel.resetCounters();
        assertEquals(true,compressWriteCounter == nonCompressWriteCounter);
    }

    @After
    public void after() throws Exception {
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
        File cacheFolder = new File(indexFolder + "naive/");
        for (File file : cacheFolder.listFiles()) {
            try {
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cacheFolder.delete();
        cacheFolder = new File(indexFolder + "dvl/");
        for (File file : cacheFolder.listFiles()) {
            try {
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cacheFolder.delete();
        new File(indexFolder).delete();
        PageFileChannel.resetCounters();
        Path rootPath = Paths.get(indexFolder);
        Files.walk(rootPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        Files.deleteIfExists(rootPath);
    }

}
