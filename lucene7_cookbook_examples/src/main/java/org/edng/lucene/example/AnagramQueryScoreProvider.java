package org.edng.lucene.example;

import org.apache.lucene.index.*;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * This class checks if an anagram of a search term exist in document, score is given
 * on matching anagram.
 *
 * Created by ed on 1/7/15.
 */
public class AnagramQueryScoreProvider extends CustomScoreProvider {

    private String field;
    private Set<String> terms;

    public AnagramQueryScoreProvider(LeafReaderContext context, String field, Set<String> terms) {
        super(context);
        this.field = field;
        this.terms = terms;
    }

    public float customScore(int doc, float subQueryScore, float valSrcScores[]) throws IOException {
        float score = subQueryScore;
        IndexReader indexReader = context.reader();
        Terms termsVector = indexReader.getTermVector(doc, field);
        if (termsVector == null) {
            return score;
        }
        TermsEnum termsEnum = termsVector.iterator();
        BytesRef term;
        String val;
        while ( (term = termsEnum.next()) != null ) {
            val = term.utf8ToString();
            if (terms.contains(val)) {
                continue;
            }
            for (String t : terms) {
                if (isAnagram(t, val)) {
                    score += 1f;
                }
            }
        }
        return score;
    }

    private boolean isAnagram(String word1, String word2) {
        if (word1.length() != word2.length()) {
            return false;
        }
        char[] chars1 = word1.toCharArray();
        char[] chars2 = word2.toCharArray();
        Arrays.sort(chars1);
        Arrays.sort(chars2);
        return Arrays.equals(chars1, chars2);
    }
}
