package org.edng.lucene7.example;

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
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryExpandWithSynonymsTest {

    private String[] queries = {
        "breast cancer",
        "breast cancer some text",
        "\"breast cancer\"",
        "\"breast cancer\" some text",
        "breast cancer AND some text",
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
        "dm",
        ".dm"
    };

    @Test
    public void testExpandByComplexPhraseQueryParser() throws Throwable {
        SynonymMap mySynonymMap;
        mySynonymMap = buildSynonym(getClass().getClassLoader().getResourceAsStream("\\synonyms.txt"));
        SynonymsAnalyzer synonymsAnalyzer = new SynonymsAnalyzer(mySynonymMap);

        ComplexPhraseQueryParser phraseQueryParser = new ComplexPhraseQueryParser("", synonymsAnalyzer);
        phraseQueryParser.setDefaultOperator(QueryParser.Operator.AND);
        phraseQueryParser.setAutoGenerateMultiTermSynonymsPhraseQuery(true);
        phraseQueryParser.setMultiTermRewriteMethod(MultiTermQuery.CONSTANT_SCORE_REWRITE);
        for (String input : queries) {
            Query parsedQuery = phraseQueryParser.parse(input.trim());
            System.out.println(parsedQuery);
        }
    }

    @Test
    public void testExpandByQueryParser() throws Throwable {
        SynonymMap mySynonymMap;
        mySynonymMap = buildSynonym(getClass().getClassLoader().getResourceAsStream("\\synonyms.txt"));
        SynonymsAnalyzer synonymsAnalyzer = new SynonymsAnalyzer(mySynonymMap);

        QueryParser queryParser = new QueryParser("", synonymsAnalyzer);
        queryParser.setAutoGenerateMultiTermSynonymsPhraseQuery(true);
        queryParser.setMultiTermRewriteMethod(MultiTermQuery.CONSTANT_SCORE_REWRITE);
        for (String input : queries) {
            Query parsedQuery = queryParser.parse(input.trim());
            System.out.println(parsedQuery);
        }
    }

    @Test
    public void testExpandByMultiFieldQueryParser() throws Throwable {
        SynonymMap mySynonymMap;
        mySynonymMap = buildSynonym(getClass().getClassLoader().getResourceAsStream("\\synonyms.txt"));
        SynonymsAnalyzer synonymsAnalyzer = new SynonymsAnalyzer(mySynonymMap);

        MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(new String[]{""}, synonymsAnalyzer);
        multiFieldQueryParser.setSplitOnWhitespace(false);
        multiFieldQueryParser.setAutoGenerateMultiTermSynonymsPhraseQuery(true);
        multiFieldQueryParser.setDefaultOperator(QueryParser.Operator.AND);
        for (String input : queries) {
            Query parsedQuery = multiFieldQueryParser.parse(input.trim());
            System.out.println(parsedQuery);
        }
    }

    @Test
    public void testExpandBySimpleQueryParser() throws Throwable {
        SynonymMap mySynonymMap;
        mySynonymMap = buildSynonym(getClass().getClassLoader().getResourceAsStream("\\synonyms.txt"));
        SynonymsAnalyzer synonymsAnalyzer = new SynonymsAnalyzer(mySynonymMap);
        SimpleQueryParser simpleQueryParser = new SimpleQueryParser(synonymsAnalyzer, "");
        simpleQueryParser.setAutoGenerateMultiTermSynonymsPhraseQuery(true);
        simpleQueryParser.setDefaultOperator(BooleanClause.Occur.SHOULD);
        for (String input : queries) {
            Query parsedQuery = simpleQueryParser.parse(input.trim());
            System.out.println(parsedQuery);
        }
    }

    @Test
    public void testExpandByStandardQueryParser() throws Throwable {
        SynonymMap mySynonymMap;
        mySynonymMap = buildSynonym(getClass().getClassLoader().getResourceAsStream("\\synonyms.txt"));
        SynonymsAnalyzer synonymsAnalyzer = new SynonymsAnalyzer(mySynonymMap);

        StandardQueryParser standardQueryParser = new StandardQueryParser(synonymsAnalyzer);
        standardQueryParser.setDefaultOperator(StandardQueryConfigHandler.Operator.AND);
        for (String input : queries) {
            Query parsedQuery = standardQueryParser.parse(input.trim(), "");
            System.out.println(parsedQuery);
        }
    }

    private SynonymMap buildSynonym(InputStream stream) throws IOException, java.text.ParseException {
        SolrSynonymParser parser = new SolrSynonymParser(true, true, new StandardAnalyzer(CharArraySet.EMPTY_SET));
        parser.parse(new InputStreamReader(stream));
        return parser.build();
    }
}
