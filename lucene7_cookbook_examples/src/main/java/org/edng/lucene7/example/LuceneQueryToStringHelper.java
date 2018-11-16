package org.edng.lucene7.example;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SynonymQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class LuceneQueryToStringHelper {

    public static String toStringSpanNearQuery(SpanNearQuery spanNearQuery) {
        StringBuilder buffer = new StringBuilder();
        String field = spanNearQuery.getField();
        if (StringUtils.isNotEmpty(field)) {
            buffer.append(field).append(":");
        }
        buffer.append("\"");
        Iterator<SpanQuery> i = Arrays.asList(spanNearQuery.getClauses()).iterator();
        while (i.hasNext()) {
            SpanQuery clause = i.next();
            if (StringUtils.isNotEmpty(field)) {
                buffer.append(clause.toString(field));
            } else {
                buffer.append(clause.toString());
            }
            if (i.hasNext()) {
                buffer.append(" ");
            }
        }
        buffer.append("\"");
        return buffer.toString();
    }

    public static String toStringSynonymQuery(SynonymQuery synonymQuery, String operator) {
        StringBuilder builder = new StringBuilder();
        List<Term> terms = synonymQuery.getTerms();
        for (int i = 0; i < terms.size(); i++) {
            if (i != 0) {
                builder.append(operator);
            }
            Query termQuery = new TermQuery(terms.get(i));
            builder.append(termQuery.toString());
        }
        builder.append(")");
        return builder.toString();
    }
}
