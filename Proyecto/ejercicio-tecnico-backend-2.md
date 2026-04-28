# Prueba Técnica  
## Arquitectura Microservicio (2023)

---

## Indicaciones generales

- Aplique todas las buenas prácticas, patrones Repository, etc que considere necesario (se tomará en cuenta este punto para la calificación).
- El manejo de entidades se debe manejar JPA / Entity Framework Core.
- Se debe manejar mensajes de excepciones.
- Se debe realizar como mínimo dos pruebas unitarias de los endpoints.
- La solución se debe desplegar en Docker.
- Posterior a la entrega de este ejercicio, se estará agendando una entrevista técnica donde el candidato deberá defender la solución planteada.

---

## Herramientas y tecnologías utilizadas

- Java Spring Boot / .NET Core 5 o superior / ASP 4.8 o inferior
- IDE de su preferencia
- Base de Datos Relacional
- Postman v9.13.2 (validador de API) / Karate DSL

---

## Complejidad por Seniority


### SemiSenior
- Separar en **2 microservicios**:
  - (Cliente, Persona)
  - (Cuenta, Movimientos)
- Debe haber **comunicación asincrónica** entre ellos.
- Implementar: F1, F2, F3, F4, F5.
- F6 es deseable.

---

## Generación de API REST

- Manejar verbos: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`

---

## Entidades

### Persona
- nombre
- genero
- edad
- identificación
- dirección
- teléfono
- Debe tener PK

### Cliente
- Hereda de Persona
- clienteId
- contraseña
- estado
- Debe tener PK única

### Cuenta
- número cuenta
- tipo cuenta
- saldo inicial
- estado
- PK única

### Movimientos
- fecha
- tipo movimiento
- valor
- saldo
- PK única

---

## Funcionalidades del API

### F1: CRUDs
Endpoints:
- `/clientes`
- `/cuentas`
- `/movimientos`

Operaciones:
- Crear
- Editar
- Actualizar
- Eliminar

---

### F2: Registro de movimientos

- Puede haber valores positivos o negativos
- Actualiza saldo disponible
- Debe registrar transacciones

---

### F3: Validación de saldo

- Si no hay saldo suficiente:
  - Mensaje: **"Saldo no disponible"**
- Manejo de error según criterio técnico

---

### F4: Reportes

Endpoint:

/reportes?fecha=rango_fechas


Debe incluir:
- Cuentas con saldos
- Movimientos asociados

Formato:
- JSON

---

### F5: Pruebas unitarias
- 1 prueba para entidad Cliente

---

### F6: Pruebas de integración
- 1 prueba de integración

---

### F7: Despliegue
- Contenedores (Docker)

---

## Casos de Uso

### 1. Creación de Usuarios

| Nombre | Dirección | Teléfono | Contraseña | Estado |
|--------|----------|----------|------------|--------|
| Jose Lema | Otavalo sn y principal | 098254785 | 1234 | True |
| Marianela Montalvo | Amazonas y NNUU | 097548965 | 5678 | True |
| Juan Osorio | 13 junio y Equinoccial | 098874587 | 1245 | True |

---

### 2. Creación de Cuentas

| Número | Tipo | Saldo Inicial | Estado | Cliente |
|--------|------|--------------|--------|---------|
| 478758 | Ahorro | 2000 | True | Jose Lema |
| 225487 | Corriente | 100 | True | Marianela Montalvo |
| 495878 | Ahorros | 0 | True | Juan Osorio |
| 496825 | Ahorros | 540 | True | Marianela Montalvo |

---

### 3. Nueva Cuenta

| Número | Tipo | Saldo Inicial | Estado | Cliente |
|--------|------|--------------|--------|---------|
| 585545 | Corriente | 1000 | True | Jose Lema |

---

### 4. Movimientos

| Cuenta | Tipo | Saldo Inicial | Movimiento |
|--------|------|--------------|------------|
| 478758 | Ahorro | 2000 | Retiro 575 |
| 225487 | Corriente | 100 | Depósito 600 |
| 495878 | Ahorros | 0 | Depósito 150 |
| 496825 | Ahorros | 540 | Retiro 540 |

---

### 5. Reporte por fechas

| Fecha | Cliente | Cuenta | Tipo | Saldo Inicial | Movimiento | Saldo Disponible |
|------|---------|--------|------|--------------|------------|------------------|
| 10/2/2022 | Marianela Montalvo | 225487 | Corriente | 100 | 600 | 700 |
| 8/2/2022 | Marianela Montalvo | 496825 | Ahorros | 540 | -540 | 0 |

---

### Ejemplo JSON

```json
{
  "Fecha": "10/2/2022",
  "Cliente": "Marianela Montalvo",
  "Numero Cuenta": "225487",
  "Tipo": "Corriente",
  "Saldo Inicial": 100,
  "Estado": true,
  "Movimiento": 600,
  "Saldo Disponible": 700
}
```
Instrucciones de despliegue
Generar script de BD: BaseDatos.sql
Ejecutar Postman:
http://{servidor}:{puerto}/api/{metodo}


Entregables
Repositorio Git público
Colección Postman (JSON)
Entrega en fecha indicada