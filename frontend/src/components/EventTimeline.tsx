import React from 'react';
import { PaymentEvent, MerchantEvent } from '../types';

interface Props {
  paymentEvents: PaymentEvent[];
  merchantEvents: MerchantEvent[];
}

type UnifiedEvent = (PaymentEvent & { source: string }) | (MerchantEvent & { source: string });

export default function EventTimeline({ paymentEvents, merchantEvents }: Props) {
  const allEvents: UnifiedEvent[] = [
    ...paymentEvents.map(e => ({ ...e, source: 'Payment' })),
    ...merchantEvents.map(e => ({ ...e, source: 'Merchant' })),
  ].sort((a, b) => new Date(b.eventDateTime).getTime() - new Date(a.eventDateTime).getTime());

  return (
    <div className="card-compact">
      <h2 className="gradient-text" style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '1.5rem' }}>
        📜 Event Sourcing - Histórico Imutável
      </h2>

      {allEvents.length === 0 ? (
        <p style={{ color: 'var(--text-muted)' }}>Nenhum evento ainda. Crie um pagamento para começar!</p>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {allEvents.map((event, idx) => (
            <div
              key={idx}
              className={`event-item ${
                event.source === 'Payment' ? 'payment' : 'merchant'
              }`}
            >
              <div className="event-icon">
                {event.eventType?.includes('CREATED') ? '✨' : '⚡'}
              </div>
              <div style={{ flex: 1 }}>
                <h3 style={{ fontWeight: 700, color: 'var(--secondary)' }}>{event.eventType}</h3>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                  {new Date(event.eventDateTime).toLocaleString('pt-BR')}
                </p>
                {'amount' in event && event.amount && (
                  <p style={{ fontSize: '0.9rem' }}>Valor: R$ {event.amount}</p>
                )}
                {'balanceAfter' in event && event.balanceAfter !== null && (
                  <p style={{ fontSize: '0.9rem' }}>Saldo: R$ {Number(event.balanceAfter).toFixed(2)}</p>
                )}
                {'description' in event && event.description && (
                  <p style={{ fontSize: '0.9rem' }}>{event.description}</p>
                )}
                  </div>
                  <span
                    style={{
                      fontSize: '0.7rem',
                      background: 'rgba(76, 29, 149, 0.8)',
                      padding: '0.25rem 0.5rem',
                      borderRadius: '9999px',
                      alignSelf: 'flex-start',
                    }}
                  >
                    {event.source}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
