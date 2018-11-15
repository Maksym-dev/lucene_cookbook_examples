package org.edng.lucene7.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.synonym.SolrSynonymParser;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.queryparser.simple.SimpleQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;

public class QueryExpandWithSynonymsTest {

    private static final String AND_OPERATOR = " AND ";
    private static final String OR_PRERATOR = " OR ";
    private static final String NOT_OPERATOR = " NOT ";
    private static SynonymsAnalyzer synonymsAnalyzer;
    private String[] queries = {
        "breast cancer",
        "breast cancer some text",
        "\"breast cancer\"",
        "\"breast cancer\" some text",
        "breast cancer AND some text",
        "tex* AND (breast cancer)",
        "breast cancer AND (some text)",
        "(breast cancer) AND (some text)",
        "breast cancer OR some text",
        "breast cancer OR (some text)",
        "(breast cancer) OR (some text)",
        "bre?st cancer",
        "breast c*ncer",
        "breast^1.4 cancer",
        "(breast cancer)^1.4",
        "breast cancer~10",
        "\"breast cancer\"~10",
        "title:\"breast cancer\"",
        "title:(breast cancer)",
        "title:(breast cancer) OR abstract:dm",
        "diabetes dm",
        ".dm"
    };

    @BeforeAll
    static void setUp() throws IOException, ParseException {
        SynonymMap mySynonymMap;
        mySynonymMap = buildSynonym(QueryExpandWithSynonymsTest.class.getClassLoader().getResourceAsStream(
            "\\synonyms.txt"));
        synonymsAnalyzer = new SynonymsAnalyzer(mySynonymMap);
    }

    private static SynonymMap buildSynonym(InputStream stream) throws IOException, java.text.ParseException {
        SolrSynonymParser parser = new SolrSynonymParser(true, true, new StandardAnalyzer(CharArraySet.EMPTY_SET));
        parser.parse(new InputStreamReader(stream));
        return parser.build();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/expand-queries.csv")
    public void testExpandByMultiFieldQueryParser(String input, String expected) throws Throwable {

        MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(new String[]{""}, synonymsAnalyzer);
        multiFieldQueryParser.setSplitOnWhitespace(false);
        multiFieldQueryParser.setAutoGenerateMultiTermSynonymsPhraseQuery(true);
        multiFieldQueryParser.setDefaultOperator(QueryParser.Operator.OR);
        Query parsedQuery = multiFieldQueryParser.parse(input.trim());
        String actual = postProcess(parsedQuery).trim();
        System.out.println(input.trim() + " => " + actual);
        assertEquals(expected, actual, "Query does not equals as expected query");
    }

    private String postProcess(Query parsedQuery) {
        if (parsedQuery instanceof BooleanQuery) {
            StringBuilder buffer = new StringBuilder();
            BooleanQuery booleanQuery = (BooleanQuery) parsedQuery;
            ListIterator<BooleanClause> iterator = booleanQuery.clauses().listIterator();
            while (iterator.hasNext()) {
                BooleanClause booleanClause = iterator.next();
                Query query = booleanClause.getQuery();
                if (query instanceof SpanOrQuery) {
                    buffer.append(toStringSpanOrQuery((SpanOrQuery) query));
                } else {
                    buffer.append(postProcess(query));
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
            return toStringSpanOrQuery((SpanOrQuery) parsedQuery);
        } else if (parsedQuery instanceof BoostQuery) {
            BoostQuery boostQuery = (BoostQuery) parsedQuery;
            Query query = boostQuery.getQuery();
            String str = postProcess(query);
            return toStringBoostQuery(str, boostQuery.getBoost());
        }
        return parsedQuery.toString();
    }

    private String toStringBoostQuery(String str, float boost) {
        return "(" + str + ")" + "^" + boost;
    }

    private String resolveBoolOccur(BooleanClause.Occur occur) {
        switch (occur) {
            case SHOULD:
                return OR_PRERATOR;
            case MUST:
                return AND_OPERATOR;
            case MUST_NOT:
                return NOT_OPERATOR;
        }
        return "";
    }

    private String toStringSpanOrQuery(SpanOrQuery spanOrQuery) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("(");
        Iterator<SpanQuery> i = Arrays.asList(spanOrQuery.getClauses()).iterator();
        while (i.hasNext()) {
            SpanQuery clause = i.next();
            if (clause instanceof SpanNearQuery) {
                buffer.append(toStringSpanNearQuery((SpanNearQuery) clause));
            } else {

                buffer.append(clause.toString());
            }
            if (i.hasNext()) {
                buffer.append(OR_PRERATOR);
            }
        }
        buffer.append(")");
        return buffer.toString();
    }

    private String toStringSpanNearQuery(SpanNearQuery spanNearQuery) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("\"");
        Iterator<SpanQuery> i = Arrays.asList(spanNearQuery.getClauses()).iterator();
        while (i.hasNext()) {
            SpanQuery clause = i.next();
            buffer.append(clause.toString());
            if (i.hasNext()) {
                buffer.append(" ");
            }
        }
        buffer.append("\"");
        return buffer.toString();
    }

    @Test
    public void testExpandByQueryParser() throws Throwable {

        QueryParser queryParser = new QueryParser("", synonymsAnalyzer);
        queryParser.setAutoGenerateMultiTermSynonymsPhraseQuery(true);
        queryParser.setMultiTermRewriteMethod(MultiTermQuery.CONSTANT_SCORE_REWRITE);
        for (String input : queries) {
            String trim = input.trim();
            Query parsedQuery = queryParser.parse(trim);
            System.out.println(input.trim() + " => " + parsedQuery);
        }
    }

    @Test
    public void testExpandByComplexPhraseQueryParser() throws Throwable {

        ComplexPhraseQueryParser phraseQueryParser = new ComplexPhraseQueryParser("", synonymsAnalyzer);
        phraseQueryParser.setSplitOnWhitespace(false);
        phraseQueryParser.setDefaultOperator(QueryParser.Operator.AND);
        phraseQueryParser.setAutoGenerateMultiTermSynonymsPhraseQuery(true);
        phraseQueryParser.setMultiTermRewriteMethod(MultiTermQuery.CONSTANT_SCORE_REWRITE);
        for (String input : queries) {
            Query parsedQuery = phraseQueryParser.parse(input.trim());
            System.out.println(input.trim() + " => " + parsedQuery);
        }
    }

    @Test
    public void testExpandBySimpleQueryParser() {
        SimpleQueryParser simpleQueryParser = new SimpleQueryParser(synonymsAnalyzer, "");
        simpleQueryParser.setAutoGenerateMultiTermSynonymsPhraseQuery(true);
        simpleQueryParser.setDefaultOperator(BooleanClause.Occur.SHOULD);
        for (String input : queries) {
            Query parsedQuery = simpleQueryParser.parse(input.trim());
            System.out.println(input.trim() + " => " + parsedQuery);
        }
    }

    @Test
    public void testExpandByStandardQueryParser() throws Throwable {

        StandardQueryParser standardQueryParser = new StandardQueryParser(synonymsAnalyzer);
        standardQueryParser.setDefaultOperator(StandardQueryConfigHandler.Operator.AND);
        for (String input : queries) {
            Query parsedQuery = standardQueryParser.parse(input.trim(), "");
            System.out.println(input.trim() + " => " + parsedQuery);
        }
    }
}
