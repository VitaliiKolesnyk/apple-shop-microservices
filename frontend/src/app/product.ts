import { Category } from "./category";

export interface Product {
    id?: string;
    skuCode: string;
    name: string;
    description: string;
    price: number;
    thumbnailUrl: string;
    quantity: number;
    category: string;
    isExclusive: boolean
  }

  export interface ProductPage {
    content: Product[];
    pageable: {
      pageNumber: number;
      pageSize: number;
      sort: {
        empty: boolean;
        sorted: boolean;
        unsorted: boolean;
      };
      offset: number;
      paged: boolean;
      unpaged: boolean;
    };
    last: boolean;
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    first: boolean;
    numberOfElements: number;
    empty: boolean;
  }
