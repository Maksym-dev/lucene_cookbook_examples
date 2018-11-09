package org.edng.lucene.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * Created by ed on 2/21/15.
 *
 */
public class NearRealTimeDirectoryReaderTest {

    @Test
    public void runTest() throws Exception {

        File indexDir = new File("data/index3");
        if (indexDir.exists()) {
            FileUtils.forceDelete(indexDir);
        }
        Directory directory = FSDirectory.open(indexDir.toPath());
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig( analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        TextField textField = new TextField("content", "", Field.Store.YES);
        String[] contents = {"Humpty Dumpty sat on a wall,",
                "Humpty Dumpty had a great fall.",
                "All the king's horses and all the king's men",
                "Couldn't put Humpty together again."};

        for (String content : contents) {
            textField.setStringValue(content);
            doc.removeField("content");
            doc.add(textField);
            indexWriter.addDocument(doc);
        }

        DirectoryReader directoryReader = DirectoryReader.open(indexWriter, true, true);
        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);

        QueryParser queryParser = new QueryParser("content", analyzer);
        Query query = queryParser.parse("humpty dumpty");

        TopDocs topDocs = indexSearcher.search(query, 100);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexSearcher.doc(scoreDoc.doc);
            if (scoreDoc.equals(topDocs.scoreDocs[0])) {
                assertEquals(1.1433706283569336, scoreDoc.score, 0.1, "Rank 1 score not match");
            }
            System.out.println(scoreDoc.score + ": " + doc.getField("content").stringValue());
        }

        indexWriter.commit();

    }
}
