package org.edng.lucene7.example;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * Expand a query by looking up synonyms for every term.
 * You need to invoke {@link Syns2IndexExt} first to build the synonym index.
 *
 * @see Syns2IndexExt
 */
public final class SynExpandExt {

    /**
     * Test driver for synonym expansion.
     * Uses boost factor of 1.2 and 0.9 for illustrative purposes.
     *
     * If you pass in the query "big dog" then it prints out:
     *
     * <code><pre>
     * Query: big^1.2 adult^0.9 bad^0.9 bighearted^0.9 boastful^0.9 boastfully^0.9 bounteous^0.9 bountiful^0.9 braggy^0.9
     * crowing^0.9 freehanded^0.9 giving^0.9 grown^0.9 grownup^0.9 handsome^0.9 large^0.9 liberal^0.9 magnanimous^0.9
     * momentous^0.9 openhanded^0.9 prominent^0.9 swelled^0.9 vainglorious^0.9 vauntingly^0.9
     * dog^1.2 andiron^0.9 blackguard^0.9 bounder^0.9 cad^0.9 chase^0.9 click^0.9 detent^0.9 dogtooth^0.9 firedog^0.9
     * frank^0.9 frankfurter^0.9 frump^0.9 heel^0.9 hotdog^0.9 hound^0.9 pawl^0.9 tag^0.9 tail^0.9 track^0.9 trail^0
     * .9 weenie^0.9 wiener^0.9 wienerwurst^0.9
     * </pre></code>
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println(
                "java org.apache.lucene.wordnet.SynExpand <index path> <query>");
        }

        FSDirectory directory = FSDirectory.open(new File(args[0]).toPath());
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(indexReader);

        String query = args[1];
        String field = "contents";

        Query q = expand(query, searcher, new StandardAnalyzer(), field, 1.2f,0.9f);
        System.out.println("Query: " + q.toString(field));


        indexReader.close();
        directory.close();
    }


    /**
     * Perform synonym expansion on a query.
     *
     * @param query users query that is assumed to not have any "special" query syntax, thus it should be just normal
     *             words, so "big dog" makes sense, but a query like "title:foo^1.2" doesn't as this should
     *              presumably be passed directly to the default query parser.
     *
     * @param syns a opened to the Lucene index you previously created with
     * {@link Syns2IndexExt}. The searcher is not closed or otherwise altered.
     *
     * @param a optional analyzer used to parse the users query else {@link StandardAnalyzer} is used
     *
     * @param f optional field name to search in or null if you want the default of "contents"
     *
     * @param wordBoost optional boost applied to words else no boost is applied
     *
     * @param synBoost optional boost applied to synonyms else no boost is applied
     *
     * @return the expanded Query
     */
    public static Query expand(String query,
                               IndexSearcher syns,
                               Analyzer a,
                               String f,
                               final float wordBoost,
                               final float synBoost)
        throws IOException {
        final Set<String> already = new TreeSet<>(); // avoid dups
        List<String> top = new LinkedList<>(); // needs to be separately listed..
        final String field = (f == null) ? "contents" : f;
        if (a == null) {
            a = new StandardAnalyzer();
        }

        // [1] Parse query into separate words so that when we expand we can avoid dups
        TokenStream ts = a.tokenStream(field, new StringReader(query));
        CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
        ts.reset();
        while (ts.incrementToken()) {
            String word = termAtt.toString();
            if (already.add(word)) {
                top.add(word);
            }
        }
        ts.end();
        ts.close();
        BooleanQuery.Builder tmp = new BooleanQuery.Builder();

        // [2] form query
        for (String word : top) {
            // [2a] add to level words in
            Query tq = new TermQuery(new Term(field, word));
            if (wordBoost > 0) {
                tq = new BoostQuery(tq, wordBoost);
            }
            tmp.add(tq, BooleanClause.Occur.SHOULD);

            syns.search(new TermQuery(new Term(Syns2IndexExt.F_WORD, word)), new Collector() {

                @Override
                public LeafCollector getLeafCollector(LeafReaderContext context) {
                    return new SimpleCollector() {
                        @Override
                        public void collect(int doc) throws IOException {
                            Document d = syns.getIndexReader().document(doc);
                            String[] values = d.getValues(Syns2IndexExt.F_SYN);
                            for (String syn : values) {
                                if (already.add(syn)) { // avoid dups of top level words and synonyms
                                    Query tq = new TermQuery(new Term(field, syn));
                                    if (synBoost > 0) { // else keep normal 1.0
                                        tq = new BoostQuery(tq, synBoost);
                                    }
                                    tmp.add(tq, BooleanClause.Occur.SHOULD);
                                }
                            }
                        }

                        @Override
                        public boolean needsScores() {
                            return true;
                        }
                    };
                }

                @Override
                public boolean needsScores() {
                    return true;
                }
            });

            // [2b] add in unique synonums
        }
        return tmp.build();
    }

}

