package org.edng.lucene7.example;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Test;

/**
 * Created by ed on 1/30/15.
 */
public class TermVectorTest {

    @Test
    public void runTest() throws Exception {

        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        FieldType textFieldType = new FieldType();
        textFieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        textFieldType.setTokenized(true);
        textFieldType.setStored(true);
        textFieldType.setStoreTermVectors(true);
        textFieldType.setStoreTermVectorPositions(true);
        textFieldType.setStoreTermVectorOffsets(true);

        Document doc = new Document();
        Field textField = new Field("content", "", textFieldType);

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

        indexWriter.commit();
        IndexReader indexReader = DirectoryReader.open(directory);
        Terms termsVector;
        TermsEnum termsEnum;
        BytesRef term;
        PostingsEnum postings = null;

        for (int i = 0; i < indexReader.maxDoc(); i++) {
            termsVector = indexReader.getTermVector(i, "content");
            termsEnum = termsVector.iterator();
            while ((term = termsEnum.next()) != null) {
                System.out.println("DocId: " + i);
                System.out.println("  term: " + term.utf8ToString());
                System.out.println("  length: " + term.length);
                int freq = termsEnum.docFreq();
                System.out.println("  freq: " + freq);
                for (int j = 0; j < freq; j++) {
                    System.out.println("    [");
                    postings = termsEnum.postings(postings, PostingsEnum.ALL);
                    while (postings.nextDoc() != PostingsEnum.NO_MORE_DOCS) {
                        System.out.println("      position: " + postings.nextPosition());
                        System.out.println("      offset start: " + postings.startOffset());
                        System.out.println("      offset end: " + postings.endOffset());
                    }
                    System.out.println("    ]");
                }
            }
        }
    }
}
