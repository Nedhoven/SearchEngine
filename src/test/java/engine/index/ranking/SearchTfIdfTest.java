
package engine.index.ranking;

import engine.analysis.Analyzer;
import engine.analysis.ComposableAnalyzer;
import engine.analysis.PorterStemmer;
import engine.analysis.PunctuationTokenizer;
import engine.index.*;
import engine.storage.Document;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SearchTfIdfTest {

    private Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
    private String path = "./index/SearchTfIdfTest/Part1";
    private InvertedIndexManager invertedIndex;
    private Document doc1 = new Document("The University of California, Irvine is a public research university located in Irvine, California");
    private Document doc2 = new Document("Irvine University offers 87 undergraduate degrees and 129 graduate and professional degrees");
    private Document doc3 = new Document("Irvine Company earns a lot of money");
    private Document doc4 = new Document("2019 Mercedes-Benz UCI Mountain Bike World Cup");
    private Compressor compressor = new DeltaVarLenCompressor();
    private static InvertedIndexManager iim;
    private InvertedIndexManager manager;
    private String folderPath = "./index/SearchTfIdfTest/Part3";
    private Document[] docs = new Document[]{
            new Document("The breed's distinctive folded ears are produced by an incompletely dominant gene that affects the cartilage of the ears, causing the ears to fold forward and downward, giving a cap-like appearance to the head."),
            new Document("Smaller, tightly ears set in a cap-like fashion are preferred to a loose fold and larger ear."),
            new Document(" The large, round eyes and rounded head, cheeks, and whisker pads add to the overall rounded appearance."),
            new Document("Despite the folded ears, folds still use their aural appendages to express themselves—the ears swivel to listen, lie back in anger and prick up when the treat bag rustles.")};

    @Before
    public void initialize(){
        manager = InvertedIndexManager.createOrOpenPositional(folderPath, new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer()), new DeltaVarLenCompressor());
    }

    @Test
    public void test1() {
        invertedIndex = InvertedIndexManager.createOrOpenPositional(path, analyzer, compressor);
        invertedIndex.addDocument(doc1);
        invertedIndex.addDocument(doc2);
        invertedIndex.addDocument(doc3);
        invertedIndex.addDocument(doc4);
        invertedIndex.flush();
        List<String> phrase = new ArrayList<>();
        phrase.add("University");
        phrase.add("of");
        phrase.add("California");
        phrase.add("Irvine");
        Iterator<Pair<Document, Double>> iterate = invertedIndex.searchTfIdf(phrase, null);
        int counter = 0;
        while (iterate.hasNext()) {
            iterate.next();
            counter++;
        }
        assertEquals(3,counter);
    }

    @Test
    public void test2() {
        invertedIndex = InvertedIndexManager.createOrOpenPositional(path, analyzer, compressor);
        invertedIndex.addDocument(doc1);
        invertedIndex.addDocument(doc2);
        invertedIndex.addDocument(doc3);
        invertedIndex.addDocument(doc4);
        invertedIndex.flush();
        List<String> phrase = new ArrayList<>();
        phrase.add("University");
        phrase.add("of");
        phrase.add("California");
        phrase.add("Irvine");
        Iterator<Pair<Document, Double>> iterate = invertedIndex.searchTfIdf(phrase, 4);
        Double prev = Double.MAX_VALUE;
        while (iterate.hasNext()) {
            Double val = iterate.next().getRight();
            Assert.assertTrue(prev >= val);
            prev = val;
        }
    }

    @Test
    public void test3(){
        iim = InvertedIndexManager.createOrOpenPositional(
                path,
                new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer()),
                new DeltaVarLenCompressor()
        );
        Document doc0 = new Document("The purpose of life is a life with purpose");
        Document doc1 = new Document("The purpose of life is to code");
        Document doc2 = new Document("The purpose of life is to eat good food");
        Document doc3 = new Document("The purpose of life is to play counter strike");
        Document doc4 = new Document("The purpose of life is to play football");
        Document doc5 = new Document("The purpose of life is to sleep");
        iim.addDocument(doc0);
        iim.addDocument(doc1);
        iim.addDocument(doc2);
        iim.addDocument(doc3);
        iim.addDocument(doc4);
        iim.addDocument(doc5);
        iim.flush();
        List<String> searchKeyword = new ArrayList<>(Arrays.asList("life"));
        Iterator<Pair<Document, Double>> res = iim.searchTfIdf(searchKeyword, 6);
        int count = 0;
        while (res.hasNext()){
            res.next();
            count ++;
        }
        assertEquals(6, count);
    }

    @Test
    public void test4(){
        iim = InvertedIndexManager.createOrOpenPositional(
                path,
                new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer()),
                new DeltaVarLenCompressor()
        );
        Document doc1 = new Document("The purpose of life is to code");
        Document doc2 = new Document("The purpose of life of life of life is to eat good food");
        Document doc3 = new Document("Life of Pi is a good movie");
        iim.addDocument(doc1);
        iim.addDocument(doc2);
        iim.addDocument(doc3);
        iim.flush();
        List<String> searchKeyword = new ArrayList<>(Arrays.asList("The", "purpose", "of", "life", "is"));
        Iterator<Pair<Document, Double>> res = iim.searchTfIdf(searchKeyword, 3);
        List<String> expected = new ArrayList<>();
        List<String> actual = new ArrayList<>();
        int count = 0;
        expected.add(doc1.getText());
        expected.add(doc2.getText());
        expected.add(doc3.getText());
        while (res.hasNext()){
            count ++;
            Pair<Document, Double> result = res.next();
            actual.add(result.getLeft().getText());
        }
        assertEquals(3, count);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test5(){
        iim = InvertedIndexManager.createOrOpenPositional(
                path,
                new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer()),
                new DeltaVarLenCompressor()
        );
        Document doc0 = new Document("The purpose of life is a life with purpose");
        Document doc1 = new Document("The purpose of life is to code");
        Document doc2 = new Document("The purpose of life is to eat good food");
        Document doc3 = new Document("The purpose of life is to play counter strike");
        Document doc4 = new Document("The purpose of life is to play football");
        Document doc5 = new Document("The purpose of life is to sleep");
        iim.addDocument(doc0);
        iim.addDocument(doc1);
        iim.addDocument(doc2);
        iim.addDocument(doc3);
        iim.addDocument(doc4);
        iim.addDocument(doc5);
        iim.flush();
        List<String> searchKeyword = new ArrayList<>(Arrays.asList("JCBKiKhudai"));
        Iterator<Pair<Document, Double>> res = iim.searchTfIdf(searchKeyword, 3);
        assertTrue(!res.hasNext());
    }

    @Test
    public void test6(){
        for(int i = 0; i < docs.length; i ++){
            manager.addDocument(docs[i]);
        }
        manager.flush();
        if (manager.getNumSegments() > 1)
            manager.mergeAllSegments();
        List<String> keywords = new ArrayList<>(Arrays.asList("fold","ears","round"));
        Iterator<Pair<Document, Double>> it = manager.searchTfIdf(keywords,3);
        List<Document> dList = new ArrayList<>();
        dList.add(docs[2]);
        dList.add(docs[3]);
        dList.add(docs[0]);
        TestCase.assertTrue(it.hasNext());
        int counter = 0;
        while (it.hasNext()){
            Document d = it.next().getLeft();
            assertEquals(d, dList.get(counter));
            counter++;
        }
        assertEquals(3, counter);
    }

    @Test
    public void test7(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
        for(int i = 0; i < docs.length; i ++){
            manager.addDocument(docs[i]);
        }
        manager.flush();
        if (manager.getNumSegments() > 1)
            manager.mergeAllSegments();
        List<String> keywords = new ArrayList<>(Arrays.asList("fold","ears"));
        Iterator<Pair<Document, Double>> it = manager.searchTfIdf(keywords,0);
        assert !it.hasNext();
    }

    @After
    public void cleanUp() {
        PageFileChannel.resetCounters();
        File f = new File(path);
        File[] files = f.listFiles();
        for (File file : files) {
            file.delete();
        }
        f.delete();
        String path1 = "./index/SearchTfIdfTest/Part2";
        File dir = new File(path1);
        for (File file: dir.listFiles()){
            if (!file.isDirectory()){
                file.delete();
            }
        }
        dir.delete();
        dir = new File(folderPath);
        for (File file: dir.listFiles()){
            if (!file.isDirectory()){
                file.delete();
            }
        }
        dir.delete();
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 8;
    }

}
