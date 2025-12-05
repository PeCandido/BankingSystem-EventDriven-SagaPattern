import { useState, useCallback, useEffect, useRef } from 'react';
import { paymentAPI } from '../api';
import { Payment, PaymentEvent } from '../types';

export interface PaymentState {
  payment: Payment | null;
  events: PaymentEvent[];
  payments: Payment[];
  loading: boolean;
  error: string | null;
}

const STORAGE_KEY = 'payments_cache';

export function usePayment() {
  const [state, setState] = useState<PaymentState>(() => {
    const cached = localStorage.getItem(STORAGE_KEY);
    return cached ? JSON.parse(cached) : {
      payment: null,
      events: [],
      payments: [],
      loading: false,
      error: null,
    };
  });

  const pollingIntervalRef = useRef<NodeJS.Timeout | null>(null);
  const paymentIdRef = useRef<string | null>(null);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  }, [state]);

  useEffect(() => {
    if (!state.payment?.id || state.payment.status !== 'PENDING') {
      if (pollingIntervalRef.current) {
        clearInterval(pollingIntervalRef.current);
        pollingIntervalRef.current = null;
      }
      return;
    }

    if (paymentIdRef.current === state.payment.id && pollingIntervalRef.current) {
      return;
    }

    paymentIdRef.current = state.payment.id;
    let attemptCount = 0;
    const maxAttempts = 60; 

    pollingIntervalRef.current = setInterval(async () => {
      try {
        attemptCount++;
        console.log(`⏳ Polling Saga (tentativa ${attemptCount}/${maxAttempts})...`);
        
        const [paymentRes, eventsRes] = await Promise.all([
          paymentAPI.getPayment(state.payment!.id!),
          paymentAPI.getPaymentEvents(state.payment!.id!),
        ]);

        console.log('💾 Status do payment:', paymentRes.data.status);
        console.log('📜 Eventos:', eventsRes.data);

        setState(prev => ({
          ...prev,
          payment: paymentRes.data,
          events: eventsRes.data,
        }));

        if (paymentRes.data.status !== 'PENDING') {
          console.log('✅ Saga completado! Status:', paymentRes.data.status);
          if (pollingIntervalRef.current) {
            clearInterval(pollingIntervalRef.current);
            pollingIntervalRef.current = null;
          }
        }

        if (attemptCount >= maxAttempts) {
          console.warn('⚠️ Máximo de tentativas atingido');
          if (pollingIntervalRef.current) {
            clearInterval(pollingIntervalRef.current);
            pollingIntervalRef.current = null;
          }
        }
      } catch (error) {
        console.error('❌ Erro no polling:', error);
      }
    }, 1500);

    return () => {
      if (pollingIntervalRef.current) {
        clearInterval(pollingIntervalRef.current);
        pollingIntervalRef.current = null;
      }
    };
  }, [state.payment?.id, state.payment?.status]);

  const createPayment = useCallback(async (data: {
    payerId: string;
    payerEmail: string;
    payeeId: string;
    amount: number;
    currency: string;
  }) => {
    setState(prev => ({ ...prev, loading: true, error: null }));
    
    try {
      console.log('🚀 POST: Criando pagamento (Saga Pattern)');
      const response = await paymentAPI.createPayment(data);
      const paymentId = response.data.id || response.data.paymentId;

      console.log('💾 Payment ID criado:', paymentId);

      const paymentRes = await paymentAPI.getPayment(paymentId);
      const eventsRes = await paymentAPI.getPaymentEvents(paymentId);

      console.log('💾 Payment objeto:', paymentRes.data);
      console.log('📜 Eventos iniciais:', eventsRes.data);

      setState(prev => ({
        ...prev,
        payment: paymentRes.data,
        events: eventsRes.data,
        payments: [...prev.payments, paymentRes.data],
      }));

      return paymentId;
    } catch (e: any) {
      const errorMsg = e.response?.data?.message || 'Erro ao criar pagamento';
      setState(prev => ({ ...prev, error: errorMsg }));
      console.error('❌ Erro:', errorMsg);
    } finally {
      setState(prev => ({ ...prev, loading: false }));
    }
  }, []);

  const loadPayments = useCallback(async () => {
    try {
      const response = await paymentAPI.getPayments();
      setState(prev => ({ ...prev, payments: response.data }));
    } catch (error) {
      console.error('❌ Erro ao carregar pagamentos:', error);
    }
  }, []);

  return {
    ...state,
    createPayment,
    loadPayments,
  };
}