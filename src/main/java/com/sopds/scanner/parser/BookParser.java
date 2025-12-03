package com.sopds.scanner.parser;

import com.sopds.scanner.model.BookInfo;

import java.io.InputStream;

public interface BookParser {

    boolean supports(String format);

    BookInfo parse(InputStream inputStream, String filename) throws Exception;
}
