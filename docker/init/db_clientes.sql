-- ============================================================
-- Inicialización de db_clientes (ms-clientes-personas)
-- ============================================================

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

-- Nota: Los datos de prueba se crean a través de la API POST /clientes.
-- Las contraseñas deben hashearse con BCrypt desde el backend.
