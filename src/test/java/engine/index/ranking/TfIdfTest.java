
package engine.index.ranking;

import engine.analysis.*;
import engine.index.*;
import engine.storage.Document;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.junit.runners.Parameterized;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Supplier;

public class TfIdfTest {

    private InvertedIndexManager iim;
    private static String indexDir = "index/TfIdfTest/Part1";
    private static Document dummy = new Document("dummy");
    private InvertedIndexManager invertedIndexManager;
    private String folderPath = "./index/TfIdfTest/Part2";
    private Analyzer analyzer = new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer());
    private Compressor compressor = new DeltaVarLenCompressor();
    private Document[] documents = new Document[]{
            new Document("The cat (Felis catus) is a small carnivorous mammal"),
            new Document("Cats are similar in anatomy to the other felid species"),
            new Document("Female domestic cats can have kittens from spring to late autumn"),
            new Document("As of 2017, the domestic cat was the second-most popular pet in the U.S."),
            new Document("She lay curled up on the sofa in the back drawing-room in Harley Street, looking very lovely in her white muslin and blue ribbons"),
            new Document("I suffered too much myself"),
            new Document("Margaret went up into the old nursery at the very top of the house")
    };
    private  static InvertedIndexManager IIM;
    private static String path = "./index/TfIdfTest/Part3";
    private  static Document[] Documents;
    private final static int frequencyNum = 20;
    private Document doc1 = new Document("dog cat fish apple banana");
    private Document doc2 = new Document("dog apple banana fish");
    private Document doc3 = new Document("fish apple");
    private static final String indexFolder = "./index/TfIdfTest/Part4";
    private NaiveCompressor naiveCompressor = new NaiveCompressor();
    private DeltaVarLenCompressor deltaVarLenCompressor = new DeltaVarLenCompressor();
    private InvertedIndexManager naiveIndexManager;
    private InvertedIndexManager dvlIndexManager;
    private Analyzer an = new NaiveAnalyzer();
    private Compressor cp = new NaiveCompressor();
    private String file = "./index/TfIdfTest/Mine";
    private InvertedIndexManager manage = InvertedIndexManager.createOrOpenPositional(file, an, cp);
    private Document d1 = new Document("cat dog");
    private Document d2 = new Document("cat elephant");
    private Document d3 = new Document("wolf dog dog");
    private Document d4 = new Document("cat dog dog");

    /**
     * Parameterized test constructor.
     *
     * @param createIndexManager supplier that creates a inverted index for testing
     */

    public TfIdfTest(Supplier<InvertedIndexManager> createIndexManager) {
        iim = createIndexManager.get();
    }

    /**
     * Defines all parameters (index manager suppliers)
     *
     * Use supplier functions as parameters instead of concrete InvertedIndexManager
     * instances to allow re-instantiation for each test case.
     *
     * @return a collection of inverted index manager suppliers.
     */

    @Parameterized.Parameters
    public static Collection indexManagerSuppliers() {
        return Arrays.<Supplier<InvertedIndexManager>>asList(
                () -> InvertedIndexManager.createOrOpen(
                        indexDir, new NaiveAnalyzer()
                ),
                () -> InvertedIndexManager.createOrOpenPositional(
                        indexDir, new NaiveAnalyzer(), new NaiveCompressor()
                )
        );
    }

    @Before
    public void initialize() {
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 10;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 4;
        IIM = InvertedIndexManager.createOrOpenPositional(path,
                new ComposableAnalyzer(new PunctuationTokenizer(), new PorterStemmer()),
                new DeltaVarLenCompressor());
        Documents = new Document[]{
                new Document("Winter is coming."),
                new Document("You know nothing Jon Snow."),
                new Document("A lion doesn’t concern himself with the opinions of a sheep."),
                new Document("When you play the game of thrones, you win or you die."),
                new Document("A Lannister always pays his debts."),
                new Document("All men must die, but we are not men."),
                new Document("There is only one god and his name is Death, and there is only one thing we say to Death: Not today.")
        };
        this.naiveIndexManager = InvertedIndexManager.createOrOpenPositional(indexFolder +"naive/", new ComposableAnalyzer(new PunctuationTokenizer(),new PorterStemmer()),naiveCompressor);
        this.dvlIndexManager = InvertedIndexManager.createOrOpenPositional(indexFolder +"dvl/", new ComposableAnalyzer(new PunctuationTokenizer(),new PorterStemmer()),deltaVarLenCompressor);
    }

    @Test
    public void test1() {
        for (int i = 0; i < InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD * 2; i++) {
            iim.addDocument(dummy);
        }
        assertEquals(
                InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD,
                iim.getNumDocuments(0)
        );
        assertEquals(
                InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD,
                iim.getNumDocuments(1)
        );
    }

    @Test
    public void test2() {
        for (int i = 0; i < InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD * InvertedIndexManager.DEFAULT_MERGE_THRESHOLD; i++) {
            iim.addDocument(dummy);
        }
        for (int i = 0; i < InvertedIndexManager.DEFAULT_MERGE_THRESHOLD / 2; i++) {
            assertEquals(InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD * 2, iim.getNumDocuments(i));
        }
    }

    @Test
    public void test3() {
        invertedIndexManager = InvertedIndexManager.createOrOpenPositional(folderPath, analyzer, compressor);
        for (Document doc : documents) {
            invertedIndexManager.addDocument(doc);
        }
        invertedIndexManager.flush();
        assertEquals(7,invertedIndexManager.getNumDocuments(0));
    }

    @Test
    public void test4() {
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 4;
        invertedIndexManager = InvertedIndexManager.createOrOpenPositional(folderPath, analyzer, compressor);
        for (Document doc : documents) {
            invertedIndexManager.addDocument(doc);
        }
        invertedIndexManager.flush();
        int segmentNumber = invertedIndexManager.getNumSegments();
        int documentNum = 0;
        for (int i = 0; i < segmentNumber; i++) {
            documentNum += invertedIndexManager.getNumDocuments(i);
        }
        Assert.assertEquals(7, documentNum);
    }

    @Test
    public void test5(){
        for (int i = 0; i < Documents.length; i++){
            for (int j = 0; j < frequencyNum; j++){
                IIM.addDocument(Documents[i]);
            }
            IIM.flush();
        }
        assertEquals(frequencyNum, IIM.getDocumentFrequency(0,"winter"));
        assertEquals(frequencyNum, IIM.getDocumentFrequency(1,"snow"));
        assertEquals(frequencyNum, IIM.getDocumentFrequency(2,"lion"));
        assertEquals(frequencyNum, IIM.getDocumentFrequency(3,"plai"));
        assertEquals(frequencyNum, IIM.getDocumentFrequency(4,"pai"));
        assertEquals(frequencyNum, IIM.getDocumentFrequency(5,"men"));
        assertEquals(frequencyNum, IIM.getDocumentFrequency(6,"death"));

    }

    @Test
    public void test6(){
        for(int i = 0; i < Documents.length; i++){
            IIM.addDocument(documents[i]);
        }
        IIM.flush();
        assertEquals(0, IIM.getDocumentFrequency(0,"cs221"));
    }

    @Test
    public void test7() {
        this.dvlIndexManager.addDocument(doc1);
        this.dvlIndexManager.addDocument(doc2);
        this.dvlIndexManager.addDocument(doc3);
        this.dvlIndexManager.flush();
        assertEquals(this.dvlIndexManager.getDocumentFrequency(0, "dog"), 2);
        assertEquals(this.dvlIndexManager.getDocumentFrequency(0, "appl"), 3);
        assertEquals(this.dvlIndexManager.getDocumentFrequency(0, "people"), 0);
    }

    @Test
    public void test8() {
        this.naiveIndexManager.addDocument(doc1);
        this.naiveIndexManager.addDocument(doc2);
        this.naiveIndexManager.addDocument(doc3);
        this.naiveIndexManager.flush();
        assertEquals(this.naiveIndexManager.getDocumentFrequency(0, "dog"), 2);
        assertEquals(this.naiveIndexManager.getDocumentFrequency(0, "appl"), 3);
        assertEquals(this.naiveIndexManager.getDocumentFrequency(0, "people"), 0);
    }

    @Test
    public void test9() {
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 3;
        List<String> query = new ArrayList<>(Arrays.asList("cat", "dog", "dog"));
        manage.addDocument(d1);
        manage.addDocument(d2);
        manage.addDocument(d3);
        manage.addDocument(d4);
        manage.flush();
        Iterator<Pair<Document, Double>> actual = manage.searchTfIdf(query, 3);
        assertEquals(d4, actual.next().getLeft());
        assertEquals(d1, actual.next().getLeft());
        assertEquals(d3, actual.next().getLeft());
        assertFalse(actual.hasNext());
    }

    /**
     * helper method to delete directory recursively.
     *
     * @param path path to the directory to be deleted.
     * @throws IOException in case of input/output exception
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

    @After
    public void clean() throws IOException {
        try {
            deleteDirectory(Paths.get(indexDir));
        }
        catch (IOException ignored) { }
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
        InvertedIndexManager.DEFAULT_MERGE_THRESHOLD = 8;
        File dir = new File("./index/TfIdfTest/Part2");
        for (File file: dir.listFiles()){
            if (!file.isDirectory()){
                file.delete();
            }
        }
        dir.delete();
        try {
            File folder = new File(path);
            String[] entries = folder.list();
            for (String s: entries) {
                File currentFile = new File(folder.getPath(),s);
                currentFile.delete();
            }
            if (folder.delete()) { }
            else {
                System.out.println("Failed to delete the folder");
            }
        }
        catch (Exception e) {
            System.out.println("Something went wrong when deleting file");
        }
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
        try{
            File index = new File(file);
            String[] f = index.list();
            for(String s: f){
                File currentFile = new File(index.getPath(),s);
                currentFile.delete();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Files.deleteIfExists(Paths.get(file));
        InvertedIndexManager.DEFAULT_FLUSH_THRESHOLD = 1000;
    }

}
