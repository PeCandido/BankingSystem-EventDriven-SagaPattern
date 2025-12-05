import React from 'react';
import { useMerchant } from '../hooks/useMerchant';
import { usePayment } from '../hooks/usePayment';
import EventBrokerMonitor from '../components/EventBrokerMonitor';

export default function KafkaMonitorPage() {
  const merchant = useMerchant();
  const payment = usePayment();

  return (
    <div className="container" style={{ paddingTop: '24px', paddingBottom: '24px' }}>
      <div style={{ marginBottom: '2rem' }}>
        <h1 className="gradient-text" style={{ fontSize: '2rem', fontWeight: 700, marginBottom: '0.5rem' }}>
          🚀 Kafka Monitor
        </h1>
        <p style={{ color: 'var(--text-secondary)' }}>
          Monitoramento em tempo real do Event Broker
        </p>
      </div>
      <EventBrokerMonitor paymentEvents={payment.events} merchantEvents={merchant.events} />
    </div>
  );
}
