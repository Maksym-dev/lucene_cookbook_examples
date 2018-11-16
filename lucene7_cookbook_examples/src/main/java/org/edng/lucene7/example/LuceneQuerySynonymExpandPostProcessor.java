package org.edng.lucene7.example;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SynonymQuery;
import org.apache.lucene.search.spans.SpanOrQuery;

import java.util.ListIterator;

public class LuceneQuerySynonymExpandPostProcessor {

    private static final String AND_OPERATOR = " AND ";
    private static final String OR_OPERATOR = " OR ";
    private static final String NOT_OPERATOR = " NOT ";

    public String postProcess(Query parsedQuery) {
        if (parsedQuery instanceof BooleanQuery) {
            StringBuilder buffer = new StringBuilder();
            BooleanQuery booleanQuery = (BooleanQuery) parsedQuery;
            int clausesCount = booleanQuery.clauses().size();
            ListIterator<BooleanClause> iterator = booleanQuery.clauses().listIterator();
            while (iterator.hasNext()) {
                BooleanClause booleanClause = iterator.next();
                Query query = booleanClause.getQuery();
                if (query instanceof SpanOrQuery) {
                    buffer.append(LuceneQueryToStringHelper.toStringSpanOrQuery((SpanOrQuery) query, OR_OPERATOR));
                } else {
                    boolean shouldBeWrapped = isShouldBeWrapped(clausesCount, query);
                    if (shouldBeWrapped) {
                        buffer.append("(");
                    }
                    buffer.append(postProcess(query));
                    if (shouldBeWrapped) {
                        buffer.append(")");
                    }
                }
                if (iterator.hasNext()) {
                    String str = resolveBoolOccur(booleanClause.getOccur());
                    BooleanClause next = iterator.next();
                    if (!iterator.hasNext()) {
                        String strNext = resolveBoolOccur(next.getOccur());
                        if (!str.equalsIgnoreCase(strNext)) {
                            buffer.append(strNext);
                        } else {
                            buffer.append(str);
                        }
                    } else {
                        buffer.append(str);
                    }
                    iterator.previous();
                }
            }
            return buffer.toString();
        } else if (parsedQuery instanceof SpanOrQuery) {
            return LuceneQueryToStringHelper.toStringSpanOrQuery((SpanOrQuery) parsedQuery, OR_OPERATOR);
        } else if (parsedQuery instanceof BoostQuery) {
            BoostQuery boostQuery = (BoostQuery) parsedQuery;
            Query query = boostQuery.getQuery();
            String str = postProcess(query);
            return LuceneQueryToStringHelper.toStringBoostQuery(str, boostQuery.getBoost());
        } else if (parsedQuery instanceof SynonymQuery) {
            SynonymQuery synonymQuery = (SynonymQuery) parsedQuery;
            return LuceneQueryToStringHelper.toStringSynonymQuery(synonymQuery, OR_OPERATOR);
        }
        return parsedQuery.toString();
    }

    private boolean isShouldBeWrapped(int clausesCount, Query query) {
        return query instanceof BooleanQuery &&
            (query.toString().contains(" ") &&
                !query.toString().contains("+") &&
                clausesCount > 1);
    }

    private String resolveBoolOccur(BooleanClause.Occur occur) {
        switch (occur) {
            case SHOULD:
                return OR_OPERATOR;
            case MUST:
                return AND_OPERATOR;
            case MUST_NOT:
                return NOT_OPERATOR;
        }
        return "";
    }
}
