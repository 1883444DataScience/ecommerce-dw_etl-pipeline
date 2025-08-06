const express = require('express');
const bodyParser = require('body-parser');
const amqp = require('amqplib');

const app = express();
const PORT = 3001;      // HTTP 接口端口
const WS_PORT = 8080;   // WebSocket 服务端口

// 解析JSON请求体
app.use(bodyParser.json());

// 保存所有 WebSocket 连接
const clients = new Set();

// 创建 WebSocket 服务器
const wss = new WebSocket.Server({ port: WS_PORT });
wss.on('connection', (ws) => {
  console.log('WebSocket client connected');
  clients.add(ws);

  ws.on('close', () => {
    console.log('WebSocket client disconnected');
    clients.delete(ws);
  });

  ws.on('message', (message) => {
    console.log('Received message from client:', message);
    // 这里可以根据需要处理客户端发来的消息
  });
});

const QUEUE = 'order_queue';
const RABBITMQ_URL = 'amqp://localhost'; // 视情况换成远程地址

let channel = null;

// 建立 RabbitMQ 连接
async function setup() {
    const connection = await amqp.connect(RABBITMQ_URL);
    channel = await connection.createChannel();
    await channel.assertQueue(QUEUE);
    console.log(`Connected to RabbitMQ, using queue "${QUEUE}"`);
}
setup();

app.post('/api/send-message', async (req, res) => {
    const order = req.body;
    // 先校验请求体合法性，不合法就返回400。
    if (!order || !order.orderId) {
        console.error('Failed to send message: invalid order payload');
        return res.status(400).json({ error: 'Invalid order payload' });
    }

    try {
	// 解析json
        const payload = JSON.stringify(order);
        channel.sendToQueue(QUEUE, Buffer.from(payload));
        console.log('Sent order:', payload);
        res.json({ status: 'ok' });
    } catch (err) {
        console.error('Failed to send message', err);
        res.status(500).json({ error: 'Failed to send message' });
    }
});


// 启动服务
app.listen(3001, () => {
    console.log('Server is running on http://localhost:3001');
});
