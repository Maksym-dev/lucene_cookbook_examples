package org.edng.lucene.example;

import static org.junit.Assert.assertTrue;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.uhighlight.DefaultPassageFormatter;
import org.apache.lucene.search.uhighlight.UnifiedHighlighter;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by ed on 4/1/15.
 */
public class HighlighterTest {

    @Test
    public void runTest() throws Exception {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        doc.add(new TextField("content", "Humpty Dumpty sat on a wall", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new TextField("content", "Humpty Dumpty had a great fall", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new TextField("content", "All the king's horses and all the king's men", Field.Store.YES));
        indexWriter.addDocument(doc);
        doc = new Document();
        doc.add(new TextField("content", "Couldn't put Humpty together again", Field.Store.YES));
        indexWriter.addDocument(doc);

        indexWriter.commit();
        indexWriter.close();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Query query = new TermQuery(new Term("content", "humpty"));

        TopDocs topDocs = indexSearcher.search(query, 10);

        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<strong>", "</strong>");
        SimpleHTMLEncoder simpleHTMLEncoder = new SimpleHTMLEncoder();
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, simpleHTMLEncoder, new QueryScorer(query));

        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            doc = indexSearcher.doc(scoreDoc.doc);
            String text = doc.get("content");
            TokenStream tokenStream = TokenSources.getAnyTokenStream(indexReader, scoreDoc.doc, "content", analyzer);
            TextFragment[] textFragments = highlighter.getBestTextFragments(tokenStream, text, false, 10);
            for (TextFragment textFragment : textFragments) {
                if (textFragment != null && textFragment.getScore() > 0) {
                    String fragment = textFragment.toString();
                    System.out.println(fragment);
                    assertTrue("Result should contain strong tag <strong>Humpty</strong> but gotten \"" + textFragment + "\" instead", fragment.contains("<strong>Humpty</strong>"));
                }
            }
        }

    }

    @Test
    public void runTestWithUnifiedHighlighter() throws Exception {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        FieldType contentsType = new FieldType();
        contentsType.setStored(true);
        contentsType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        contentsType.setStoreTermVectors(true);
        contentsType.setStoreTermVectorOffsets(true);
        contentsType.setStoreTermVectorPositions(true);
        contentsType.setStoreTermVectorPayloads(true);
        String fieldName = "content";
        Field textField = new Field(fieldName, "Humpty Dumpty sat on a wall", contentsType);
        doc.add(textField);
        indexWriter.addDocument(doc);
        doc = new Document();
        Field content = new Field(fieldName, "Humpty Dumpty had a great fall", contentsType);
        doc.add(content);
        indexWriter.addDocument(doc);
        doc = new Document();
        Field content1 = new Field(fieldName, "All the king's horses and all the king's men", contentsType);
        doc.add(content1);
        indexWriter.addDocument(doc);
        doc = new Document();
        Field content2 = new Field(fieldName, "Couldn't put Humpty together again", contentsType);
        doc.add(content2);
        indexWriter.addDocument(doc);

        indexWriter.commit();
        indexWriter.close();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        Query phraseQuery = new QueryBuilder(analyzer).createPhraseQuery(fieldName, "humpty dumpty");
        TopDocs topDocs = indexSearcher.search(phraseQuery, 10, Sort.INDEXORDER);
        UnifiedHighlighter highlighter = new UnifiedHighlighter(indexSearcher, analyzer);
        highlighter.setHandleMultiTermQuery(true);
        highlighter.setHighlightPhrasesStrictly(true);
        String[] fragmentsTitle = highlighter.highlight(fieldName, phraseQuery, topDocs);
        System.out.println(Arrays.toString(fragmentsTitle));
        for (String fragment : fragmentsTitle) {
            assertTrue("Result should contain strong tag <b>Humpty</b> <b>Dumpty</b> but gotten \"" + fragment + "\" " +
                "instead", fragment.contains("<b>Humpty</b> <b>Dumpty</b>"));
        }
    }

    @Test
    public void runTestWithUnifiedHighlighterWithoutSearcher() throws Exception {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        FieldType contentsType = new FieldType();
        contentsType.setStored(true);
        contentsType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        contentsType.setStoreTermVectors(true);
        contentsType.setStoreTermVectorOffsets(true);
        contentsType.setStoreTermVectorPositions(true);
        contentsType.setStoreTermVectorPayloads(true);
        Field textField = new Field("content", "Humpty Dumpty sat on a wall", contentsType);
        doc.add(textField);
        indexWriter.addDocument(doc);
        doc = new Document();
        Field content = new Field("content", "Humpty Dumpty had a great fall", contentsType);
        doc.add(content);
        indexWriter.addDocument(doc);
        doc = new Document();
        Field content1 = new Field("content", "All the king's horses and all the king's men", contentsType);
        doc.add(content1);
        indexWriter.addDocument(doc);
        doc = new Document();
        Field content2 = new Field("content", "Couldn't put Humpty together again", contentsType);
        doc.add(content2);
        indexWriter.addDocument(doc);

        indexWriter.commit();
        indexWriter.close();

        MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"content"}, analyzer);
        parser.setAutoGenerateMultiTermSynonymsPhraseQuery(true);
        Query query = parser.parse("\"humpty dumpty\"");
        UnifiedHighlighter highlighter = new UnifiedHighlighter(null, analyzer);
        highlighter.setHandleMultiTermQuery(true);
        highlighter.setHighlightPhrasesStrictly(true);
        highlighter.setMaxNoHighlightPassages(0);
        highlighter.setFormatter(new DefaultPassageFormatter("<strong>", "</strong>", "... ", false));
        String fragmentsTitle = String.valueOf(highlighter.highlightWithoutSearcher("content", query, "Humpty Dumpty " +
            "sat on a wall", 5));
        System.out.println(fragmentsTitle);
        assertTrue("Result should contain strong tag <strong>Humpty</strong> <strong>Dumpty</strong> but gotten \"" + fragmentsTitle + "\" " +
            "instead", fragmentsTitle.contains("<strong>Humpty</strong> <strong>Dumpty</strong>"));
    }

    @Test
    public void runTestWithFastVectorHighlighter() throws Exception {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document doc = new Document();
        FieldType contentsType = new FieldType();
        contentsType.setStored(true);
        contentsType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        contentsType.setStoreTermVectors(true);
        contentsType.setStoreTermVectorOffsets(true);
        contentsType.setStoreTermVectorPositions(true);
        contentsType.setStoreTermVectorPayloads(true);
        String fieldName = "content";
        Field textField = new Field(fieldName, "Humpty Dumpty sat on a wall", contentsType);
        doc.add(textField);
        indexWriter.addDocument(doc);
        doc = new Document();
        Field content = new Field(fieldName, "Humpty Dumpty had a great fall", contentsType);
        doc.add(content);
        indexWriter.addDocument(doc);
        doc = new Document();
        Field content1 = new Field(fieldName, "All the king's horses and all the king's men", contentsType);
        doc.add(content1);
        indexWriter.addDocument(doc);
        doc = new Document();
        Field content2 = new Field(fieldName, "Couldn't put Humpty together again", contentsType);
        doc.add(content2);
        indexWriter.addDocument(doc);

        indexWriter.commit();
        indexWriter.close();

        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        QueryParser parser = new MultiFieldQueryParser(new String[]{fieldName}, analyzer);
        Query query = parser.parse("\"humpty dumpty\"");
        TopDocs topDocs = indexSearcher.search(query, 10);
        FastVectorHighlighter highlighter = new FastVectorHighlighter();
        FieldQuery fieldQuery = highlighter.getFieldQuery(query);
        for (int i = 0; i < topDocs.scoreDocs.length; ++i) {
            String[] fragmentsTitle = highlighter.getBestFragments(fieldQuery, indexReader, topDocs.scoreDocs[i].doc,
                fieldName, Integer.MAX_VALUE, 1);
            System.out.println(Arrays.toString(fragmentsTitle));
            for (String fragment : fragmentsTitle) {
                assertTrue("Result should contain strong tag <b>Humpty Dumpty</b> but gotten \"" + fragment + "\" " +
                    "instead", fragment.contains("<b>Humpty Dumpty</b>"));
            }
        }
    }
}
