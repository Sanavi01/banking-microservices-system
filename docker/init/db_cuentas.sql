-- ============================================================
-- Inicialización de db_cuentas (ms-cuentas-movimientos)
-- ============================================================

CREATE TABLE IF NOT EXISTS clientes (
    cliente_id VARCHAR(36) PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cuentas (
    id BIGSERIAL PRIMARY KEY,
    numero_cuenta VARCHAR(20) NOT NULL UNIQUE,
    tipo_cuenta VARCHAR(20) NOT NULL CHECK (tipo_cuenta IN ('Ahorro', 'Corriente')),
    saldo_inicial DECIMAL(15,2) NOT NULL DEFAULT 0,
    estado BOOLEAN NOT NULL DEFAULT true,
    cliente_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cliente_id) REFERENCES clientes(cliente_id)
);

CREATE TABLE IF NOT EXISTS movimientos (
    id BIGSERIAL PRIMARY KEY,
    cuenta_id BIGINT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_movimiento VARCHAR(20) NOT NULL CHECK (tipo_movimiento IN ('Depósito', 'Retiro')),
    valor DECIMAL(15,2) NOT NULL,
    saldo DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cuenta_id) REFERENCES cuentas(id)
);

-- Seed data: Clientes (se sincronizan vía RabbitMQ, pero dejamos base por si no hay eventos)
INSERT INTO clientes (cliente_id, nombre, estado)
VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Jose Lema', true),
    ('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'Marianela Montalvo', true),
    ('c3d4e5f6-a7b8-9012-cdef-123456789012', 'Juan Osorio', true)
ON CONFLICT (cliente_id) DO NOTHING;

-- Seed data: Cuentas
INSERT INTO cuentas (numero_cuenta, tipo_cuenta, saldo_inicial, estado, cliente_id)
VALUES
    ('478758', 'Ahorro', 2000, true, 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'),
    ('225487', 'Corriente', 100, true, 'b2c3d4e5-f6a7-8901-bcde-f12345678901'),
    ('495878', 'Ahorro', 0, true, 'c3d4e5f6-a7b8-9012-cdef-123456789012'),
    ('496825', 'Ahorro', 540, true, 'b2c3d4e5-f6a7-8901-bcde-f12345678901'),
    ('585545', 'Corriente', 1000, true, 'a1b2c3d4-e5f6-7890-abcd-ef1234567890')
ON CONFLICT (numero_cuenta) DO NOTHING;

-- Seed data: Movimientos
INSERT INTO movimientos (cuenta_id, fecha, tipo_movimiento, valor, saldo)
SELECT c.id, '2022-02-08 10:00:00', 'Retiro', -575, 1425
FROM cuentas c WHERE c.numero_cuenta = '478758'
AND NOT EXISTS (SELECT 1 FROM movimientos m WHERE m.cuenta_id = c.id AND m.valor = -575);

INSERT INTO movimientos (cuenta_id, fecha, tipo_movimiento, valor, saldo)
SELECT c.id, '2022-02-10 11:00:00', 'Depósito', 600, 700
FROM cuentas c WHERE c.numero_cuenta = '225487'
AND NOT EXISTS (SELECT 1 FROM movimientos m WHERE m.cuenta_id = c.id AND m.valor = 600);

INSERT INTO movimientos (cuenta_id, fecha, tipo_movimiento, valor, saldo)
SELECT c.id, '2022-02-12 12:00:00', 'Depósito', 150, 150
FROM cuentas c WHERE c.numero_cuenta = '495878'
AND NOT EXISTS (SELECT 1 FROM movimientos m WHERE m.cuenta_id = c.id AND m.valor = 150);

INSERT INTO movimientos (cuenta_id, fecha, tipo_movimiento, valor, saldo)
SELECT c.id, '2022-02-08 09:00:00', 'Retiro', -540, 0
FROM cuentas c WHERE c.numero_cuenta = '496825'
AND NOT EXISTS (SELECT 1 FROM movimientos m WHERE m.cuenta_id = c.id AND m.valor = -540);
