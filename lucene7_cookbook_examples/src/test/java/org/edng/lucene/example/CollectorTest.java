package org.edng.lucene.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ed on 1/30/15.
 */
public class CollectorTest {

    public static class MyCollector implements Collector {

        private MyLeafCollector myLeafCollector = new MyLeafCollector();

        @Override
        public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
            return myLeafCollector;
        }

        @Override
        public boolean needsScores() {
            return true;
        }

        public MyCollector() {
        }

        public int getTotalHits() {
            return myLeafCollector.totalHits;
        }

        public ScoreDoc[] getScoreDocs() {
            if (myLeafCollector.scoreDocs != null) {
                return myLeafCollector.scoreDocs;
            }
            myLeafCollector.topDocs.sort((d1, d2) -> Float.compare(d2.score, d1.score));
            myLeafCollector.scoreDocs = myLeafCollector.topDocs.toArray(new ScoreDoc[0]);
            return myLeafCollector.scoreDocs;
        }
    }

    public static class MyLeafCollector implements LeafCollector {

        private int totalHits = 0;
        private List<ScoreDoc> topDocs = new ArrayList<>();
        private ScoreDoc[] scoreDocs;
        private Scorer scorer;

        @Override
        public void setScorer(Scorer scorer) {
            this.scorer = scorer;
        }

        @Override
        public void collect(int doc) throws IOException {
            float score = scorer.score();
            if (score > 0) {
                score += (1 / (doc + 1));
            }
            ScoreDoc scoreDoc = new ScoreDoc(doc, score);
            topDocs.add(scoreDoc);
            totalHits++;
        }
    }

    @Test
    public void runTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        TextField textField = new TextField("content", "", Field.Store.YES);

        String[] contents = {"Humpty Dumpty sat on a wall,",
                "Humpty Dumpty had a great fall.",
                "All the king's horses and all the king's men",
                "Couldn't put Humpty together again."};
        for (String content : contents) {
            textField.setStringValue(content);
            doc.removeField("content");
            doc.add(textField);
            indexWriter.addDocument(doc);
        }

        indexWriter.commit();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        QueryParser queryParser = new QueryParser("content", analyzer);
        Query query = queryParser.parse("humpty together");

        CollectorTest.MyCollector collector = new CollectorTest.MyCollector();
        indexSearcher.search(query, collector);

        assertEquals(3, collector.getTotalHits(), "Total should be 3");

        System.out.println(collector.getTotalHits());

        ScoreDoc[] scoreDocs = collector.getScoreDocs();
        for (ScoreDoc scoreDoc : scoreDocs) {
            System.out.println("doc " + scoreDoc.doc + ", " + scoreDoc.score);
        }
    }
}
