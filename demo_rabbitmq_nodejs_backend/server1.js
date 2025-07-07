// index.js
const express = require('express');
const amqp = require('amqplib');
const cors = require('cors');
const WebSocket = require('ws'); // 引入 WebSocket 库

const app = express();
const HTTP_PORT = 3001; // 后端 HTTP 服务端口
const WS_PORT = 8080; // WebSocket 服务端口 (通常与 HTTP 端口不同，或在同一端口上多路复用)

app.use(express.json());
app.use(cors());

let channel; // RabbitMQ 通道
const clients = new Set(); // 存储所有连接的 WebSocket 客户端

// --- RabbitMQ 连接和消息处理 ---
async function connectRabbitMQ() {
    try {
        // 请替换为你的 RabbitMQ 连接字符串，如果设置了密码，请包含用户名和密码
        const connection = await amqp.connect('amqp://guest:guest@localhost:5672');
        channel = await connection.createChannel();
        await channel.assertQueue('my_queue'); // 声明要监听的队列
        console.log('Connected to RabbitMQ and asserting queue: flink-input-queue');

        // 开始消费 RabbitMQ 队列中的消息
        channel.consume('flink-input-queue', (msg) => {
            if (msg !== null) {
                const messageContent = msg.content.toString();
                console.log(`Received message from RabbitMQ: "${messageContent}"`);

                // 将接收到的消息通过 WebSocket 推送给所有连接的前端客户端
                clients.forEach(client => {
                    if (client.readyState === WebSocket.OPEN) {
                        client.send(JSON.stringify({ type: 'rabbitmqMessage', content: messageContent }));
                    }
                });

                channel.ack(msg); // 确认消息已处理
            }
        });

    } catch (error) {
        console.error('Failed to connect to RabbitMQ:', error.message);
        // 如果连接失败，5秒后重试
        setTimeout(connectRabbitMQ, 5000);
    }
}

// 后端启动时连接 RabbitMQ
connectRabbitMQ();

// --- HTTP API 路由 (用于 React 发送消息到 RabbitMQ) ---
app.post('/send-message', async (req, res) => {
    const { message } = req.body;
    if (!message) {
        return res.status(400).send('Message is required');
    }

    try {
        if (!channel) {
            return res.status(500).send('RabbitMQ channel not established yet.');
        }
        // 将消息发送到 RabbitMQ 队列
        channel.sendToQueue('flink-input-queue', Buffer.from(message));
        console.log(`Sent message to RabbitMQ: "${message}"`);
        res.status(200).send('Message sent to RabbitMQ');
    } catch (error) {
        console.error('Error sending message to RabbitMQ:', error.message);
        res.status(500).send('Failed to send message');
    }
});

// 启动 HTTP 服务器
app.listen(HTTP_PORT, () => {
    console.log(`Node.js HTTP backend running on http://localhost:${HTTP_PORT}`);
});


// --- WebSocket 服务器 ---
const wss = new WebSocket.Server({ port: WS_PORT });

wss.on('connection', ws => {
    console.log('New WebSocket client connected!');
    clients.add(ws); // 将新连接的客户端添加到 Set 中

    ws.on('message', message => {
        // 如果前端需要发送消息给后端 (例如心跳包或指令)，可以在这里处理
        console.log(`Received message from WebSocket client: ${message}`);
        // 示例：客户端发送 {"type": "ping"}，服务器回应 {"type": "pong"}
        try {
            const parsedMessage = JSON.parse(message);
            if (parsedMessage.type === 'ping') {
                ws.send(JSON.stringify({ type: 'pong' }));
            }
        } catch (e) {
            console.error('Failed to parse WebSocket message:', e);
        }
    });

    ws.on('close', () => {
        console.log('WebSocket client disconnected.');
        clients.delete(ws); // 从 Set 中移除断开的客户端
    });

    ws.on('error', error => {
        console.error('WebSocket error:', error);
    });
});

console.log(`WebSocket server running on ws://localhost:${WS_PORT}`);
