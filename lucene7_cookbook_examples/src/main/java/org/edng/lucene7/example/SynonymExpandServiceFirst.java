package org.edng.lucene7.example;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.synonym.SolrSynonymParser;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SynonymQuery;
import org.apache.lucene.search.spans.SpanOrQuery;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ListIterator;

/**
 * Service to expand query that contains multi-word synonyms.
 */
public class SynonymExpandServiceFirst implements ISynonymExpandService {

    private static final String AND_OPERATOR = " AND ";
    private static final String OR_OPERATOR = " OR ";
    private static final String NOT_OPERATOR = " NOT ";

    private Analyzer synonymsAnalyzer;

    public SynonymExpandServiceFirst() {
        try {
            SynonymMap mySynonymMap = buildSynonym(SynonymExpandServiceFirst.class.getClassLoader().getResourceAsStream(
                "\\synonyms.txt"));
            synonymsAnalyzer = new SynonymsAnalyzer(mySynonymMap);
        } catch (IOException | java.text.ParseException e) {
            e.printStackTrace();
            synonymsAnalyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);
        }
    }

    /**
     * Expand query string by applying phrase synonyms.
     *
     * @param inputQuery - source query.
     * @return expanding result.
     */
    public String expand(String inputQuery) throws ParseException {
        if (StringUtils.isBlank(inputQuery)) {
            return "";
        }
        MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParserExt(new String[]{""}, synonymsAnalyzer);
        multiFieldQueryParser.setSplitOnWhitespace(false);
        multiFieldQueryParser.setAutoGenerateMultiTermSynonymsPhraseQuery(true);
        multiFieldQueryParser.setDefaultOperator(QueryParser.Operator.OR);
        Query parsedQuery = multiFieldQueryParser.parse(inputQuery.trim());
        return  postProcess(parsedQuery).trim();
    }

    private String postProcess(Query parsedQuery) {
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

   private SynonymMap buildSynonym(InputStream stream) throws IOException, java.text.ParseException {
        SolrSynonymParser parser = new SolrSynonymParser(true, true, new StandardAnalyzer(CharArraySet.EMPTY_SET));
        parser.parse(new InputStreamReader(stream));
        return parser.build();
    }

}
