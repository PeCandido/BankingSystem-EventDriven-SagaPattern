import axios from 'axios';
import { Merchant, Payment, PaymentEvent, MerchantEvent } from '../types/index';

const PAYMENT_SERVICE = 'http://localhost:8081/api';
const MERCHANT_SERVICE = 'http://localhost:8082/api';
const NOTIFICATION_SERVICE = 'http://localhost:8083/api';

const apiClient = axios.create({
  baseURL: '',
  timeout: 10000,
  withCredentials: false, 
});

apiClient.interceptors.request.use(config => {
  config.headers['Content-Type'] = 'application/json';
  config.headers['Accept'] = 'application/json';
  if (!config.headers.Authorization) {
    delete config.headers.Authorization;
  }
  return config;
});

apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      console.error('❌ Status:', error.response.status);
      console.error('❌ Data:', error.response.data);
      console.error('❌ Headers:', error.response.headers);
    } else if (error.request) {
      console.error('❌ Sem resposta do servidor:', error.request);
    } else {
      console.error('❌ Erro:', error.message);
    }
    return Promise.reject(error);
  }
);

export const paymentAPI = {
  createPayment: (data: {
    payerId: string;
    payerEmail: string;
    payeeId: string;
    amount: number;
    currency: string;
  }) => {
    console.log('🚀 POST', `${PAYMENT_SERVICE}/payments`, data);
    return apiClient.post(`${PAYMENT_SERVICE}/payments`, data);
  },

  getPayment: (id: string) => {
    console.log('📖 GET', `${PAYMENT_SERVICE}/payments/${id}`);
    return apiClient.get<Payment>(`${PAYMENT_SERVICE}/payments/${id}`);
  },

  getPaymentEvents: (id: string) => {
    console.log('📖 GET', `${PAYMENT_SERVICE}/payments/${id}/events`);
    return apiClient.get<PaymentEvent[]>(`${PAYMENT_SERVICE}/payments/${id}/events`);
  },

  getPayments: () => {
    console.log('📖 GET', `${PAYMENT_SERVICE}/payments`);
    return apiClient.get<Payment[]>(`${PAYMENT_SERVICE}/payments`);
  },
};

export const merchantAPI = {
  createMerchant: (data: {
    name: string;
    email: string;
    phone: string;
    initialBalance: number;
    currency: string;
  }) => {
    console.log('🏪 POST', `${MERCHANT_SERVICE}/merchants`, data);
    return apiClient.post(`${MERCHANT_SERVICE}/merchants`, data);
  },

  getMerchant: (id: string) => {
    console.log('📖 GET', `${MERCHANT_SERVICE}/merchants/${id}`);
    return apiClient.get<Merchant>(`${MERCHANT_SERVICE}/merchants/${id}`);
  },

  getMerchantBalance: (id: string) => {
    console.log('📖 GET', `${MERCHANT_SERVICE}/merchants/${id}/balance`);
    return apiClient.get<{ balance: number }>(
      `${MERCHANT_SERVICE}/merchants/${id}/balance`
    );
  },

  getMerchantEvents: (id: string) => {
    console.log('📖 GET', `${MERCHANT_SERVICE}/merchants/${id}/events`);
    return apiClient.get<MerchantEvent[]>(`${MERCHANT_SERVICE}/merchants/${id}/events`);
  },

  getMerchants: () => {
    console.log('📖 GET', `${MERCHANT_SERVICE}/merchants`);
    return apiClient.get<Merchant[]>(`${MERCHANT_SERVICE}/merchants`);
  },
};

export const notificationAPI = {
  getNotifications: () => {
    console.log('📖 GET', `${NOTIFICATION_SERVICE}/notifications`);
    return apiClient.get(`${NOTIFICATION_SERVICE}/notifications`);
  },

  sendNotification: (data: { message: string; type: string; userId: string }) => {
    console.log('📤 POST', `${NOTIFICATION_SERVICE}/notifications`, data);
    return apiClient.post(`${NOTIFICATION_SERVICE}/notifications`, data);
  },
};