package org.edng.lucene7.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.GroupingSearch;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Test;

/**
 * Created by ed on 3/28/15.
 */
public class GroupingTwoPassTest {

    @Test
    public void runTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig( analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        doc.add(new StringField("BookId", "B1", Field.Store.YES));
        FieldType fieldType = new FieldType();
        fieldType.setStored(true);
        fieldType.setDocValuesType(DocValuesType.SORTED);
        StoredField category1 = new StoredField("Category", new BytesRef("Cat 1"), fieldType);
        doc.add(category1);
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new StringField("BookId", "B2", Field.Store.YES));
        StoredField category2 = new StoredField("Category", new BytesRef("Cat 1"), fieldType);
        doc.add(category2);
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new StringField("BookId", "B3", Field.Store.YES));
        StoredField category3 = new StoredField("Category", new BytesRef("Cat 2"), fieldType);
        doc.add(category3);
        indexWriter.addDocument(doc);

        indexWriter.commit();

        GroupingSearch groupingSearch = new GroupingSearch("Category");
        groupingSearch.setAllGroups(true);
        groupingSearch.setGroupDocsLimit(10);

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        TopGroups topGroups = groupingSearch.search(indexSearcher, new MatchAllDocsQuery(), 0, 10);

        assertEquals(2, topGroups.totalGroupCount.longValue(), "Total group count not match");
        System.out.println("Total group count: " + topGroups.totalGroupCount);
        assertEquals(3, topGroups.totalGroupedHitCount, "Total group hit count not match");
        System.out.println("Total group hit count: " + topGroups.totalGroupedHitCount);

        for (GroupDocs groupDocs : topGroups.groups) {
            System.out.println("Group: " + ((BytesRef)groupDocs.groupValue).utf8ToString());
            for (ScoreDoc scoreDoc : groupDocs.scoreDocs) {
                doc = indexSearcher.doc(scoreDoc.doc);
                String category = doc.getField("Category").stringValue();
                String bookId = doc.getField("BookId").stringValue();
                System.out.println("Category: " + category + ", BookId: " + bookId);
            }
        }
    }
}

