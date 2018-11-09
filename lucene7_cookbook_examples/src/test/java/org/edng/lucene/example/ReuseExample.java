package org.edng.lucene.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.jupiter.api.Test;

/**
 * Created by ed on 12/24/14.
 */
public class ReuseExample {

    @Test
    public void runTest() throws Exception {

        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        StringField stringField = new StringField("name", "", Field.Store.YES);

        String[] names = {"John", "Mary", "Peter"};
        for (String name : names) {
            stringField.setStringValue(name);
            doc.removeField("name");
            doc.add(stringField);
            indexWriter.addDocument(doc);
        }

        indexWriter.commit();
        IndexReader reader = DirectoryReader.open(directory);
        for (int i = 0; i < 3; i++) {
            doc = reader.document(i);
            switch (i) {
                case 0:
                    assertEquals("John", doc.getField("name").stringValue(), "Rank " + (i + 1) + " not match");
                    break;
                case 1:
                    assertEquals("Mary", doc.getField("name").stringValue(), "Rank " + (i + 1) + " not match");
                    break;
                case 2:
                    assertEquals("Peter", doc.getField("name").stringValue(), "Rank " + (i + 1) + " not match");
                    break;
            }
            System.out.println("DocId: " + i + ", name: " + doc.getField("name").stringValue());
        }
    }
}
