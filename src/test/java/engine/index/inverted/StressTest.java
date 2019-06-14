
package engine.index.inverted;

import engine.analysis.*;
import engine.index.InvertedIndexManager;
import engine.index.InvertedIndexSegmentForTest;
import engine.index.PageFileChannel;
import engine.storage.Document;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class StressTest {

    private InvertedIndexManager iim;
    private Analyzer analyzer = new NaiveAnalyzer();
    private String indexDir = "./index/StressTest/Part1";
    private Document[] largeDocs = new Document[] {
            generateDoc(5),
            generateDoc(5)
    };
    private Document[] manyDocs = new Document[] {
            generateDoc(1),
            generateDoc(1),
            generateDoc(1),
            generateDoc(1),
            generateDoc(1),
            generateDoc(1),
            generateDoc(1),
            generateDoc(1),
            generateDoc(1),
            generateDoc(1)
    };
    private static Analyzer analyzing;
    private static InvertedIndexManager invertedIndexManager;
    private String folder = "./index/StressTest/Part2";

    @Before
    public void initialize() {
        iim = InvertedIndexManager.createOrOpen(indexDir, analyzer);
        iim.DEFAULT_FLUSH_THRESHOLD = 1;
        PageFileChannel.resetCounters();
        analyzing = new ComposableAnalyzer(new PunctuationTokenizer(),new PorterStemmer());
        invertedIndexManager = InvertedIndexManager.createOrOpen(folder, analyzing);
        invertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 200;
        invertedIndexManager.DEFAULT_MERGE_THRESHOLD = 6;
    }

    @Test(timeout = 300000)
    public void test1() {
        for (Document doc: largeDocs) {
            iim.addDocument(doc);
        }
        assertEquals(2, iim.getNumSegments());
        iim.mergeAllSegments();
        assertEquals(iim.getNumSegments(), 1);
        assertTrue(PageFileChannel.writeCounter >= 3);
        assertTrue(PageFileChannel.readCounter >= 2);
        InvertedIndexSegmentForTest segment = iim.getIndexSegment(0);
        Map<Integer, Document> docs = segment.getDocuments();
        assertEquals(docs.size(), largeDocs.length);
        Iterator<Document> itr = iim.searchQuery("university");
        int count = 0;
        while (itr.hasNext()) {
            itr.next();
            count++;
        }
        assertEquals(count, largeDocs.length);
    }

    @Test(timeout = 300000)
    public void test2() {
        for (Document doc: manyDocs) {
            iim.addDocument(doc);
        }
        assertTrue(PageFileChannel.writeCounter >= 11);
        assertTrue(PageFileChannel.readCounter >= 2);
        Iterator<Document> itr =
                iim.searchAndQuery(Arrays.asList("GibberishThatNotInDoc", "university"));
        assertTrue(!itr.hasNext());
    }

    @Test(timeout = 1200000)
    public void test3() {
        String text = "";
        try {
            URL url = new URL("https://grape.ics.uci.edu/wiki/public/raw-attachment/wiki/cs221-2019-spring-project2/Team2StressTest.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(inputLine);
                stringBuilder.append(System.lineSeparator());
            }
            bufferedReader.close();
            text = stringBuilder.toString().trim();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 1500; i++) {
            invertedIndexManager.addDocument(new Document(text));
        }
        invertedIndexManager.addDocument(new Document("qwertyuiop elizabeth"));
        invertedIndexManager.addDocument(new Document("qwertyuiop"));
        invertedIndexManager.flush();
        try {
            test4();
        }
        catch (Throwable e) {
            System.out.println("test4 FAILED");
            e.printStackTrace();
        }
        try {
            test5();
        }
        catch (Throwable e) {
            System.out.println("test4 FAILED");
            e.printStackTrace();
        }
    }

    public void test4(){
        Iterator<Document> result = invertedIndexManager.searchQuery("elizabeth");
        int counter = 0;
        while (result.hasNext()) {
            counter++;
            result.next();
        }
        assertEquals(1501, counter);
    }

    public void test5(){
        List<String> keywords = Arrays.asList("qwertyuiop", "elizabeth");
        Iterator<Document> result = invertedIndexManager.searchAndQuery(keywords);
        int counter = 0;
        while (result.hasNext()) {
            counter++;
            result.next();
        }
        assertEquals(1, counter);
    }

    /**
     * helper method
     *
     * @param path: the directory path
     * @throws IOException: if found IO error
     */
    private void deleteDirectory(Path path) throws IOException {
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteDirectory(entry);
                }
            }
        }
        Files.delete(path);
    }

    /**
     * helper method
     *
     * @param n: number for the document
     * @return the generated document
     */

    private Document generateDoc(int n) {
        try {
            URL dictResource = StressTest.class.getClassLoader()
                    .getResource("cs221_frequency_dictionary_en.txt");
            String text = Files.readAllLines(Paths.get(dictResource.toURI())).stream()
                    .map(line -> line.startsWith("\uFEFF") ? line.substring(1) : line)
                    .map(line -> line.split(" ")[0])
                    .collect(Collectors.joining(" "));
            InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
            InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 8;
            return new Document(String.join(" ", Collections.nCopies(n, text)));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void clean() throws IOException {
        deleteDirectory(Paths.get(indexDir));
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
        deleteDirectory(Paths.get(folder));
        iim.DEFAULT_FLUSH_THRESHOLD = 1000;
        invertedIndexManager.DEFAULT_MERGE_THRESHOLD = 8;
    }

}
