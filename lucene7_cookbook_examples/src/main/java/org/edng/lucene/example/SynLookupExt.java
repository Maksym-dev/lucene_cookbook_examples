package org.edng.lucene.example;

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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;


/**
 * Test program to look up synonyms.
 */
public class SynLookupExt {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println(
                "java org.apache.lucene.wordnet.SynLookup <index path> <word>");
        }

        FSDirectory directory = FSDirectory.open(new File(args[0]).toPath());
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(indexReader);

        String word = args[1];
        Query query = new TermQuery(new Term(Syns2IndexExt.F_WORD, word));
        CountingCollector countingCollector = new CountingCollector();
        searcher.search(query, countingCollector);

        if (countingCollector.numHits == 0) {
            System.out.println("No synonyms found for " + word);
        } else {
            System.out.println("Synonyms found for \"" + word + "\":");
        }

        ScoreDoc[] hits = searcher.search(query, countingCollector.numHits).scoreDocs;

        for (ScoreDoc hit : hits) {
            Document doc = searcher.doc(hit.doc);

            String[] values = doc.getValues(Syns2IndexExt.F_SYN);

            for (String value : values) {
                System.out.println(value);
            }
        }

        indexReader.close();
        directory.close();
    }

    final static class CountingCollector implements Collector {

        public int numHits = 0;

        @Override
        public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
            return new SimpleCollector() {
                @Override
                public void collect(int doc) throws IOException {
                    numHits++;
                }

                @Override
                public boolean needsScores() {
                    return false;
                }
            };
        }

        @Override
        public boolean needsScores() {
            return false;
        }
    }
}
