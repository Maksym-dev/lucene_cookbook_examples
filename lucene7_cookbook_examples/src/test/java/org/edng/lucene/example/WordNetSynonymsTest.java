package org.edng.lucene.example;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.File;

public class WordNetSynonymsTest {

    @Test
    public void testExpand() throws Throwable {
        String indexDir = "data/index750";
        String[] args2 = {indexDir, "big dog"};
        SynExpandExt.main(args2);
    }

    @Test
    public void testExpandExtended() throws Throwable {
        String indexDir = "data/index750";
        String[] args2 = {indexDir, "big dog"};

        FSDirectory directory = FSDirectory.open(new File(args2[0]).toPath());
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        String query = args2[1];
        String field = "contents";

        Query q = SynExpandExt.expand( query, searcher, new StandardAnalyzer(), field, 1.2f,0.9f);
        System.out.println( "Query: " + q.toString( field));
    }

    @Test
    public void synLoockup() throws Throwable {
        String indexDir = "data/index750";
        String[] args2 = {indexDir, "big"};
        SynLookupExt.main(args2);
    }

    @Test
    public void indexSynonyms() throws Throwable {
        String indexDir = "data/index750";
        String[] args = {"wn_s.pl", indexDir};
        Syns2IndexExt.main(args);
    }
}
