package org.edng.lucene.example;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.wordnet.SynExpand;
import org.apache.lucene.wordnet.SynLookup;
import org.apache.lucene.wordnet.Syns2Index;
import org.junit.Test;

import java.io.File;

public class WordNetSynonymsTest {

    @Test
    public void testExpand() throws Throwable {
        String indexDir = "data/index777";
        String[] args2 = {indexDir, "big dog"};
        SynExpand.main(args2);
    }

    @Test
    public void testExpandExtended() throws Throwable {
        String indexDir = "data/index777";
        String[] args2 = {indexDir, "big d?g"};

        FSDirectory directory = FSDirectory.open(new File(args2[0]));
        IndexSearcher searcher = new IndexSearcher(directory, true);
        String query = args2[1];
        String field = "contents";

        Query q = SynExpandExt.expand( query, searcher, new StandardAnalyzer(Version.LUCENE_CURRENT), field, 1.2f,0.9f);
        System.out.println( "Query: " + q.toString( field));
//        SynExpand.main(args2);
    }

    @Test
    public void synLoockup() throws Throwable {
        String indexDir = "data/index777";
        String[] args2 = {indexDir, "big"};
        SynLookup.main(args2);
    }

    @Test
    public void indexSynonyms() throws Throwable {
        String indexDir = "data/index777";
        String[] args = {"wn_s.pl", indexDir};
        Syns2Index.main(args);
    }
}
