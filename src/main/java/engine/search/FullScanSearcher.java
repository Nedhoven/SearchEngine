
package engine.search;

import engine.analysis.Analyzer;
import engine.storage.Document;
import engine.storage.DocumentStore;

import java.util.ArrayList;
import java.util.List;

/**
 * FullScanSearcher is a naive inefficient implementation of searching,
 * it reads all the documents one-by-one, analyze each doc, and compares it with the query.
 *
 * This class is solely for debugging purposes.
 *
 */

public class FullScanSearcher implements AutoCloseable {

    private final DocumentStore documentStore;
    private final Analyzer analyzer;

    public FullScanSearcher(DocumentStore docStore, Analyzer analyze) {
        documentStore = docStore;
        analyzer = analyze;
    }

    public List<Integer> search(String keyword) {
        List<Integer> resultDocs = new ArrayList<>();

        this.documentStore.iterator().forEachRemaining(entry -> {
            int docID = entry.getKey();
            Document doc = entry.getValue();
            List<String> keywordAnalyzed = analyzer.analyze(keyword);
            if (! keywordAnalyzed.isEmpty() && analyzer.analyze(doc.getText()).containsAll(keywordAnalyzed)) {
                resultDocs.add(docID);
            }
        });

        return resultDocs;
    }

    @Override
    public void close() {
        this.documentStore.close();
    }

}
