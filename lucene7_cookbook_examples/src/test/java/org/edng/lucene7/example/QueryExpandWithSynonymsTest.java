package org.edng.lucene7.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.lang3.StringUtils;
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
import org.apache.lucene.search.SynonymQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
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
        long start = System.currentTimeMillis();
        SynonymMap mySynonymMap;
        mySynonymMap = buildSynonym(QueryExpandWithSynonymsTest.class.getClassLoader().getResourceAsStream(
            "\\synonyms.txt"));
        synonymsAnalyzer = new SynonymsAnalyzer(mySynonymMap);
        System.out.println("Time to load synonyms " + (System.currentTimeMillis() - start) + " ms" + System.lineSeparator());
    }

    private static SynonymMap buildSynonym(InputStream stream) throws IOException, java.text.ParseException {
        SolrSynonymParser parser = new SolrSynonymParser(true, true, new StandardAnalyzer(CharArraySet.EMPTY_SET));
        parser.parse(new InputStreamReader(stream));
        return parser.build();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/expand-queries.csv")
    public void testExpandByMultiFieldQueryParserWithDefaultOrOperator(String input, String expected) throws Throwable {

        MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParserExt(new String[]{""}, synonymsAnalyzer);
        multiFieldQueryParser.setSplitOnWhitespace(false);
        multiFieldQueryParser.setAutoGenerateMultiTermSynonymsPhraseQuery(true);
        multiFieldQueryParser.setDefaultOperator(QueryParser.Operator.OR);
        Query parsedQuery = multiFieldQueryParser.parse(input.trim());
        String actual = postProcess(parsedQuery).trim();
        System.out.println(input.trim() + " => " + actual);
        assertEquals(expected, actual, "Query does not equals as expected query");
    }

    @Test
    public void testSimpleExpandByMultiFieldQueryParserWithDefaultOrOperator() throws Throwable {
//        String input = "\"dm\"~4";
        String input = "\"breast cancer\"~8 AND \"dm\"~4";
        MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParserExt(new String[]{""}, synonymsAnalyzer);
        multiFieldQueryParser.setSplitOnWhitespace(false);
        multiFieldQueryParser.setAutoGenerateMultiTermSynonymsPhraseQuery(true);
        multiFieldQueryParser.setDefaultOperator(QueryParser.Operator.OR);
        Query parsedQuery = multiFieldQueryParser.parse(input.trim());
        String actual = postProcess(parsedQuery).trim();
        System.out.println(input.trim() + " => " + actual);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/expand-queries-second.csv")
    public void testExpandByMultiFieldQueryParserWithDefaultAndOperator(String input, String expected) throws Throwable {

        MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParserExt(new String[]{""}, synonymsAnalyzer);
        multiFieldQueryParser.setSplitOnWhitespace(false);
        multiFieldQueryParser.setAutoGenerateMultiTermSynonymsPhraseQuery(true);
        multiFieldQueryParser.setDefaultOperator(QueryParser.Operator.AND);
        Query parsedQuery = multiFieldQueryParser.parse(input.trim());
        String actual = postProcess(parsedQuery).trim();
        System.out.println(input.trim() + " => " + actual);
        assertEquals(expected, actual, "Query does not equals as expected query");
    }

    @Test
    public void testSimpleExpandByMultiFieldQueryParserWithDefaultAndOperator() throws Throwable {
        String input = "\"breast cancer\"~8 AND (\"dm\"~4 OR ruby)";
        MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParserExt(new String[]{""}, synonymsAnalyzer);
        multiFieldQueryParser.setSplitOnWhitespace(false);
        multiFieldQueryParser.setAutoGenerateMultiTermSynonymsPhraseQuery(true);
        multiFieldQueryParser.setDefaultOperator(QueryParser.Operator.AND);
        Query parsedQuery = multiFieldQueryParser.parse(input.trim());
        String actual = postProcess(parsedQuery).trim();
        System.out.println(input.trim() + " => " + actual);
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
                    buffer.append(LuceneQueryToStringHelper.toStringSpanOrQuery((SpanOrQuery) query, OR_PRERATOR));
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
            return LuceneQueryToStringHelper.toStringSpanOrQuery((SpanOrQuery) parsedQuery, OR_PRERATOR);
        } else if (parsedQuery instanceof BoostQuery) {
            BoostQuery boostQuery = (BoostQuery) parsedQuery;
            Query query = boostQuery.getQuery();
            String str = postProcess(query);
            return toStringBoostQuery(str, boostQuery.getBoost());
        } else if (parsedQuery instanceof SynonymQuery) {
            SynonymQuery synonymQuery = (SynonymQuery) parsedQuery;
            return LuceneQueryToStringHelper.toStringSynonymQuery(synonymQuery, OR_PRERATOR);
        }
        return parsedQuery.toString();
    }

    private boolean isShouldBeWrapped(int clausesCount, Query query) {
        return query instanceof BooleanQuery &&
            (query.toString().contains(" ") &&
                !query.toString().contains("+") &&
                clausesCount > 1);
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

    @ParameterizedTest
    @CsvFileSource(resources = "/expand-queries.csv")
    public void testExpandByComplexPhraseQueryParser(String input, String expected) throws Throwable {

        ComplexPhraseQueryParser phraseQueryParser = new ComplexPhraseQueryParser("", synonymsAnalyzer);
        phraseQueryParser.setSplitOnWhitespace(false);
        phraseQueryParser.setDefaultOperator(QueryParser.Operator.OR);
        phraseQueryParser.setAutoGenerateMultiTermSynonymsPhraseQuery(true);
        phraseQueryParser.setMultiTermRewriteMethod(MultiTermQuery.CONSTANT_SCORE_REWRITE);
        Query parsedQuery = phraseQueryParser.parse(input.trim());
        String actual = postProcess(parsedQuery).trim();
        System.out.println(input.trim() + " => " + actual);
        assertEquals(expected, actual, "Query does not equals as expected query");
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
