package org.edng.lucene.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Test;

/**
 * Created by ed on 1/30/15.
 */
public class SortTest {

    @Test
    public void runTest() throws Exception {
        String nameFieldNameSorted = "name";

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        FieldType fieldType = new FieldType();
        fieldType.setStored(true);
        fieldType.setDocValuesType(DocValuesType.SORTED);
        StoredField storedField = new StoredField(nameFieldNameSorted, new BytesRef(""), fieldType);

        String[] contents = {"foxtrot", "echo", "delta", "charlie", "bravo", "alpha"};
        for (String content : contents) {
            storedField.setBytesValue(new BytesRef(content));
            doc.removeField(nameFieldNameSorted);
            doc.add(storedField);
            indexWriter.addDocument(doc);
        }

        indexWriter.commit();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        WildcardQuery query = new WildcardQuery(new Term(nameFieldNameSorted, "*"));
        SortField sortField = new SortField(nameFieldNameSorted, SortField.Type.STRING_VAL);
        Sort sort = new Sort(sortField);

        TopDocs topDocs = indexSearcher.search(new MatchAllDocsQuery(), 100, sort);
        System.out.println("Found docs: " + topDocs.scoreDocs.length);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexReader.document(scoreDoc.doc);
            BytesRef bytesRef = doc.getField(nameFieldNameSorted).binaryValue();
            String s = new String(bytesRef.bytes);
            if (scoreDoc.equals(topDocs.scoreDocs[0])) {
                assertEquals("alpha", s, "Rank 1 result not match");
            }
            System.out.println(scoreDoc.score + ": " + s);
        }
    }
}
