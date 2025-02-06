export interface OrderDto {
    id: number;
    orderNumber: string;
    totalAmount: number;
    quantity: number;
    status: string,
    newStatus: string,
    orderedAt: Date
  }