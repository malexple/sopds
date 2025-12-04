package com.sopds.service.reader;

import com.sopds.dto.BookContentDto;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class Fb2BookReader implements BookReader {

    private static final int SECTIONS_PER_PAGE = 1; // секций на страницу

    @Override
    public String getFormat() {
        return "fb2";
    }

    @Override
    public boolean supports(String format) {
        return "fb2".equalsIgnoreCase(format);
    }

    @Override
    public BookContentDto read(InputStream inputStream, Long bookId, String title, int page) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);

        // Получаем все секции (главы)
        NodeList sections = doc.getElementsByTagName("section");
        List<String> chapters = new ArrayList<>();
        List<String> sectionContents = new ArrayList<>();

        for (int i = 0; i < sections.getLength(); i++) {
            Element section = (Element) sections.item(i);

            // Получаем заголовок секции
            NodeList titles = section.getElementsByTagName("title");
            String chapterTitle = "Глава " + (i + 1);
            if (titles.getLength() > 0) {
                chapterTitle = titles.item(0).getTextContent().trim();
            }
            chapters.add(chapterTitle);

            // Получаем контент секции
            String content = parseSectionContent(section);
            sectionContents.add(content);
        }

        // Если секций нет, пробуем получить body
        if (sectionContents.isEmpty()) {
            NodeList bodies = doc.getElementsByTagName("body");
            if (bodies.getLength() > 0) {
                String content = parseSectionContent((Element) bodies.item(0));
                sectionContents.add(content);
                chapters.add("Содержимое");
            }
        }

        int totalPages = sectionContents.size();
        page = Math.max(1, Math.min(page, totalPages));

        String htmlContent = sectionContents.isEmpty() ? "<p>Содержимое не найдено</p>" : sectionContents.get(page - 1);

        return BookContentDto.builder()
                .bookId(bookId)
                .title(title)
                .format("fb2")
                .content(htmlContent)
                .currentPage(page)
                .totalPages(totalPages)
                .chapters(chapters)
                .encoding("UTF-8")
                .supportsReading(true)
                .build();
    }

    private String parseSectionContent(Element element) {
        StringBuilder html = new StringBuilder();
        NodeList children = element.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) child;
                String tagName = el.getTagName().toLowerCase();

                switch (tagName) {
                    case "title" -> html.append("<h2>").append(el.getTextContent().trim()).append("</h2>");
                    case "subtitle" -> html.append("<h3>").append(el.getTextContent().trim()).append("</h3>");
                    case "p" -> html.append("<p>").append(parseInlineContent(el)).append("</p>");
                    case "empty-line" -> html.append("<br/>");
                    case "epigraph" -> html.append("<blockquote class='epigraph'>").append(parseSectionContent(el)).append("</blockquote>");
                    case "cite" -> html.append("<blockquote>").append(parseSectionContent(el)).append("</blockquote>");
                    case "poem" -> html.append("<div class='poem'>").append(parseSectionContent(el)).append("</div>");
                    case "stanza" -> html.append("<div class='stanza'>").append(parseSectionContent(el)).append("</div>");
                    case "v" -> html.append("<p class='verse'>").append(el.getTextContent().trim()).append("</p>");
                    case "text-author" -> html.append("<p class='text-author'>").append(el.getTextContent().trim()).append("</p>");
                    case "section" -> {} // Пропускаем вложенные секции
                    default -> html.append(parseSectionContent(el));
                }
            }
        }

        return html.toString();
    }

    private String parseInlineContent(Element element) {
        StringBuilder result = new StringBuilder();
        NodeList children = element.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                result.append(child.getTextContent());
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) child;
                String tagName = el.getTagName().toLowerCase();

                switch (tagName) {
                    case "strong" -> result.append("<strong>").append(el.getTextContent()).append("</strong>");
                    case "emphasis" -> result.append("<em>").append(el.getTextContent()).append("</em>");
                    case "a" -> {
                        String href = el.getAttribute("l:href");
                        if (href.startsWith("#")) {
                            result.append("<a href='").append(href).append("'>").append(el.getTextContent()).append("</a>");
                        } else {
                            result.append(el.getTextContent());
                        }
                    }
                    default -> result.append(el.getTextContent());
                }
            }
        }

        return result.toString();
    }
}
