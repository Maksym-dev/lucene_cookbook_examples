package org.edng.lucene7.example;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

public class WordNetSynonymsTest {

    private static final String INDEX_DIR = "data/index750";

    @BeforeAll
    static void setUpIndex() throws Throwable {
        File file = new File(INDEX_DIR);
        if (!file.exists()) {
            String[] args = {"wn_s.pl", INDEX_DIR};
            Syns2IndexExt.main(args);
        }
    }

    @Test
    public void testExpand() throws Throwable {
        String[] args2 = {INDEX_DIR, "big dog"};
        SynExpandExt.main(args2);
    }

    @Test
    public void testExpandExtended() throws Throwable {
        String[] args2 = {INDEX_DIR, "big dog"};

        FSDirectory directory = FSDirectory.open(new File(args2[0]).toPath());
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        String query = args2[1];
        String field = "contents";

        Query q = SynExpandExt.expand(query, searcher, new StandardAnalyzer(), field, 1.2f, 0.9f);
        System.out.println("Query: " + q.toString(field));
    }

    @Test
    public void synLoockup() throws Throwable {
        String[] args2 = {INDEX_DIR, "big"};
        SynLookupExt.main(args2);
    }
}
