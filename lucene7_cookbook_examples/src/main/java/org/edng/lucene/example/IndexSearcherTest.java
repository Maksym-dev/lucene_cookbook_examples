package org.edng.lucene.example;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;

/**
 * Created by ed on 2/2/15.
 */
public class IndexSearcherTest {
    public static void main(String[] args) throws Exception {
        Directory directory = FSDirectory.open(Paths.get("/data/index5"));
        DirectoryReader directoryReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
    }
}
