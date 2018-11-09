package org.edng.lucene7.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafFieldComparator;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * This class tests a custom FieldComparator comparing terms by length only where
 * the results is sorted by length.
 * <p>
 * Created by ed on 1/30/15.
 */
public class ComparatorTest {

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
        StringField stringField = new StringField(nameFieldNameSorted, "", Field.Store.YES);

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
        SortField sortField = new SortField(nameFieldNameSorted, new MyFieldComparatorSource());
        Sort sort = new Sort(sortField);

        TopDocs topDocs = indexSearcher.search(new MatchAllDocsQuery(), 100, sort);
        int i = 0;
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexReader.document(scoreDoc.doc);
            String s = new String(doc.getField(nameFieldNameSorted).binaryValue().bytes);
            System.out.println(scoreDoc.score + ": " + s);
            if (i == 0) {
                assertEquals("echo", s, "Position 0 should be 'echo' but gotten '" + s + "' instead");
            }
            if (i == 5) {
                assertTrue(s.equals("charlie") || s.equals("foxtrot"), "Position 5 should be 'charlie' or 'foxtrot' but gotten '" + s + "' instead");
            }
            i++;
        }
    }

    public static class MyFieldComparator extends FieldComparator<String> implements LeafFieldComparator {
        private String field;
        private String bottom;
        private String topValue;
        private BinaryDocValues cache;
        private String[] values;

        public MyFieldComparator(String field, int numHits) {
            this.field = field;
            this.values = new String[numHits];
        }

        public int compare(int slot1, int slot2) {
            return compareValues(values[slot1], values[slot2]);
        }

        public int compareBottom(int doc) {
            try {
                cache.advanceExact(doc);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                return compareValues(bottom, cache.binaryValue().utf8ToString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1;
        }

        public int compareTop(int doc) {
            try {
                cache.advanceExact(doc);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                return compareValues(topValue, cache.binaryValue().utf8ToString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1;
        }

        public int compareValues(String first, String second) {
            int val = first.length() - second.length();
            return val == 0 ? first.compareTo(second) : val;
        }

        public void copy(int slot, int doc) {
            try {
                cache.advanceExact(doc);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                values[slot] = cache.binaryValue().utf8ToString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setBottom(int slot) {
            this.bottom = values[slot];
        }

        public void setTopValue(String value) {
            this.topValue = value;
        }

        public String value(int slot) {
            return values[slot];
        }

        @Override
        public LeafFieldComparator getLeafComparator(LeafReaderContext context) throws IOException {
            this.cache = DocValues.getBinary(context.reader(), field);
            return this;
        }

        @Override
        public void setScorer(Scorer scorer) throws IOException {

        }
    }

    public static class MyFieldComparatorSource extends FieldComparatorSource {
        public FieldComparator newComparator(String fieldName, int numHits, int sortPos, boolean reversed) {
            return new MyFieldComparator(fieldName, numHits);
        }
    }
}
