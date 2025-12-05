import React, { useState, useEffect } from 'react';
import { useAppContext } from '../context/AppContext';

export default function PaymentFlow() {
  const { merchant, payment } = useAppContext();
  const [amount, setAmount] = useState(0);
  const [selectedPayerId, setSelectedPayerId] = useState('');
  const [selectedPayeeId, setSelectedPayeeId] = useState('');

  const payerEmail = merchant.merchants.find(m => m.id === selectedPayerId)?.email || '';

  useEffect(() => {
    if (payment.payment?.status === 'APPROVED') {
      console.log('✅ Pagamento aprovado! Atualizando saldos...');

      const timer = setTimeout(async () => {
        await merchant.loadMerchants();
        console.log('💰 Saldos atualizados!');
      }, 1500);

      return () => clearTimeout(timer);
    }
  }, [payment.payment?.status]);

  const handlePayment = async () => {
    if (!selectedPayerId || !selectedPayeeId || amount <= 0) return;
    if (selectedPayerId === selectedPayeeId) {
      alert('❌ Payer e Payee não podem ser iguais!');
      return;
    }

    await payment.createPayment({
      payerId: selectedPayerId,
      payerEmail,
      payeeId: selectedPayeeId,
      amount,
      currency: 'BRL',
    });

    setTimeout(() => {
      merchant.fetchMerchant(selectedPayerId);
      merchant.fetchMerchant(selectedPayeeId);
    }, 3000);

    setAmount(0);
    setSelectedPayerId('');
    setSelectedPayeeId('');
  };

  return (
    <div className="card">
      <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '1.5rem' }}>
        💳 Criar Pagamento (Saga Pattern)
      </h2>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '1.5rem' }}>
        {/* PAYER */}
        <div>
          <label
            style={{
              display: 'block',
              fontSize: '0.8rem',
              fontWeight: 500,
              color: 'var(--text-secondary)',
              marginBottom: '0.5rem',
            }}
          >
            Merchant Pagador (Payer)
          </label>
          <select
            value={selectedPayerId}
            onChange={e => setSelectedPayerId(e.target.value)}
            className="input-field"
          >
            <option value="">-- Selecione um merchant para PAGAR --</option>
            {merchant.merchants.map(m => (
              <option key={m.id} value={m.id}>
                {m.name} ({m.email}) - Saldo:{' '}
                {m.balance.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
              </option>
            ))}
          </select>

          {selectedPayerId && (
            <p
              style={{
                fontSize: '0.75rem',
                color: 'var(--color-primary)',
                marginTop: '0.4rem',
                fontWeight: 500,
              }}
            >
              📧 {payerEmail}
            </p>
          )}
        </div>

        {/* PAYEE */}
        <div>
          <label
            style={{
              display: 'block',
              fontSize: '0.8rem',
              fontWeight: 500,
              color: 'var(--text-secondary)',
              marginBottom: '0.5rem',
            }}
          >
            Merchant Destinatário (Payee)
          </label>
          <select
            value={selectedPayeeId}
            onChange={e => setSelectedPayeeId(e.target.value)}
            className="input-field"
          >
            <option value="">-- Selecione um merchant para RECEBER --</option>
            {merchant.merchants.map(m => (
              <option key={m.id} value={m.id}>
                {m.name} ({m.email}) - Saldo:{' '}
                {m.balance.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
              </option>
            ))}
          </select>
        </div>

        {/* AMOUNT */}
        <div>
          <label
            style={{
              display: 'block',
              fontSize: '0.8rem',
              fontWeight: 500,
              color: 'var(--text-secondary)',
              marginBottom: '0.5rem',
            }}
          >
            Valor (R$)
          </label>
          <input
            type="number"
            placeholder="100.00"
            value={amount || ''}
            onChange={e => setAmount(Number(e.target.value))}
            className="input-field"
            min={0}
            step={0.01}
          />
        </div>

        {/* BUTTON */}
        <button
          onClick={handlePayment}
          disabled={payment.loading || !selectedPayerId || !selectedPayeeId || amount <= 0}
          className="btn btn-primary"
          style={{ width: '100%', paddingBlock: '1rem', fontSize: '1rem' }}
        >
          {payment.loading ? '⏳ Processando Saga...' : '🚀 Iniciar Pagamento'}
        </button>
      </div>

      {/* PAYMENT STATUS */}
      {payment.payment && (
        <div
          className="card-compact"
          style={{
            marginTop: '1.5rem',
            borderColor: 'rgba(34,197,94,0.3)',
            background:
              'linear-gradient(135deg, rgba(22,163,74,0.2), rgba(5,150,105,0.2))',
          }}
        >
          <h3 style={{ fontWeight: 700, fontSize: '1.1rem', marginBottom: '1rem' }}>
            📋 Status do Pagamento
          </h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.9rem' }}>
            <p>
              <span
                style={{
                  fontFamily: 'monospace',
                  background: 'rgba(15,23,42,0.7)',
                  padding: '0.15rem 0.4rem',
                  borderRadius: '0.25rem',
                }}
              >
                ID:
              </span>{' '}
              {payment.payment.id}
            </p>
            <p>
              <span
                style={{
                  fontFamily: 'monospace',
                  background: 'rgba(15,23,42,0.7)',
                  padding: '0.15rem 0.4rem',
                  borderRadius: '0.25rem',
                  marginRight: '0.5rem',
                }}
              >
                Status:
              </span>
              <span
                className={
                  payment.payment.status === 'APPROVED'
                    ? 'badge badge-approved'
                    : payment.payment.status === 'REJECTED'
                    ? 'badge badge-rejected'
                    : 'badge badge-processing'
                }
              >
                {payment.payment.status}
              </span>
            </p>
            <p>
              <span
                style={{
                  fontFamily: 'monospace',
                  background: 'rgba(15,23,42,0.7)',
                  padding: '0.15rem 0.4rem',
                  borderRadius: '0.25rem',
                }}
              >
                Valor:
              </span>{' '}
              R ${' '}
              {payment.payment.amount.toLocaleString('pt-BR', {
                minimumFractionDigits: 2,
              })}
            </p>
          </div>
        </div>
      )}

      {/* ERROR */}
      {payment.error && (
        <div className="message error-message" style={{ marginTop: '1.5rem' }}>
          ❌ {payment.error}
        </div>
      )}
    </div>
  );
}