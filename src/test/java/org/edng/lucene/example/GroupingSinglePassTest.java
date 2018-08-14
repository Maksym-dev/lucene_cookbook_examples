package org.edng.lucene.example;

import static org.junit.Assert.assertEquals;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;
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
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ed on 3/28/15.
 */
public class GroupingSinglePassTest {

    @Test
    public void runTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig( analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        FieldType groupEndFieldType = new FieldType();
        groupEndFieldType.setStored(false);
        groupEndFieldType.setTokenized(false);
        groupEndFieldType.setIndexOptions(IndexOptions.DOCS);
        groupEndFieldType.setOmitNorms(true);
        groupEndFieldType.setDocValuesType(DocValuesType.SORTED);
        String groupEnd = "groupEnd";
        Field groupEndField = new Field(groupEnd, new BytesRef("x"), groupEndFieldType);
        Field groupEndField1 = new Field(groupEnd, new BytesRef(""), groupEndFieldType);

        List<Document> documentList = new ArrayList<>();
        Document doc = new Document();
        doc.add(new StringField("BookId", "B1", Field.Store.YES));
        doc.add(new StringField("Category", "Cat 1", Field.Store.YES));
        documentList.add(doc);
        doc.add(groupEndField1);
        doc = new Document();
        doc.add(new StringField("BookId", "B2", Field.Store.YES));
        doc.add(new StringField("Category", "Cat 1", Field.Store.YES));
        documentList.add(doc);
        doc.add(groupEndField);
        indexWriter.addDocuments(documentList);

        documentList = new ArrayList<>();
        doc = new Document();
        doc.add(new StringField("BookId", "B3", Field.Store.YES));
        doc.add(new StringField("Category", "Cat 2", Field.Store.YES));
        documentList.add(doc);
        doc.add(groupEndField);
        indexWriter.addDocuments(documentList);

        indexWriter.commit();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        GroupingSearch groupingSearch = new GroupingSearch(groupEnd);
        TopGroups topGroups = groupingSearch.search(indexSearcher, new MatchAllDocsQuery(), 0, 10);

        int totalGroupCount = topGroups.groups.length;
        assertEquals("Total group count not match", 2, totalGroupCount);
        System.out.println("Total group count: " + totalGroupCount);
        assertEquals("Total group hit count not match", 3, topGroups.totalGroupedHitCount);
        System.out.println("Total group hit count: " + topGroups.totalGroupedHitCount);

        for (GroupDocs groupDocs : topGroups.groups) {
            System.out.println("Group: " + groupDocs.groupValue);
            System.out.println("Group size: " + groupDocs.scoreDocs.length);
            for (ScoreDoc scoreDoc : groupDocs.scoreDocs) {
                doc = indexSearcher.doc(scoreDoc.doc);
                String category = doc.getField("Category").stringValue();
                String bookId = doc.getField("BookId").stringValue();
                System.out.println("Category: " + category + ", BookId: " + bookId);
            }
        }

    }
}

