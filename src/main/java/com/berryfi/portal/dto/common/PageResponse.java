package com.berryfi.portal.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Custom pagination response DTO to provide stable JSON structure.
 * This replaces direct serialization of Spring's PageImpl which can have unstable JSON structure.
 * 
 * @param <T> The type of content in the page
 */
public class PageResponse<T> {
    
    private List<T> content;
    private PageMetadata page;
    
    public PageResponse() {}
    
    public PageResponse(List<T> content, PageMetadata page) {
        this.content = content;
        this.page = page;
    }
    
    /**
     * Create PageResponse from Spring Data Page object
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        PageMetadata metadata = new PageMetadata(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.isEmpty(),
            page.hasNext(),
            page.hasPrevious()
        );
        
        return new PageResponse<>(page.getContent(), metadata);
    }
    
    // Getters and setters
    public List<T> getContent() {
        return content;
    }
    
    public void setContent(List<T> content) {
        this.content = content;
    }
    
    public PageMetadata getPage() {
        return page;
    }
    
    public void setPage(PageMetadata page) {
        this.page = page;
    }
    
    /**
     * Page metadata containing pagination information
     */
    public static class PageMetadata {
        private int number;           // Current page number (0-based)
        private int size;             // Size of the page
        private long totalElements;   // Total number of elements
        private int totalPages;       // Total number of pages
        private boolean first;        // Whether this is the first page
        private boolean last;         // Whether this is the last page
        private boolean empty;        // Whether the page is empty
        private boolean hasNext;      // Whether there is a next page
        private boolean hasPrevious;  // Whether there is a previous page
        
        public PageMetadata() {}
        
        public PageMetadata(int number, int size, long totalElements, int totalPages, 
                          boolean first, boolean last, boolean empty, boolean hasNext, boolean hasPrevious) {
            this.number = number;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.first = first;
            this.last = last;
            this.empty = empty;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }
        
        // Getters and setters
        public int getNumber() {
            return number;
        }
        
        public void setNumber(int number) {
            this.number = number;
        }
        
        public int getSize() {
            return size;
        }
        
        public void setSize(int size) {
            this.size = size;
        }
        
        public long getTotalElements() {
            return totalElements;
        }
        
        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }
        
        public int getTotalPages() {
            return totalPages;
        }
        
        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
        
        public boolean isFirst() {
            return first;
        }
        
        public void setFirst(boolean first) {
            this.first = first;
        }
        
        public boolean isLast() {
            return last;
        }
        
        public void setLast(boolean last) {
            this.last = last;
        }
        
        public boolean isEmpty() {
            return empty;
        }
        
        public void setEmpty(boolean empty) {
            this.empty = empty;
        }
        
        public boolean isHasNext() {
            return hasNext;
        }
        
        public void setHasNext(boolean hasNext) {
            this.hasNext = hasNext;
        }
        
        public boolean isHasPrevious() {
            return hasPrevious;
        }
        
        public void setHasPrevious(boolean hasPrevious) {
            this.hasPrevious = hasPrevious;
        }
    }
}