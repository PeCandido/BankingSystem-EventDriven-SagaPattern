import React from 'react';
import { useMerchant } from '../hooks/useMerchant';
import { usePayment } from '../hooks/usePayment';
import EventTimeline from '../components/EventTimeline';

export default function EventTimelinePage() {
  const merchant = useMerchant();
  const payment = usePayment();

  return (
    <div className="container" style={{ paddingTop: '24px', paddingBottom: '24px' }}>
      <div style={{ marginBottom: '2rem' }}>
        <h1 className="gradient-text" style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '0.5rem' }}>
          📜 Event Sourcing
        </h1>
        <p style={{ color: 'var(--text-secondary)' }}>
          Histórico completo e imutável de todos os eventos
        </p>
      </div>
      <EventTimeline paymentEvents={payment.events} merchantEvents={merchant.events} />
    </div>
  );
}
