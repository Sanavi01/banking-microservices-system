-- ============================================================
-- BaseDatos.sql — Esquema completo del sistema bancario
-- ============================================================

-- ========== db_clientes (ms-clientes-personas) ==========

CREATE TABLE IF NOT EXISTS personas (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    genero VARCHAR(20),
    edad INTEGER,
    identificacion VARCHAR(20) NOT NULL UNIQUE,
    direccion VARCHAR(300) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS clientes (
    persona_id BIGINT PRIMARY KEY,
    cliente_id VARCHAR(36) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    estado BOOLEAN NOT NULL,
    FOREIGN KEY (persona_id) REFERENCES personas(id)
);

-- ========== db_cuentas (ms-cuentas-movimientos) ==========

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
    tipo_movimiento VARCHAR(20) NOT NULL,
    valor DECIMAL(15,2) NOT NULL,
    saldo DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cuenta_id) REFERENCES cuentas(id)
);

-- ========== Datos de prueba ==========

-- Clientes
INSERT INTO personas (nombre, genero, edad, identificacion, direccion, telefono)
VALUES
    ('Jose Lema', 'Masculino', 30, '1234567890', 'Otavalo sn y principal', '098254785'),
    ('Marianela Montalvo', 'Femenino', 28, '0987654321', 'Amazonas y NNUU', '097548965'),
    ('Juan Osorio', 'Masculino', 35, '1357924680', '13 junio y Equinoccial', '098874587')
ON CONFLICT (identificacion) DO NOTHING;
