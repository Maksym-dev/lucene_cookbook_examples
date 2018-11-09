package org.edng.lucene7.example;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.synonym.SolrSynonymParser;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class QueryExpandWithSynonymsTest {

    private String[] queries = {
        "breast cancer",
        "\"breast cancer\"",
        "breast cancer AND some text",
        "breast cancer AND (some text)",
        "(breast cancer) AND (some text)",
        "bre?st cancer",
        "breast c*ncer",
        "breast^1.4 cancer",
        "breast cancer~10",
        "title:\"breast cancer\""
    };

    @Test
    public void testExpandByComplexPhraseQueryParser() throws Throwable {
        SynonymMap mySynonymMap;
        mySynonymMap = buildSynonym(getClass().getClassLoader().getResourceAsStream("\\synonyms.txt"));
        SynonymsAnalyzer synonymsAnalyzer = new SynonymsAnalyzer(mySynonymMap);

        ComplexPhraseQueryParser phraseQueryParser = new ComplexPhraseQueryParser("", synonymsAnalyzer);
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
        multiFieldQueryParser.setAutoGenerateMultiTermSynonymsPhraseQuery(true);
        multiFieldQueryParser.setDefaultOperator(QueryParser.Operator.AND);
        for (String input : queries) {
            Query parsedQuery = multiFieldQueryParser.parse(input.trim());
            System.out.println(parsedQuery);
        }
    }

    private SynonymMap buildSynonym(InputStream stream) throws IOException, java.text.ParseException {
        SolrSynonymParser parser = new SolrSynonymParser(true, true, new StandardAnalyzer(CharArraySet.EMPTY_SET));
        parser.parse(new InputStreamReader(stream));
        return parser.build();
    }
}
