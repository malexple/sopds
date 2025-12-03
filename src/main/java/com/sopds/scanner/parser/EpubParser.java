package com.sopds.scanner.parser;

import com.sopds.scanner.model.AuthorInfo;
import com.sopds.scanner.model.BookInfo;
import com.sopds.scanner.model.GenreInfo;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@Slf4j
public class EpubParser implements BookParser {

    @Override
    public boolean supports(String format) {
        return "epub".equalsIgnoreCase(format);
    }

    @Override
    public BookInfo parse(InputStream inputStream, String filename) throws Exception {
        try (ZipInputStream zipIn = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                String name = entry.getName().toLowerCase();
                if (name.endsWith(".opf") || name.equals("content.opf") || name.contains("content.opf")) {
                    return parseOpf(zipIn, filename);
                }
            }
        }

        log.warn("No OPF file found in EPUB: {}", filename);
        return createMinimalBookInfo(filename);
    }

    private BookInfo parseOpf(InputStream inputStream, String filename) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);
        doc.getDocumentElement().normalize();

        String title = getMetadataValue(doc, "title");
        String language = getMetadataValue(doc, "language");
        String description = getMetadataValue(doc, "description");
        String date = getMetadataValue(doc, "date");

        return BookInfo.builder()
                .title(title != null ? title : filename.replaceAll("\\.[^.]+$", ""))
                .annotation(description)
                .language(language)
                .docDate(date)
                .authors(parseAuthors(doc))
                .genres(parseGenres(doc))
                .build();
    }

    private String getMetadataValue(Document doc, String dcElement) {
        // Try dc:element
        NodeList nodes = doc.getElementsByTagName("dc:" + dcElement);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }

        // Try without namespace
        nodes = doc.getElementsByTagName(dcElement);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }

        return null;
    }

    private List<AuthorInfo> parseAuthors(Document doc) {
        List<AuthorInfo> authors = new ArrayList<>();

        NodeList nodes = doc.getElementsByTagName("dc:creator");
        if (nodes.getLength() == 0) {
            nodes = doc.getElementsByTagName("creator");
        }

        for (int i = 0; i < nodes.getLength(); i++) {
            String authorName = nodes.item(i).getTextContent().trim();
            if (!authorName.isBlank()) {
                authors.add(parseAuthorName(authorName));
            }
        }

        return authors;
    }

    private AuthorInfo parseAuthorName(String fullName) {
        String[] parts = fullName.split("\\s+");
        if (parts.length >= 2) {
            return AuthorInfo.builder()
                    .firstName(parts[0])
                    .lastName(parts[parts.length - 1])
                    .middleName(parts.length > 2 ? parts[1] : null)
                    .build();
        }
        return AuthorInfo.builder()
                .lastName(fullName)
                .build();
    }

    private List<GenreInfo> parseGenres(Document doc) {
        List<GenreInfo> genres = new ArrayList<>();

        NodeList nodes = doc.getElementsByTagName("dc:subject");
        if (nodes.getLength() == 0) {
            nodes = doc.getElementsByTagName("subject");
        }

        for (int i = 0; i < nodes.getLength(); i++) {
            String subject = nodes.item(i).getTextContent().trim();
            if (!subject.isBlank()) {
                genres.add(GenreInfo.builder()
                        .genre(subject.toLowerCase().replace(" ", "_"))
                        .section("EPUB")
                        .subsection(subject)
                        .build());
            }
        }

        return genres;
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
