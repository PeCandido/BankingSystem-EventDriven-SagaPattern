export interface PaymentEvent {
  eventId: string;
  eventType: 'PAYMENT_CREATED' | 'PAYMENT_PROCESSED';
  paymentId: string;
  payerId: string;
  payeeId: string;
  amount: number;
  currency: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  payerEmail: string;
  description: string;
  eventDateTime: string;
}

export interface MerchantEvent {
  id: string;
  eventType: 'MERCHANT_REGISTERED' | 'PAYMENT_RECEIVED' | 'PAYMENT_DEBITED';
  merchantId: string;
  balanceChange: number;
  newBalance: number;
  description: string;
  eventDateTime: string;
}

export interface Payment {
  id: string;
  payerId: string;
  payeeId: string;
  amount: number;
  currency: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
  updatedAt: string;
}

export interface Merchant {
  id: string;
  name: string;
  email: string;
  phone: string;
  balance: number;
  currency: string;
  createdAt: string;
}
