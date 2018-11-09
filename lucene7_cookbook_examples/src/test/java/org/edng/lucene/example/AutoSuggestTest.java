package org.edng.lucene.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.search.suggest.analyzing.FreeTextSuggester;
import org.apache.lucene.search.suggest.analyzing.FuzzySuggester;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * This class has several tests on auto-suggest feature.
 *
 * Created by ed on 4/1/15.
 */
public class AutoSuggestTest {

    @Test
    public void analyzingSuggesterTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig( analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        doc.add(new StringField("content", "Humpty Dumpty sat on a wall", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new StringField("content", "Humpty Dumpty had a great fall", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new StringField("content", "All the king's horses and all the king's men", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new StringField("content", "Couldn't put Humpty together again", Field.Store.YES));
        indexWriter.addDocument(doc);

        indexWriter.commit();
        indexWriter.close();

        IndexReader indexReader = DirectoryReader.open(directory);

        Dictionary dictionary = new LuceneDictionary(indexReader, "content");

        AnalyzingSuggester analyzingSuggester = new AnalyzingSuggester(new RAMDirectory(), "suggest",
            new StandardAnalyzer());
        analyzingSuggester.build(dictionary);

        List<Lookup.LookupResult> lookupResultList = analyzingSuggester.lookup("humpty dum", false, 10);

        assertEquals(2, lookupResultList.size(), 0.01, "Number of hits not matching");

        for (Lookup.LookupResult lookupResult : lookupResultList) {
            System.out.println(lookupResult.key + ": " + lookupResult.value);
        }

    }

    @Test
    public void analyzingInfixSuggesterTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig( analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        doc.add(new StringField("content", "Humpty Dumpty sat on a wall", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new StringField("content", "Humpty Dumpty had a great fall", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new StringField("content", "All the king's horses and all the king's men", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new StringField("content", "Couldn't put Humpty together again", Field.Store.YES));
        indexWriter.addDocument(doc);

        indexWriter.commit();
        indexWriter.close();

        IndexReader indexReader = DirectoryReader.open(directory);

        Dictionary dictionary = new LuceneDictionary(indexReader, "content");

        AnalyzingInfixSuggester analyzingInfixSuggester = new AnalyzingInfixSuggester( directory, analyzer);
        analyzingInfixSuggester.build(dictionary);

        List<Lookup.LookupResult> lookupResultList = analyzingInfixSuggester.lookup("put h", false, 10);

        assertEquals(1, lookupResultList.size(), 0.01, "Number of hits not matching");

        for (Lookup.LookupResult lookupResult : lookupResultList) {
            System.out.println(lookupResult.key + ": " + lookupResult.value);
        }

    }

    @Test
    public void freeTextSuggesterTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig( analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        doc.add(new StringField("content", "Humpty Dumpty sat on a wall", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new StringField("content", "Humpty Dumpty had a great fall", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new StringField("content", "All the king's horses and all the king's men", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new StringField("content", "Couldn't put Humpty together again", Field.Store.YES));
        indexWriter.addDocument(doc);

        indexWriter.commit();
        indexWriter.close();

        IndexReader indexReader = DirectoryReader.open(directory);

        Dictionary dictionary = new LuceneDictionary(indexReader, "content");

        FreeTextSuggester freeTextSuggester = new FreeTextSuggester(analyzer, analyzer, 3);
        freeTextSuggester.build(dictionary);

        List<Lookup.LookupResult> lookupResultList = freeTextSuggester.lookup("h", false, 10);

        assertEquals(3, lookupResultList.size(), 0.01, "Number of hits not matching");

        for (Lookup.LookupResult lookupResult : lookupResultList) {
            System.out.println(lookupResult.key + ": " + lookupResult.value);
        }

    }

    @Test
    public void fuzzySuggesterTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig( analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        doc.add(new StringField("content", "Humpty Dumpty sat on a wall", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new StringField("content", "Humpty Dumpty had a great fall", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new StringField("content", "All the king's horses and all the king's men", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new StringField("content", "Couldn't put Humpty together again", Field.Store.YES));
        indexWriter.addDocument(doc);

        indexWriter.commit();
        indexWriter.close();

        IndexReader indexReader = DirectoryReader.open(directory);

        Dictionary dictionary = new LuceneDictionary(indexReader, "content");

        FuzzySuggester fuzzySuggester = new FuzzySuggester(new RAMDirectory(), "suggest-fuzzy", new StandardAnalyzer());
        fuzzySuggester.build(dictionary);

        List<Lookup.LookupResult> lookupResultList = fuzzySuggester.lookup("hampty", false, 10);

        assertEquals(2, lookupResultList.size(), 0.01, "Number of hits not matching");

        for (Lookup.LookupResult lookupResult : lookupResultList) {
            System.out.println(lookupResult.key + ": " + lookupResult.value);
        }

    }
}
