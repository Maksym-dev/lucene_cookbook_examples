package org.edng.lucene.example;

/**
 * Created by ed on 12/24/14.
 */
public class SimilarityTest {

//    @Test
//    public void runTest() throws Exception {
//
//        class MySimilarity extends Similarity {
//            private Similarity sim = null;
//            public MySimilarity(Similarity sim) {
//                this.sim = sim;
//            }
//            @Override
//            public long computeNorm(FieldInvertState state) {
//                return sim.computeNorm(state);
//            }
//            @Override
//            public Similarity.SimWeight computeWeight(float queryBoost,
//                                                      CollectionStatistics collectionStats,
//                                                      TermStatistics... termStats) {
//                return sim.computeWeight(queryBoost, collectionStats, termStats);
//            }
//            @Override
//            public Similarity.SimScorer simScorer(Similarity.SimWeight weight,
//                                                  LeafReaderContext context)
//                    throws IOException {
//                final Similarity.SimScorer scorer = sim.simScorer(weight, context);
//                final NumericDocValues values = context.reader().getNumericDocValues("ranking");
//
//                return new SimScorer() {
//                    @Override
//                    public float score(int i, float v) {
//                        return values.get(i) * scorer.score(i, v);
//                    }
//
//                    @Override
//                    public float computeSlopFactor(int i) {
//                        return scorer.computeSlopFactor(i);
//                    }
//
//                    @Override
//                    public float computePayloadFactor(int i, int i1, int i2, BytesRef bytesRef) {
//                        return scorer.computePayloadFactor(i, i1, i2, bytesRef);
//                    }
//                };
//            }
//        }
//
//        Analyzer analyzer = new StandardAnalyzer();
//        Directory directory = new RAMDirectory();
//        IndexWriterConfig config = new IndexWriterConfig(analyzer);
//        MySimilarity similarity = new MySimilarity(new ClassicSimilarity());
//        config.setSimilarity(similarity);
//        IndexWriter indexWriter = new IndexWriter(directory, config);
//
//        Document doc = new Document();
//        TextField textField = new TextField("name", "", Field.Store.YES);
//        NumericDocValuesField docValuesField = new NumericDocValuesField("ranking", 1);
//
//        long ranking = 1l;
//        String[] names = {"hair dye for black hair", "hair dye", "Peter Smith"};
//        for (String name : names) {
//            ranking *= 2;
//            textField.setStringValue(name);
//            docValuesField.setLongValue(ranking);
//            doc.removeField("name");
//            doc.removeField("ranking");
//            doc.add(textField);
//            doc.add(docValuesField);
//            indexWriter.addDocument(doc);
//        }
//
//        indexWriter.commit();
//
//        IndexReader indexReader = DirectoryReader.open(directory);
//        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
//        indexSearcher.setSimilarity(similarity);
//        Query query = new TermQuery(new Term("name", "smith"));
//        TopDocs topDocs = indexSearcher.search(query, 100);
//        System.out.println("Searching 'smith'");
//        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
//            doc = indexReader.document(scoreDoc.doc);
//            assertEquals("Result not match", "Peter Smith", doc.getField("name").stringValue());
//            System.out.println(doc.getField("name").stringValue());
//        }
//    }
}
