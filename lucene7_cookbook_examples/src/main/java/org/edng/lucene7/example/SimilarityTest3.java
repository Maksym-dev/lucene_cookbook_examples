package org.edng.lucene7.example;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.queries.payloads.AveragePayloadFunction;
import org.apache.lucene.queries.payloads.PayloadDecoder;
import org.apache.lucene.queries.payloads.PayloadFunction;
import org.apache.lucene.queries.payloads.PayloadScoreQuery;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Test;

/**
 * Created by ed on 12/24/14.
 */
public class SimilarityTest3 {

    @Test
    public void runTest() throws Exception {

        class MySimilarity extends ClassicSimilarity {
            @Override
            public float idf(long docFreq, long numDocs) {
                return super.idf(docFreq, numDocs);
                /*
                if (docFreq > 2) {
                    return super.idf(docFreq, numDocs);
                } else {
                    return super.idf(docFreq * 100, numDocs);
                }
                */
            }
            @Override
            public float lengthNorm(int state) {
                /*
                if (state.getLength() % 2 == 1) {
                    return super.lengthNorm(state) * 100;
                }
                */
                return super.lengthNorm(state);
            }

            @Override
            public float scorePayload(int doc, int start, int end, BytesRef payload) {
                float val = PayloadHelper.decodeFloat(payload.bytes);
                if (start == 0 || start == 1) {
                    return val * 0.1f;
                }
                return val * 100f;
            }
            @Override
            public float sloppyFreq(int distance) {
                /*
                if (distance == 0) {
                    return super.sloppyFreq(distance) * 100;
                }
                */
                return super.sloppyFreq(distance);
            }
            @Override
            public float tf(float freq) {
                /*
                if (freq > 1f) {
                    return super.tf(freq) * 100;
                }
                */
                return super.tf(freq);
            }
        }

        PayloadAnalyzer analyzer = new PayloadAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig( analyzer);
        MySimilarity similarity = new MySimilarity();
        config.setSimilarity(similarity);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        String contentFieldName = "content";
        TextField textField = new TextField(contentFieldName, "", Field.Store.YES);

        String[] contents = {"Humpty Dumpty sat on a wall,",
                "Humpty Dumpty had a great fall.",
                "All the king's horses and all the king's men",
                "Couldn't put Humpty together again."};
        for (String content : contents) {
            textField.setStringValue(content);
            doc.removeField(contentFieldName);
            doc.add(textField);
            indexWriter.addDocument(doc);
        }

        indexWriter.commit();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        indexSearcher.setSimilarity(similarity);
        QueryParser queryParser = new QueryParser(contentFieldName, analyzer);
//        Query query = queryParser.parse("humpty dumpty");

        PayloadFunction payloadFunction = new AveragePayloadFunction();
        Term humptyTerm = new Term(contentFieldName, "humpty");
        Term dumptyTerm = new Term(contentFieldName, "dumpty");
        Query query = new BooleanQuery.Builder()
            .add(new PayloadScoreQuery(
                new SpanTermQuery(humptyTerm, /*"humpty"*/TermContext.build(indexReader.getContext(), humptyTerm)),
                    payloadFunction, PayloadDecoder.FLOAT_DECODER, true),
                BooleanClause.Occur.SHOULD)
            .add(new PayloadScoreQuery(
                new SpanTermQuery(dumptyTerm, /*"dumpty"*/TermContext.build(indexReader.getContext(), dumptyTerm)),
                    payloadFunction, PayloadDecoder.FLOAT_DECODER, true),
                BooleanClause.Occur.SHOULD)
            .build();

        TopDocs topDocs = indexSearcher.search(query, 100);
        System.out.println("Found docs: " + topDocs.scoreDocs.length);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexReader.document(scoreDoc.doc);
            if (scoreDoc.equals(topDocs.scoreDocs[0])) {
                assertEquals(71.432, scoreDoc.score, 0.1, "Rank 1 score not match");
            }
            System.out.println(scoreDoc.score + ": " + doc.getField(contentFieldName).stringValue());
        }
    }
}
