import React, { useState, useEffect } from 'react';
import { useAppContext } from '../context/AppContext';

const maskPhone = (value: string) => {
  return value
    .replace(/\D/g, '')
    .replace(/(\d{2})(\d)/, '($1) $2')
    .replace(/(\d{5})(\d)/, '$1-$2')
    .replace(/(-\d{4})\d+?$/, '$1');
};

const formatCurrency = (value: string) => {
  const number = value.replace(/\D/g, '');
  return (Number(number) / 100).toLocaleString('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  });
};

const parseCurrency = (value: string) => {
  return Number(value.replace(/\D/g, '')) / 100;
};

const isValidEmail = (email: string) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

export default function MerchantCard() {
  const { merchant } = useAppContext();

  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phone: '',
    initialBalance: '',
  });

  const [selectedMerchantId, setSelectedMerchantId] = useState('');
  const [validationErrors, setValidationErrors] = useState<string[]>([]);

  useEffect(() => {
    if (merchant.merchants.length > 0 && !selectedMerchantId) {
      const firstId = merchant.merchants[0]?.id;
      if (firstId) {
        setSelectedMerchantId(firstId);
      }
    }
  }, [merchant.merchants]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;

    if (name === 'phone') {
      setFormData(prev => ({ ...prev, phone: maskPhone(value) }));
    } else if (name === 'initialBalance') {
      setFormData(prev => ({ ...prev, initialBalance: formatCurrency(value) }));
    } else {
      setFormData(prev => ({ ...prev, [name]: value }));
    }

    setValidationErrors([]);
  };

  const validateForm = (): boolean => {
    const errors: string[] = [];

    if (!formData.name.trim()) {
      errors.push('Nome é obrigatório');
    } else if (formData.name.trim().length < 3) {
      errors.push('Nome deve ter pelo menos 3 caracteres');
    }

    if (!formData.email.trim()) {
      errors.push('Email é obrigatório');
    } else if (!isValidEmail(formData.email)) {
      errors.push('Email inválido (ex: usuario@empresa.com.br)');
    }

    if (!formData.phone.trim()) {
      errors.push('Telefone é obrigatório');
    } else if (formData.phone.replace(/\D/g, '').length < 10) {
      errors.push('Telefone deve ter 10 ou 11 dígitos');
    }

    if (!formData.initialBalance) {
      errors.push('Saldo inicial é obrigatório');
    } else {
      const balance = parseCurrency(formData.initialBalance);
      if (balance < 0) {
        errors.push('Saldo não pode ser negativo');
      }
      if (balance === 0) {
        errors.push('Saldo deve ser maior que R$ 0,00');
      }
    }

    setValidationErrors(errors);
    return errors.length === 0;
  };

  const handleSubmit = async () => {
    if (!validateForm()) return;

    try {
      await merchant.createMerchant({
        name: formData.name,
        email: formData.email.toLowerCase(),
        phone: formData.phone,
        initialBalance: parseCurrency(formData.initialBalance),
        currency: 'BRL',
      });

      setShowForm(false);
      setFormData({ name: '', email: '', phone: '', initialBalance: '' });
      setValidationErrors([]);
      await merchant.loadMerchants();
    } catch (err) {
      console.error('Erro ao criar merchant:', err);
    }
  };

  const handleSelectMerchant = async (id: string) => {
    setSelectedMerchantId(id);
    if (id) {
      await merchant.fetchMerchant(id);
    }
  };

  return (
    <div className="card">
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '1.5rem',
        }}
      >
        <h2
          className="gradient-text"
          style={{ fontSize: '1.875rem', fontWeight: 700 }}
        >
          🏪 Merchants ({merchant.merchants.length})
        </h2>
        <button
          onClick={() => {
            setShowForm(!showForm);
            setValidationErrors([]);
          }}
          className="btn btn-secondary"
          style={{
            fontSize: '0.8rem',
            paddingInline: '0.9rem',
            paddingBlock: '0.5rem',
          }}
        >
          {showForm ? 'Cancelar' : '+ Novo'}
        </button>
      </div>

      {showForm ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          <input
            type="text"
            name="name"
            placeholder="Nome do Merchant"
            value={formData.name}
            onChange={handleChange}
            className="input-field"
          />
          <input
            type="email"
            name="email"
            placeholder="contato@empresa.com.br"
            value={formData.email}
            onChange={handleChange}
            className="input-field"
          />
          <input
            type="tel"
            name="phone"
            placeholder="(11) 99999-9999"
            value={formData.phone}
            onChange={handleChange}
            className="input-field"
            maxLength={15}
          />
          <input
            type="text"
            name="initialBalance"
            placeholder="Saldo Inicial (R$ 0,00)"
            value={formData.initialBalance}
            onChange={handleChange}
            className="input-field"
          />

          {validationErrors.length > 0 && (
            <div
              style={{
                background: 'rgba(239, 68, 68, 0.15)',
                border: '1px solid rgba(239, 68, 68, 0.5)',
                borderRadius: '0.5rem',
                padding: '0.75rem',
                marginTop: '0.25rem',
              }}
            >
              {validationErrors.map((error, idx) => (
                <p
                  key={idx}
                  style={{
                    color: 'var(--color-error)',
                    fontSize: '0.85rem',
                    margin: '4px 0',
                  }}
                >
                  ⚠️ {error}
                </p>
              ))}
            </div>
          )}

          <button
            onClick={handleSubmit}
            disabled={merchant.loading || validationErrors.length > 0}
            className="btn btn-primary"
            style={{ width: '100%', marginTop: '0.5rem' }}
          >
            {merchant.loading ? '⏳ Criando...' : '✅ Criar Merchant'}
          </button>
        </div>
      ) : merchant.merchants.length > 0 ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <label
              style={{
                fontSize: '0.8rem',
                fontWeight: 500,
                color: 'var(--text-secondary)',
              }}
            >
              Selecionar Merchant:
            </label>
            <select
              value={selectedMerchantId}
              onChange={e => handleSelectMerchant(e.target.value)}
              className="input-field"
            >
              <option value="">-- Selecione um merchant --</option>
              {merchant.merchants.map(m => (
                <option key={m.id} value={m.id}>
                  {m.name} - R${' '}
                  {m.balance.toLocaleString('pt-BR', {
                    minimumFractionDigits: 2,
                  })}
                </option>
              ))}
            </select>
          </div>

          {merchant.merchant && (
            <div
              className="card-compact"
              style={{
                textAlign: 'center',
                padding: '2rem',
                background: 'linear-gradient(135deg, #111827 0%, #0f172a 100%)',
                borderColor: 'rgba(34, 197, 94, 0.2)',
                marginTop: '0.5rem',
              }}
            >
              <h3
                style={{
                  fontSize: '1.5rem',
                  fontWeight: 700,
                  marginBottom: '0.5rem',
                  color: 'var(--color-text)',
                }}
              >
                {merchant.merchant.name}
              </h3>
              <p
                style={{
                  color: 'var(--text-secondary)',
                  marginBottom: '1.5rem',
                  fontSize: '0.9rem',
                }}
              >
                {merchant.merchant.email}
              </p>
              <div
                style={{
                  fontSize: '2.5rem',
                  fontWeight: 900,
                  color: '#ffffff',
                  marginBottom: '0.5rem',
                }}
              >
                R${' '}
                {merchant.balance.toLocaleString('pt-BR', {
                  minimumFractionDigits: 2,
                  maximumFractionDigits: 2,
                })}
              </div>
              <p
                style={{
                  fontSize: '0.8rem',
                  color: 'var(--color-primary)',
                  fontWeight: 600,
                }}
              >
                {merchant.merchants.length} merchants ativos
              </p>
            </div>
          )}
        </div>
      ) : (
        <div style={{ textAlign: 'center', padding: '3rem 0' }}>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1rem' }}>
            Nenhum merchant criado
          </p>
          <button onClick={() => setShowForm(true)} className="btn btn-secondary">
            Criar primeiro merchant
          </button>
        </div>
      )}

      {merchant.error && (
        <div className="message error-message" style={{ marginTop: '1.5rem' }}>
          ❌ {merchant.error}
        </div>
      )}
    </div>
  );
}
