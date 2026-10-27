# Aplicación de apoyo al estudio para estudiantes en formación

## Descripción del proyecto

MVP académico para la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles**. La aplicación busca apoyar el estudio de estudiantes en formación de ESFOCAR mediante contenidos académicos, ejercicios de autoevaluación y material de apoyo accesible desde el teléfono, incluso con conectividad limitada o sin internet.

El desarrollo debe mantenerse como un prototipo académico: se utilizarán datos ficticios o de prueba y no se conectará la aplicación a plataformas institucionales productivas.

## Integrantes

- Gustavo Oporto

## Instalación y ejecución

1. Clonar el repositorio y abrir la carpeta raíz en Android Studio.
2. Usar JDK 17 y sincronizar Gradle.
3. Ejecutar `app` en un emulador Android o dispositivo físico.
4. Para compilar desde terminal: `./gradlew assembleDebug`; para las pruebas: `./gradlew testDebugUnitTest`.

Las cuentas, IDs y códigos de esta versión son de demostración. Los perfiles reciben un ID `EST-######` o `PROF-######`. El código docente local es `DOCENTE-DEMO-2026`; no representa una verificación de Carabineros ni debe usarse en producción.

## Arquitectura

- `modelo/`: entidades Room, DAO, repositorios, validadores de dominio y lectura local de PDF.
- `vistaModelo/`: estado observable con `StateFlow` y lógica de acceso/documentos.
- `vista/`: pantallas y componentes Jetpack Compose con Material Design 3.
- Room/SQLite conserva perfiles y documentos; los PDF se guardan en el directorio privado de la app.

## Alcance de trabajo actual

En esta etapa trabajaremos en el flujo de acceso y en la carga de material del estudiante:

- Inicio de sesión (**Login**).
- Registro de usuario (**Register**).
- Validación de usuarios y de los datos ingresados.
- Validaciones para textos y correos electrónicos.
- Validación de imágenes seleccionadas o cargadas, cuando corresponda al flujo definido.
- Espacio para que el estudiante pueda seleccionar y subir un archivo PDF.
- Perfiles de prueba de estudiante y profesor, con edición de nombre y correo.
- Biblioteca local de documentos PDF para listar, abrir, resumir y eliminar archivos.
- Escaneo de una hoja con la cámara del dispositivo y conversión local de la captura a PDF.
- Organización de documentos por categorías locales: General, Clases, Normativa, Tareas y Escaneos.

La validación de usuario del MVP debe operar con datos de prueba; no debe consultar ni verificar cuentas en sistemas reales de ESFOCAR o Duoc UC.

## Flujo de trabajo que seguiremos

1. **Acordar el alcance de cada entrega**
   - Mantener el trabajo acotado al MVP y a los hitos de la asignatura.
   - Priorizar ahora Login, Register, validaciones y selección/carga de PDF.
   - Dejar anotadas las decisiones pendientes antes de ampliar funcionalidades.

2. **Definir pantallas y navegación**
   - Identificar las pantallas y estados necesarios para registro, inicio de sesión y carga de PDF.
   - Definir las acciones principales, mensajes de error, confirmaciones y estados vacíos.
   - Aplicar una interfaz Android coherente con Jetpack Compose y Material Design 3, respetando la identidad visual institucional disponible.

3. **Implementar registro y acceso**
   - Crear los formularios de registro e inicio de sesión.
   - Validar campos obligatorios, formato y longitud de textos, formato de correo y reglas acordadas para las credenciales.
   - Mostrar mensajes claros junto al campo correspondiente y evitar enviar formularios inválidos.
   - Usar exclusivamente usuarios ficticios/de prueba; no recopilar RUT, correos reales ni otros identificadores personales.

4. **Implementar validaciones de archivos**
   - Para imágenes: comprobar tipo/formato y tamaño permitido, y manejar archivos inválidos o errores de lectura. Usar únicamente imágenes ficticias o no identificables si se requieren para pruebas.
   - Para PDF: permitir seleccionar un documento, comprobar que corresponda al tipo esperado y gestionar errores, cancelación y confirmación de selección.
   - Definir límites de tamaño y ubicación/retención local antes de implementar la carga. No subir documentos a servicios externos sin autorización y definición explícita del alcance.

5. **Persistir el estado necesario localmente**
   - Diseñar la persistencia local de acuerdo con MVVM y Room/SQLite cuando corresponda.
   - Mantener disponibles sin conexión las funciones de estudio y autoevaluación previstas para el MVP.
   - Evitar guardar información personal innecesaria; tratar los PDFs como material de prueba autorizado.

6. **Probar cada flujo**
   - Probar registro y Login con entradas válidas e inválidas.
   - Cubrir campos vacíos, formatos incorrectos, límites de texto, archivos no compatibles, archivos demasiado grandes y errores de selección/lectura.
   - Añadir pruebas unitarias para las reglas de validación y comprobar los estados de interfaz.
   - Verificar los casos con conexión normal, limitada y sin conexión cuando aplique.

7. **Integrar y revisar los cambios**
   - Mantener cambios pequeños y enfocados en una tarea.
   - Revisar que el proyecto compile y que las pruebas relevantes pasen antes de integrar.
   - Comprobar que no se hayan incluido datos personales, credenciales ni archivos sensibles.
   - Actualizar este README cuando cambie el alcance o el flujo acordado.

8. **Preparar la entrega académica**
   - Completar las funcionalidades priorizadas dentro del tiempo del semestre.
   - Ejecutar pruebas unitarias y revisión final del MVP.
   - Generar el APK firmado en modo release como parte de la entrega definida por la asignatura.

## Criterios y restricciones del caso

- Plataforma objetivo: teléfonos Android.
- Tecnologías definidas para el proyecto: Kotlin, Android Studio, Jetpack Compose, Material Design 3, arquitectura MVVM, Room/SQLite para persistencia local, Retrofit para consumo de API REST y microservicios Spring Boot para contenido remoto cuando corresponda.
- Enfoque offline-first para el estudio y la autoevaluación.
- No utilizar datos reales de estudiantes ni conectar con sistemas institucionales productivos.
- El material académico y los casos utilizados deben ser ficticios, sintéticos o estar depurados y autorizados para uso académico.
- El MVP no representa una aplicación productiva ni definitiva para la institución.
- Los documentos se guardan en el almacenamiento privado del dispositivo. El resumen es extractivo, se calcula sin conexión sobre texto seleccionable de las primeras páginas y no incluye OCR para PDFs escaneados.

## Identidad visual

Usar el logo oficial de ESFOCAR y su paleta verde y dorada como identidad principal de la aplicación. La paleta azul del formato académico Duoc UC corresponde al documento del curso, no a la identidad de Carabineros. No recrear ni alterar el logo.

## Fotografías de bienvenida

Las fotografías del fondo animado se empaquetan en formato WebP para que la bienvenida funcione sin conexión. Fuentes consultadas:

- [El Ovallino: reconocimiento al cuerpo docente de ESFOCAR Grupo Ovalle](https://www.elovallino.cl/articulo/esfocar-grupo-ovalle-reconoce-labor-su-cuerpo-docente/)
- [Dirección de Educación, Doctrina e Historia: Escuela de Formación](https://decarcarabineros.cl/sitio/estate/7)
- [Fotografía publicada en Blogger](https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEgjCD95PqeHjBdhAIJGFUkIhzzbw9HDGcsmIC76-m_XVA-iAXJGySpDTQ2GFyKJ0dPcBCBqdG1ZOW7AMIhULpXB6x2nYMMBdgz27grcFnID__B6IKI_J0gGQsnRf2J_z3QA5NUg1DtZ0DU3IUSi2rR8dF08Yj-L6FFD6mbSZmLZ4gRibt_h4zqymA_Y/s1600/3%204.jpg)
- [Comité para la Prevención de la Tortura: visita a ESFOCAR](https://mnpt.cl/2025/10/29/visita-preventiva-del-cpt-a-la-escuela-de-formacion-de-carabineros-esfocar/)
- [Sitio oficial de ESFOCAR](https://siga.esfocar.carabineros.cl/es) (imagen de portada)

La URL de Instagram compartida corresponde al perfil, no a una publicación de imagen; por eso no se usó su foto de perfil como fondo.
