import React from 'react';
import MerchantCard from './MerchantCard';
import PaymentFlow from './PaymentFlow';

export default function Dashboard() {
  return (
    <div className="container" style={{ paddingTop: '24px', paddingBottom: '24px' }}>
      <div className="grid-2">
        <MerchantCard />
        <PaymentFlow />
      </div>
    </div>
  );
}
