# [cite_start]Prueba Técnica: Arquitectura Microservicio (2023) [cite: 1, 2]

## 1. Indicaciones generales
* [cite_start]Aplique todas las buenas prácticas, patrones Repository, etc que considere necesario (se tomará en cuenta este punto para la calificación)[cite: 5, 6].
* [cite_start]El manejo de entidades se debe manejar JPA / Entity Framework Core[cite: 7].
* [cite_start]Se debe manejar mensajes de excepciones[cite: 8].
* [cite_start]Se debe realizar como mínimo dos pruebas unitarias de los endpoints[cite: 9].
* [cite_start]La solución se debe desplegar en Docker[cite: 10].
* [cite_start]Posterior a la entrega de este ejercicio, se estará agendando una entrevista técnica donde el candidato deberá defender la solución planteada[cite: 11, 12].

## 2. Herramientas y tecnologías utilizadas
* [cite_start]**Lenguajes:** Java spring boot / NetCore 5 o superior / Asp 4.8 o inferior[cite: 14].
* [cite_start]**IDE:** De su preferencia[cite: 15].
* [cite_start]**Base de Datos:** Relacional[cite: 16].
* [cite_start]**Validación:** Postman v9.13.2 (validador de API) / Karate DSL[cite: 17].

## 3. Complejidad por Seniority
> [cite_start]**Nota:** Considerar las siguientes indicaciones en base al perfil al que esta aplicando[cite: 19].

* **Junior:** Implementar los diferentes endpoints para cumplir las funcionalidades: F1, F2, F3. [cite_start]No es mandatorio funcionalidades: F4, F5, F6[cite: 20].
* [cite_start]**SemiSenior:** Separar en 2 microservicios, agrupando (Cliente, Persona) y (Cuenta, Movimientos) donde se contemple una comunicación asincrónica entre los 2 microservicios[cite: 21]. [cite_start]Cumplir las funcionalidades: F1, F2, F3, F4, F5; deseable la funcionalidad F6[cite: 22].
* [cite_start]**Senior:** Implementar en 2 microservicios, agrupando (Cliente, Persona) y (Cuenta, Movimientos) donde se contemple una comunicación asincrónica entre los 2 microservicios[cite: 23]. [cite_start]Cumplir las funcionalidades F1, F2, F3, F4, F5, F6, F7[cite: 24]. [cite_start]La solución debe contemplar (no necesariamente implementado) factores como: rendimiento, escalabilidad, resiliencia[cite: 25].

---

## 4. Definición de Entidades
[cite_start]**Generación de Api Rest:** Manejar los verbos Get, Post, Put, Push (Patch), Delete[cite: 26, 27].

### Persona
* [cite_start]Datos: nombre, genero, edad, identificación, dirección, teléfono[cite: 29].
* [cite_start]Debe manejar su clave primaria (PK)[cite: 30].

### Cliente
* [cite_start]Debe manejar una entidad que herede de la clase persona[cite: 32].
* [cite_start]Atributos: clienteid, contraseña, estado[cite: 33].
* [cite_start]Debe tener una clave única (PK)[cite: 34].

### Cuenta
* [cite_start]Atributos: número cuenta, tipo cuenta, saldo Inicial, estado[cite: 37].
* [cite_start]Debe manejar su clave única[cite: 38].

### Movimientos
* [cite_start]Atributos: Fecha, tipo movimiento, valor, saldo[cite: 42].
* [cite_start]Debe manejar su clave única[cite: 43].

---

## 5. Funcionalidades del API (F)
[cite_start]Los API's debe tener las siguientes operaciones[cite: 45]:

* [cite_start]**F1: Generación de CRUDS:** Crear, editar, actualizar y eliminar registros para las entidades Cliente, Cuenta y Movimiento[cite: 46].
    * [cite_start]Endpoints: `/cuentas`, `/clientes`, `/movimientos`[cite: 48, 49, 50].
* [cite_start]**F2: Registro de movimientos:** * Se pueden tener valores positivos o negativos[cite: 52].
    * [cite_start]Al realizar un movimiento se debe actualizar el saldo disponible[cite: 53].
    * [cite_start]Se debe llevar el registro de las transacciones realizadas[cite: 53].
* [cite_start]**F3: Validación de saldo:** Al realizar un movimiento el cual no cuente con saldo, debe alertar mediante el mensaje **"Saldo no disponible"**[cite: 54]. [cite_start]Defina la mejor manera de capturar y mostrar el error[cite: 55].
* [cite_start]**F4: Reportes:** Generar un reporte de "Estado de cuenta" especificando un rango de fechas y cliente[cite: 56].
    * [cite_start]Debe contener: Cuentas asociadas con saldos y detalle de movimientos[cite: 58].
    * [cite_start]Endpoint: `/reportes?fecha=rango fechas`[cite: 59].
    * [cite_start]Formato: JSON[cite: 61].
* [cite_start]**F5: Pruebas unitarias:** Implementar 1 prueba unitaria para la entidad de dominio Cliente[cite: 64].
* [cite_start]**F6: Pruebas de Integración:** Implementar 1 prueba de integración[cite: 65].
* [cite_start]**F7: Despliegue:** Despliegue de la solución en contenedores[cite: 66].

---

## 6. Casos de Uso (Ejemplos)

### [cite_start]1. Creación de Usuarios [cite: 68, 69]
| Nombres | Dirección | Teléfono | Contraseña | Estado |
| :--- | :--- | :--- | :--- | :--- |
| Jose Lema | Otavalo sn y principal | 098254785 | 1234 | True |
| Marianela Montalvo | Amazonas y NNUU | 097548965 | 5678 | True |
| Juan Osorio | 13 junio y Equinoccial | 098874587 | 1245 | True |

### [cite_start]2. Creación de Cuentas de Usuario [cite: 70, 71, 72, 73]
| Numero Cuenta | Tipo | Saldo Inicial | Estado | Cliente |
| :--- | :--- | :--- | :--- | :--- |
| 478758 | Ahorro | 2000 | True | Jose Lema |
| 225487 | Corriente | 100 | True | Marianela Montalvo |
| 495878 | Ahorros | 0 | True | Juan Osorio |
| 496825 | Ahorros | 540 | True | Marianela Montalvo |
| 585545 | Corriente | 1000 | True | Jose Lema |

### [cite_start]3. Registro de Movimientos [cite: 74, 75, 76]
* [cite_start]**Cuenta 478758:** Retiro de 575 (Saldo inicial: 2000)[cite: 75].
* [cite_start]**Cuenta 225487:** Deposito de 600 (Saldo inicial: 100)[cite: 76].
* [cite_start]**Cuenta 495878:** Deposito de 150 (Saldo inicial: 0)[cite: 76].
* [cite_start]**Cuenta 496825:** Retiro de 540 (Saldo inicial: 540)[cite: 76].

### [cite_start]4. Listado de Movimiento (Reporte) [cite: 77, 78]
| Fecha | Cliente | Numero Cuenta | Tipo | Saldo Inicial | Estado | Movimiento | Saldo Disponible |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 10/2/2022 | Marianela Montalvo | 225487 | Corriente | 100 | True | 600 | 700 |
| 8/2/2022 | Marianela Montalvo | 496825 | Ahorros | 540 | True | -540 | 0 |

[cite_start]**Ejemplo Json de salida:** [cite: 79]
```json
{
 "Fecha":"10/2/2022",
 "Cliente":"Marianela Montalvo",
 "Numero Cuenta":"225487",
 "Tipo":"Corriente",
 "Saldo Inicial":100,
 "Estado":true,
 "Movimiento":600,
 "Saldo Disponible":700
}

7. Instrucciones de despliegue y entrega

Script DB: Generar el script de base datos, entidades y esquema datos, con el nombre BaseDatos.sql.  


Validación: Ejecutar Postman para realizar las verificaciones (http://{servidor}:{puerto}/api/{metodo}).  

Entregables:

Repositorio Git público (enviar ruta).  

Archivo Json de Postman para validación de endpoints.  

Entrega antes de la fecha y hora indicada por correo.