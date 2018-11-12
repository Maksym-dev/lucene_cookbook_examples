package org.edng.lucene7.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.ext.ExtendableQueryParser;
import org.apache.lucene.queryparser.ext.ExtensionQuery;
import org.apache.lucene.queryparser.ext.Extensions;
import org.apache.lucene.queryparser.ext.ParserExtension;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

public class ExtendableQueryParserTest {

    @Test
    public void runTest() throws Exception {
        // Be careful: QueryParser is not thread-safe.
        QueryParser queryParser = new ExtendableQueryParser("", new StandardAnalyzer(), createExtensions());
        Query query = queryParser.parse("_spanfirst:hello");

        // prints "spanFirst(spanNear([hello], 1, true), 1)"
        System.out.println(query);

        queryParser = new ExtendableQueryParser("text", new StandardAnalyzer(), createExtensions());
        query = queryParser.parse("_spanfirst:hello");

        // prints "spanFirst(spanNear([text:hello], 1, true), 1)"
        System.out.println(query);

        queryParser = new ExtendableQueryParser("text", new StandardAnalyzer(), createExtensions());
        query = queryParser.parse("hello");

        // prints "text:hello"
        System.out.println(query);
    }

    private static Extensions createExtensions() {
        ParserExtension parserExtension = new ParserExtension() {

            @Override
            public Query parse(final ExtensionQuery query) throws ParseException {

                QueryParser parser = query.getTopLevelParser();
                // Although Analyzer implements java.io.Closeable, don't use it in try-with-resources.
                // Otherwise, it won't be able to parse the rest of the query...
                Analyzer analyzer = parser.getAnalyzer();
                String rawQueryString = query.getRawQueryString();

                List<SpanQuery> spans = new LinkedList<>();
                try (TokenStream stream = analyzer.tokenStream(query.getField(), new StringReader(rawQueryString))) {

                    stream.reset();

                    while (stream.incrementToken()) {
                        String term = stream.getAttribute(CharTermAttribute.class).toString();
                        spans.add(new SpanTermQuery(new Term(query.getField(), term)));
                    }

                } catch (IOException e) {
                    // Shouldn't happen as it's being read from RAM.
                    throw new ParseException(e.getMessage());
                }

                SpanQuery[] spansArray = spans.toArray(new SpanQuery[0]);
                SpanNearQuery spanNear = new SpanNearQuery(spansArray, 1, true);
                return new SpanFirstQuery(spanNear, 1);
            }
        };

        Extensions extensions = new Extensions('_');
        extensions.add("spanfirst", parserExtension);
        return extensions;
    }
}
