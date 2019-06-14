
package engine.index.positional;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import engine.analysis.*;
import engine.index.InvertedIndexManager;
import engine.storage.Document;
import engine.index.Compressor;
import engine.index.NaiveCompressor;
import engine.index.PositionalIndexSegmentForTest;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class PositionalMergeTest {

    String indexFolder = "./index/PositionalMergeTest/";
    String[]  str = new String[]{
            "dog cat penguin whale",
            "snake bird cat lion",
            "fish bird whale penguin",
            "anteater fish snake dog",
            "cat dog bird penguin cat fish bird dog whale cat",
            "whale bird fish dog bird fish cat bird dog whale"
    };

    InvertedIndexManager im;
    PositionalIndexSegmentForTest imt;
    Analyzer analyzer;
    Compressor compressor;

    @After
    public void deleteWrittenFiles() throws IOException  {
        File file = new File(indexFolder);
        if (file.exists() && file.isDirectory()) {
            String[] fileList = file.list();
            File temp;
            for (int i = 0; i < fileList.length; i++) {
                if (indexFolder.endsWith(File.separator)) {
                    temp = new File(indexFolder + fileList[i]);
                } else {
                    temp = new File(indexFolder + File.separator + fileList[i]);
                }
                if (temp.isFile()) {
                    temp.delete();
                }
            }
        }
        Files.deleteIfExists(Paths.get(indexFolder));
    }

    @Test
    public void test0() {
        ComposableAnalyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), token -> token);
        InvertedIndexManager manager = InvertedIndexManager.createOrOpenPositional(indexFolder, analyzer, new NaiveCompressor());
        manager.addDocument(new Document(str[0]));
        manager.addDocument(new Document(str[1]));
        manager.flush();
        manager.addDocument(new Document(str[2]));
        manager.addDocument(new Document(str[3]));
        manager.flush();
        manager.addDocument(new Document(str[4]));
        manager.addDocument(new Document(str[5]));
        manager.flush();
        manager.addDocument(new Document(str[0]));
        manager.addDocument(new Document(str[1]));
        manager.flush();
        manager.mergeAllSegments();
        assertEquals(2, manager.getNumSegments());
    }

    @Test
    public void test1() {
        ComposableAnalyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), token -> token);
        InvertedIndexManager manager = InvertedIndexManager.createOrOpenPositional(indexFolder, analyzer, new NaiveCompressor());
        manager.addDocument(new Document(str[0]));
        manager.flush();
        manager.addDocument(new Document(str[2]));
        manager.flush();
        manager.mergeAllSegments();
        HashMap<String, List<Integer>> invertedLists = new HashMap<>();
        HashMap<Integer, Document> documents = new HashMap<>();
        Table<String, Integer, List<Integer>> positions = HashBasedTable.create();
        invertedLists.put("dog",Arrays.asList(0));
        invertedLists.put("cat",Arrays.asList(0));
        invertedLists.put("penguin",Arrays.asList(0,1));
        invertedLists.put("whale",Arrays.asList(0,1));
        invertedLists.put("fish",Arrays.asList(1));
        invertedLists.put("bird",Arrays.asList(1));
        documents.put(0, new Document(str[0]));
        documents.put(1, new Document(str[2]));
        positions.put("dog",0,Arrays.asList(0));
        positions.put("cat",0,Arrays.asList(1));
        positions.put("penguin",0,Arrays.asList(2));
        positions.put("whale",0,Arrays.asList(3));
        positions.put("fish",1,Arrays.asList(0));
        positions.put("bird",1,Arrays.asList(1));
        positions.put("whale",1,Arrays.asList(2));
        positions.put("penguin",1,Arrays.asList(3));
        PositionalIndexSegmentForTest expected = new PositionalIndexSegmentForTest(invertedLists,documents,positions);
        assertEquals(expected,manager.getIndexSegmentPositional(0));
    }

    @Test
    public void test2() {
        ComposableAnalyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), token -> token);
        InvertedIndexManager manager = InvertedIndexManager.createOrOpenPositional(indexFolder, analyzer, new NaiveCompressor());
        manager.addDocument(new Document(str[4]));
        manager.flush();
        manager.addDocument(new Document(str[5]));
        manager.flush();
        manager.mergeAllSegments();
        Table<String, Integer, List<Integer>> positions = HashBasedTable.create();
        positions.put("cat",0,Arrays.asList(0,4,9));
        positions.put("dog",0,Arrays.asList(1,7));
        positions.put("bird",0,Arrays.asList(2,6));
        positions.put("penguin",0,Arrays.asList(3));
        positions.put("fish",0,Arrays.asList(5));
        positions.put("whale",0,Arrays.asList(8));
        positions.put("whale",1,Arrays.asList(0,9));
        positions.put("bird",1,Arrays.asList(1,4,7));
        positions.put("fish",1,Arrays.asList(2,5));
        positions.put("dog",1,Arrays.asList(3,8));
        positions.put("cat",1,Arrays.asList(6));
        assertEquals(positions,manager.getIndexSegmentPositional(0).getPositions());
    }

    @Test
    public void test3(){
        Tokenizer tokenizer = new PunctuationTokenizer();
        Stemmer stemmer = new PorterStemmer();
        analyzer = new ComposableAnalyzer(tokenizer, stemmer);
        compressor = new NaiveCompressor();
        im = InvertedIndexManager.createOrOpenPositional(indexFolder, analyzer, compressor );
        int ori_flush_th = im.DEFAULT_FLUSH_THRESHOLD, ori_mer_th = im.DEFAULT_MERGE_THRESHOLD;
        im.DEFAULT_FLUSH_THRESHOLD = 1;
        im.DEFAULT_MERGE_THRESHOLD = 4;
        Document[] documents = new Document[] {
                new Document("In this project"),
                new Document("you'll be implementing a disk-based inverted index and the search operations."),
                new Document("At a high level, inverted index stores a mapping from keywords to the ids of documents they appear in."),
                new Document("A simple in-memory structure could be"),
                new Document("where each key is a keyword token"),
                new Document("and each value is a list of document IDs"),
                new Document("In this project, the disk-based index structure is based on the idea of LSM"),
                new Document("Its main idea is the following")
        };
        List<Document> seg1docs = new ArrayList<>();
        List<Document> seg2docs = new ArrayList<>();
        for (int i = 0; i < documents.length; i++){
            im.addDocument(documents[i]);
            if (i < documents.length-2) {
                seg1docs.add(documents[i]);
            } else {
                seg2docs.add(documents[i]);
            }
        }
        assertEquals(2, im.getNumSegments());
        checkSegment(0, seg1docs);
        checkSegment(1, seg2docs);
        checkPositional(0, seg1docs);
        checkPositional(1, seg2docs);
        im.DEFAULT_FLUSH_THRESHOLD = ori_flush_th;
        im.DEFAULT_MERGE_THRESHOLD = ori_mer_th;
    }

    @Test
    public void test4(){
        Tokenizer tokenizer = new PunctuationTokenizer();
        Stemmer stemmer = new PorterStemmer();
        analyzer = new ComposableAnalyzer(tokenizer, stemmer);
        compressor = new NaiveCompressor();
        im = InvertedIndexManager.createOrOpenPositional(indexFolder, analyzer, compressor );
        Document doc1 = new Document("Information retrieval");
        Document doc2 = new Document("There is no easy way");
        im.addDocument(doc1);
        im.addDocument(doc2);
        im.flush();
        Document doc3 = new Document("vector space and Boolean queries");
        Document doc4 = new Document("A general theory of information retrieval");
        im.addDocument(doc3);
        im.addDocument(doc4);
        im.flush();
        im.mergeAllSegments();
        List<Document> seg1Docs = new ArrayList<>();
        seg1Docs.add(doc1);
        seg1Docs.add(doc2);
        seg1Docs.add(doc3);
        seg1Docs.add(doc4);
        assertEquals(1, im.getNumSegments());
        checkSegment(0, seg1Docs);
        checkPositional(0, seg1Docs);
    }

    private Table<String, Integer, List<Integer>> createPositional(List<Document> docs){
        Tokenizer tokenizer = new PunctuationTokenizer();
        Stemmer stemmer = new PorterStemmer();
        analyzer = new ComposableAnalyzer(tokenizer, stemmer);
        compressor = new NaiveCompressor();
        im = InvertedIndexManager.createOrOpenPositional(indexFolder, analyzer, compressor );
        Table<String, Integer, List<Integer>> table = HashBasedTable.create();
        int docID = 0;
        for(Document d: docs){
            List<String> tokens = analyzer.analyze(d.getText());
            for (int i = 0; i < tokens.size(); i++){
                if (table.get(tokens.get(i), docID) == null){
                    table.put(tokens.get(i), docID, new ArrayList<>());
                }
                table.get(tokens.get(i), docID).add(i);
            }
            docID++;
        }
        return table;
    }

    private void checkPositional(int segmentNum, List<Document> docs){
        imt = im.getIndexSegmentPositional(segmentNum);
        Table<String, Integer, List<Integer>> pTable = imt.getPositions(), groundTruth = createPositional(docs);
        for (Table.Cell<String, Integer, List<Integer>> cell: groundTruth.cellSet()){
            List<Integer> l = pTable.get(cell.getRowKey(), cell.getColumnKey());
            assertTrue(l != null);
            Set<Integer> pSet = new HashSet<>(l);
            for(int pos : cell.getValue()){
                assertTrue(pSet.contains(pos));
            }
        }
    }

    private HashMap<String, HashSet<Document>> createInvertedIndex2Doc(List<Document> docs){
        HashMap<String, HashSet<Document>> output = new HashMap<>();
        for(Document d: docs){
            List<String> tokens = analyzer.analyze(d.getText());
            for (String token: tokens){
                if (!output.containsKey(token)){
                    output.put(token, new HashSet<>());
                }
                output.get(token).add(d);
            }
        }
        return output;
    }

    private void checkSegment(int segmentNum, List<Document> docs){
        imt = im.getIndexSegmentPositional(segmentNum);
        Map<String, List<Integer>> segIndex = imt.getInvertedLists();
        Map<Integer, Document> segDoc = imt.getDocuments();
        Map<String,HashSet<Document>> segGroundTrue = createInvertedIndex2Doc(docs);
        for (Map.Entry<String, List<Integer>> entry : segIndex.entrySet()) {
            String keyword = entry.getKey();
            List<Integer> postingList = entry.getValue();
                for (int docID : postingList){
                Document d = segDoc.get(docID);
                assertTrue(segGroundTrue.get(keyword).contains(d));
            }
        }
        for (Document d: docs){
            assertTrue(segDoc.containsValue(d));
        }
    }

}
