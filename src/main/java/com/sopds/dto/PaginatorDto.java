package com.sopds.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.IntStream;

@Data
@Builder
public class PaginatorDto {

    private int number;          // текущая страница (1-based)
    private int numPages;        // всего страниц
    private boolean hasPrevious;
    private boolean hasNext;
    private int previousPageNumber;
    private int nextPageNumber;
    private List<Integer> pageRange;

    public static PaginatorDto of(int currentPage, int totalItems, int itemsPerPage, int halfPagesLinks) {
        int numPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        if (numPages == 0) numPages = 1;

        int page = Math.max(1, Math.min(currentPage, numPages));

        int startPage = Math.max(1, page - halfPagesLinks);
        int endPage = Math.min(numPages, page + halfPagesLinks);

        List<Integer> pageRange = IntStream.rangeClosed(startPage, endPage)
                .boxed()
                .toList();

        return PaginatorDto.builder()
                .number(page)
                .numPages(numPages)
                .hasPrevious(page > 1)
                .hasNext(page < numPages)
                .previousPageNumber(page - 1)
                .nextPageNumber(page + 1)
                .pageRange(pageRange)
                .build();
    }

    public int getFirstItemIndex(int itemsPerPage) {
        return (number - 1) * itemsPerPage;
    }
}
