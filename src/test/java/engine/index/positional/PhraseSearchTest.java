
package engine.index.positional;

import engine.analysis.Analyzer;
import engine.analysis.ComposableAnalyzer;
import engine.analysis.PorterStemmer;
import engine.analysis.PunctuationTokenizer;
import engine.index.InvertedIndexManager;
import engine.index.Compressor;
import engine.index.DeltaVarLenCompressor;
import engine.index.NaiveCompressor;
import engine.storage.Document;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.*;

public class PhraseSearchTest {

    private String indexPath = "./index/PhraseSearchTest/Part1";
    private Document[] documents = new Document[] {
            new Document("That sounds like a good idea. Maybe we should go out to eat beforehand."),
            new Document("Let's meet at Summer Pizza House. I have not gone there for a long time."),
            new Document("Good idea again. I heard they just came up with a new pizza."),
            new Document("We can meet at Summer Pizza House at noon. That will give us plenty of time to enjoy our pizza."),
            new Document("She graduated last June, and she will start her teaching career next week when the new school term begins."),
            new Document("The kids might even look forward to going to school since they have so many friends to play with."),
            new Document("I am always amazed by the things kindergarten teachers do so it's a good idea to let her join us.")
    };
    private Analyzer analyzer;
    private InvertedIndexManager index;
    Document[] documents1 = new Document[] { new Document("This morning I ate eggs"),
            new Document("Abstraction is often one floor above you."),
            new Document("Everyone was busy, so I went to the movie alone."),
            new Document("Please wait outside of the house."),
            new Document("Wednesday is hump day, but has anyone asked the camel if he’s happy about it?"),
            new Document("He told us a very exciting adventure story."),
            new Document("My Mom tries to be cool by saying that she likes all the same things that I do."),
            new Document("She advised him to come back at once."), new Document(
            "She works two jobs to make ends meet; at least, that was her reason for not having time to join us."),
            new Document("How was the math test?"), new Document("Eggs come from chickens."),
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
            new Document("As a camel do you have one or two humps?"),
            new Document("I hate going to the movie alone."),
            new Document("the movie alone."),
            new Document("You went to the  movie alone.")};
    Document[] documents2 = new Document[] { new Document("Hello"), new Document("I like to eat pineapples."),
            new Document("Last week I took the express train to San Diego."),
            new Document("Pineapple Express was a great movie."),
            new Document("Mother always said to eat my vegetables, but I never listened."),
            new Document("Fridays are the best part of my week."), new Document("Last Friday I watched a movie."),
            new Document("Next Friday I will watch the new Avengers movie."),
            new Document("I've started a new diet with vegetables and I've had a terrible week."),
            new Document("Atleast I can still eat pineapples."), new Document("My mother would be proud of me."),
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
            new Document("I'm going to have to find a way to get my diced pineapples into the movie theater"),};
    private final String path = "./index/PhraseSearchTest/Part2";
    private Document doc1 = new Document("too young, too simple, sometimes naive");
    private Document doc2 = new Document("I'm angry!");
    private Document doc3 = new Document("The West Virginia Central Junction is a place in United States of America");
    private Document doc4 = new Document("Los Ranchos de Albuquerque is the name of a place");
    private InvertedIndexManager iim;
    String folderPath = "./index/PhraseSearchTest/Part3";
    InvertedIndexManager manage;
    Document[] Documents1 = new Document[]{
            new Document("this is a test case"),
            new Document("Even though I devoted quantities of time to the test, cases appeared to be like they were rarely covered in class"),
            new Document("I had a test last Friday which is the hardest case I've ever experienced"),
            new Document("test of the case")
    };
    Document[] Documents2 = new Document[]{
            new Document("I visited new york city last summer."),
            new Document("New York is a popular travelling destination and a city with a huge population."),
            new Document("Before the new year's day, mom made some dishes with york, and we will drive to the city tomorrow.")
    };

    @Before
    public void before() {
        analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
        Compressor compressor = new DeltaVarLenCompressor();
        index = InvertedIndexManager.createOrOpenPositional(indexPath, analyzer, compressor);
        manage = InvertedIndexManager.createOrOpenPositional(folderPath, new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer()), new DeltaVarLenCompressor());
        iim.DEFAULT_FLUSH_THRESHOLD = 5;
    }

    @Test
    public void test1(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
        index.addDocument(documents[0]);
        Iterator<Document> itr = index.searchPhraseQuery(Arrays.asList("eat", "beforehand"));
        while (itr.hasNext()){
            assertEquals(itr.next(), documents[0]);
        }
    }

    @Test
    public void test2(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
        for (Document doc : documents){
            index.addDocument(doc);
        }
        int i = 0;
        List<Document> expectedDocuments = Arrays.asList(documents[1], documents[3]);
        Iterator<Document> itr = index.searchPhraseQuery(Arrays.asList("Summer", "Pizza", "House"));
        while (itr.hasNext()){
            assertEquals(expectedDocuments.get(i++), itr.next());
        }
    }

    @Test
    public void test3(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 4;
        for (Document doc : documents){
            index.addDocument(doc);
        }
        int i = 0;
        List<Document> expectedDocuments = Arrays.asList(documents[0], documents[2], documents[6]);
        Iterator<Document> itr = index.searchPhraseQuery(Arrays.asList("good", "idea"));
        while (itr.hasNext()){
            assertEquals(itr.next(), expectedDocuments.get(i++));
        }
    }

    @Test
    public void test4(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1;
        for (Document doc : documents){
            index.addDocument(doc);
        }
        Iterator<Document> itr = index.searchPhraseQuery(Arrays.asList("start", "teaching", "school"));
        assertFalse(itr.hasNext());
    }

    @Test
    public void test5() {
        List<String> phrase = new ArrayList<>();
        Iterator<Document> iterator = index.searchPhraseQuery(phrase);
        assertFalse(iterator.hasNext());
    }

    @Test
    public void test6() {
        index.addDocument(new Document("She goes to University of California, Los Angeles. She likes it there." +
                " Despite the fact that she doesn't have time to travel, she visits Orange county every weekend. Most of her friends live in " +
                "Irvine"));
        index.addDocument(new Document("Although California is not the largest state, there are many universities."));
        index.flush();
        List<String> phrase = new ArrayList<>();
        phrase.add("University");
        phrase.add("of");
        phrase.add("California");
        phrase.add("Irvine");
        Iterator<Document> iterator = index.searchPhraseQuery(phrase);
        assertFalse(iterator.hasNext());
    }

    @Test
    public void test7() {
        Document doc1 = new Document("He studies Computer Science at the University of California, Irvine.");
        Document doc2 = new Document("The University of California, Irvine is a public research university" +
                " located in Irvine, California.");
        Document doc3 = new Document("Unlike most other University of California campuses, UCI was not named" +
                " for the city it was built in; at the time of the university's founding (1965), the current city of Irvine" +
                " (incorporated in 1971) did not exist. The name Irvine is a reference to James Irvine, a landowner who" +
                " administered the 94,000-acre (38,000 ha) Irvine Ranch.");
        index.addDocument(doc1);
        index.addDocument(doc2);
        index.addDocument(doc3);
        index.flush();
        List<String> phrase = new ArrayList<>();
        phrase.add("UnivErsIty");
        phrase.add("of");
        phrase.add(",California");
        phrase.add("Irvine");
        Iterator<Document> iterator = index.searchPhraseQuery(phrase);
        int counter = 0;
        while (iterator.hasNext()) {
            String text = iterator.next().getText();
            assertTrue(text.equals(doc1.getText()) || text.equals(doc2.getText()));
            counter++;
        }
        assertEquals(2, counter);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test8() {
        InvertedIndexManager indexManager = InvertedIndexManager.createOrOpen("./index/Unsupported", analyzer);
        Document doc1 = new Document("He studies Computer Science at the University of California, Irvine.");
        Document doc2 = new Document("The University of California, Irvine is a public research university" +
                " located in Irvine, California.");
        Document doc3 = new Document("Unlike most other University of California campuses, UCI was not named" +
                " for the city it was built in; at the time of the university's founding (1965), the current city of Irvine" +
                " (incorporated in 1971) did not exist. The name Irvine is a reference to James Irvine, a landowner who" +
                " administered the 94,000-acre (38,000 ha) Irvine Ranch.");//text taken from https://en.wikipedia.org/wiki/University_of_California,_Irvine
        indexManager.addDocument(doc1);
        indexManager.addDocument(doc2);
        indexManager.addDocument(doc3);
        indexManager.flush();
        List<String> phrase = new ArrayList<>();
        phrase.add("University");
        phrase.add("of");
        phrase.add("California");
        phrase.add("Irvine");
        indexManager.searchPhraseQuery(phrase);
    }

    @Test
    public void test9() {
        Document doc = new Document("The University of California, Irvine is a public research university" +
                " located in Irvine, California.");
        for (int i = 0; i < 16; ++i) {
            index.addDocument(doc);
            index.flush();
        }
        while (index.getNumSegments() != 1) {
            index.mergeAllSegments();
        }
        List<String> phrase = new ArrayList<>();
        phrase.add("University");
        phrase.add("of");
        phrase.add("California");
        phrase.add("Irvine");
        Iterator<Document> iterator = index.searchPhraseQuery(phrase);
        int counter = 0;
        while (iterator.hasNext()) {
            iterator.next();
            counter++;
        }
        assertEquals(16, counter);
    }

    @Test
    public void test10() {
        for (Document doc : documents2) {
            index.addDocument(doc);
        }
        index.flush();
        queryIndex(Arrays.asList("movie", "theater"), 2);
        queryIndex(Arrays.asList("credit", "card"), 1);
        queryIndex(Arrays.asList("diced", "pineapples"), 3);
        queryIndex(Arrays.asList("eat", "pineapples"), 4);
        queryIndex(Arrays.asList("San", "Diego"), 3);
        queryIndex(Arrays.asList("Avengers", "Movie"), 2);
        queryIndex(Arrays.asList("next", "week"), 2);
        queryIndex(Arrays.asList("Last", "Friday"), 2);
    }

    @Test
    public void test11() {
        for (Document doc : documents1) {
            index.addDocument(doc);
        }
        index.flush();
        queryIndex(Arrays.asList("never", "mind"), 0);
    }

    @Test // TODO: too slow
    public void test12(){
        for (Document doc : documents1) {
            index.addDocument(doc);
            index.flush();
        }
        queryIndex(Arrays.asList("movie", "alone"),4);
    }

    @Test
    public void test13(){
        iim = InvertedIndexManager.createOrOpenPositional(path, new ComposableAnalyzer( new PunctuationTokenizer(), new PorterStemmer()), new NaiveCompressor());
        Iterator<Document> itr = iim.searchPhraseQuery(new ArrayList<String>());
        assertEquals(false, itr.hasNext());
    }

    @Test (expected = UnsupportedOperationException.class)
    public void test14(){
        iim = InvertedIndexManager.createOrOpen(path, new ComposableAnalyzer( new PunctuationTokenizer(), new PorterStemmer()));
        Iterator<Document> itr = iim.searchPhraseQuery(new ArrayList<String>());
    }

    @Test
    public void test15(){
        iim = InvertedIndexManager.createOrOpenPositional(path, new ComposableAnalyzer( new PunctuationTokenizer(), new PorterStemmer()), new NaiveCompressor());
        for(int i=0; i<InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD+1; i++){
            iim.addDocument(doc3);
            iim.addDocument(doc4);
        }
        List<String> phrases = new ArrayList<>();
        phrases.add("west");
        phrases.add("virginia");
        phrases.add("central");
        phrases.add("junction");
        Iterator<Document> itr = iim.searchPhraseQuery(phrases);
        int count = 0;
        while(itr.hasNext()){
            itr.hasNext();
            Document nextDoc = itr.next();
            assertEquals(doc3, nextDoc);
            count++;
        }
        assertEquals(InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD, count);
    }

    @Test
    public void test16(){
        iim = InvertedIndexManager.createOrOpenPositional(path, new ComposableAnalyzer( new PunctuationTokenizer(), new PorterStemmer()), new NaiveCompressor());
        iim.addDocument(doc2);
        for(int i=0; i<InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD*4; i++){
            iim.addDocument(doc1);
        }
        iim.mergeAllSegments();
        List<String> phrases = new ArrayList<>();
        phrases.add("young");
        Iterator<Document> iter = iim.searchPhraseQuery(phrases);
        int count = 0;
        while(iter.hasNext()){
            iter.hasNext();
            Document nextDoc = iter.next();
            assertEquals(doc1, nextDoc);
            count++;
        }
        assertEquals(InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD*4-1, count);
    }

    @Test
    public void test17(){
        iim = InvertedIndexManager.createOrOpenPositional(path, new ComposableAnalyzer( new PunctuationTokenizer(), new PorterStemmer()), new NaiveCompressor());
        iim.addDocument(doc2);
        for(int i=0; i<InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD*4; i++){
            iim.addDocument(doc1);
        }
        iim.mergeAllSegments();
        List<String> phrases = new ArrayList<>();
        phrases.add("too");
        phrases.add("young");
        phrases.add("too");
        phrases.add("simple");
        Iterator<Document> iter = iim.searchPhraseQuery(phrases);
        int count = 0;
        while(iter.hasNext()){
            iter.hasNext();
            Document nextDoc = iter.next();
            assertEquals(doc1, nextDoc);
            count++;
        }
        assertEquals(InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD*4-1, count);
        phrases.clear();
        phrases.add("young");
        phrases.add("naive");
        Iterator<Document> iter2 = iim.searchPhraseQuery(phrases);
        assertEquals(false, iter2.hasNext());
    }

    @Test
    public void test18(){
        for(Document doc : Documents1){
            manage.addDocument(doc);
        }
        manage.flush();
        Iterator<Document> it = manage.searchPhraseQuery(Arrays.asList("test", "case"));
        List<Document> res = new LinkedList<>();
        while(it.hasNext()){
            res.add(it.next());
        }
        assertEquals(3, res.size());
        TestCase.assertTrue(Documents1[0].getText().equals(res.get(0).getText()));
        TestCase.assertTrue(Documents1[1].getText().equals(res.get(1).getText()));
        TestCase.assertTrue(Documents1[3].getText().equals(res.get(2).getText()));
    }

    @Test
    public void test19(){
        for(Document doc : Documents2){
            manage.addDocument(doc);
        }
        manage.flush();
        Iterator<Document> it = manage.searchPhraseQuery(Arrays.asList("new", "york", "city"));
        while(it.hasNext()){
            Document result = it.next();
            TestCase.assertTrue(Documents2[0].getText().equals(result.getText()));
        }
    }

    @Test
    public void test20(){
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 2;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 4;
        for (Document doc : Documents1){
            manage.addDocument(doc);
        }
        Iterator<Document> it = manage.searchPhraseQuery(Arrays.asList("test", "case"));
        List<Document> expectedResult = new LinkedList<>();
        expectedResult.add(Documents1[0]);
        expectedResult.add(Documents1[1]);
        expectedResult.add(Documents1[3]);
        List<Document> result = new LinkedList<>();
        while(it.hasNext()){
            result.add(it.next());
        }
        assertEquals(expectedResult.size(), result.size());
        for(int i = 0; i < expectedResult.size(); i ++){
            TestCase.assertTrue(expectedResult.get(i).getText().equals(result.get(i).getText()));
        }
    }

    /**
     * helper method
     * @param keyWords: list of words
     * @param expectedCount: expected result return
     */

    private void queryIndex(List<String> keyWords, int expectedCount) {
        Iterator<Document> it = index.searchPhraseQuery(keyWords);
        int counter = 0;
        while (it.hasNext()) {
            counter++;
            it.next();
        }
        assertEquals(expectedCount, counter);
    }

    @After
    public void clean() {
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 8;
        try {
            File folder = new File(indexPath);
            String[] entries = folder.list();
            for(String s: entries) {
                File currentFile = new File(folder.getPath(),s);
                currentFile.delete();
            }
            folder.delete();
        }
        catch (Exception e) {
            System.out.println("Something went wrong when deleting file");
        }
        File dir = new File(path);
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
    }

}
