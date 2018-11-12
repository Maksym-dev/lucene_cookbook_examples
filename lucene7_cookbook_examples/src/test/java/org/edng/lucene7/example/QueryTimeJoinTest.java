package org.edng.lucene7.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.join.JoinUtil;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Test;

/**
 * Created by ed on 3/24/15.
 */
public class QueryTimeJoinTest {

    @Test
    public void runTest() throws Exception {
        String bookAuthorId = "bookAuthorId";
        int value = 1;

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig( analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        doc.add(new StringField("name", "A Book", Field.Store.YES));
        doc.add(new StringField("type", "book", Field.Store.YES));
        doc.add(new TextField(bookAuthorId, "1", Field.Store.NO));
        doc.add(new SortedDocValuesField(bookAuthorId, new BytesRef("1")));
        doc.add(new LongPoint("bookId", value));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new StringField("name", "An Author", Field.Store.YES));
        doc.add(new StringField("type", "author", Field.Store.YES));
        doc.add(new TextField("authorId", "1", Field.Store.NO));
        doc.add(new SortedDocValuesField("authorId", new BytesRef("1")));
        indexWriter.addDocument(doc);
        indexWriter.commit();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        String fromField = bookAuthorId;
        boolean multipleValuesPerDocument = false;
        String toField = "authorId";
        ScoreMode scoreMode = ScoreMode.Max;
        Query fromQuery = new TermQuery(new Term("type", "book"));

        Query joinQuery = JoinUtil.createJoinQuery(
                fromField,
                multipleValuesPerDocument,
                toField,
                fromQuery,
                indexSearcher,
                scoreMode);
        System.out.println("Query : " + joinQuery);
        TopDocs topDocs = indexSearcher.search(joinQuery, 10);

        assertEquals(value, topDocs.totalHits, "Total hits not match");
        System.out.println("Total hits: " + topDocs.totalHits);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexReader.document(scoreDoc.doc);
            System.out.println(scoreDoc.score + ": " + doc.getField("name").stringValue());
        }
    }
}
