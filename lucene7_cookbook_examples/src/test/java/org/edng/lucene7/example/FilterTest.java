package org.edng.lucene7.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by ed on 12/29/14.
 */
public class FilterTest {

    @Test
    public void runTest() throws Exception {
        Analyzer analyzer = new StandardAnalyzer();
        Path tempDirectory = Files.createTempDirectory("filter");
        Directory directory = FSDirectory.open(tempDirectory);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        Document doc = new Document();
        StringField stringField = new StringField("name", "", Field.Store.YES);
        TextField textField = new TextField("content", "", Field.Store.YES);
        IntPoint intField = new IntPoint("num", 0);
        StoredField intStored = new StoredField("num", 0);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("First");
        textField.setStringValue("Humpty Dumpty sat on a wall,");
        intField.setIntValue(100);
        intStored.setIntValue(100);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("Second");
        textField.setStringValue("Humpty Dumpty had a great fall.");
        intField.setIntValue(200);
        intStored.setIntValue(200);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("Third");
        textField.setStringValue("All the king's horses and all the king's men");
        intField.setIntValue(300);
        intStored.setIntValue(300);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("Fourth");
        textField.setStringValue("Couldn't put Humpty together again.");
        intField.setIntValue(400);
        intStored.setIntValue(400);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        indexWriter.commit();
        indexWriter.close();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Query query = new TermQuery(new Term("content", "humpty"));

        //        FieldValueFilter fieldValueFilter = new FieldValueFilter("name1");
        //TODO

//        FieldCacheTermsFilter fieldCacheTermsFilter = new FieldCacheTermsFilter("name", "First"); // single term field only because of FieldCache
        TermQuery termQuery = new TermQuery(new Term("name", "First"));

        //        QueryWrapperFilter queryWrapperFilter = new QueryWrapperFilter(new TermQuery(new Term("content", "together")));
        //TODO
        BooleanQuery bquery = new BooleanQuery.Builder()
            .add(query, BooleanClause.Occur.MUST)
            .add(termQuery, BooleanClause.Occur.FILTER)
            .build();

        TopDocs topDocs = indexSearcher.search(bquery, 100);
        System.out.println("Searching 'humpty'");
        assertEquals(1, topDocs.totalHits, "Number of result doesn't match");
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexReader.document(scoreDoc.doc);
            System.out.println("name: " +
                doc.getField("name").stringValue() +
                " - content: " +
                doc.getField("content").stringValue() +
                " - num: " +
                doc.getField("num").stringValue());
        }

        indexReader.close();
        directory.close();

        FileUtils.deleteDirectory(tempDirectory.toFile());
    }

    @Test
    public void runTestWithChainedFilterQueries() throws Exception {
        Analyzer analyzer = new StandardAnalyzer();
        Path tempDirectory = Files.createTempDirectory("filter");
        Directory directory = FSDirectory.open(tempDirectory);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        Document doc = new Document();
        StringField stringField = new StringField("name", "", Field.Store.YES);
        TextField textField = new TextField("content", "", Field.Store.YES);
        IntPoint intField = new IntPoint("num", 0);
        StoredField intStored = new StoredField("num", 0);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("First");
        textField.setStringValue("Humpty Dumpty sat on a wall,");
        intField.setIntValue(100);
        intStored.setIntValue(100);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("Second");
        textField.setStringValue("Humpty Dumpty had a great fall.");
        intField.setIntValue(200);
        intStored.setIntValue(200);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("Third");
        textField.setStringValue("All the king's horses and all the king's men");
        intField.setIntValue(300);
        intStored.setIntValue(300);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("Fourth");
        textField.setStringValue("Couldn't put Humpty together again.");
        intField.setIntValue(400);
        intStored.setIntValue(400);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        indexWriter.commit();
        indexWriter.close();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Query query = new TermQuery(new Term("content", "humpty"));

        TermRangeQuery termRangeQuery = new TermRangeQuery("name", new BytesRef("A"), new BytesRef("G"), true, true);
        Query numericRangeQuery = IntPoint.newRangeQuery("num", 200, 400);
//        ChainedFilter chainedFilter = new ChainedFilter(new Filter[]{termRangeFilter, numericRangeFilter});
        BooleanQuery chainedFilterQuery = new BooleanQuery.Builder()
            .add(termRangeQuery, BooleanClause.Occur.MUST)
            .add(numericRangeQuery, BooleanClause.Occur.SHOULD).build();
        BooleanQuery bquery = new BooleanQuery.Builder()
            .add(query, BooleanClause.Occur.MUST)
            .add(chainedFilterQuery, BooleanClause.Occur.FILTER)
            .build();

        TopDocs topDocs = indexSearcher.search(bquery, 100);
        System.out.println("Searching 'humpty'");
        assertEquals(2, topDocs.totalHits, "Number of result doesn't match");
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexReader.document(scoreDoc.doc);
            System.out.println("name: " +
                doc.getField("name").stringValue() +
                " - content: " +
                doc.getField("content").stringValue() +
                " - num: " +
                doc.getField("num").stringValue());
        }

        indexReader.close();
        directory.close();

        FileUtils.deleteDirectory(tempDirectory.toFile());
    }

    @Test
    public void runTestPrefixQuery() throws Exception {
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        Document doc = new Document();
        StringField stringField = new StringField("name", "", Field.Store.YES);
        TextField textField = new TextField("content", "", Field.Store.YES);
        IntPoint intField = new IntPoint("num", 0);
        StoredField intStored = new StoredField("num", 0);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("First");
        textField.setStringValue("Humpty Dumpty sat on a wall,");
        intField.setIntValue(100);
        intStored.setIntValue(100);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("Second");
        textField.setStringValue("Humpty Dumpty had a great fall.");
        intField.setIntValue(200);
        intStored.setIntValue(200);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("Third");
        textField.setStringValue("All the king's horses and all the king's men");
        intField.setIntValue(300);
        intStored.setIntValue(300);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("Fourth");
        textField.setStringValue("Couldn't put Humpty together again.");
        intField.setIntValue(400);
        intStored.setIntValue(400);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        indexWriter.commit();
        indexWriter.close();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Query query = new TermQuery(new Term("content", "humpty"));
//        PrefixFilter prefixFilter = new PrefixFilter(new Term("name", "F"));
        PrefixQuery prefixQuery = new PrefixQuery(new Term("name", "F"));

        BooleanQuery bquery = new BooleanQuery.Builder()
            .add(query, BooleanClause.Occur.MUST)
            .add(prefixQuery, BooleanClause.Occur.FILTER)
            .build();

        TopDocs topDocs = indexSearcher.search(bquery, 100);
        System.out.println("Searching 'humpty'");
        assertEquals(2, topDocs.totalHits, "Number of result doesn't match");
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexReader.document(scoreDoc.doc);
            System.out.println("name: " +
                doc.getField("name").stringValue() +
                " - content: " +
                doc.getField("content").stringValue() +
                " - num: " +
                doc.getField("num").stringValue());
        }

        indexReader.close();
    }

    @Test
    public void runTestWithTermRangeQuery() throws Exception {
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        Document doc = new Document();
        StringField stringField = new StringField("name", "", Field.Store.YES);
        TextField textField = new TextField("content", "", Field.Store.YES);
        IntPoint intField = new IntPoint("num", 0);
        StoredField intStored = new StoredField("num", 0);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("First");
        textField.setStringValue("Humpty Dumpty sat on a wall,");
        intField.setIntValue(100);
        intStored.setIntValue(100);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("Second");
        textField.setStringValue("Humpty Dumpty had a great fall.");
        intField.setIntValue(200);
        intStored.setIntValue(200);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("Third");
        textField.setStringValue("All the king's horses and all the king's men");
        intField.setIntValue(300);
        intStored.setIntValue(300);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("Fourth");
        textField.setStringValue("Couldn't put Humpty together again.");
        intField.setIntValue(400);
        intStored.setIntValue(400);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        indexWriter.commit();
        indexWriter.close();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Query query = new TermQuery(new Term("content", "humpty"));
//        FieldCacheRangeFilter fieldCacheTermRangeFilter = FieldCacheRangeFilter.newStringRange("name", "A", "G", true, true); // single term field only because of FieldCache
        TermRangeQuery termRangeQuery = new TermRangeQuery("name", new BytesRef("A"), new BytesRef("G"), true, true);

        BooleanQuery bquery = new BooleanQuery.Builder()
            .add(query, BooleanClause.Occur.MUST)
            .add(termRangeQuery, BooleanClause.Occur.FILTER)
            .build();

        TopDocs topDocs = indexSearcher.search(bquery, 100);
        System.out.println("Searching 'humpty'");
        assertEquals(2, topDocs.totalHits, "Number of result doesn't match");
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexReader.document(scoreDoc.doc);
            System.out.println("name: " +
                doc.getField("name").stringValue() +
                " - content: " +
                doc.getField("content").stringValue() +
                " - num: " +
                doc.getField("num").stringValue());
        }

        indexReader.close();
    }

    @Test
    public void runTestWithNumericRangeQuery() throws Exception {
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        Document doc = new Document();
        StringField stringField = new StringField("name", "", Field.Store.YES);
        TextField textField = new TextField("content", "", Field.Store.YES);
        IntPoint intField = new IntPoint("num", 0);
        StoredField intStored = new StoredField("num", 0);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("First");
        textField.setStringValue("Humpty Dumpty sat on a wall,");
        intField.setIntValue(100);
        intStored.setIntValue(100);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("Second");
        textField.setStringValue("Humpty Dumpty had a great fall.");
        intField.setIntValue(200);
        intStored.setIntValue(200);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("Third");
        textField.setStringValue("All the king's horses and all the king's men");
        intField.setIntValue(300);
        intStored.setIntValue(300);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        doc.removeField("name"); doc.removeField("content"); doc.removeField("num");
        stringField.setStringValue("Fourth");
        textField.setStringValue("Couldn't put Humpty together again.");
        intField.setIntValue(400);
        intStored.setIntValue(400);
        doc.add(stringField); doc.add(textField); doc.add(intField); doc.add(intStored);
        indexWriter.addDocument(doc);

        indexWriter.commit();
        indexWriter.close();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Query query = new TermQuery(new Term("content", "humpty"));
        Query numericRangeQuery = IntPoint.newRangeQuery("num", 200, 400);

        BooleanQuery bquery = new BooleanQuery.Builder()
            .add(query, BooleanClause.Occur.MUST)
            .add(numericRangeQuery, BooleanClause.Occur.FILTER)
            .build();

        TopDocs topDocs = indexSearcher.search(bquery, 100);
        System.out.println("Searching 'humpty'");
        assertEquals(2, topDocs.totalHits, "Number of result doesn't match");
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexReader.document(scoreDoc.doc);
            System.out.println("name: " +
                doc.getField("name").stringValue() +
                " - content: " +
                doc.getField("content").stringValue() +
                " - num: " +
                doc.getField("num").stringValue());
        }

        indexReader.close();
    }
}
