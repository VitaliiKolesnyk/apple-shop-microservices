export interface Cart {
  id?: string;
  userId?: string;
  cartItems?: CartItem[];
  totalPrice?: number;
}

import { Product } from "./product"; 

export interface CartItem {
  product: Product;
  quantity: number;
  totalPrice: number;
  thumbnailUrl: string
}