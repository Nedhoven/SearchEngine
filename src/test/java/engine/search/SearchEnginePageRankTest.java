
package engine.search;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import engine.analysis.Analyzer;
import engine.analysis.ComposableAnalyzer;
import engine.analysis.PorterStemmer;
import engine.analysis.PunctuationTokenizer;
import engine.index.InvertedIndexManager;
import engine.index.Pair;

import org.junit.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SearchEnginePageRankTest {

    private static Path webPagesPath = Paths.get("./webpages");
    private static Path indexPath = Paths.get("./index/SearchEngineTest");
    private static Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
    private static SearchEngine searchEngine;
    private static BiMap<Integer, String> idUrlMap;

    @BeforeClass
    public static void setup() throws Exception {
        InvertedIndexManager invertedIndexManager = InvertedIndexManager.createOrOpen(indexPath.toString(), analyzer);
        searchEngine = SearchEngine.createSearchEngine(webPagesPath, invertedIndexManager);
        searchEngine.computePageRank(100);
        idUrlMap = HashBiMap.create();
        Files.readAllLines(webPagesPath.resolve("url.tsv")).stream().map(line -> line.split("\\s")).forEach(line -> {
            idUrlMap.put(Integer.parseInt(line[0].trim()), line[1].trim());
        });
    }

    @Test
    public void test1() {
        List<Pair<Integer, Double>> pageRankScores = searchEngine.getPageRankScores();
        Assert.assertTrue(pageRankScores.stream().limit(5).map(p -> p.getLeft()).map(id -> idUrlMap.get(id))
                .anyMatch(url -> url.contains("wics.ics.uci.edu")));
    }

    @Test
    public void test2() {
        List<Pair<Integer, Double>> pageRankScores = searchEngine.getPageRankScores();
        Assert.assertTrue(pageRankScores.stream().limit(20).map(p -> p.getLeft()).map(id -> idUrlMap.get(id))
                .anyMatch(url -> url.equalsIgnoreCase("www.ics.uci.edu")));
    }

    @Test
    public void test3() {
        List<Pair<Integer, Double>> pageRankScores = searchEngine.getPageRankScores();
        Assert.assertTrue(pageRankScores.stream().limit(100).map(p -> p.getLeft()).map(id -> idUrlMap.get(id))
                .anyMatch(url -> url.equalsIgnoreCase("isg.ics.uci.edu")));
    }

    @Test
    public void test4() {
        List<Pair<Integer, Double>> pageRankScores = searchEngine.getPageRankScores();
        Assert.assertTrue(pageRankScores.stream().limit(1000).map(p -> p.getLeft()).map(id -> idUrlMap.get(id))
                .filter(url -> url.contains("grape.ics.uci.edu")).count() > 5);
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
