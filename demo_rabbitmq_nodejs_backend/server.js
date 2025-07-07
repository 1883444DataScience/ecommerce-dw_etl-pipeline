// server.js
const express = require('express');
const amqp = require('amqplib');

const app = express();
app.use(express.json());

const RABBITMQ_URL = process.env.RABBITMQ_URL || 'amqp://guest:guest@localhost:5672/';
const QUEUE_NAME = 'flink-input-queue';
// const QUEUE_NAME = process.env.QUEUE_NAME || 'flink-input-queue';

let connection;
let channel;

// 初始化 RabbitMQ 连接和频道（复用）
async function initRabbitMQ() {
  connection = await amqp.connect(RABBITMQ_URL);
  channel = await connection.createChannel();
  await channel.assertQueue(QUEUE_NAME, { durable: true });
  console.log('RabbitMQ connected and channel created.');
}

// 发送消息到 RabbitMQ 队列
async function sendToRabbitMQ(msg) {
  if (!channel) {
    throw new Error('RabbitMQ channel is not initialized');
  }
  channel.sendToQueue(QUEUE_NAME, Buffer.from(msg), { persistent: true });
  console.log(`Sent message to queue ${QUEUE_NAME}: ${msg}`);
}

// HTTP POST 接口：前端调用此接口发送消息
app.post('/send', async (req, res) => {
  const { message } = req.body;
  if (!message) {
    return res.status(400).json({ error: 'Missing "message" in request body' });
  }

  try {
    await sendToRabbitMQ(message);
    res.json({ status: 'success', message: 'Message sent to RabbitMQ' });
  } catch (err) {
    console.error('Error sending message to RabbitMQ:', err);
    res.status(500).json({ status: 'error', message: 'Failed to send message' });
  }
});

// 启动服务器和初始化 RabbitMQ
const PORT = process.env.PORT || 3000;
app.listen(PORT, async () => {
  try {
    await initRabbitMQ();
    console.log(`Server listening on port ${PORT}`);
  } catch (err) {
    console.error('Failed to initialize RabbitMQ:', err);
    process.exit(1);
  }
});
