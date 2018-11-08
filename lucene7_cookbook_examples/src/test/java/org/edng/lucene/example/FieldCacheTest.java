package org.edng.lucene.example;

import static org.junit.Assert.assertEquals;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;

/**
 * Created by ed on 1/30/15.
 */
public class FieldCacheTest {

    @Test
    public void runTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig( analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        FieldType fieldType = new FieldType();
        fieldType.setStored(true);
        fieldType.setDocValuesType(DocValuesType.SORTED);
        StoredField stringField = new StoredField("name", new BytesRef(""), fieldType);

        String[] contents = {"alpha", "bravo", "charlie", "delta", "echo", "foxtrot"};
        for (String content : contents) {
            stringField.setBytesValue(new BytesRef(content));
            doc.removeField("name");
            doc.add(stringField);
            indexWriter.addDocument(doc);
        }

        indexWriter.commit();
        indexWriter.close();

        IndexReader indexReader = DirectoryReader.open(directory);

        BinaryDocValues cache = DocValues.getBinary(indexReader.leaves().get(0).reader(), "name");

        for (int i = 0; i < indexReader.maxDoc(); i++) {
            cache.advance(i);
            BytesRef bytesRef = cache.binaryValue();
            System.out.println(i + ": " + bytesRef.utf8ToString());
            switch (i) {
                case 0:
                    assertEquals("Result doesn't match on position " + i, "alpha", bytesRef.utf8ToString());
                    break;
                case 1:
                    assertEquals("Result doesn't match on position " + i, "bravo", bytesRef.utf8ToString());
                    break;
                case 2:
                    assertEquals("Result doesn't match on position " + i, "charlie", bytesRef.utf8ToString());
                    break;
                case 3:
                    assertEquals("Result doesn't match on position " + i, "delta", bytesRef.utf8ToString());
                    break;
                case 4:
                    assertEquals("Result doesn't match on position " + i, "echo", bytesRef.utf8ToString());
                    break;
                case 5:
                    assertEquals("Result doesn't match on position " + i, "foxtrot", bytesRef.utf8ToString());
                    break;
            }
        }

    }
}
