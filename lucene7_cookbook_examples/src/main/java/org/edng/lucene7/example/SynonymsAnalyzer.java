package org.edng.lucene7.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;

public class SynonymsAnalyzer extends Analyzer {

    private SynonymMap synonyms;

    public SynonymsAnalyzer(SynonymMap synonyms) {
        this.synonyms = synonyms;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final StandardTokenizer src = new StandardTokenizer();
        TokenStream filter = new LowerCaseFilter(src);
        filter = new SynonymGraphFilter(filter, synonyms, true);
        return new TokenStreamComponents(src, filter);
    }
}
