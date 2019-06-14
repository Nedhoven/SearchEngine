
package engine.search;

import engine.analysis.Analyzer;
import engine.analysis.NaiveAnalyzer;
import engine.storage.Document;
import engine.storage.DocumentStore;

import engine.storage.MapdbDocStore;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FullScanSearcherTest {

    @Test
    public void test1() {
        DocumentStore documentStore = MapdbDocStore.createOrOpen("./docs.db");
        documentStore.addDocument(0, new Document("UCI CS221 Information Retrieval"));
        documentStore.addDocument(1, new Document("Information Systems"));
        documentStore.addDocument(2, new Document("UCI ICS"));
        documentStore.close();
        documentStore = MapdbDocStore.createOrOpen("./docs.db");
        String query = "uci";
        Analyzer analyzer = new NaiveAnalyzer();
        FullScanSearcher fullScanSearcher = new FullScanSearcher(documentStore, analyzer);
        List<Integer> searchResult = fullScanSearcher.search(query);
        fullScanSearcher.close();
        assertEquals(new HashSet<>(searchResult), new HashSet<>(Arrays.asList(0, 2)));
    }

}
