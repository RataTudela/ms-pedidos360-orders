1- ms-orders (ms-pedidos360-orders)
Markdown
#Microservicio de Órdenes (`ms-orders`)

Este microservicio se encarga de la orquestación principal en la creación de pedidos, la validación de reglas de negocio y la publicación de eventos y comandos hacia el resto de microservicios de la arquitectura.


## Requisitos e Inicio Rápido

Este microservicio es el orquestador inicial del ecosistema, Debe ser el **PRIMERO** en encenderse ya que levanta o requiere la red compartida y la infraestructura base.

### 1. Crear la red compartida (si aún no existe)
docker network create red-compartida
2. Levantar el microservicio con Docker Compose
docker compose up -d --build
Integración de Mensajería: RabbitMQ y Kafka
1. RabbitMQ (Flujo de Comandos y Eventos síncronos/asíncronos)
Canal de Comandos de Stock (Direct Exchange):

Publica en: catalog.direct.exchange con Routing Key catalog.stock.cmd.key.

Propósito: Solicita a ms-catalog la reserva/descuento de inventario tras crear un pedido.

Canal de Respuestas de Stock:

Escucha en: catalog.stock.updated.queue.

Propósito: Recibe la confirmación o actualización de stock desde ms-catalog para actualizar el estado del pedido.

Publicación de Eventos de Dominio (Topic Exchange):

Publica en: pedidos360.events.topic con claves como order.created.

Propósito: Emite eventos globales para que servicios suscriptores (como ms-audit) registren la actividad.

2. Apache Kafka (Streaming e Histórico de Eventos)
Propósito: Se utiliza para la ingesta y procesamiento de flujos de eventos a gran escala (Event Streaming) en tiempo real.

Comportamiento: Mientras RabbitMQ gestiona la mensajería basada en cola/comandos orientada a acciones directas (stock y eventos de estado), Kafka registra la secuencia inmutable de eventos de órdenes para analítica en tiempo real y procesamiento masivo de datos.

Comandos de Monitoreo
# Ver logs en tiempo real
docker compose logs -f
