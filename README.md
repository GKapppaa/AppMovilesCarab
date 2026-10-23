# Aplicación de apoyo al estudio para estudiantes en formación

## Descripción del proyecto

MVP académico para la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles**. La aplicación busca apoyar el estudio de estudiantes en formación de ESFOCAR mediante contenidos académicos, ejercicios de autoevaluación y material de apoyo accesible desde el teléfono, incluso con conectividad limitada o sin internet.

El desarrollo debe mantenerse como un prototipo académico: se utilizarán datos ficticios o de prueba y no se conectará la aplicación a plataformas institucionales productivas.

## Integrantes

- Daniel Pinto
- Hernán García

## Alcance de trabajo actual

En esta etapa trabajaremos en el flujo de acceso y en la carga de material del estudiante:

- Inicio de sesión (**Login**).
- Registro de usuario (**Register**).
- Validación de usuarios y de los datos ingresados.
- Validaciones para textos y correos electrónicos.
- Validación de imágenes seleccionadas o cargadas, cuando corresponda al flujo definido.
- Espacio para que el estudiante pueda seleccionar y subir un archivo PDF.

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

## Identidad visual

Usar como referencia la identidad institucional mostrada en el material del caso de **Duoc UC · Escuela de Informática y Telecomunicaciones · CITT**. Mantener una paleta basada en los tonos azules institucionales visibles en ese material. Incorporar un archivo de logo oficial únicamente cuando el equipo disponga de un recurso autorizado; no recrear ni inventar el logo.
