export interface Merchant {
  id: string;
  name: string;
  email: string;
  phone: string;
  balance: number;
  currency: string;
}

export interface Payment {
  id: string;
  payerId: string;
  payeeId: string;
  payerEmail: string;
  amount: number;
  currency: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
}

export interface PaymentEvent {
  id: string;
  paymentId: string;
  eventType: 'PAYMENT_CREATED' | 'PAYMENT_PROCESSED';
  status: string;
  eventDateTime: string;
  amount?: number;
  description?: string;
}

export interface MerchantEvent {
  id: string;
  merchantId: string;
  eventType: 'MERCHANT_CREATED' | 'PAYMENT_RECEIVED';
  eventDateTime: string;
  amount?: number;
  balanceAfter?: number;
}
