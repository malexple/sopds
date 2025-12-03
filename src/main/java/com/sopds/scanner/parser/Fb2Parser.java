package com.sopds.scanner.parser;

import com.sopds.scanner.model.AuthorInfo;
import com.sopds.scanner.model.BookInfo;
import com.sopds.scanner.model.GenreInfo;
import com.sopds.scanner.model.SeriesInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class Fb2Parser implements BookParser {

    @Override
    public boolean supports(String format) {
        return "fb2".equalsIgnoreCase(format);
    }

    @Override
    public BookInfo parse(InputStream inputStream, String filename) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);
        doc.getDocumentElement().normalize();

        Element titleInfo = getFirstElement(doc, "title-info");
        if (titleInfo == null) {
            log.warn("No title-info found in {}", filename);
            return createMinimalBookInfo(filename);
        }

        return BookInfo.builder()
                .title(parseTitle(titleInfo))
                .annotation(parseAnnotation(titleInfo))
                .language(parseLanguage(titleInfo))
                .docDate(parseDocDate(doc))
                .authors(parseAuthors(titleInfo))
                .genres(parseGenres(titleInfo))
                .series(parseSeries(titleInfo))
                .build();
    }

    private String parseTitle(Element titleInfo) {
        Element bookTitle = getFirstElement(titleInfo, "book-title");
        if (bookTitle != null) {
            return bookTitle.getTextContent().trim();
        }
        return "Unknown Title";
    }

    private String parseAnnotation(Element titleInfo) {
        Element annotation = getFirstElement(titleInfo, "annotation");
        if (annotation != null) {
            return annotation.getTextContent().trim();
        }
        return null;
    }

    private String parseLanguage(Element titleInfo) {
        Element lang = getFirstElement(titleInfo, "lang");
        if (lang != null) {
            return lang.getTextContent().trim();
        }
        return null;
    }

    private String parseDocDate(Document doc) {
        Element documentInfo = getFirstElement(doc, "document-info");
        if (documentInfo != null) {
            Element date = getFirstElement(documentInfo, "date");
            if (date != null) {
                String value = date.getAttribute("value");
                if (!value.isBlank()) {
                    return value;
                }
                return date.getTextContent().trim();
            }
        }
        return null;
    }

    private List<AuthorInfo> parseAuthors(Element titleInfo) {
        List<AuthorInfo> authors = new ArrayList<>();
        NodeList authorNodes = titleInfo.getElementsByTagName("author");

        for (int i = 0; i < authorNodes.getLength(); i++) {
            Element authorElement = (Element) authorNodes.item(i);

            AuthorInfo author = AuthorInfo.builder()
                    .firstName(getElementText(authorElement, "first-name"))
                    .middleName(getElementText(authorElement, "middle-name"))
                    .lastName(getElementText(authorElement, "last-name"))
                    .build();

            if (!author.getFullName().equals("Unknown Author")) {
                authors.add(author);
            }
        }

        return authors;
    }

    private List<GenreInfo> parseGenres(Element titleInfo) {
        List<GenreInfo> genres = new ArrayList<>();
        NodeList genreNodes = titleInfo.getElementsByTagName("genre");

        for (int i = 0; i < genreNodes.getLength(); i++) {
            String genreCode = genreNodes.item(i).getTextContent().trim();
            if (!genreCode.isBlank()) {
                genres.add(GenreInfo.builder()
                        .genre(genreCode)
                        .section(getGenreSection(genreCode))
                        .subsection(getGenreSubsection(genreCode))
                        .build());
            }
        }

        return genres;
    }

    private List<SeriesInfo> parseSeries(Element titleInfo) {
        List<SeriesInfo> seriesList = new ArrayList<>();
        NodeList sequenceNodes = titleInfo.getElementsByTagName("sequence");

        for (int i = 0; i < sequenceNodes.getLength(); i++) {
            Element seqElement = (Element) sequenceNodes.item(i);
            String name = seqElement.getAttribute("name");
            String numberStr = seqElement.getAttribute("number");

            if (name != null && !name.isBlank()) {
                int number = 0;
                try {
                    if (!numberStr.isBlank()) {
                        number = Integer.parseInt(numberStr);
                    }
                } catch (NumberFormatException ignored) {
                }

                seriesList.add(SeriesInfo.builder()
                        .name(name.trim())
                        .number(number)
                        .build());
            }
        }

        return seriesList;
    }

    private Element getFirstElement(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return (Element) nodes.item(0);
        }
        return null;
    }

    private Element getFirstElement(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return (Element) nodes.item(0);
        }
        return null;
    }

    private String getElementText(Element parent, String tagName) {
        Element element = getFirstElement(parent, tagName);
        if (element != null) {
            return element.getTextContent().trim();
        }
        return null;
    }

    private String getGenreSection(String genreCode) {
        // Simplified genre mapping
        if (genreCode.startsWith("sf")) return "Фантастика";
        if (genreCode.startsWith("detective")) return "Детективы";
        if (genreCode.startsWith("prose")) return "Проза";
        if (genreCode.startsWith("love")) return "Любовные романы";
        if (genreCode.startsWith("adv")) return "Приключения";
        if (genreCode.startsWith("child")) return "Детское";
        if (genreCode.startsWith("sci")) return "Наука";
        if (genreCode.startsWith("comp")) return "Компьютеры";
        if (genreCode.startsWith("ref")) return "Справочники";
        if (genreCode.startsWith("religion")) return "Религия";
        if (genreCode.startsWith("humor")) return "Юмор";
        if (genreCode.startsWith("home")) return "Дом и семья";
        return "Прочее";
    }

    private String getGenreSubsection(String genreCode) {
        return genreCode;
    }

    private BookInfo createMinimalBookInfo(String filename) {
        return BookInfo.builder()
                .title(filename.replaceAll("\\.[^.]+$", ""))
                .authors(List.of(AuthorInfo.builder()
                        .lastName("Unknown")
                        .build()))
                .build();
    }
}
