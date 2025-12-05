import React, { useState } from 'react';
import { PaymentEvent, MerchantEvent } from '../types';

interface Props {
  paymentEvents: PaymentEvent[];
  merchantEvents: MerchantEvent[];
}

export default function EventBrokerMonitor({ paymentEvents, merchantEvents }: Props) {
  const [activeTab, setActiveTab] = useState<'saga' | 'payment' | 'merchant'>('saga');

  const paymentCreatedEvents = paymentEvents.filter(e => e.eventType === 'PAYMENT_CREATED');
  const paymentProcessedEvents = paymentEvents.filter(e => e.eventType === 'PAYMENT_PROCESSED');
  
  const approvedCount = paymentProcessedEvents.filter(e => e.status === 'APPROVED').length;
  const rejectedCount = paymentProcessedEvents.filter(e => e.status === 'REJECTED').length;

  const kafkaTopics = [
    {
      topic: 'payment-created',
      count: paymentCreatedEvents.length,
      icon: '📤',
      description: 'Pagamentos iniciados',
      color: '#3b82f6'
    },
    {
      topic: 'payment-processed',
      count: paymentProcessedEvents.length,
      icon: '📥',
      description: 'Pagamentos processados',
      color: '#10b981'
    },
    {
      topic: 'merchant-events',
      count: merchantEvents.length,
      icon: '🏪',
      description: 'Eventos de merchant',
      color: '#f59e0b'
    },
  ];

  const formatDate = (date: string) => {
    return new Date(date).toLocaleString('pt-BR');
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    }).format(value);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      {/* Header */}
      <div style={{ backgroundColor: 'rgba(59, 130, 246, 0.08)', padding: '1.5rem', borderRadius: '0.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.5rem', color: '#3b82f6' }}>
          🚀 Kafka Event Broker - Monitoramento
        </h2>
        <p style={{ color: '#666', margin: 0 }}>
          Acompanhe o fluxo de eventos em tempo real através do Kafka
        </p>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '0.5rem', borderBottom: '2px solid #e5e7eb' }}>
        <button
          onClick={() => setActiveTab('saga')}
          style={{
            padding: '0.75rem 1rem',
            backgroundColor: activeTab === 'saga' ? '#3b82f6' : 'transparent',
            color: activeTab === 'saga' ? 'white' : '#666',
            border: 'none',
            borderRadius: '0.25rem 0.25rem 0 0',
            cursor: 'pointer',
            fontWeight: 500,
            fontSize: '0.875rem'
          }}
        >
          📊 Fluxo SAGA
        </button>
        <button
          onClick={() => setActiveTab('payment')}
          style={{
            padding: '0.75rem 1rem',
            backgroundColor: activeTab === 'payment' ? '#3b82f6' : 'transparent',
            color: activeTab === 'payment' ? 'white' : '#666',
            border: 'none',
            cursor: 'pointer',
            fontWeight: 500,
            fontSize: '0.875rem'
          }}
        >
          💳 Payment Events ({paymentEvents.length})
        </button>
        <button
          onClick={() => setActiveTab('merchant')}
          style={{
            padding: '0.75rem 1rem',
            backgroundColor: activeTab === 'merchant' ? '#3b82f6' : 'transparent',
            color: activeTab === 'merchant' ? 'white' : '#666',
            border: 'none',
            cursor: 'pointer',
            fontWeight: 500,
            fontSize: '0.875rem'
          }}
        >
          🏪 Merchant Events ({merchantEvents.length})
        </button>
      </div>

      {/* Saga Flow Tab */}
      {activeTab === 'saga' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {/* Saga Steps */}
          <div className="card-compact">
            <h3 style={{ fontWeight: 700, marginBottom: '1rem', color: '#1f2937' }}>
              📋 Etapas da Saga Pattern
            </h3>
            <div style={{
              display: 'flex',
              justifyContent: 'space-around',
              alignItems: 'center',
              gap: '1rem',
              overflowX: 'auto',
              padding: '1rem',
              backgroundColor: '#f9fafb',
              borderRadius: '0.5rem'
            }}>
              <div style={{ textAlign: 'center', minWidth: '100px' }}>
                <div style={{
                  width: '50px',
                  height: '50px',
                  borderRadius: '50%',
                  backgroundColor: '#3b82f6',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'white',
                  fontWeight: 700,
                  margin: '0 auto 0.5rem'
                }}>1️⃣</div>
                <p style={{ margin: '0.25rem 0', fontWeight: 600, fontSize: '0.875rem' }}>Payment Created</p>
                <p style={{ margin: '0.25rem 0', fontSize: '0.75rem', color: '#666' }}>{paymentCreatedEvents.length} eventos</p>
              </div>

              <div style={{ fontSize: '1.5rem', color: '#10b981' }}>→</div>

              <div style={{ textAlign: 'center', minWidth: '100px' }}>
                <div style={{
                  width: '50px',
                  height: '50px',
                  borderRadius: '50%',
                  backgroundColor: '#10b981',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'white',
                  fontWeight: 700,
                  margin: '0 auto 0.5rem'
                }}>2️⃣</div>
                <p style={{ margin: '0.25rem 0', fontWeight: 600, fontSize: '0.875rem' }}>Saga Orchestrator</p>
                <p style={{ margin: '0.25rem 0', fontSize: '0.75rem', color: '#666' }}>Processing...</p>
              </div>

              <div style={{ fontSize: '1.5rem', color: '#10b981' }}>→</div>

              <div style={{ textAlign: 'center', minWidth: '100px' }}>
                <div style={{
                  width: '50px',
                  height: '50px',
                  borderRadius: '50%',
                  backgroundColor: '#f59e0b',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'white',
                  fontWeight: 700,
                  margin: '0 auto 0.5rem'
                }}>3️⃣</div>
                <p style={{ margin: '0.25rem 0', fontWeight: 600, fontSize: '0.875rem' }}>Payment Processed</p>
                <p style={{ margin: '0.25rem 0', fontSize: '0.75rem', color: '#666' }}>{paymentProcessedEvents.length} eventos</p>
              </div>

              <div style={{ fontSize: '1.5rem', color: '#10b981' }}>→</div>

              <div style={{ textAlign: 'center', minWidth: '100px' }}>
                <div style={{
                  width: '50px',
                  height: '50px',
                  borderRadius: '50%',
                  backgroundColor: '#8b5cf6',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'white',
                  fontWeight: 700,
                  margin: '0 auto 0.5rem'
                }}>4️⃣</div>
                <p style={{ margin: '0.25rem 0', fontWeight: 600, fontSize: '0.875rem' }}>Merchant Updated</p>
                <p style={{ margin: '0.25rem 0', fontSize: '0.75rem', color: '#666' }}>{merchantEvents.length} eventos</p>
              </div>
            </div>
          </div>

          {/* Kafka Topics */}
          <div className="card-compact">
            <h3 style={{ fontWeight: 700, marginBottom: '1rem', color: '#1f2937' }}>
              📡 Tópicos Kafka
            </h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1rem' }}>
              {kafkaTopics.map((topic) => (
                <div
                  key={topic.topic}
                  style={{
                    padding: '1rem',
                    border: `2px solid ${topic.color}`,
                    borderRadius: '0.5rem',
                    backgroundColor: `${topic.color}08`
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
                    <span style={{ fontSize: '1.5rem' }}>{topic.icon}</span>
                    <span style={{ fontWeight: 700, color: topic.color }}>{topic.topic}</span>
                  </div>
                  <p style={{ margin: '0.25rem 0', fontSize: '0.875rem', color: '#666' }}>
                    {topic.description}
                  </p>
                  <div style={{
                    marginTop: '0.75rem',
                    padding: '0.5rem',
                    backgroundColor: 'white',
                    borderRadius: '0.25rem',
                    textAlign: 'center',
                    fontWeight: 700,
                    color: topic.color
                  }}>
                    {topic.count} mensagens
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Status Summary */}
          <div className="card-compact">
            <h3 style={{ fontWeight: 700, marginBottom: '1rem', color: '#1f2937' }}>
              📈 Resumo de Status
            </h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
              <div style={{ padding: '1rem', backgroundColor: '#10b98108', borderLeft: '4px solid #10b981', borderRadius: '0.25rem' }}>
                <p style={{ margin: '0 0 0.5rem 0', fontSize: '0.875rem', color: '#666' }}>✅ Aprovados</p>
                <p style={{ margin: 0, fontSize: '2rem', fontWeight: 700, color: '#10b981' }}>{approvedCount}</p>
              </div>
              <div style={{ padding: '1rem', backgroundColor: '#ef444408', borderLeft: '4px solid #ef4444', borderRadius: '0.25rem' }}>
                <p style={{ margin: '0 0 0.5rem 0', fontSize: '0.875rem', color: '#666' }}>❌ Rejeitados</p>
                <p style={{ margin: 0, fontSize: '2rem', fontWeight: 700, color: '#ef4444' }}>{rejectedCount}</p>
              </div>
              <div style={{ padding: '1rem', backgroundColor: '#f59e0b08', borderLeft: '4px solid #f59e0b', borderRadius: '0.25rem' }}>
                <p style={{ margin: '0 0 0.5rem 0', fontSize: '0.875rem', color: '#666' }}>⏳ Taxa de Sucesso</p>
                <p style={{ margin: 0, fontSize: '2rem', fontWeight: 700, color: '#f59e0b' }}>
                  {paymentProcessedEvents.length > 0 ? Math.round((approvedCount / paymentProcessedEvents.length) * 100) : 0}%
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Payment Events Tab */}
      {activeTab === 'payment' && (
        <div className="card-compact">
          <h3 style={{ fontWeight: 700, marginBottom: '1rem', color: '#1f2937' }}>
            💳 Eventos de Pagamento
          </h3>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', fontSize: '0.875rem', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid #e5e7eb' }}>
                  <th style={{ padding: '0.75rem', textAlign: 'left', fontWeight: 700, color: '#374151' }}>ID</th>
                  <th style={{ padding: '0.75rem', textAlign: 'left', fontWeight: 700, color: '#374151' }}>Tipo</th>
                  <th style={{ padding: '0.75rem', textAlign: 'left', fontWeight: 700, color: '#374151' }}>Status</th>
                  <th style={{ padding: '0.75rem', textAlign: 'left', fontWeight: 700, color: '#374151' }}>Valor</th>
                  <th style={{ padding: '0.75rem', textAlign: 'left', fontWeight: 700, color: '#374151' }}>Data/Hora</th>
                </tr>
              </thead>
              <tbody>
                {paymentEvents.slice(-10).reverse().map((event, idx) => (
                  <tr key={idx} style={{ borderBottom: '1px solid #f3f4f6', backgroundColor: idx % 2 === 0 ? '#f9fafb' : 'white' }}>
                    <td style={{ padding: '0.75rem', color: '#3b82f6', fontWeight: 600 }}>
                      {event.paymentId.substring(0, 8)}...
                    </td>
                    <td style={{ padding: '0.75rem' }}>
                      <span style={{
                        padding: '0.25rem 0.5rem',
                        borderRadius: '0.25rem',
                        backgroundColor: '#3b82f6',
                        color: 'white',
                        fontSize: '0.75rem',
                        fontWeight: 600
                      }}>
                        {event.eventType}
                      </span>
                    </td>
                    <td style={{ padding: '0.75rem' }}>
                      <span style={{
                        padding: '0.25rem 0.5rem',
                        borderRadius: '0.25rem',
                        backgroundColor: event.status === 'APPROVED' ? '#10b981' : event.status === 'REJECTED' ? '#ef4444' : '#f59e0b',
                        color: 'white',
                        fontSize: '0.75rem',
                        fontWeight: 600
                      }}>
                        {event.status === 'APPROVED' ? '✅ Aprovado' : event.status === 'REJECTED' ? '❌ Rejeitado' : '⏳ Pendente'}
                      </span>
                    </td>
                    <td style={{ padding: '0.75rem', fontWeight: 600, color: '#3b82f6' }}>
                      {formatCurrency(event.amount)} {event.currency}
                    </td>
                    <td style={{ padding: '0.75rem', color: '#666', fontSize: '0.75rem' }}>
                      {formatDate(event.eventDateTime)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {paymentEvents.length === 0 && (
              <div style={{ padding: '2rem', textAlign: 'center', color: '#999' }}>
                Nenhum evento de pagamento registrado
              </div>
            )}
          </div>
        </div>
      )}

      {/* Merchant Events Tab */}
      {activeTab === 'merchant' && (
        <div className="card-compact">
          <h3 style={{ fontWeight: 700, marginBottom: '1rem', color: '#1f2937' }}>
            🏪 Eventos de Merchant
          </h3>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', fontSize: '0.875rem', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid #e5e7eb' }}>
                  <th style={{ padding: '0.75rem', textAlign: 'left', fontWeight: 700, color: '#374151' }}>Tipo</th>
                  <th style={{ padding: '0.75rem', textAlign: 'left', fontWeight: 700, color: '#374151' }}>Merchant ID</th>
                  <th style={{ padding: '0.75rem', textAlign: 'left', fontWeight: 700, color: '#374151' }}>Alteração</th>
                  <th style={{ padding: '0.75rem', textAlign: 'left', fontWeight: 700, color: '#374151' }}>Novo Saldo</th>
                  <th style={{ padding: '0.75rem', textAlign: 'left', fontWeight: 700, color: '#374151' }}>Data/Hora</th>
                </tr>
              </thead>
              <tbody>
                {merchantEvents.slice(-10).reverse().map((event, idx) => (
                  <tr key={idx} style={{ borderBottom: '1px solid #f3f4f6', backgroundColor: idx % 2 === 0 ? '#f9fafb' : 'white' }}>
                    <td style={{ padding: '0.75rem' }}>
                      <span style={{
                        padding: '0.25rem 0.5rem',
                        borderRadius: '0.25rem',
                        backgroundColor: event.eventType === 'PAYMENT_RECEIVED' ? '#10b981' : '#ef4444',
                        color: 'white',
                        fontSize: '0.75rem',
                        fontWeight: 600
                      }}>
                        {event.eventType}
                      </span>
                    </td>
                    <td style={{ padding: '0.75rem', color: '#f59e0b', fontWeight: 600, fontSize: '0.75rem' }}>
                      {event.merchantId.substring(0, 8)}...
                    </td>
                    <td style={{
                      padding: '0.75rem',
                      fontWeight: 600,
                      color: event.balanceChange < 0 ? '#ef4444' : '#10b981'
                    }}>
                      {event.balanceChange < 0 ? '📉' : '📈'} {formatCurrency(Math.abs(event.balanceChange))}
                    </td>
                    <td style={{ padding: '0.75rem', fontWeight: 700, color: '#3b82f6' }}>
                      {formatCurrency(event.newBalance)}
                    </td>
                    <td style={{ padding: '0.75rem', color: '#666', fontSize: '0.75rem' }}>
                      {formatDate(event.eventDateTime)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {merchantEvents.length === 0 && (
              <div style={{ padding: '2rem', textAlign: 'center', color: '#999' }}>
                Nenhum evento de merchant registrado
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
