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
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Service to expand query that contains multi-word synonyms.
 */
public class SynonymExpandServiceFirst implements ISynonymExpandService {

    private Analyzer synonymsAnalyzer;

    private LuceneQuerySynonymExpandPostProcessor luceneQuerySynonymExpandPostProcessor;

    public SynonymExpandServiceFirst() {
        try {
            SynonymMap mySynonymMap = buildSynonym(SynonymExpandServiceFirst.class.getClassLoader().getResourceAsStream(
                "\\synonyms.txt"));
            synonymsAnalyzer = new SynonymsAnalyzer(mySynonymMap);
        } catch (IOException | java.text.ParseException e) {
            e.printStackTrace();
            synonymsAnalyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);
        }
        luceneQuerySynonymExpandPostProcessor = new LuceneQuerySynonymExpandPostProcessor();
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
        return  luceneQuerySynonymExpandPostProcessor.postProcess(parsedQuery).trim();
    }

   private SynonymMap buildSynonym(InputStream stream) throws IOException, java.text.ParseException {
        SolrSynonymParser parser = new SolrSynonymParser(true, true, new StandardAnalyzer(CharArraySet.EMPTY_SET));
        parser.parse(new InputStreamReader(stream));
        return parser.build();
    }

}
