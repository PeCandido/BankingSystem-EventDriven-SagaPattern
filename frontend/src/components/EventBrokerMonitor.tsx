import React from 'react';
import { PaymentEvent, MerchantEvent } from '../types';

interface Props {
  paymentEvents: PaymentEvent[];
  merchantEvents: MerchantEvent[];
}

export default function EventBrokerMonitor({ paymentEvents, merchantEvents }: Props) {
  const kafkaEvents = [
    {
      topic: 'payment-created',
      count: paymentEvents.filter(e => e.eventType === 'PAYMENT_CREATED').length,
      icon: '📤',
    },
    {
      topic: 'payment-processed',
      count: paymentEvents.filter(e => e.eventType === 'PAYMENT_PROCESSED').length,
      icon: '📥',
    },
    {
      topic: 'merchant-events',
      count: merchantEvents.length,
      icon: '🏪',
    },
  ];

  return (
    <div className="card-compact">
      <h2 className="gradient-text" style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '1.5rem' }}>
        🚀 Kafka Event Broker - Monitoramento
      </h2>

      <div className="grid-3">
        {kafkaEvents.map(event => (
          <div key={event.topic} className="kafka-card">
            <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>{event.icon}</div>
            <h3 className="gradient-text" style={{ fontWeight: 700, marginBottom: '0.5rem' }}>
              {event.topic}
            </h3>
            <div className="kafka-value">{event.count}</div>
            <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.5rem' }}>
              mensagens processadas
            </p>
          </div>
        ))}
      </div>

      <div className="card-compact" style={{ marginTop: '1.5rem' }}>
        <h3 style={{ fontWeight: 700, marginBottom: '0.75rem' }}>📊 Fluxo SAGA Pattern:</h3>
        <div className="saga-flow">
          <div className="saga-step">
            <div className="saga-step-number">1️⃣</div>
            <span className="saga-step-label">Payment Created</span>
          </div>
          <div className="saga-arrow">→</div>
          <div className="saga-step">
            <div className="saga-step-number">2️⃣</div>
            <span className="saga-step-label">Kafka Publish</span>
          </div>
          <div className="saga-arrow">→</div>
          <div className="saga-step">
            <div className="saga-step-number">3️⃣</div>
            <span className="saga-step-label">Merchant Process</span>
          </div>
          <div className="saga-arrow">→</div>
          <div className="saga-step">
            <div className="saga-step-number">4️⃣</div>
            <span className="saga-step-label">Payment Updated</span>
          </div>
        </div>
      </div>
    </div>
  );
}
