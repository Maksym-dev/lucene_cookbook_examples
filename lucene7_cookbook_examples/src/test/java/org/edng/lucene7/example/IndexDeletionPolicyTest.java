package org.edng.lucene7.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.NoDeletionPolicy;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Created by ed on 12/24/14.
 */
public class IndexDeletionPolicyTest {

    @Test
    public void runTest() throws Exception {

        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setIndexDeletionPolicy(NoDeletionPolicy.INSTANCE);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document document = new Document();
        indexWriter.addDocument(document);
        indexWriter.commit();

        document = new Document();
        indexWriter.addDocument(document);
        indexWriter.commit();

        document = new Document();
        indexWriter.addDocument(document);
        indexWriter.rollback();

        indexWriter.close();

        List<IndexCommit> commits = DirectoryReader.listCommits(directory);

        for (IndexCommit commit : commits) {
            IndexReader reader = DirectoryReader.open(commit);
            if (commit.equals(commits.get(0))) {
                assertEquals(1, reader.numDocs(), "Commit 1 number of docs doesn't match");
            } else if (commit.equals(commits.get(1))) {
                assertEquals(2, reader.numDocs(),"Commit 2 number of docs doesn't match");
            }
            System.out.println("Commit " + commit.getSegmentCount());
            System.out.println("Number of docs: " + reader.numDocs());
        }
    }
}
