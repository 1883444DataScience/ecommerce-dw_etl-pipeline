const express = require('express');
const bodyParser = require('body-parser');
const amqp = require('amqplib');

const app = express();
const PORT = 3001;

app.use(bodyParser.json());

const RABBITMQ_URL = 'amqp://localhost';
const ORDER_QUEUE = 'order_queue';
const MY_QUEUE = 'my_queue';

let channel;

async function connectRabbitMQ() {
  const connection = await amqp.connect(RABBITMQ_URL);
  channel = await connection.createChannel();
  await channel.assertQueue(ORDER_QUEUE, { durable: true });
  await channel.assertQueue(MY_QUEUE, { durable: true });
}

connectRabbitMQ().catch(console.error);

app.post('/api/send', async (req, res) => {
  const data = req.body;

  if (!data) {
    return res.status(400).json({ error: 'Empty request body' });
  }

  try {
    if (
      typeof data === 'object' &&
      data.orderId && data.userId && data.productId &&
      data.quantity && data.unitPrice
    ) {
      // 订单格式，发送到 order_queue
      const payload = JSON.stringify(data);
      channel.sendToQueue(ORDER_QUEUE, Buffer.from(payload), { persistent: true });
      console.log('Sent order to order_queue:', payload);
      return res.json({ status: 'order sent' });
    } else if (typeof data === 'string') {
      // 普通字符串消息，发送到 my_queue
      channel.sendToQueue(MY_QUEUE, Buffer.from(data), { persistent: true });
      console.log('Sent message to my_queue:', data);
      return res.json({ status: 'message sent' });
    } else {
      // 其他类型数据也可以转为字符串处理
      const payload = JSON.stringify(data);
      channel.sendToQueue(MY_QUEUE, Buffer.from(payload), { persistent: true });
      console.log('Sent JSON message to my_queue:', payload);
      return res.json({ status: 'message sent as JSON' });
    }
  } catch (err) {
    console.error('Error sending to queue:', err);
    return res.status(500).json({ error: 'Failed to send message' });
  }
});

app.listen(PORT, () => {
  console.log(`Server listening on port ${PORT}`);
});
