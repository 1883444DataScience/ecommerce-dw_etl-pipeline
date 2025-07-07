# Flink RabbitMQ Redis Demo

This project demonstrates a simple streaming pipeline:

1. Frontend: sends message to backend via HTTP.
2. Backend (Node.js): publishes messages to RabbitMQ.
3. Flink Job: consumes RabbitMQ messages, counts words, writes results to Redis.
4. Redis: stores word count as a hash `word_counts`.

## Run Instructions

### Backend
```bash
cd backend
npm install
node server.js
```

### Frontend
```bash
cd frontend
npm install
npm start
```

### flink-redis
```bash
cd backend/flink-job
mvn clean package
flink run target/FlinkRedisProcessor-shaded-1.0.jar
```

### RabbitMQ message test
```bash
rabbitmqadmin publish exchange=amq.default routing_key=flink-input-queue payload="hello world hello flink"
```

### Redis check
```bash
redis-cli HGETALL word_counts
```
