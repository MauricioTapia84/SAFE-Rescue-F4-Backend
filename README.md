# SAFE Rescue

*Gestión eficiente y rápida de incidentes en situaciones de emergencia.*

<p align="center">
  <img src="./README/SRCover.png" alt="Portada SAFE Rescue" width="900px">
</p>

SAFE Rescue es una aplicación diseñada para la gestión eficiente y rápida de incidentes en situaciones de emergencia. Esta herramienta permite a los equipos de respuesta coordinar recursos, monitorear el desarrollo de incidentes y tomar decisiones informadas en tiempo real. Con un enfoque en la seguridad y la comunicación efectiva, SAFE Rescue optimiza la respuesta a emergencias y ayuda a mitigar los riesgos en situaciones críticas.

---

## Últimos cambios

### ❚❙❘ VERSIÓN 0.0.9.4
> #### ❚❙❘ 0.0.9
> <br>• Se implementa en perfiles lógica para autentificación
> <br>• Se añade configuraciones para seguridad, uso de token y dtos para la validación
> <br>• Se corrigen client de otras apis
> <br>• Se implementan las pruebas unitarias de autentificación
> <br>>>> Corregir pruebas unitarias para el controllador de autentificación
> #### ❚❙❘ 0.0.9.1
> <br>• Se corrigen problemas de conexión entre apis utilizando un token interno
> <br>• Se implementa lógica en api de perfiles para validar el token interno para comunicación entre microservicios
> #### ❚❙❘ 0.0.9.2
> <br>• Se corrigen problemas de conexión del auth controller
> <br>• Se hashean las contraseñas del dataloader
> <br>• Se implementa un cors config que permite aplicar métodos de los endpoints en el front
> <br>• Se implementas log para control de errores y trazabilidad del proceso de inicio de sesion
> #### ❚❙❘ 0.0.9.3
> <br>• Se hicieron modificaciones en los controladores de 3 microservicios: Registros, Geolocalizacion e Incidentes.
> <br>• Se hicieron cambios en algunos POST y GET de los 3 microservicios modificados.
> <br>• Se ajustó la versión de Java del pom.xml de la versión 17 a la 22.
> <br>• Se ajustó la versión de Java del modulo de los proyectos de a la última versión disponible.
> #### ❚❙❘ 0.0.9.4
> <br>• Se hicieron modificaciones en la lógica de inicio de sesión y registro
> <br>• Se crean DTOs para inicio de sesión y registro
> <br>• Se ajustó el dataloader de geolocalización para que no fuera borrando y creando IDs adicionales cada vez que se iniciaba
> <br>• Se creo y ajusto los corsconfig para dar acceso al backend desde el front
> <br>

---

## Características Principales

### Coordinación y Comunicación Centralizada
La comunicación oportuna hace la diferencia. SAFE Rescue proporciona un canal de comunicación unificado que permite a las centrales de alarma y a los equipos en terreno estar perfectamente sincronizados, asegurando que la información crítica llegue a quien la necesita sin demoras.

<p align="center">
  <img src="./README/350_central_alarmas_osorno.jpg" alt="Central de Alarmas Osorno" width="500px">
</p>

### Gestión de Recursos en Terreno
La valentía y la preparación salvan vidas. La aplicación equipa a los bomberos y personal de emergencia con herramientas para visualizar la ubicación de los recursos, actualizar el estadoDTO de los incidentes y recibir instrucciones claras, optimizando cada segundo de la operación.

<p align="center">
  <img src="./README/bomberos_en_accion.jpg" alt="Bomberos en acción" width="500px">
</p>

## Tecnologías Utilizadas
* **JAVA**
* **Spring Boot**
* **MySQL**

<br>

<p align="center">
  <img src="./README/SafeRescueLogo.png" alt="Safe Rescue Logo" width="400px">
</p>
