import React, { createContext, useContext, useEffect } from 'react';
import { useMerchant } from '../hooks/useMerchant';
import { usePayment } from '../hooks/usePayment';

interface AppContextType {
  merchant: ReturnType<typeof useMerchant>;
  payment: ReturnType<typeof usePayment>;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export function AppProvider({ children }: { children: React.ReactNode }) {
  const merchant = useMerchant();
  const payment = usePayment();

  useEffect(() => {
    merchant.loadMerchants();
  }, []);

  return (
    <AppContext.Provider value={{ merchant, payment }}>
      {children}
    </AppContext.Provider>
  );
}

export function useAppContext() {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useAppContext deve estar dentro de AppProvider');
  }
  return context;
}
