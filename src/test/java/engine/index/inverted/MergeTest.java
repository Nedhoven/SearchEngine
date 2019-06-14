
package engine.index.inverted;

import engine.analysis.*;
import engine.index.InvertedIndexManager;
import engine.index.InvertedIndexSegmentForTest;
import engine.storage.Document;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MergeTest {

    private String path = "./index/MergeTest";
    private PunctuationTokenizer tokenizer = new PunctuationTokenizer();
    private PorterStemmer porterStemmer = new PorterStemmer();
    private Analyzer analyzer = new ComposableAnalyzer(tokenizer, porterStemmer);
    private InvertedIndexManager iim;
    private Document[] documents = new Document[] {
            new Document("import edu uci ics cs221 analysis  Analyzer"),
            new Document("import edu uci ics cs221 analysis  ComposableAnalyzer"),
            new Document("import edu uci ics cs221 analysis  PorterStemmer"),
            new Document("import edu uci ics cs221 analysis  PunctuationTokenizer"),
            new Document("import edu uci ics cs221 index     inverted            InvertedIndexManager"),
            new Document("import edu uci ics cs221 storage   Document")
    };
    private Document[] documents1 = new Document[] {
            new Document("This morning I ate eggs"),
            new Document("Abstraction is often one floor above you."),
            new Document("Everyone was busy, so I went to the movie alone."),
            new Document("Please wait outside of the house."),
            new Document("Wednesday is hump day, but has anyone asked the camel if he's happy about it?"),
            new Document("He told us a very exciting adventure story."),
            new Document("My Mom tries to be cool by saying that she likes all the same things that I do."),
            new Document("She advised him to come back at once."),
            new Document("She works two jobs to make ends meet; at least, that was her reason for not having time to join us."),
            new Document("How was the math test?"),
            new Document("Eggs come from chickens."),
            new Document("Abstraction is used in this project."),
            new Document("Everyone was busy with math homework, and so I went out alone"),
            new Document("The job we did alone at the house of my boss and we weren't happy about it."),
            new Document("Camels are the horses of the middle east. "),
            new Document("Once upon a time the egg came from the camel and became a project."),
            new Document("At the end we had a chance to meet at the movie, but weren't thinking the same thing."),
            new Document("Math is like an egg the hard it is the better it is."),
            new Document("Jobs seem like a waste of time if you aren't happy"),
            new Document("My mom has a job that is like an adventure every day."),
            new Document("The weather outside was too cool for the camel."),
            new Document("Wednesday is the day that our chicken produces a lot of eggs."),
            new Document("Two jobs to make ends meet, means we need to less eggs."),
            new Document("As a camel do you have one or two humps?") };
    Document[] documents2 = new Document[] {
            new Document("Hello"),
            new Document("I like to eat pineapples."),
            new Document("Last week I took the express train to San Diego."),
            new Document("Pineapple Express was a great movie."),
            new Document("Mother always said to eat my vegetables, but I never listened."),
            new Document("Fridays are the best part of my week."),
            new Document("Last Friday I watched a movie."),
            new Document("Next Friday I will watch the new Avengers movie."),
            new Document("I've started a new diet with vegetables and I've had a terrible week."),
            new Document("Atleast I can still eat pineapples."),
            new Document("My mother would be proud of me."),
            new Document("I ate a lot of pineapples in San Diego."),
            new Document("I can't believe mother keeps eating all of my chocolate."),
            new Document("I live for chocolate and pineapples"),
            new Document("My favorite activity is watching a movie and eating pineapples at the same time"),
            new Document("Last Friday I ate my pineapples diced"),
            new Document("Next week I will try eating my pineapple raw"),
            new Document("I wonder if next week I will take the express back to San Diego"),
            new Document("Don't tell mother but I stole her credit card and used it to buy pineapples"),
            new Document("I predict the new Avengers movie will be worthy of a diced pineapples"),
            new Document("Unfortudently, the movie theater doesn't sell diced pineapples"),
            new Document("I'm going to have to find a way to get my diced pineapples into the movie theater")};

    @Before
    public void init() {
        iim = InvertedIndexManager.createOrOpen(path, analyzer);
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
    }

    @Test
    public void test1() {
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 2;
        iim.addDocument(documents[0]);
        iim.addDocument(documents[1]);
        int expectedNumSegments = 1;
        assertEquals(expectedNumSegments, iim.getNumSegments());
    }

    @Test
    public void test2() {
        iim.addDocument(new Document("Implement LSM-like disk-based inverted index that supports insertions"));
        iim.addDocument(new Document("Implement merge of inverted index segments"));
        iim.addDocument(new Document("Implement keyword search, boolean AND search, and boolean OR search"));
        iim.addDocument(new Document("(Optional Extra Credit): Implement deletions"));
        iim.mergeAllSegments();
        assertEquals(2, iim.getNumSegments());
    }

    @Test
    public void test3() {
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 4;
        iim.addDocument(new Document("In this project"));
        iim.addDocument(new Document("you'll be implementing a disk-based inverted index and the search operations."));
        iim.addDocument(new Document("At a high level, inverted index stores a mapping from keywords to the ids of documents they appear in."));
        iim.addDocument(new Document("A simple in-memory structure could be"));
        iim.addDocument(new Document("where each key is a keyword token"));
        iim.addDocument(new Document("and each value is a list of document IDs"));
        iim.addDocument(new Document("In this project, the disk-based index structure is based on the idea of LSM"));
        iim.addDocument(new Document("Its main idea is the following"));
        assertEquals(2, iim.getNumSegments());
    }

    @Test
    public void test4() {
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
        for (int i = 0; i < 4 ; i++){
            iim.addDocument(documents[i]);
        }
        iim.mergeAllSegments();
        int expectedNumSegments = 2;
        assertEquals(expectedNumSegments, iim.getNumSegments());
    }
    @Test
    public void test5() {
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 4;
        for (Document doc : documents) {
            iim.addDocument(doc);
        }
        int expectedNumSegments = 2;
        assertEquals(expectedNumSegments, iim.getNumSegments());
        InvertedIndexSegmentForTest it = iim.getIndexSegment(0);
        Map<String, List<Integer>> invertedLists = it.getInvertedLists();
        List<Integer> docIds = invertedLists.get("import");
        List<Integer> expectedDocIds = Arrays.asList(0, 1, 2, 3);
        assertEquals(expectedDocIds, docIds);
    }

    @Test
    public void test6() {
        InvertedIndexManager invertedManager;
        invertedManager = InvertedIndexManager.createOrOpen(path, new NaiveAnalyzer());
        Document doc0 = new Document("what is he doing today");
        Document doc1 = new Document("what a cute dog");
        Document doc2 = new Document("i saw you doing there today");
        Document doc3 = new Document("a dog is there");
        invertedManager.addDocument(doc0);
        invertedManager.addDocument(doc1);
        invertedManager.flush(); // segment 0
        invertedManager.addDocument(doc2);
        invertedManager.addDocument(doc3);
        invertedManager.flush(); // segment 1
        Map<String,List<Integer>> expectedList = new HashMap<>();
        expectedList.put("what", Arrays.asList(0,1));
        expectedList.put("is", Arrays.asList(0,3));
        expectedList.put("he", Arrays.asList(0));
        expectedList.put("doing", Arrays.asList(0,2));
        expectedList.put("today", Arrays.asList(0,2));
        expectedList.put("a", Arrays.asList(1,3));
        expectedList.put("cute", Arrays.asList(1));
        expectedList.put("dog", Arrays.asList(1,3));
        expectedList.put("i", Arrays.asList(2));
        expectedList.put("saw", Arrays.asList(2));
        expectedList.put("you", Arrays.asList(2));
        expectedList.put("there", Arrays.asList(2,3));
        Map<Integer, Document> expectedDocuments = new HashMap<>();
        expectedDocuments.put(0, doc0);
        expectedDocuments.put(1, doc1);
        expectedDocuments.put(2, doc2);
        expectedDocuments.put(3, doc3);
        InvertedIndexSegmentForTest expected = new InvertedIndexSegmentForTest(expectedList, expectedDocuments);
        invertedManager.mergeAllSegments();
        assertEquals(expected, invertedManager.getIndexSegment(0));
    }

    @Test
    public void test7() {
        InvertedIndexManager invertedManager;
        invertedManager = InvertedIndexManager.createOrOpen(path, new NaiveAnalyzer());
        Document doc0 = new Document("cat dog");
        Document doc1 = new Document("dog wolf cat");
        Document doc2 = new Document("wolf dog");
        Document doc3 = new Document("wolf cat");
        Document doc4 = new Document("pig wolf cat");
        Document doc5 = new Document("dog pig");
        Document doc6 = new Document("cat wolf");
        Document doc7 = new Document("cat pig dog");
        invertedManager.addDocument(doc0);
        invertedManager.addDocument(doc1);
        invertedManager.flush(); // segment 0
        invertedManager.addDocument(doc2);
        invertedManager.addDocument(doc3);
        invertedManager.flush(); // segment 1
        invertedManager.addDocument(doc4);
        invertedManager.addDocument(doc5);
        invertedManager.flush(); // segment 2
        invertedManager.addDocument(doc6);
        invertedManager.addDocument(doc7);
        invertedManager.flush(); // segment 3
        Map<String,List<Integer>> expectedList1 = new HashMap<>();
        expectedList1.put("cat",Arrays.asList(0,1,3));
        expectedList1.put("dog",Arrays.asList(0,1,2));
        expectedList1.put("wolf",Arrays.asList(1,2,3));
        Map<Integer, Document> expectedDocuments1 = new HashMap<>();
        expectedDocuments1.put(0,doc0);
        expectedDocuments1.put(1,doc1);
        expectedDocuments1.put(2,doc2);
        expectedDocuments1.put(3,doc3);
        Map<String,List<Integer>> expectedList2 = new HashMap<>();
        expectedList2.put("pig", Arrays.asList(0,1,3));
        expectedList2.put("wolf",Arrays.asList(0,2));
        expectedList2.put("cat",Arrays.asList(0,2,3));
        expectedList2.put("dog",Arrays.asList(1,3));
        Map<Integer, Document> expectedDocuments2 = new HashMap<>();
        expectedDocuments2.put(0,doc4);
        expectedDocuments2.put(1,doc5);
        expectedDocuments2.put(2,doc6);
        expectedDocuments2.put(3,doc7);
        InvertedIndexSegmentForTest expected1 = new InvertedIndexSegmentForTest(expectedList1, expectedDocuments1);
        InvertedIndexSegmentForTest expected2 = new InvertedIndexSegmentForTest(expectedList2, expectedDocuments2);
        invertedManager.mergeAllSegments();
        assertEquals(expected1,invertedManager.getIndexSegment(0));
        assertEquals(expected2,invertedManager.getIndexSegment(1));
    }

    @Test
    public void test8() {
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 2;
        for (Document d : documents1) {
            iim.addDocument(d);
            assert iim.getNumSegments() == 1;
        }
    }

    @Test
    public void test9() {
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 4;
        for (Document d : documents2) {
            iim.addDocument(d);
        }
        while (iim.getNumSegments() != 1) {
            iim.mergeAllSegments();
        }
        InvertedIndexSegmentForTest segment = iim.getIndexSegment(0);
        Map<Integer, Document> docs = segment.getDocuments();
        Map<String, List<Integer>> invertedLists = segment.getInvertedLists();
        assert docs.size() == 22;
        assert invertedLists.get("pineappl").size() == 12;
        assert invertedLists.get("movi").size() == 7;
        assert invertedLists.get("week").size() == 5;
        assert invertedLists.get("mother").size() == 4;
        assert invertedLists.get("dice").size() == 4;
        assert invertedLists.get("express").size() == 3;
        assert invertedLists.get("diego").size() == 3;
        assert invertedLists.get("fridai").size() == 4;
        assert invertedLists.get("san").size() == 3;
        assert invertedLists.get("veget").size() == 2;
        assert invertedLists.get("aveng").size() == 2;
        assert invertedLists.get("theater").size() == 2;
        assert invertedLists.get("chocol").size() == 2;
        assert invertedLists.get("stole").size() == 1;
    }

    @After
    public void cleanup() {
        File p = new File(path);
        String[] entries = p.list();
        for (int i = 0; i < entries.length; ++i) {
            File currentFile = new File(p.getPath(), entries[i]);
            currentFile.delete();
        }
        p.delete();
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 8;
    }

}
