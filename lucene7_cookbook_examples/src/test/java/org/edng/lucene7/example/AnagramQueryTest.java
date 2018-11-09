package org.edng.lucene7.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.jupiter.api.Test;

/**
 * Created by ed on 1/8/15.
 */
public class AnagramQueryTest {

    @Test
    public void runTest() throws Exception {
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        Document doc = new Document();

        FieldType textFieldType = new FieldType();
        textFieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        textFieldType.setTokenized(true);
        textFieldType.setStored(true);
        textFieldType.setStoreTermVectors(true);

        Field textField = new Field("content", "", textFieldType);

        doc.removeField("content");
        textField.setStringValue("Listen, my child, to the silent.");
        doc.add(textField);
        indexWriter.addDocument(doc);

        doc.removeField("content");
        textField.setStringValue("silent a silence that turns valleys and echoes slippery,");
        doc.add(textField);
        indexWriter.addDocument(doc);

        doc.removeField("content");
        textField.setStringValue("that bends foreheads toward the ground.");
        doc.add(textField);
        indexWriter.addDocument(doc);

        indexWriter.commit();
        indexWriter.close();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        AnagramQuery query = new AnagramQuery(new TermQuery(new Term("content", "silent")), "content");
        TopDocs topDocs = indexSearcher.search(query, 100);

        assertEquals(2, topDocs.totalHits, 0.01, "Total hits not matching");

        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexReader.document(scoreDoc.doc);
            System.out.println("score: " + scoreDoc.score + ", content: " + doc.getField("content").stringValue());
        }

        indexReader.close();
    }
}
