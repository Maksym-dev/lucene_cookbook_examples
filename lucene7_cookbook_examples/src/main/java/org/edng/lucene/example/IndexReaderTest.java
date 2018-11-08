package org.edng.lucene.example;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.util.List;

/**
 * Created by ed on 1/28/15.
 */
public class IndexReaderTest {
    public static void main(String[] args) throws Exception {

        // open a directory
        Directory directory = FSDirectory.open(Paths.get("/data/index4"));
        // set up a DirectoryReader
        DirectoryReader directoryReader = DirectoryReader.open(directory);
        // pull a list of underlying AtomicReaders
        List<LeafReaderContext> atomicReaderContexts = directoryReader.leaves();
        // retrieve the first AtomicReader from the list
        LeafReader atomicReader = atomicReaderContexts.get(0).reader();
        // open another DirectoryReader by calling openIfChanged
        DirectoryReader newDirectoryReader = DirectoryReader.openIfChanged(directoryReader);
        // assign newDirectoryReader
        if (newDirectoryReader != null) {
            IndexSearcher indexSearcher = new IndexSearcher(newDirectoryReader);
            // close the old DirectoryReader
            directoryReader.close();
        }
    }
}
