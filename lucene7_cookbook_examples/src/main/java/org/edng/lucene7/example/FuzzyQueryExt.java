package org.edng.lucene7.example;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;

public class FuzzyQueryExt extends FuzzyQuery {

    float minimumSimilarity;

    public FuzzyQueryExt(Term term, int maxEdits, int prefixLength, int maxExpansions, boolean transpositions) {
        super(term, maxEdits, prefixLength, maxExpansions, transpositions);
    }

    public FuzzyQueryExt(Term term, int maxEdits, int prefixLength, float minimumSimilarity) {
        super(term, maxEdits, prefixLength);
        this.minimumSimilarity = minimumSimilarity;
    }

    public FuzzyQueryExt(Term term, int maxEdits) {
        super(term, maxEdits);
    }

    public FuzzyQueryExt(Term term, float minimumSimilarity) {
        super(term);
        this.minimumSimilarity = minimumSimilarity;
    }

    @Override
    public String toString(String field) {
        final StringBuilder buffer = new StringBuilder();
        if (!getTerm().field().equals(field)) {
            buffer.append(getTerm().field());
            buffer.append(":");
        }
        buffer.append(getTerm().text());
        buffer.append('~');
        buffer.append(Float.toString(minimumSimilarity));
        return buffer.toString();
    }
}
