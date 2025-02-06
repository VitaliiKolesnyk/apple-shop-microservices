export interface ProductRequest {
    skuCode: string;
    name: string;
    description: string;
    price: number;
    quantity: number;
    category: string,
    thumbnail: File;
    isExclusive: boolean;
  }
