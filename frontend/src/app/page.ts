export interface Page<T> {
    content: T[];          // The array of products or items in the current page
    totalPages: number;    // Total number of pages available
    totalElements: number; // Total number of items
    size: number;          // Size of each page
    number: number;        // The current page number (0-based)
  }