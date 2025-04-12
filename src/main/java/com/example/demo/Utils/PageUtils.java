package com.example.demo.Utils;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public class PageUtils {
    public static <T> List<Integer> getPageNumbers(Page<T> page) {
        int totalPages = page.getTotalPages();
        int currentPage = page.getNumber();
        List<Integer> pageNumbers = new ArrayList<>();

        for (int i = 0; i < totalPages; i++) {
            pageNumbers.add(i);
        }

        return pageNumbers;
    }

    public static boolean hasPrevious(Page<?> page) {
        return page.hasPrevious();
    }

    public static boolean hasNext(Page<?> page) {
        return page.hasNext();
    }

    public static int getPreviousPage(Page<?> page) {
        return page.hasPrevious() ? page.getNumber() - 1 : 0;
    }

    public static int getNextPage(Page<?> page) {
        return page.hasNext() ? page.getNumber() + 1 : page.getNumber();
    }
}
