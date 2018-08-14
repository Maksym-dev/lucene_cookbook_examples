package org.edng.lucene.example;

import static org.junit.Assert.assertEquals;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.Test;

/**
 * Created by ed on 12/23/14.
 */
public class DocValuesTest {

    @Test
    public void runTest() throws Exception {
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig( analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document document = new Document();
        document.add(new SortedDocValuesField("sorted_string", new BytesRef("hello")));
        indexWriter.addDocument(document);

        document = new Document();
        document.add(new SortedDocValuesField("sorted_string", new BytesRef("world")));
        indexWriter.addDocument(document);

        indexWriter.commit();

        indexWriter.close();

        IndexReader reader = DirectoryReader.open(directory);

        document = reader.document(0);
        System.out.println("doc 0: " + document.toString());
        document = reader.document(1);
        System.out.println("doc 1: " + document.toString());


        for (LeafReaderContext context : reader.leaves()) {
            LeafReader atomicReader = context.reader();
            SortedDocValues sortedDocValues = DocValues.getSorted(atomicReader, "sorted_string");
            assertEquals("Count should be 2", 2, sortedDocValues.getValueCount());
            System.out.println("Value count: " + sortedDocValues.getValueCount());
            assertEquals("doc 0 sorted_string not match", "hello", sortedDocValues.lookupOrd(0).utf8ToString());
            System.out.println("doc 0 sorted_string: " + sortedDocValues.lookupOrd(0).utf8ToString());
            assertEquals("doc 1 sorted_string not match", "world", sortedDocValues.lookupOrd(1).utf8ToString());
            System.out.println("doc 1 sorted_string: " + sortedDocValues.lookupOrd(1).utf8ToString());
        }

        reader.close();
    }
}
