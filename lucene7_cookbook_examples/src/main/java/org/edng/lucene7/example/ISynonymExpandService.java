package org.edng.lucene7.example;

import org.apache.lucene.queryparser.classic.ParseException;

public interface ISynonymExpandService {

    String expand(String inputQuery) throws ParseException;
}
