package org.edng.lucene.example;

import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * Created by ed on 1/20/15.
 */
public class PayloadAnalyzer extends StopwordAnalyzerBase {

    protected TokenStreamComponents createComponents(final String fieldName) {
        final StandardTokenizer source = new StandardTokenizer();
        TokenStream filter = new StandardFilter(source);
        filter = new LowerCaseFilter(filter);
        filter = new StopFilter(filter, stopwords);
        filter = new PayloadFilter(filter);
        return new TokenStreamComponents(source, filter);
    }
}
