
package engine.index.ranking;

import engine.analysis.Analyzer;
import engine.analysis.ComposableAnalyzer;
import engine.analysis.PorterStemmer;
import engine.analysis.PunctuationTokenizer;
import engine.index.DeltaVarLenCompressor;
import engine.index.InvertedIndexManager;
import engine.index.PageFileChannel;
import engine.storage.Document;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DocumentFrequencyTest {

    private String path = "./index/DocumentFrequencyTest/Part1";
    private String path1 = "./index/DocumentFrequencyTest/Part2";
    private Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
    private InvertedIndexManager invertedList;
    private InvertedIndexManager manager;
    private Document doc1 = new Document("dog, bone and fishes");
    private Document doc2 = new Document("cats, fishes and dogs");
    private Document doc3 = new Document("fishes, birds and sky dog");
    private Document doc4 = new Document("cats, bones and something");
    private Document doc5 = new Document("Apple is the name of a dog and it is also the name of a tree.");
    private Document doc6 = new Document("The name of a dog is apple which is also a name of a tree.");
    private Document doc7 = new Document("Apple trees will have fruit once a year.");
    private Document doc8 = new Document("What is the name of that dog. Is it apple?");

    @Before
    public void setUp() {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        invertedList = InvertedIndexManager.createOrOpen(path, analyzer);
        invertedList.addDocument(new Document("cat dog toy"));
        invertedList.addDocument(new Document("cat Dot"));
        invertedList.addDocument(new Document("cat dot toy"));
        invertedList.flush();
        invertedList.addDocument(new Document("cat toy Dog"));
        invertedList.addDocument(new Document("toy dog cat"));
        invertedList.flush();
        invertedList.addDocument(new Document("cat Dog"));
        invertedList.flush();
        invertedList.addDocument(new Document("cat Dog"));
        invertedList.flush();
        manager = InvertedIndexManager.createOrOpenPositional(path1, analyzer, new DeltaVarLenCompressor());
        directory = new File(path1);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        manager.addDocument(doc1);
        manager.addDocument(doc2);
        manager.addDocument(doc3);
        manager.addDocument(doc4);
        manager.flush();
        manager.addDocument(doc5);
        manager.addDocument(doc6);
        manager.addDocument(doc7);
        manager.addDocument(doc8);
        manager.flush();
    }

    @Test
    public void Test1() {
        String words = "cat dog Toy Dot";
        List<String> new_words = analyzer.analyze(words);
        int result = invertedList.getDocumentFrequency(0, new_words.get(0));
        assertEquals(3, result);
        result = invertedList.getDocumentFrequency(1, new_words.get(0));
        assertEquals(2, result);
        result = invertedList.getDocumentFrequency(2, new_words.get(0));
        assertEquals(1, result);
        result = invertedList.getDocumentFrequency(0, new_words.get(1));
        assertEquals(1, result);
        result = invertedList.getDocumentFrequency(1, new_words.get(1));
        assertEquals(2, result);
        result = invertedList.getDocumentFrequency(2, new_words.get(1));
        assertEquals(1, result);
        result = invertedList.getDocumentFrequency(0, new_words.get(2));
        assertEquals(2, result);
        result = invertedList.getDocumentFrequency(1, new_words.get(2));
        assertEquals(2, result);
        result = invertedList.getDocumentFrequency(2, new_words.get(2));
        assertEquals(0, result);
        result = invertedList.getDocumentFrequency(0, new_words.get(3));
        assertEquals(2, result);
        result = invertedList.getDocumentFrequency(1, new_words.get(3));
        assertEquals(0, result);
        result = invertedList.getDocumentFrequency(2, new_words.get(3));
        assertEquals(0, result);
    }

    @Test
    public void Test2() {
        String words = "sdasjdlslsah";
        List<String> new_words = analyzer.analyze(words);
        int n = invertedList.getNumSegments();
        for (int i = 0; i < new_words.size(); i++) {
            for (int j = 0; j < n; j++) {
                int result = invertedList.getDocumentFrequency(j, new_words.get(i));
                assertEquals(0, result);
            }
        }
    }

    @Test
    public void test3() {
        invertedList.mergeAllSegments();
        String words = "cat dog Toy Dot";
        List<String> new_words = analyzer.analyze(words);
        int result = invertedList.getDocumentFrequency(0, new_words.get(0));
        assertEquals(5, result);
        result = invertedList.getDocumentFrequency(0, new_words.get(2));
        assertEquals(4, result);
        result = invertedList.getDocumentFrequency(1, new_words.get(2));
        assertEquals(0, result);
        result = invertedList.getDocumentFrequency(1, new_words.get(3));
        assertEquals(0, result);
        result = invertedList.getDocumentFrequency(1, new_words.get(0));
        assertEquals(2, result);
    }

    @Test
    public void test4() {
        List<Integer> expectedList = new ArrayList<>();
        expectedList.add(3);
        expectedList.add(3);
        List<Integer> resultList = new ArrayList<>();
        resultList.add(manager.getDocumentFrequency(0, "dog"));
        resultList.add(manager.getDocumentFrequency(1, "tree"));
        assertEquals(expectedList, resultList);
    }

    @Test
    public void test5() {
        int freqSum1 = 0;
        for(int i = 0; i < manager.getNumSegments(); i++){
            freqSum1 += manager.getDocumentFrequency(i, "dog");
        }
        manager.mergeAllSegments();
        int freqSum2 = 0;
        for (int i = 0; i < manager.getNumSegments(); i++){
            freqSum2 += manager.getDocumentFrequency(i, "dog");
        }
        assertEquals(6, freqSum1);
        assertEquals(6, freqSum2);
    }

    @After
    public void deleteTmp() {
        PageFileChannel.resetCounters();
        File f = new File(path);
        File[] files = f.listFiles();
        for (File file : files) {
            file.delete();
        }
        f.delete();
        File cacheFolder = new File(path1);
        for (File file : cacheFolder.listFiles()) {
            try {
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cacheFolder.delete();
    }

}





