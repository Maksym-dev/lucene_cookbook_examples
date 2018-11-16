package org.edng.lucene7.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiFieldQueryParserExt extends MultiFieldQueryParser {

    public MultiFieldQueryParserExt(String[] fields, Analyzer analyzer, Map<String, Float> boosts) {
        super(fields, analyzer, boosts);
    }

    public MultiFieldQueryParserExt(String[] fields, Analyzer analyzer) {
        super(fields, analyzer);
    }

    @Override
    protected Query newFuzzyQuery(Term term, float minimumSimilarity, int prefixLength) {
        // FuzzyQuery doesn't yet allow constant score rewrite
        String text = term.text();
        int numEdits = FuzzyQuery.floatToEdits(minimumSimilarity,
            text.codePointCount(0, text.length()));
        return new FuzzyQueryExt(term, numEdits, prefixLength, minimumSimilarity);
    }

    @Override
    protected Query getFieldQuery(String field, String queryText, int slop) throws ParseException {
        if (field == null) {
            List<Query> clauses = new ArrayList<>();
            for (String internalField : fields) {
                Query q = super.getFieldQuery(internalField, queryText, true);
                if (q != null) {
                    //If the user passes a map of boosts
                    if (boosts != null) {
                        //Get the boost from the map and apply them
                        Float boost = boosts.get(internalField);
                        if (boost != null) {
                            q = new BoostQuery(q, boost);
                        }
                    }
                    q = applySlop(q, slop);
                    clauses.add(q);
                }
            }
            if (clauses.size() == 0)  // happens for stopwords
            {
                return null;
            }
            return getMultiFieldQuery(clauses);
        }
        Query q = super.getFieldQuery(field, queryText, true);
        q = applySlop(q, slop);
        return q;
    }

    private Query applySlop(Query q, int slop) {
        if (q instanceof PhraseQuery) {
            PhraseQuery.Builder builder = new PhraseQuery.Builder();
            builder.setSlop(slop);
            PhraseQuery pq = (PhraseQuery) q;
            org.apache.lucene.index.Term[] terms = pq.getTerms();
            int[] positions = pq.getPositions();
            for (int i = 0; i < terms.length; ++i) {
                builder.add(terms[i], positions[i]);
            }
            q = builder.build();
        } else if (q instanceof MultiPhraseQuery) {
            MultiPhraseQuery mpq = (MultiPhraseQuery) q;

            if (slop != mpq.getSlop()) {
                q = new MultiPhraseQuery.Builder(mpq).setSlop(slop).build();
            }
        } else if (q instanceof SpanOrQuery && slop > 0) {
            // TODO if recursion needed for complex queries?
            SpanOrQuery spanOrQuery = (SpanOrQuery) q;
            System.out.println(spanOrQuery);
            SpanQuery clause = spanOrQuery.getClauses()[spanOrQuery.getClauses().length - 1];
            System.out.println(clause);
            MultiPhraseQuery.Builder builder = newMultiPhraseQueryBuilder();
            if (clause instanceof SpanTermQuery) {
                SpanTermQuery spanTermQuery = (SpanTermQuery) clause;
                builder.add(spanTermQuery.getTerm());
            } else if (clause instanceof SpanNearQuery) {
                SpanNearQuery spanNearQuery = (SpanNearQuery) clause;
                SpanQuery[] clauses = spanNearQuery.getClauses();
                for (SpanQuery termQuery : clauses) {
                    SpanTermQuery spanTermQuery = (SpanTermQuery) termQuery;
                    builder.add(spanTermQuery.getTerm());
                }
            }
            builder.setSlop(slop);
            return builder.build();
        }
        return q;
    }
}
