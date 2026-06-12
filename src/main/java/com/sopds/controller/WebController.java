package com.sopds.controller;

import com.sopds.config.SopdsProperties;
import com.sopds.domain.Author;
import com.sopds.domain.Book;
import com.sopds.domain.Catalog;
import com.sopds.domain.Genre;
import com.sopds.domain.Series;
import com.sopds.dto.PaginatorDto;
import com.sopds.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebController {

    private final SopdsProperties properties;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final SeriesRepository seriesRepository;
    private final GenreRepository genreRepository;
    private final CatalogRepository catalogRepository;

    private static final int MAX_ITEMS = 60;
    private static final int HALF_PAGES_LINKS = 3;

    /**
     * Главная страница
     */
    @GetMapping({"/", "/web"})
    public String hello(Model model) {
        model.addAttribute("breadcrumbs", List.of("HOME"));
        model.addAttribute("current", "home");
        return "sopds_hello";
    }

    @GetMapping("/search/books")
    @Transactional(readOnly = true)
    public String searchBooks(
            @RequestParam(defaultValue = "m") String searchtype,
            @RequestParam(defaultValue = "") String searchterms,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        log.info("searchBooks: searchtype={}, searchterms='{}', page={}", searchtype, searchterms, page);

        page = Math.max(1, page);
        PageRequest pageRequest = PageRequest.of(page - 1, MAX_ITEMS);
        Page<Book> booksPage;
        List<String> breadcrumbs = new ArrayList<>();
        String searchobject = "title";

        switch (searchtype) {
            case "m" -> {
                booksPage = bookRepository.searchByTitleContains(searchterms.toLowerCase(), pageRequest);
                breadcrumbs = List.of("Книги", "Поиск по названию", searchterms);
                searchobject = "title";
            }
            case "b" -> {
                booksPage = bookRepository.searchByTitleStartsWith(searchterms.toLowerCase(), pageRequest);
                breadcrumbs = List.of("Книги", "Поиск по названию", searchterms);
                searchobject = "title";
            }
            case "a" -> {
                try {
                    Long authorId = Long.parseLong(searchterms);
                    Author author = authorRepository.findById(authorId).orElse(null);
                    String authorName = author != null ? author.getFullName() : "";
                    booksPage = bookRepository.findByAuthorId(authorId, pageRequest);
                    breadcrumbs = List.of("Книги", "Поиск по автору", authorName);
                } catch (NumberFormatException e) {
                    booksPage = Page.empty(pageRequest);
                    breadcrumbs = List.of("Книги", "Поиск по автору");
                }
                searchobject = "author";
            }
            case "s" -> {
                try {
                    Long seriesId = Long.parseLong(searchterms);
                    Series series = seriesRepository.findById(seriesId).orElse(null);
                    String seriesName = series != null ? series.getSer() : "";
                    booksPage = bookRepository.findBySeriesId(seriesId, pageRequest);
                    breadcrumbs = List.of("Книги", "Поиск по серии", seriesName);
                } catch (NumberFormatException e) {
                    booksPage = Page.empty(pageRequest);
                    breadcrumbs = List.of("Книги", "Поиск по серии");
                }
                searchobject = "series";
            }
            case "g" -> {
                try {
                    Long genreId = Long.parseLong(searchterms);
                    Genre genre = genreRepository.findById(genreId).orElse(null);
                    booksPage = bookRepository.findByGenreId(genreId, pageRequest);
                    if (genre != null) {
                        breadcrumbs = List.of("Книги", "Поиск по жанру", genre.getSection(), genre.getSubsection());
                    } else {
                        breadcrumbs = List.of("Книги", "Поиск по жанру");
                    }
                } catch (NumberFormatException e) {
                    booksPage = Page.empty(pageRequest);
                    breadcrumbs = List.of("Книги", "Поиск по жанру");
                }
                searchobject = "genre";
            }
            case "i" -> {
                try {
                    Long bookId = Long.parseLong(searchterms);
                    Book book = bookRepository.findById(bookId).orElse(null);
                    List<Book> bookList = book != null ? List.of(book) : List.of();
                    booksPage = new PageImpl<>(bookList, pageRequest, bookList.size());
                    breadcrumbs = List.of("Книги", book != null ? book.getTitle() : "");
                } catch (NumberFormatException e) {
                    booksPage = Page.empty(pageRequest);
                    breadcrumbs = List.of("Книги");
                }
                searchobject = "title";
            }
            case "d" -> {
                try {
                    Long bookId = Long.parseLong(searchterms);
                    Book book = bookRepository.findById(bookId).orElse(null);
                    if (book != null) {
                        List<Long> authorIds = book.getAuthors().stream().map(Author::getId).toList();
                        List<Book> doubles = authorIds.isEmpty() ? List.of() : bookRepository.findDoubles(book.getTitle(), authorIds, bookId);
                        booksPage = new PageImpl<>(doubles, pageRequest, doubles.size());
                        breadcrumbs = List.of("Книги", "Дубликаты", book.getTitle());
                    } else {
                        booksPage = Page.empty(pageRequest);
                        breadcrumbs = List.of("Книги", "Дубликаты");
                    }
                } catch (NumberFormatException e) {
                    booksPage = Page.empty(pageRequest);
                    breadcrumbs = List.of("Книги", "Дубликаты");
                }
                searchobject = "title";
            }
            default -> {
                booksPage = Page.empty(pageRequest);
                breadcrumbs = List.of("Книги");
            }
        }

        // Инициализируем lazy-коллекции в рамках транзакции
        List<Book> books = booksPage.getContent();
        for (Book book : books) {
            // Просто обращаемся к коллекциям чтобы инициализировать их
            if (book.getAuthors() != null) book.getAuthors().size();
            if (book.getGenres() != null) book.getGenres().size();
            if (book.getSeries() != null) book.getSeries().size();
        }

        PaginatorDto paginator = PaginatorDto.of(page, (int) booksPage.getTotalElements(), MAX_ITEMS, HALF_PAGES_LINKS);

        model.addAttribute("books", books);  // Передаём List<Book> напрямую!
        model.addAttribute("paginator", paginator);
        model.addAttribute("searchtype", searchtype);
        model.addAttribute("searchterms", searchterms);
        model.addAttribute("searchobject", searchobject);
        model.addAttribute("breadcrumbs", breadcrumbs);
        model.addAttribute("current", "search");

        return "sopds_books";
    }

    /**
     * Поиск авторов
     */
    @GetMapping("/search/authors")
    @Transactional(readOnly = true)
    public String searchAuthors(
            @RequestParam(defaultValue = "m") String searchtype,
            @RequestParam(defaultValue = "") String searchterms,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        page = Math.max(1, page);
        PageRequest pageRequest = PageRequest.of(page - 1, MAX_ITEMS);
        Page<Author> authorsPage;

        switch (searchtype) {
            case "m" -> authorsPage = authorRepository.searchByNameContains(searchterms.toLowerCase(), pageRequest);
            case "b" -> authorsPage = authorRepository.searchByNameStartsWith(searchterms.toLowerCase(), pageRequest);
            case "e" -> authorsPage = authorRepository.searchByNameExact(searchterms.toLowerCase(), pageRequest);
            default -> authorsPage = authorRepository.searchByNameContains(searchterms.toLowerCase(), pageRequest);
        }

        // Преобразуем в DTO с подсчётом книг
        List<Map<String, Object>> authors = new ArrayList<>();
        for (Author author : authorsPage.getContent()) {
            Map<String, Object> authorMap = new HashMap<>();
            authorMap.put("id", author.getId());
            authorMap.put("fullName", author.getFullName());
            authorMap.put("langCode", author.getLangCode());
            authorMap.put("bookCount", author.getBooks() != null ? author.getBooks().size() : 0);
            authors.add(authorMap);
        }

        PaginatorDto paginator = PaginatorDto.of(page, (int) authorsPage.getTotalElements(), MAX_ITEMS, HALF_PAGES_LINKS);

        model.addAttribute("authors", authors);
        model.addAttribute("paginator", paginator);
        model.addAttribute("searchtype", searchtype);
        model.addAttribute("searchterms", searchterms);
        model.addAttribute("searchobject", "author");
        model.addAttribute("breadcrumbs", List.of("Авторы", "Поиск", searchterms));
        model.addAttribute("current", "search");

        return "sopds_authors";
    }

    /**
     * Поиск серий
     */
    @GetMapping("/search/series")
    @Transactional(readOnly = true)
    public String searchSeries(
            @RequestParam(defaultValue = "m") String searchtype,
            @RequestParam(defaultValue = "") String searchterms,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        page = Math.max(1, page);
        PageRequest pageRequest = PageRequest.of(page - 1, MAX_ITEMS);
        Page<Series> seriesPage;

        switch (searchtype) {
            case "m" -> seriesPage = seriesRepository.searchByNameContains(searchterms.toLowerCase(), pageRequest);
            case "b" -> seriesPage = seriesRepository.searchByNameStartsWith(searchterms.toLowerCase(), pageRequest);
            case "e" -> seriesPage = seriesRepository.searchByNameExact(searchterms.toLowerCase(), pageRequest);
            default -> seriesPage = seriesRepository.searchByNameContains(searchterms.toLowerCase(), pageRequest);
        }

        // Преобразуем в DTO с подсчётом книг
        List<Map<String, Object>> seriesList = new ArrayList<>();
        for (Series series : seriesPage.getContent()) {
            Map<String, Object> seriesMap = new HashMap<>();
            seriesMap.put("id", series.getId());
            seriesMap.put("ser", series.getSer());
            seriesMap.put("langCode", series.getLangCode());
            seriesMap.put("bookCount", series.getBooks() != null ? series.getBooks().size() : 0);
            seriesList.add(seriesMap);
        }

        PaginatorDto paginator = PaginatorDto.of(page, (int) seriesPage.getTotalElements(), MAX_ITEMS, HALF_PAGES_LINKS);

        model.addAttribute("series", seriesList);
        model.addAttribute("paginator", paginator);
        model.addAttribute("searchtype", searchtype);
        model.addAttribute("searchterms", searchterms);
        model.addAttribute("searchobject", "series");
        model.addAttribute("breadcrumbs", List.of("Серии", "Поиск", searchterms));
        model.addAttribute("current", "search");

        return "sopds_series";
    }

    /**
     * Каталоги
     */
    @GetMapping("/catalog")
    @Transactional(readOnly = true)
    public String catalogs(
            @RequestParam(required = false) Long cat,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        page = Math.max(1, page);

        Catalog currentCatalog = null;
        List<Catalog> catalogs;
        List<Book> books;

        if (cat != null) {
            currentCatalog = catalogRepository.findById(cat).orElse(null);
            catalogs = catalogRepository.findByParentId(cat);
            books = bookRepository.findByCatalogId(cat);
        } else {
            catalogs = catalogRepository.findRootCatalogs();
            books = List.of();
        }

        // Формируем items (каталоги + книги)
        List<Map<String, Object>> items = new ArrayList<>();

        for (Catalog catalog : catalogs) {
            Map<String, Object> item = new HashMap<>();
            item.put("isCatalog", 1);
            item.put("id", catalog.getId());
            item.put("title", catalog.getCatName());
            item.put("catType", catalog.getCatType());
            item.put("parentId", catalog.getParent() != null ? catalog.getParent().getId() : null);
            items.add(item);
        }

        for (Book book : books) {
            Map<String, Object> item = new HashMap<>();
            item.put("isCatalog", 0);
            item.put("id", book.getId());
            item.put("title", book.getTitle());
            item.put("format", book.getFormat());
            item.put("authors", book.getAuthors());
            items.add(item);
        }

        // Пагинация
        int totalItems = items.size();
        PaginatorDto paginator = PaginatorDto.of(page, totalItems, MAX_ITEMS, HALF_PAGES_LINKS);
        int start = paginator.getFirstItemIndex(MAX_ITEMS);
        int end = Math.min(start + MAX_ITEMS, totalItems);
        List<Map<String, Object>> pageItems = items.subList(start, end);

        // Breadcrumbs
        List<Object[]> breadcrumbsCat = new ArrayList<>();
        if (currentCatalog != null) {
            Catalog c = currentCatalog;
            while (c.getParent() != null) {
                breadcrumbsCat.add(0, new Object[]{c.getCatName(), c.getId()});
                c = c.getParent();
            }
            breadcrumbsCat.add(0, new Object[]{"ROOT", 0});
        }

        model.addAttribute("items", pageItems);
        model.addAttribute("paginator", paginator);
        model.addAttribute("catId", cat);
        model.addAttribute("breadcrumbs", List.of("Каталоги"));
        model.addAttribute("breadcrumbsCat", breadcrumbsCat);
        model.addAttribute("current", "catalog");

        return "sopds_catalogs";
    }

    /**
     * Выбор книг по алфавиту
     */
    @GetMapping("/book")
    public String selectBook(
            @RequestParam(defaultValue = "0") int lang,
            @RequestParam(defaultValue = "") String chars,
            Model model) {

        int length = chars.length() + 1;

        List<Object[]> rawItems = bookRepository.findAll().stream()
                .filter(b -> b.getAvail() != null && b.getAvail() == 2)
                .filter(b -> lang == 0 || (b.getLangCode() != null && b.getLangCode() == lang))
                .filter(b -> b.getSearchTitle() != null && b.getSearchTitle().toUpperCase().startsWith(chars.toUpperCase()))
                .map(b -> b.getSearchTitle().toUpperCase())
                .filter(t -> t.length() >= length)
                .map(t -> t.substring(0, length))
                .distinct()
                .sorted()
                .map(prefix -> new Object[]{prefix, countBooksWithPrefix(prefix, lang)})
                .toList();

        List<Map<String, Object>> items = new ArrayList<>();
        for (Object[] row : rawItems) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", row[0]);
            item.put("cnt", row[1]);
            items.add(item);
        }

        Map<Integer, String> langMenuMap = new LinkedHashMap<>();
        langMenuMap.put(0, "Все языки");
        langMenuMap.put(1, "Русский");
        langMenuMap.put(2, "Английский");

        String langName = langMenuMap.getOrDefault(lang, "Все языки");

        model.addAttribute("items", items);
        model.addAttribute("langCode", lang);
        model.addAttribute("breadcrumbs", List.of("Книги", "Выбор", langName, chars));
        model.addAttribute("current", "book");

        return "sopds_selectbook";
    }

    private long countBooksWithPrefix(String prefix, int lang) {
        return bookRepository.findAll().stream()
                .filter(b -> b.getAvail() != null && b.getAvail() == 2)
                .filter(b -> lang == 0 || (b.getLangCode() != null && b.getLangCode() == lang))
                .filter(b -> b.getSearchTitle() != null && b.getSearchTitle().toUpperCase().startsWith(prefix.toUpperCase()))
                .count();
    }

    /**
     * Выбор авторов по алфавиту
     */
    @GetMapping("/author")
    public String selectAuthor(
            @RequestParam(defaultValue = "0") int lang,
            @RequestParam(defaultValue = "") String chars,
            Model model) {

        int length = chars.length() + 1;

        List<Object[]> rawItems = authorRepository.findAll().stream()
                .filter(a -> lang == 0 || (a.getLangCode() != null && a.getLangCode() == lang))
                .filter(a -> a.getSearchFullName() != null && a.getSearchFullName().toUpperCase().startsWith(chars.toUpperCase()))
                .map(a -> a.getSearchFullName().toUpperCase())
                .filter(n -> n.length() >= length)
                .map(n -> n.substring(0, length))
                .distinct()
                .sorted()
                .map(prefix -> new Object[]{prefix, countAuthorsWithPrefix(prefix, lang)})
                .toList();

        List<Map<String, Object>> items = new ArrayList<>();
        for (Object[] row : rawItems) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", row[0]);
            item.put("cnt", row[1]);
            items.add(item);
        }

        Map<Integer, String> langMenuMap = new LinkedHashMap<>();
        langMenuMap.put(0, "Все языки");

        String langName = langMenuMap.getOrDefault(lang, "Все языки");

        model.addAttribute("items", items);
        model.addAttribute("langCode", lang);
        model.addAttribute("breadcrumbs", List.of("Авторы", "Выбор", langName, chars));
        model.addAttribute("current", "author");

        return "sopds_selectauthor";
    }

    private long countAuthorsWithPrefix(String prefix, int lang) {
        return authorRepository.findAll().stream()
                .filter(a -> lang == 0 || (a.getLangCode() != null && a.getLangCode() == lang))
                .filter(a -> a.getSearchFullName() != null && a.getSearchFullName().toUpperCase().startsWith(prefix.toUpperCase()))
                .count();
    }

    /**
     * Выбор серий по алфавиту
     */
    @GetMapping("/series")
    public String selectSeries(
            @RequestParam(defaultValue = "0") int lang,
            @RequestParam(defaultValue = "") String chars,
            Model model) {

        int length = chars.length() + 1;

        List<Object[]> rawItems = seriesRepository.findAll().stream()
                .filter(s -> lang == 0 || (s.getLangCode() != null && s.getLangCode() == lang))
                .filter(s -> s.getSearchSer() != null && s.getSearchSer().toUpperCase().startsWith(chars.toUpperCase()))
                .map(s -> s.getSearchSer().toUpperCase())
                .filter(n -> n.length() >= length)
                .map(n -> n.substring(0, length))
                .distinct()
                .sorted()
                .map(prefix -> new Object[]{prefix, countSeriesWithPrefix(prefix, lang)})
                .toList();

        List<Map<String, Object>> items = new ArrayList<>();
        for (Object[] row : rawItems) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", row[0]);
            item.put("cnt", row[1]);
            items.add(item);
        }

        Map<Integer, String> langMenuMap = new LinkedHashMap<>();
        langMenuMap.put(0, "Все языки");

        String langName = langMenuMap.getOrDefault(lang, "Все языки");

        model.addAttribute("items", items);
        model.addAttribute("langCode", lang);
        model.addAttribute("breadcrumbs", List.of("Серии", "Выбор", langName, chars));
        model.addAttribute("current", "series");

        return "sopds_selectseries";
    }

    private long countSeriesWithPrefix(String prefix, int lang) {
        return seriesRepository.findAll().stream()
                .filter(s -> lang == 0 || (s.getLangCode() != null && s.getLangCode() == lang))
                .filter(s -> s.getSearchSer() != null && s.getSearchSer().toUpperCase().startsWith(prefix.toUpperCase()))
                .count();
    }

    /**
     * Выбор жанров
     */
    @GetMapping("/genre")
    public String selectGenres(
            @RequestParam(defaultValue = "0") int section,
            Model model) {

        List<Map<String, Object>> items = new ArrayList<>();

        if (section == 0) {
            // Секции жанров
            List<Object[]> sections = genreRepository.getGenreSections();
            for (Object[] row : sections) {
                Map<String, Object> item = new HashMap<>();
                item.put("section", row[0]);
                item.put("sectionId", row[1]);
                item.put("numBook", row[2]);
                items.add(item);
            }
            model.addAttribute("breadcrumbs", List.of("Жанры", "Выбор"));
        } else {
            // Подсекции жанров
            Genre sectionGenre = genreRepository.findById((long) section).orElse(null);
            String sectionName = sectionGenre != null ? sectionGenre.getSection() : "";

            List<Object[]> subsections = genreRepository.getGenresBySection(sectionName);
            for (Object[] row : subsections) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", row[0]);
                item.put("genre", row[1]);
                item.put("section", row[2]);
                item.put("subsection", row[3]);
                item.put("numBook", row[4]);
                items.add(item);
            }
            model.addAttribute("breadcrumbs", List.of("Жанры", "Выбор", sectionName));
        }

        model.addAttribute("items", items);
        model.addAttribute("parentId", section);
        model.addAttribute("current", "genre");

        return "sopds_selectgenres";
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("breadcrumbs", List.of("Вход"));
        model.addAttribute("current", "login");
        return "sopds_login";
    }
}
