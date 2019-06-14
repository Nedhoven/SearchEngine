
package engine;

import engine.analysis.Analyzer;
import engine.analysis.NaiveAnalyzer;
import engine.search.FullScanSearcher;
import engine.storage.Document;
import engine.storage.DocumentStore;
import engine.storage.MapdbDocStore;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * This is a Hello World program of the Search Engine.
 * It shows how to use the skeleton API of the search engine:
 *  1. use DocumentStore to add and save documents
 *  2. use FullScanSearcher and NaiveAnalyzer to do a search
 */

public class Main {

    public static void main(String[] args) throws Exception {
        String docStorePath = "./docs.db";
        Files.deleteIfExists(Paths.get(docStorePath));
        // create the document store, add a few documents, and close() to flush them to disk
        DocumentStore documentStore = MapdbDocStore.createOrOpen(docStorePath);
        // put information into the document
        documentStore.addDocument(0, new Document("Information Retrieval"));
        documentStore.addDocument(1, new Document("School of Information and Computer Sciences"));
        documentStore.addDocument(2, new Document("UCI School of Information"));
        // closing the document
        documentStore.close();
        // open the existing document store again. use FullScanSearcher to search a query
        documentStore = MapdbDocStore.createOrOpen(docStorePath);
        // item to look for
        String query = "school";
        Analyzer analyzer = new NaiveAnalyzer();
        FullScanSearcher fullScanSearcher = new FullScanSearcher(documentStore, analyzer);
        List<Integer> searchResult = fullScanSearcher.search(query);
        // the results
        System.out.println("query: " + query);
        System.out.println("search results: ");
        for (int docID : searchResult) {
            System.out.println(docID + ": " + documentStore.getDocument(docID));
        }
        // ending
        fullScanSearcher.close();
    }

}
