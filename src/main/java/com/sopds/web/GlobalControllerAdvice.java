package com.sopds.web;

import com.sopds.config.SopdsProperties;
import com.sopds.domain.Book;
import com.sopds.dto.StatsDto;
import com.sopds.repository.AuthorRepository;
import com.sopds.repository.BookRepository;
import com.sopds.repository.GenreRepository;
import com.sopds.repository.SeriesRepository;
import com.sopds.service.CounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final SopdsProperties properties;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final SeriesRepository seriesRepository;
    private final CounterService counterService;

    // Языковое меню
    private static final Map<Integer, String> LANG_MENU = new LinkedHashMap<>();
    static {
        LANG_MENU.put(0, "Все языки");
        LANG_MENU.put(1, "Русский");
        LANG_MENU.put(2, "Английский");
        LANG_MENU.put(3, "Немецкий");
        LANG_MENU.put(4, "Французский");
        LANG_MENU.put(5, "Испанский");
        LANG_MENU.put(6, "Итальянский");
        LANG_MENU.put(7, "Украинский");
        LANG_MENU.put(8, "Белорусский");
        LANG_MENU.put(9, "Другие");
    }

    @ModelAttribute("appTitle")
    public String appTitle() {
        return "Simple OPDS";
    }

    @ModelAttribute("sopdsVersion")
    public String sopdsVersion() {
        return "1.0-JAVA";
    }

    @ModelAttribute("sopdsAuth")
    public boolean sopdsAuth() {
        // Пока отключена авторизация
        return false;
    }

    @ModelAttribute("alphabet")
    public boolean alphabet() {
        // Включено ли меню по языкам
        return true;
    }

    @ModelAttribute("splititems")
    public int splititems() {
        return 300;
    }

    @ModelAttribute("fb2toepub")
    public boolean fb2toepub() {
        return false;
    }

    @ModelAttribute("fb2tomobi")
    public boolean fb2tomobi() {
        return false;
    }

    @ModelAttribute("nozip")
    public String[] nozip() {
        return new String[]{"epub", "mobi", "pdf", "djvu"};
    }

    @ModelAttribute("langMenu")
    public Map<Integer, String> langMenu() {
        return LANG_MENU;
    }

    @ModelAttribute("stats")
    public StatsDto stats() {
        return StatsDto.builder()
                .allbooks(bookRepository.countAvailable())
                .allauthors(authorRepository.count())
                .allgenres(genreRepository.count())
                .allseries(seriesRepository.count())
                .lastscanDate(counterService.getLastScanDate())
                .build();
    }

    @ModelAttribute("randomBook")
    public Book randomBook() {
        long count = bookRepository.countAvailable();
        if (count == 0) {
            return null;
        }
        int randomIndex = new Random().nextInt((int) count);
        return bookRepository.findRandomBook(randomIndex);
    }
}
