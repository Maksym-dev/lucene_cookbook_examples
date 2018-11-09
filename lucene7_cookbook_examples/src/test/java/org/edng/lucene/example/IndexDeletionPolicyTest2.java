package org.edng.lucene.example;

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
import org.apache.lucene.index.SnapshotDeletionPolicy;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Created by ed on 12/24/14.
 */
public class IndexDeletionPolicyTest2 {

    @Test
    public void runTest() throws Exception {

        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        SnapshotDeletionPolicy policy = new SnapshotDeletionPolicy(NoDeletionPolicy.INSTANCE);
        config.setIndexDeletionPolicy(policy);
        IndexWriter indexWriter = new IndexWriter(directory, config);
        IndexCommit lastSnapshot;

        Document document = new Document();
        indexWriter.addDocument(document);
        indexWriter.commit();

        lastSnapshot = policy.snapshot();

        document = new Document();
        indexWriter.addDocument(document);
        indexWriter.commit();

        lastSnapshot = policy.snapshot();

        document = new Document();
        indexWriter.addDocument(document);
        indexWriter.rollback();

        indexWriter.close();

        List<IndexCommit> commits = DirectoryReader.listCommits(directory);
        assertEquals(2, commits.size(), "Commits count doesn't match");
        System.out.println("Commits count: " + commits.size());

        for (IndexCommit commit : commits) {
            IndexReader reader = DirectoryReader.open(commit);
            System.out.println("Commit " + commit.getSegmentCount());
            System.out.println("Number of docs: " + reader.numDocs());
        }

        System.out.println("");
        assertEquals(2, policy.getSnapshotCount(), "Snapshots count doesn't match");
        System.out.println("Snapshots count: " + policy.getSnapshotCount());

        List<IndexCommit> snapshots = policy.getSnapshots();

        for (IndexCommit snapshot : snapshots) {
            IndexReader reader = DirectoryReader.open(snapshot);
            System.out.println("Snapshot " + snapshot.getSegmentCount());
            System.out.println("Number of docs: " + reader.numDocs());
        }

        policy.release(lastSnapshot);

        System.out.println("");
        assertEquals(1, policy.getSnapshotCount(), "Snapshots count doesn't match");
        System.out.println("Snapshots count: " + policy.getSnapshotCount());
    }
}
