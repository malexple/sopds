package com.sopds.catalog.service.parser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
@Component
public class Fb2Parser {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Fb2Metadata {
        private String title;
        private List<AuthorInfo> authors;
        private List<String> genres;
        private String annotation;
        private String lang;
        private String date;
        private String isbn;
        private String seriesName;
        private Integer seriesNumber;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class AuthorInfo {
            private String firstName;
            private String middleName;
            private String lastName;

            public String getFullName() {
                StringBuilder sb = new StringBuilder();
                if (lastName != null && !lastName.isEmpty()) {
                    sb.append(lastName);
                }
                if (firstName != null && !firstName.isEmpty()) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(firstName);
                }
                if (middleName != null && !middleName.isEmpty()) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(middleName);
                }
                return sb.length() > 0 ? sb.toString() : "Unknown Author";
            }
        }
    }

    public Fb2Metadata parse(InputStream inputStream) {
        try {
            SAXReader reader = new SAXReader();
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            Document document = reader.read(inputStream);
            Element root = document.getRootElement();

            // Get description element
            Element description = root.element("description");
            if (description == null) {
                log.warn("No description element found in FB2 file");
                return null;
            }

            Element titleInfo = description.element("title-info");
            if (titleInfo == null) {
                log.warn("No title-info element found in FB2 file");
                return null;
            }

            Fb2Metadata metadata = new Fb2Metadata();
            metadata.setAuthors(new ArrayList<>());
            metadata.setGenres(new ArrayList<>());

            // Parse title
            Element bookTitle = titleInfo.element("book-title");
            if (bookTitle != null) {
                metadata.setTitle(bookTitle.getTextTrim());
            }

            // Parse authors
            List<Element> authorElements = titleInfo.elements("author");
            for (Element author : authorElements) {
                Fb2Metadata.AuthorInfo authorInfo = Fb2Metadata.AuthorInfo.builder()
                        .firstName(getElementText(author, "first-name"))
                        .middleName(getElementText(author, "middle-name"))
                        .lastName(getElementText(author, "last-name"))
                        .build();
                metadata.getAuthors().add(authorInfo);
            }

            // Parse genres
            List<Element> genreElements = titleInfo.elements("genre");
            for (Element genre : genreElements) {
                metadata.getGenres().add(genre.getTextTrim());
            }

            // Parse annotation
            Element annotation = titleInfo.element("annotation");
            if (annotation != null) {
                metadata.setAnnotation(annotation.asXML().replaceAll("<[^>]+>", " ").trim());
            }

            // Parse language
            Element lang = titleInfo.element("lang");
            if (lang != null) {
                metadata.setLang(lang.getTextTrim());
            }

            // Parse date
            Element date = titleInfo.element("date");
            if (date != null) {
                metadata.setDate(date.getTextTrim());
            }

            // Parse series info
            Element sequence = titleInfo.element("sequence");
            if (sequence != null) {
                metadata.setSeriesName(sequence.attributeValue("name"));
                String number = sequence.attributeValue("number");
                if (number != null && !number.isEmpty()) {
                    try {
                        metadata.setSeriesNumber(Integer.parseInt(number));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid series number: {}", number);
                    }
                }
            }

            // Try to get ISBN from publish-info
            Element publishInfo = description.element("publish-info");
            if (publishInfo != null) {
                Element isbn = publishInfo.element("isbn");
                if (isbn != null) {
                    String isbnValue = isbn.getTextTrim();
                    // Sanitize ISBN - remove extra characters, limit length
                    if (isbnValue != null && !isbnValue.isEmpty()) {
                        isbnValue = isbnValue.replaceAll("[^0-9X-]", "");
                        if (isbnValue.length() > 50) {
                            isbnValue = isbnValue.substring(0, 50);
                        }
                        metadata.setIsbn(isbnValue);
                    }
                }
            }

            return metadata;

        } catch (Exception e) {
            log.error("Error parsing FB2 file", e);
            return null;
        }
    }

    public Fb2Metadata parseFromZip(ZipFile zipFile, ZipEntry entry) {
        try (InputStream is = zipFile.getInputStream(entry)) {
            return parse(is);
        } catch (Exception e) {
            log.error("Error parsing FB2 from ZIP: {}", entry.getName(), e);
            return null;
        }
    }

    private String getElementText(Element parent, String elementName) {
        Element element = parent.element(elementName);
        return element != null ? element.getTextTrim() : null;
    }
}