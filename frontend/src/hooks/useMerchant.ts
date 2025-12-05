import { useState, useCallback, useEffect } from 'react';
import { merchantAPI } from '../api';
import { Merchant, MerchantEvent } from '../types';

export interface MerchantState {
  merchant: Merchant | null;
  balance: number;
  events: MerchantEvent[];
  loading: boolean;
  error: string | null;
  merchants: Merchant[];
}

const STORAGE_KEY = 'merchants_cache';

export function useMerchant() {
  const [state, setState] = useState<MerchantState>(() => {
    const cached = localStorage.getItem(STORAGE_KEY);
    return cached ? JSON.parse(cached) : {
      merchant: null,
      balance: 0,
      events: [],
      loading: false,
      error: null,
      merchants: [],
    };
  });

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  }, [state]);

  useEffect(() => {
    loadMerchants();
  }, []);

  const loadMerchants = useCallback(async () => {
    try {
      const response = await merchantAPI.getMerchants();
      setState(prev => ({ ...prev, merchants: response.data }));
    } catch (error) {
      console.error('❌ Erro ao carregar merchants:', error);
    }
  }, []);

  const createMerchant = useCallback(async (data: {
    name: string;
    email: string;
    phone: string;
    initialBalance: number;
    currency: string;
  }) => {
    setState(prev => ({ ...prev, loading: true, error: null }));
    try {
      const response = await merchantAPI.createMerchant(data);
      const newMerchant = response.data;
      setState(prev => ({
        ...prev,
        merchant: newMerchant,
        balance: newMerchant.balance,
        merchants: [...prev.merchants, newMerchant],
      }));
      return newMerchant.id;
    } catch (e: any) {
      const errorMsg = e.response?.data?.message || 'Erro ao criar merchant';
      setState(prev => ({ ...prev, error: errorMsg }));
      throw errorMsg;
    } finally {
      setState(prev => ({ ...prev, loading: false }));
    }
  }, []);

  const fetchMerchant = useCallback(async (merchantId: string) => {
    setState(prev => ({ ...prev, loading: true, error: null }));
    try {
      const [merchantRes, balanceRes, eventsRes] = await Promise.all([
        merchantAPI.getMerchant(merchantId),
        merchantAPI.getMerchantBalance(merchantId),
        merchantAPI.getMerchantEvents(merchantId),
      ]);

      console.log('💾 Merchant atualizado:', merchantRes.data);
      console.log('💰 Novo saldo:', balanceRes.data.balance);
      console.log('📜 Eventos merchant:', eventsRes.data);

      setState(prev => ({
        ...prev,
        merchant: merchantRes.data,
        balance: balanceRes.data.balance,
        events: eventsRes.data,
        loading: false,
      }));
    } catch (e: any) {
      const errorMsg = e.response?.data?.message || 'Erro ao buscar merchant';
      setState(prev => ({ ...prev, error: errorMsg, loading: false }));
    }
  }, []);
  

  return {
    ...state,
    createMerchant,
    fetchMerchant,
    loadMerchants,
  };
}
