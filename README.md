# AppMovilesCarab — ESFOCAR

Aplicación Android de apoyo al estudio para estudiantes en formación. Es un **MVP académico** para DSY1105 — Desarrollo de Aplicaciones Móviles, no un producto institucional aprobado ni conectado a sistemas productivos de Carabineros, ESFOCAR o Duoc UC.

La app permite guardar material de estudio en el dispositivo, practicar con preguntas construidas desde los PDF y gestionar aula entre perfiles ficticios. El microservicio propio Spring Boot/Java administra sesiones de demo, almacena los bytes PDF en el servidor y permite que profesores los compartan con estudiantes. Room conserva la copia local para trabajar sin conexión.

> **Datos de prueba solamente.** No usar RUT, correos reales, información institucional confidencial ni documentos sin autorización.

## Estado actual

- Android nativo en Kotlin, Jetpack Compose y Material 3.
- `applicationId`: `com.example.appcarab`; versión actual `1.0` (`versionCode 1`).
- El idioma predeterminado de la app es español, aunque el sistema Android esté en otro idioma; inglés queda disponible como selección manual en Ajustes.
- Navegación inferior: **Inicio**, **Aula**, **Material** y **Repaso**; el perfil se abre desde el icono superior.
- La vista docente permite vincular estudiantes una vez y luego seleccionar al destinatario para compartir un PDF, enviar mensajes o asignar tareas.
- Las preguntas de trivia se generan localmente desde texto extraído de los PDF del usuario; ese contenido nunca se envía a servicios externos.
- En Repaso se añade un buscador complementario de libros desde Open Library; no sustituye la trivia privada basada en PDF.
- Los registros de aula existentes siguen sincronizándose best-effort con una bandeja Room y WorkManager.
- Los PDF se almacenan localmente y en el backend cuando hay sesión/conectividad; el profesor puede compartirlos entre instalaciones y el estudiante los descarga para uso offline.
- Mensajes y tareas todavía se mantienen locales; el intercambio remoto implementado en esta entrega es la vinculación de perfiles y la compartición de PDF.
- Los bearer tokens limitan las rutas de recursos al perfil/rol de una sesión de demo, pero no sustituyen autenticación de producción: el login/contraseña se valida localmente y el endpoint que emite sesión solo comprueba perfil/rol sincronizados.

## Funcionalidades por rol

### Estudiante

- Crear una cuenta ficticia, iniciar sesión y editar nombre/correo.
- Ver su ID de perfil.
- Consultar mensajes y tareas locales; recibir PDF que el profesor comparta desde otra instalación.
- Cargar PDF, ver resumen, cambiar categoría, abrir y eliminar documentos.
- Escanear una hoja y guardarla como PDF.
- Crear preguntas de práctica basadas en texto seleccionable de sus documentos.

### Profesor

- Crear perfil de profesor con el código demo `DOCENTE-DEMO-2026`.
- Vincular estudiantes por su ID de perfil; la relación remota queda disponible para compartir desde otras instalaciones.
- Seleccionar un estudiante vinculado y cambiar entre **Compartir**, **Mensaje** y **Tarea**.
- Cargar PDF propios en el servidor y compartirlos con estudiantes vinculados; también puede enviar mensajes y asignar tareas en la copia local.

## Requisitos

- Android Studio con **JDK 21**.
- Android SDK 37 y emulador/dispositivo con Android 13 o superior (`minSdk 33`).
- Para probar la integración local, ADB conectado y backend iniciado.
- Linux/macOS: los comandos usan `./gradlew`. En Windows, usar `gradlew.bat`.

## Ejecutar la app y el backend

### 1. Iniciar el backend

Desde la raíz del repositorio, en una terminal que se mantenga abierta:

```bash
./gradlew -p backend bootRun
```

El backend escucha en `http://localhost:8081`. `bootRun` es un proceso de servidor: la terminal queda ocupada mientras se ejecuta y eso es esperado. Comprueba que arrancó con:

```bash
curl --fail http://localhost:8081/api/v1/health
```

Respuesta esperada:

```json
{"status":"ok","service":"ESFOCAR academic API"}
```

**No inicies una segunda instancia** sobre el mismo directorio: H2 usa el archivo `backend/data/esfocar.mv.db` y puede bloquearlo si dos procesos intentan abrirlo.

### 2. Instalar/ejecutar Android Debug

Con un emulador abierto o un dispositivo Android conectado por ADB:

```bash
./gradlew installDebug
```

También se puede usar **Run ▶** desde Android Studio. La tarea `installDebug` crea el reenvío ADB del puerto 8081 (`adb reverse tcp:8081 tcp:8081`); la app Debug usa `http://127.0.0.1:8081/api/v1/`.

El reenvío se pierde al reiniciar el emulador. Tras reiniciarlo, vuelve a ejecutar desde Android Studio o `./gradlew installDebug`; abrir únicamente el icono de una app que ya estaba instalada no recrea el puente.

Si hay más de un dispositivo ADB conectado, selecciona uno:

```bash
ANDROID_SERIAL=emulator-5554 ./gradlew installDebug
```

Para cambiar la dirección compilada:

```bash
./gradlew installDebug -PapiBaseUrl=http://<host>:8081/api/v1/
```

En un teléfono por USB/ADB se puede usar el puente de igual forma. Si se utiliza una dirección LAN, ambos dispositivos deben estar en la misma red y el firewall debe permitir el puerto 8081.

> El APK Android no se instala en iPhone. Para probar la app hace falta un emulador Android o un teléfono Android.

## API académica

Spring Boot 3.5.7, Java 21, Spring Data JPA y **H2**. El backend de este repositorio no está configurado para PostgreSQL: usa H2 persistente al ejecutar y H2 en memoria en sus pruebas. La API almacena registros JSON genéricos identificados por tipo y clave; el `contenido` se guarda como texto JSON opaco, sin un esquema de dominio rígido.

Tipos permitidos:

- `perfiles`
- `secciones`
- `mensajes`
- `materiales`
- `tareas`

Endpoints:

| Método | Endpoint | Uso |
|---|---|---|
| `GET` | `/api/v1/health` | Estado del servicio |
| `POST` | `/api/v1/sesiones` | Emitir una sesión bearer de demo para un perfil sincronizado |
| `GET` | `/api/v1/sesiones/actual` | Validar sesión |
| `DELETE` | `/api/v1/sesiones/actual` | Revocar sesión |
| `GET` | `/api/v1/{tipo}` | Listar registros |
| `POST` | `/api/v1/{tipo}` | Crear registro |
| `GET` | `/api/v1/{tipo}/{clave}` | Obtener uno |
| `PUT` | `/api/v1/{tipo}/{clave}` | Reemplazar su contenido |
| `DELETE` | `/api/v1/{tipo}/{clave}` | Eliminar |
| `POST` | `/api/v1/aula/alumnos` | Vincular alumno al profesor de la sesión |
| `GET` | `/api/v1/aula/alumnos` | Consultar alumnos vinculados |
| `POST` | `/api/v1/documentos` | Subir un PDF con multipart/form-data |
| `GET` | `/api/v1/documentos/mios` | Listar los PDF del perfil autenticado |
| `GET` | `/api/v1/documentos/compartidos` | Listar PDF recibidos por el estudiante |
| `POST` | `/api/v1/documentos/{id}/compartir` | Compartir un PDF con un alumno vinculado |
| `GET` | `/api/v1/documentos/{id}/contenido` | Descargar un PDF propio o compartido |
| `DELETE` | `/api/v1/documentos/{id}` | Eliminar un PDF propio y sus enlaces compartidos |

Ejemplo de creación:

```bash
curl -X POST http://localhost:8081/api/v1/secciones \
  -H 'Content-Type: application/json' \
  -d '{"clave":"demo-01","contenido":"{\"profesorId\":1,\"alumnoId\":2}"}'
```

`clave` es única por tipo y tiene un máximo de 120 caracteres. Los errores principales son `400` para tipo/cuerpo inválido, `404` para registro inexistente y `409` para una clave ya creada.

### Sesión de demo y PDF del usuario

Tras login local, la app abre `POST /api/v1/sesiones` con el código/rol del perfil que ya se sincronizó. El backend devuelve un bearer opaco con caducidad de 12 horas; se envía en `Authorization: Bearer <token>`. Los tokens viven en memoria: reiniciar Spring Boot los invalida y el usuario debe iniciar sesión de nuevo. Si el perfil estaba pendiente de sincronización porque el backend no estaba disponible, se puede reintentar la conexión desde la app.

Ejemplo de sesión y carga de un PDF:

```bash
curl -X POST http://localhost:8081/api/v1/sesiones \
  -H 'Content-Type: application/json' \
  -d '{"codigoPerfil":"PROF-<UUID>","rol":"PROFESOR"}'

curl -X POST http://localhost:8081/api/v1/documentos \
  -H 'Authorization: Bearer <token>' \
  -F 'archivo=@unidad.pdf;type=application/pdf' \
  -F 'resumen=Material de prueba' \
  -F 'categoria=Clases'
```

Los bytes se guardan fuera de H2 en `backend/data/documentos/<perfil>/<uuid>.pdf`; H2 conserva propietario, metadatos y comparticiones. El servicio valida la firma `%PDF-`, acepta PDF de hasta 20 MiB y guarda los archivos usando nombres internos generados por el servidor. El profesor debe vincular al alumno antes de compartir. El estudiante lista los PDF recibidos y la app descarga/guarda la copia en Room/almacenamiento privado. Al borrar el documento del profesor se borra la copia del servidor y se revoca el enlace; una copia que el estudiante ya descargó puede seguir en su dispositivo hasta que la elimine.

La configuración de almacenamiento es `APP_DOCUMENTOS_DIRECTORIO` (por defecto `./data/documentos`, relativo al directorio desde donde corre Spring Boot); el tamaño máximo se configura con `APP_DOCUMENTOS_MAX_BYTES` (20 MiB por defecto).

> **Seguridad del MVP:** abrir una sesión valida que el perfil exista en `perfiles` y que su rol coincida, pero el backend no verifica la contraseña. Es un mecanismo de aislamiento de demo, no autenticación segura. No expongas este servicio a Internet ni guardes PDFs sensibles o reales.

La consola H2, habilitada para desarrollo local, está en `http://localhost:8081/db-console`:

- JDBC URL: `jdbc:h2:file:./data/esfocar`
- Usuario: `sa`
- Contraseña: vacía

La interfaz ya no muestra un botón de “Comprobar conexión”. La API se usa internamente al sincronizar datos; para comprobar el servidor en desarrollo se usa `/api/v1/health` desde `curl` o el navegador del computador.

### API externa complementaria: Open Library

En la sección **Repaso** hay un buscador de libros complementarios que consume Open Library directamente desde Android con Retrofit:

- Endpoint: `https://openlibrary.org/search.json`
- Parámetros usados: `q`, `limit=10` y `fields=key,title,author_name,first_publish_year,cover_i,subject`.
- Permite escribir una búsqueda o usar sugerencias como Derecho, Seguridad Pública, Criminología y Constitución.
- Muestra título, autores, año, temas y un enlace a la ficha del libro en Open Library.
- Es un catálogo externo complementario, **no material oficial de ESFOCAR**. La trivia principal sigue generándose localmente desde los PDF; el contenido de esos PDF nunca se envía a Open Library.
- Requiere Internet; si falla, la biblioteca local, el material propio y la trivia PDF siguen funcionando.

Referencia: [Open Library Search API](https://openlibrary.org/developers/api). Esta API externa no debe confundirse con el microservicio propio Spring Boot para usuarios y PDF.

## Sincronización, almacenamiento y límites

### Android / Room

Room conserva localmente:

- cuentas y perfiles de prueba;
- documentos, su resumen, texto extraído y categoría;
- relaciones profesor–estudiante;
- mensajes, tareas y materiales compartidos;
- registros pendientes de enviar al backend.

La sincronización remota de registros de aula se intenta después de guardar localmente. Si falla, el registro queda en una bandeja Room. WorkManager reintenta con requisito de conectividad, backoff exponencial desde 15 segundos y vuelve a programar pendientes al abrir la app. Los PDF que sí se suben se asocian al perfil remoto y cada descarga compartida se importa a la biblioteca local del estudiante.

La sincronización multi-dispositivo está implementada para perfiles vinculados y PDF compartidos. **Mensajes y tareas todavía no se descargan como bandeja remota**; quedan locales y su reflejo en el backend no constituye una sincronización completa.

### PDF y trivia

- Los bytes se copian al almacenamiento privado de la app bajo `filesDir/documentos/{usuarioId}`. Con sesión remota disponible también se suben al almacenamiento propio del backend para compartirlos; la copia local se mantiene para offline.
- Se extrae texto seleccionable de hasta las primeras **20 páginas** y **40.000 caracteres**.
- El resumen local usa hasta cuatro oraciones y un máximo de 700 caracteres.
- La trivia selecciona una oración, oculta un término y ofrece alternativas obtenidas del vocabulario del mismo documento.
- Es un generador heurístico local, no un modelo de IA; documentos breves pueden no tener suficientes oraciones o términos distintos para formar alternativas.
- Las preguntas se crean en el dispositivo. Se indica el nombre del PDF fuente y su contenido no se envía a otra API.
- Los PDF escaneados o de imagen requieren OCR, que **no está incluido**. Sin texto seleccionable, no se puede crear trivia a partir de ese archivo.
- Al compartir se envía una referencia al PDF almacenado, no se vuelve a subir el archivo.

### Cuentas, privacidad y seguridad

- Nombre: 2–5 palabras válidas.
- Correo con formato válido; la cuenta se crea localmente.
- Contraseña: 8–64 caracteres, con mayúscula, minúscula, número y símbolo, sin espacios. La contraseña local se deriva con PBKDF2 y salt; no forma parte del JSON enviado para sincronizar el perfil.
- ID de perfil: `EST-<UUID>` o `PROF-<UUID>` para cuentas nuevas. Para compatibilidad, el validador acepta también IDs antiguos `EST-######`.
- Código docente de demo: `DOCENTE-DEMO-2026`.
- El bearer de demo limita operaciones por perfil/rol, pero el backend no autentica credenciales locales ni cifra su token en el almacenamiento de app. H2 y su consola son de desarrollo. No exponer este servicio a Internet ni usar datos reales.
- El manifiesto habilita HTTP claro **solo en Debug**; una distribución Release requiere endpoint HTTPS y configuración de seguridad apropiada.

## Arquitectura y directorios

```text
app/src/main/java/com/example/appcarab/
├── modelo/       # Room, DAO, repositorios, Retrofit, PDF y trivia local
├── vistaModelo/  # Estado y lógica observable con ViewModel/StateFlow
├── vista/        # Pantallas y componentes Compose
└── ui/theme/     # Colores, tema y tipografía ESFOCAR

backend/
├── src/main/java/cl/esfocar/api/  # Spring Boot, entidad y CRUD REST
├── src/main/resources/            # Configuración H2/puerto
└── src/test/                      # Pruebas de API
```

Tecnologías:

- Android: Kotlin, Jetpack Compose, Material 3, MVVM, Room/SQLite, Retrofit, OkHttp, WorkManager y PDFBox Android.
- Backend: Java 21, Spring Boot, Spring Data JPA y H2.
- Diseño: navegación móvil de cuatro secciones, perfil desde menú superior y paleta verde/dorada de ESFOCAR.

## Compilar y probar

```bash
# App Android
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug

# Backend
./gradlew -p backend test
./gradlew -p backend bootJar
```

### Cobertura de pruebas disponible

Hay pruebas para validadores, resumen y trivia local, repositorios de usuario/API, DAO y bienvenida. La rama `hernan-desarrollo` añadió fakes y pruebas unitarias/instrumentadas para esos componentes.

- `testDebugUnitTest`: pruebas JVM de la app (incluye repositorios, validadores y Open Library).
- `compileDebugAndroidTestKotlin`: comprueba que compilen las pruebas instrumentadas.
- `connectedDebugAndroidTest`: se ejecutó en el emulador; 6 pruebas instrumentadas pasaron después de actualizar AndroidX Test/Espresso.
- `./gradlew -p backend test`: 9 pruebas MockMvc de salud, CRUD, sesiones y ciclo de vida/aislamiento de PDF pasan con H2 en memoria.

Última verificación local: `testDebugUnitTest`, `connectedDebugAndroidTest` (emulador) y `lintDebug` pasaron; también `./gradlew -p backend test`. La rama de Gustavo aportó cuatro pruebas unitarias para el flujo Open Library.

**Importante:** hay pruebas implementadas, pero el proyecto todavía no configura JaCoCo ni tiene un reporte que demuestre el 80% de cobertura lógica exigido por la pauta.

Para usar PostgreSQL hace falta configuración y credenciales propias. El PostgreSQL que pudiera estar instalado en el equipo no forma parte del proyecto y no se usa en estas pruebas; no se conecta ni modifica sin que se proporcione la configuración autorizada.

APK Debug:

```text
app/build/outputs/apk/debug/app-debug.apk
```

La variante Release no tiene keystore/JKS productivo configurado. Antes de una entrega instalable fuera de desarrollo hay que definir firma, endpoint HTTPS, permisos y despliegue seguro del backend.

## Diagnóstico frente a la rúbrica de Evaluación Parcial 4

Revisión basada en la pauta adjunta de DSY1105. Es un diagnóstico del estado del repositorio, no una nota estimada:

| Indicador | Estado observado | Qué falta para evidenciar el nivel destacado |
|---|---|---|
| **IE 3.1.1 — Pantallas, formularios y flujos (15%)** | Implementación sustancial: acceso, Inicio, Aula, Material, Repaso y perfil. La bienvenida tiene pruebas instrumentadas; el flujo docente se probó en emulador. | Hacer regresión completa de navegación, validación, edición, PDFs, tareas, mensajes y trivia en un dispositivo de defensa. Las pruebas actuales no cubren todos los flujos de UI. |
| **IE 3.1.2 — Microservicio y base de datos (15%)** | Spring Boot/Java, H2 persistente, CRUD genérico y API de archivos; pruebas del backend pasan. | El repositorio usa H2, no PostgreSQL. Si la pauta/defensa exige PostgreSQL, falta acordar y configurar esa base y probar contra ella. |
| **IE 3.1.3 — Integración y CRUD desde la app (15%)** | Sesiones bearer de demo; profesor/alumno se vinculan en Spring Boot; PDF se sube, lista, comparte, descarga y elimina con alcance por perfil. | Los mensajes y tareas siguen sin flujos de lectura/escritura remotos completos; su sincronización genérica no llena Room en otro dispositivo. Probar PDF compartidos en dos instalaciones. |
| **IE 3.1.4 — API externa visible (15%)** | Open Library se consume con Retrofit y se muestra en Repaso como fuente complementaria; los PDF permanecen locales. | Demostrarla durante la defensa con Internet y explicar que su catálogo no es material oficial institucional. |
| **IE 3.2.1 — Cobertura lógica ≥80% (15%)** | Hay 24 pruebas JVM, 6 instrumentadas y 9 de backend, incluyendo Open Library y acceso/compartición de PDF. Las suites configuradas pasan. | No hay reporte JaCoCo ni evidencia cuantitativa de 80%. Medir cobertura lógica y ampliar pruebas de ViewModel/Repository/aula. |
| **IE 3.3.1 — APK Release firmado (10%)** | Se genera APK Debug, firmado con la clave de depuración. | **Falta** configurar firma Release, generar/probar el APK firmado y documentar el proceso. No hay `.jks` de entrega. Proteger la clave y contraseñas; no subir secretos a un repo público sin acordar una entrega segura. |
| **IE 3.3.2 — GitHub y Trello (15%)** | Hay historial de commits de Daniel y Hernán; existe la rama `hernan-desarrollo`. | Falta evidencia de tablero/Trello, tareas distribuidas y capturas/enlace para AVA. La integración local de `hernan-desarrollo` debe subirse a `Daniel-Branch` para que esté consolidada en la rama a entregar. |

### Prioridad recomendada

1. Probar subida y compartición de PDF profesor→estudiante con dos instalaciones/sesiones.
2. Completar mensajes/tareas bidireccionales si forman parte de la demostración.
3. Configurar firma Release y validar instalación del APK firmado.
4. Configurar cobertura y llegar a ≥80%; ejecutar también las pruebas instrumentadas.
5. Completar regresión manual de todos los recorridos y guardar evidencia.
6. Añadir enlace/capturas de Trello y GitHub al README y revisar requisitos AVA.

La pauta advierte que cambios al repositorio después de la fecha límite pueden implicar nota 1.0; confirmar el plazo antes de publicar la entrega.

## Historial de interfaces

- `Daniel-Branch`: interfaz móvil actual y desarrollo vigente.
- `diseno-antiguo`: rama con la interfaz anterior, guardada antes del rediseño.
- Etiqueta local `backup/interfaz-anterior` apunta al checkpoint previo a la interfaz nueva.

## Equipo

- Gustavo Oporto

## Créditos de fotografías de bienvenida

Las fotografías del fondo animado se empaquetan en WebP para que la bienvenida funcione sin conexión:

- [El Ovallino — reconocimiento al cuerpo docente de ESFOCAR Grupo Ovalle](https://www.elovallino.cl/articulo/esfocar-grupo-ovalle-reconoce-labor-su-cuerpo-docente/)
- [Dirección de Educación, Doctrina e Historia — Escuela de Formación](https://decarcarabineros.cl/sitio/estate/7)
- [Fotografía publicada en Blogger](https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEgjCD95PqeHjBdhAIJGFUkIhzzbw9HDGcsmIC76-m_XVA-iAXJGySpDTQ2GFyKJ0dPcBCBqdG1ZOW7AMIhULpXB6x2nYMMBdgz27grcFnID__B6IKI_J0gGQsnRf2J_z3QA5NUg1DtZ0DU3IUSi2rR8dF08Yj-L6FFD6mbSZmLZ4gRibt_h4zqymA_Y/s1600/3%204.jpg)
- [Comité para la Prevención de la Tortura — visita a ESFOCAR](https://mnpt.cl/2025/10/29/visita-preventiva-del-cpt-a-la-escuela-de-formacion-de-carabineros-esfocar/)
- [Sitio oficial de ESFOCAR](https://siga.esfocar.carabineros.cl/es)

La URL de Instagram compartida en el trabajo original correspondía al perfil, no a una publicación de imagen; por eso no se usó su foto de perfil como fondo.
