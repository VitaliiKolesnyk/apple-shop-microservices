export interface Order {
    id?: number;
    userId: string,
    orderNumber?: string;
    quantity: number;
    userDetails: UserDetails;
    contactDetails: ContactDetails,
    products: ProducDto[],
    orderedAt?: Date
    deliveredAt?: Date
  }

  export interface UserDetails {
    email: string;
    firstName: string;
    lastName: string;
  }

  export interface ProducDto {
    name: string,
    skuCode: string,
    price: number,
    quantity: number,
    totalAmount: number,
    thumbnailUrl: string
  }

  export interface ContactDetails {
    name: string,
    surname: string,
    email: string,
    phone: string,
    country: string,
    city: string,
    street:string
  }

  export interface StripeChargeDto {
    token: string;
    amount: number;
}

export interface OrderResponse {
  id: number;
  orderNumber: string;
  quantity: number;
  products: {
    name: string;
    skuCode: string;
    price: number;
    quantity: number;
    totalAmount: number;
    thumbnailUrl: string;
  }[];
  contactDetails: {
    name: string;
    surname: string;
    country: string;
    city: string;
    street: string;
  };
  totalAmount: number;
  status: string;
  orderedAt: string;
  deliveredAt: string | null;
}
