package org.edng.lucene7.example;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a custom query class that uses AnagramQueryScoreProvider for scoring.
 *
 * Created by ed on 1/6/15.
 */
public class AnagramQuery extends CustomScoreQuery {
    private final String field;
    private final Set<String> terms = new HashSet<>();

    public AnagramQuery(Query subquery, String field) {
        super(subquery);
        this.field = field;
        HashSet<Term> termSet = new HashSet<>();
        try {
            subquery.createWeight(new IndexSearcher(new MultiReader()), false, 1).extractTerms(termSet);
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
        for (Term term : termSet) {
            terms.add(term.text());
        }
    }

    @Override
    protected CustomScoreProvider getCustomScoreProvider(LeafReaderContext context) {
        return new AnagramQueryScoreProvider(context, field, terms);
    }
}
