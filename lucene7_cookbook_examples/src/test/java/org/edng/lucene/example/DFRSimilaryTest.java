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
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.AfterEffectL;
import org.apache.lucene.search.similarities.BasicModelIF;
import org.apache.lucene.search.similarities.DFRSimilarity;
import org.apache.lucene.search.similarities.NormalizationH1;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.jupiter.api.Test;

/**
 * Created by ed on 1/24/15.
 */
public class DFRSimilaryTest {

    @Test
    public void runTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        DFRSimilarity similarity = new DFRSimilarity(new BasicModelIF(), new AfterEffectL(), new NormalizationH1());
        config.setSimilarity(similarity);
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
        indexSearcher.setSimilarity(similarity);
        QueryParser queryParser = new QueryParser("content", analyzer);
        Query query = queryParser.parse("humpty dumpty");

        TopDocs topDocs = indexSearcher.search(query, 100);
        assertEquals(3, topDocs.totalHits, "Wrong number of results");
        int i = 0;
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexReader.document(scoreDoc.doc);
            System.out.println(scoreDoc.score + ": " + doc.getField("content").stringValue());
            switch (i) {
                case 0:
                    assertEquals(1.59d, scoreDoc.score, 0.01d, "Score incorrect for position 0");
                    assertEquals("Humpty Dumpty sat on a wall,", doc.getField("content").stringValue(), "Wrong document on position 0");
                    break;
                case 1:
                    assertEquals(1.432535171508789d, scoreDoc.score, 0.01d, "Score incorrect for position 1");
                    assertEquals("Humpty Dumpty had a great fall.", doc.getField("content").stringValue(), "Wrong document on position 0");
                    break;
                case 2:
                    assertEquals(0.6400539875030518d, scoreDoc.score, 0.01d, "Score incorrect for position 2");
                    assertEquals("Couldn't put Humpty together again.", doc.getField("content").stringValue(), "Wrong document on position 0");
                    break;
            }
            i++;
        }
    }
}
