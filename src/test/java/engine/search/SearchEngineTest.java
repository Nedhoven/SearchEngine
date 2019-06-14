
package engine.search;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;

import engine.analysis.Analyzer;
import engine.analysis.ComposableAnalyzer;
import engine.analysis.PorterStemmer;
import engine.analysis.PunctuationTokenizer;
import engine.index.InvertedIndexManager;
import engine.index.Pair;
import engine.storage.Document;

import org.junit.*;
import org.junit.rules.Timeout;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class SearchEngineTest {

    @ClassRule
    public static Timeout classTimeout = Timeout.seconds(900);
    private static Path webPagesPath = Paths.get("./webpages");
    private static Path indexPath = Paths.get("./index/SearchEngineTest");
    private static Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
    private static InvertedIndexManager invertedIndexManager;
    private static SearchEngine searchEngine;
    private static BiMap<Integer, String> idUrlMap;

    @BeforeClass
    public static void setup() throws Exception {
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 2000;
        invertedIndexManager = InvertedIndexManager.createOrOpen(indexPath.toString(), analyzer);
        searchEngine = SearchEngine.createSearchEngine(webPagesPath, invertedIndexManager);
        searchEngine.writeIndex();
        searchEngine.computePageRank(100);
        idUrlMap = HashBiMap.create();
        Files.readAllLines(webPagesPath.resolve("url.tsv")).stream().map(line -> line.split("\\s")).forEach(line -> {
            idUrlMap.put(Integer.parseInt(line[0].trim()), line[1].trim());
        });
    }

    @Test
    public void test1() {
        Iterator<Pair<Document, Double>> resultIterator = searchEngine.searchQuery(
                Arrays.asList("ISG"),
                20, 1.0);
        ImmutableList<Pair<Document, Double>> resultList = ImmutableList.copyOf(resultIterator);
        Assert.assertEquals("isg.ics.uci.edu", getDocumentUrl(resultList.get(0).getLeft().getText()));
        Assert.assertTrue(resultList.stream().limit(10).map(p -> getDocumentUrl(p.getLeft().getText()))
                .anyMatch(p -> p.contains("hobbes.ics.uci.edu")));
        Assert.assertTrue(resultList.stream().limit(20).map(p -> getDocumentUrl(p.getLeft().getText()))
                .anyMatch(p -> p.equals("ipubmed2.ics.uci.edu")));
    }

    @Test
    public void test2() {
        Iterator<Pair<Document, Double>> resultIterator = searchEngine.searchQuery(Arrays.asList("anteater"),
                10, 1000000000.0);
        ImmutableList<Pair<Document, Double>> resultList = ImmutableList.copyOf(resultIterator);
        Assert.assertEquals(10, resultList.size());
        Assert.assertTrue(resultList.stream().limit(3).map(p -> p.getLeft())
                .anyMatch(doc -> doc.getText().contains("wics.ics.uci.edu")));
    }

    @Test
    public void test3() {
        Iterator<Pair<Document, Double>> resultIterator = searchEngine.searchQuery(Arrays.asList("anteater"),
                100, 0.0);
        List<Double> resultScoresCombined = ImmutableList.copyOf(resultIterator)
                .stream().map(p -> p.getRight()).collect(Collectors.toList());
        Iterator<Pair<Document, Double>> resultIteratorTfIdf = invertedIndexManager.searchTfIdf(Arrays.asList("anteater"), 100);
        List<Double> resultScoresTfIdf = ImmutableList.copyOf(resultIteratorTfIdf)
                .stream().map(p -> p.getRight()).collect(Collectors.toList());
        Assert.assertEquals(resultScoresTfIdf, resultScoresCombined);
    }

    /**
     * helper method
     *
     * @param text: to retireve
     * @return the main url stored in line 1
     */

    private static String getDocumentUrl(String text) {
        String[] result = text.split("\n");
        return result[1].trim();
    }

    @AfterClass
    public static void cleanup() {
        try {
            File index = new File(String.valueOf(indexPath));
            String[] entries = index.list();
            for (String s : entries) {
                File currentFile = new File(index.getPath(), s);
                currentFile.delete();
            }
            index.delete();
        }
        catch (Exception e) {
            System.err.println(e.toString());
        }
    }

}
