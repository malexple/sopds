package com.sopds.service.reader;

import com.sopds.dto.BookContentDto;

import java.io.InputStream;

public interface BookReader {

    /**
     * Поддерживаемый формат
     */
    String getFormat();

    /**
     * Проверяет, поддерживается ли формат
     */
    boolean supports(String format);

    /**
     * Читает содержимое книги
     * @param inputStream поток данных книги
     * @param bookId ID книги
     * @param title название книги
     * @param page номер страницы (для постраничного чтения)
     * @return DTO с контентом
     */
    BookContentDto read(InputStream inputStream, Long bookId, String title, int page) throws Exception;
}
