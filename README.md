# explora-map · backend

API REST da aplicación de cartografía colaborativa Explora Map.

![Java](https://img.shields.io/badge/Java-25-blue?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.4-brightgreen?style=flat-square)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-PostGIS-336791?style=flat-square)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?style=flat-square)
![License](https://img.shields.io/badge/License-GPL--3.0-orange?style=flat-square)

---

## Sobre a aplicación

Explora Map é unha plataforma de cartografía colaborativa de software libre, deseñada con criterios de *privacy by design* e despregada en [https://explora-mapa.eu](https://explora-mapa.eu). Permite crear mapas con marcadores xeográficos, organizalos por categorías, compartir mapas con outras persoas e xestionar os permisos de edición por roles.

Este repositorio contén o backend: a API REST que alimenta a SPA de frontend. Expón todos os recursos mediante JSON sobre HTTP e xestiona a autenticación mediante JWT stateless.

---

## Stack tecnolóxico

- Java 25: Linguaxe de programación
- Spring Boot 4.0.4: Framework principal (web, security, JPA, mail, validation)
- `io.jsonwebtoken:jjwt-api` 0.13.0: Xeración e validación de tokens JWT
- `net.postgis:postgis-jdbc` 2025.1.1: Soporte de tipos xeométricos PostGIS
- `org.postgresql:postgresql` (xestionada por Spring Boot): Driver JDBC para PostgreSQL (produción)
- `com.h2database:h2` 2.4.240: Base de datos en memoria para o perfil de desenvolvemento
- `org.projectlombok:lombok` 1.18.44: Xeración de boilerplate (getters, builders, constructores)
- `org.springdoc:springdoc-openapi-starter-webmvc-ui` 2.8.8: Documentación OpenAPI / Swagger UI
- `spring-boot-starter-mail` (xestionada por Spring Boot): Envío de correos de verificación
- `spring-boot-starter-validation` (xestionada por Spring Boot): Bean Validation (Jakarta)

---

## Estrutura de cartafoles

```
src/main/java/explora/map/
├── Main.java               # Punto de entrada (@SpringBootApplication)
├── controller/             # Controllers REST (capa de presentación)
├── service/                # Interfaces e implementacións da lóxica de negocio
├── repository/             # Repositorios Spring Data JPA
├── entity/                 # Entidades JPA e enums de dominio
├── dto/                    # Obxectos de transferencia de datos (request/response)
├── config/                 # Configuración de Spring (Security, JPA, excepcións globais)
└── security/               # Filtros JWT, UserDetails, CORS e AuthEntryPoint
```

### Entidades e enums

- `Usuaria` Entidade: Conta de usuaria da aplicación
- `Mapa` Entidade: Mapa colaborativo, con propietaria e tipo de visibilidade
- `Marcador` Entidade: Marcador xeográfico pertencente a un mapa
- `Categoria` Entidade: Categoría para agrupar marcadores dentro dun mapa
- `MapaMembro` Entidade: Relación usuaria–mapa cun rol asignado
- `Convite` Entidade: Convite de colaboración enviado a outra usuaria
- `Historial` Entidade: Rexistro de accións realizadas sobre un mapa
- `MapaGardado` Entidade: Marcado de mapa como favorito por parte dunha usuaria
- `RefreshToken` Entidade: Token de refresco rotativo almacenado en base de datos
- `TokenVerificacion` Entidade: Token de verificación de enderezo de correo
- `RolMapa` Enum: Rol dunha usuaria dentro dun mapa (`PROPIETARIA`, `EDITORA`, `LECTORA`)
- `RolApp` Enum: Rol global na aplicación
- `TipoMapa` Enum: Visibilidade do mapa (`PUBLICO`, `PRIVADO`)
- `TipoElemento` Enum: Tipo de elemento rexistrado no historial
- `TipoAccion` Enum: Acción rexistrada no historial (`CREAR`, `EDITAR`, `ELIMINAR`)
- `EstadoConvite` Enum: Estado dun convite (`PENDENTE`, `ACEPTADO`, `REXEITADO`)

---

## Instalación e desenvolvemento local

### Requisitos

- Java 25+
- Maven 3.9+
- (Opcional) Docker: para levantar PostgreSQL en local no perfil de produción

### Arrincar en modo desenvolvemento

```bash
# Clonar
git clone https://github.com/explora-map/backend.git
cd backend

# Arrincar con perfil dev (H2 en memoria, sen BD externa)
./mvnw spring-boot:run
```

O perfil `dev` (activo por defecto) usa H2 en memoria: non require PostgreSQL nin ningunha configuración externa. Para o correo electrónico utiliza [Mailpit](https://github.com/axllent/mailpit) en `localhost:1025`.

Unha vez arrincado:

- API REST: `http://localhost:8080/api`
- Consola H2: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:exploradb`, usuario: `sa`, contrasinal: vacío)
- Swagger UI: `http://localhost:8080/swagger-ui.html`

### Arrincar en modo produción

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Require PostgreSQL con extensión PostGIS e todas as variables de entorno definidas (ver sección seguinte).

### Executar tests

```bash
./mvnw test
```

---

## Variables de entorno (produción)

O perfil `prod` le a configuración mediante variables de entorno. Ningunha ten valor por defecto agás `MAIL_PORT`.

- `POSTGRES_USER`: Usuario de PostgreSQL
- `POSTGRES_PASSWORD`: Contrasinal de PostgreSQL
- `MAIL_HOST`: Host do servidor SMTP
- `MAIL_PORT`: Porto SMTP (valor por defecto: `25`)
- `MAIL_USERNAME`: Usuario SMTP
- `MAIL_PASSWORD`: Contrasinal SMTP
- `APP_BASE_URL`: URL pública do frontend (usada en ligazóns de verificación)
- `CORS_ALLOWED_ORIGINS`: Lista de orixes CORS permitidas, separadas por comas
- `jwt.secret`: Segredo JWT: mínimo 32 caracteres para HS256

---

## Arquitectura

O backend segue un patrón de capas clásico de Spring:

**Controller › Service › Repository › Entity**

Cada capa ten responsabilidades ben delimitadas: os controllers reciben as peticións HTTP e delegan na capa de servizo; os servizos conteñen a lóxica de negocio e acceden aos datos a través dos repositorios JPA; as entidades mapean as táboas da base de datos.

O **control de acceso aos mapas** está centralizado en `MapaAccesoService`, que determina se unha usuaria ten permiso de lectura ou escritura sobre un recurso concreto en función do tipo de mapa e do seu rol como membro.

A **xestión de excepcións** está centralizada en `GlobalExceptionHandler` (`@RestControllerAdvice`), que mapea as excepcións de negocio a respostas HTTP con código e corpo JSON uniformes:

- `MethodArgumentNotValidException`: 400: erros de validación, devolve `{ campo: mensaxe }`
- `IllegalArgumentException`: 404: recurso non atopado
- `IllegalStateException`: 403: violación de permiso
- `UsernameNotFoundException`: 404: usuaria non atopada
- `BadCredentialsException`: 401: credenciais incorrectas
- `Exception` (xenérica): 500: erro interno (sen expor o stack trace ao cliente)

A **autenticación** é JWT stateless: o `accessToken` vive en memoria no cliente (non se persiste), mentres que o `refreshToken` é rotativo e se almacena en base de datos, o que permite invalidalo en calquera momento.

Os **perfís de Spring** definen dous entornos:
- `dev`: H2 en memoria, Mailpit para correo, CORS aberto a `localhost:5173`.
- `prod`: PostgreSQL con PostGIS, SMTP real con STARTTLS, configuración completa por variables de entorno.

---

## Endpoints principais

A documentación completa de cada endpoint, parámetros e respostas está dispoñible en **Swagger UI**: `http://localhost:8080/swagger-ui.html`

Os seguintes endpoints son públicos (non requiren autenticación): `GET /api/mapas/publicos`, `GET /api/mapas/{id}`, `GET /api/mapas/{mapaId}/categorias`, `GET /api/mapas/{mapaId}/marcadores`, e todo o baixo `/api/auth/**`.

- Autenticación `/api/auth`: Rexistro, login, renovar token, verificación de correo
- Mapas `/api/mapas`: CRUD de mapas, listado de públicos, mapas gardados
- Marcadores `/api/mapas/{mapaId}/marcadores`, `/api/marcadores/{id}`: CRUD de marcadores xeográficos
- Categorías `/api/mapas/{mapaId}/categorias`, `/api/categorias/{id}`: CRUD de categorías de marcadores
- Membros `/api/mapas/{mapaId}/membros`: Listado, cambio de rol e eliminación de membros
- Convites `/api/convites`: Envío, aceptación e rexeitamento de convites de colaboración
- Historial `/api/mapas/{mapaId}/historial`: Consulta do historial de cambios dun mapa
- Perfil `/api/perfil`: Consulta e edición do perfil da usuaria autenticada

---

## Repositorios relacionados

- [explora-map/frontend](https://github.com/explora-map/frontend): SPA React/TypeScript
- [explora-map/docs](https://github.com/explora-map/docs): Documentación do proxecto
- [explora-map/deploy](https://github.com/explora-map/deploy): Configuración de despregamento (Docker Compose, Nginx)

---

## Licenza

Este proxecto está licenciado baixo os termos da [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html).