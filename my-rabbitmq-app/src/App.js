// src/App.js
import React, { useState, useEffect, useRef } from 'react';
import './App.css'; // 确保 App.css 存在，或者删除此行

function App() {
  const [message, setMessage] = useState('');
  const [response, setResponse] = useState('');
  const [receivedMessages, setReceivedMessages] = useState([]); // 用于存储从 WebSocket 接收到的消息
  const ws = useRef(null); // 使用 useRef 来保存 WebSocket 实例

  // 组件挂载时建立 WebSocket 连接
  useEffect(() => {
    // 建立 WebSocket 连接
    ws.current = new WebSocket('ws://localhost:8080'); // 指向你的 WebSocket 后端端口

    ws.current.onopen = () => {
      console.log('WebSocket connected');
      // 可以发送一个初始化消息，例如 ping
      ws.current.send(JSON.stringify({ type: 'ping' }));
    };

    ws.current.onmessage = (event) => {
      const data = JSON.parse(event.data);
      console.log('Received from WebSocket:', data);

      if (data.type === 'rabbitmqMessage') {
        setReceivedMessages(prevMessages => [...prevMessages, data.content]); // 将 RabbitMQ 消息添加到列表
      } else if (data.type === 'pong') {
          console.log('Pong received from server');
      }
      // 可以根据 data.type 处理不同类型的消息
    };

    ws.current.onclose = () => {
      console.log('WebSocket disconnected');
      // 可以在这里实现重连逻辑
    };

    ws.current.onerror = (error) => {
      console.error('WebSocket error:', error);
    };

    // 组件卸载时关闭 WebSocket 连接
    return () => {
      if (ws.current) {
        ws.current.close();
      }
    };
  }, []); // 空依赖数组表示只在组件挂载和卸载时运行

  // 发送消息到 Node.js 后端 (通过 HTTP)
  const sendMessage = async () => {
    try {
      const res = await fetch('http://localhost:3001/send-message', { // 指向你的 Node.js HTTP 后端, 后端端口3001
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ message }),
      });
      const data = await res.text();
      setResponse(data);
      setMessage(''); // 清空输入框
    } catch (error) {
      console.error('Error sending message:', error);
      setResponse('Failed to send message');
    }
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>React to RabbitMQ with WebSocket Test</h1>

        {/* 发送消息部分 */}
        <div style={{ marginBottom: '20px' }}>
	          <h2>Send Message to RabbitMQ (via HTTP POST)</h2>
          <input
            type="text"
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            placeholder="Enter your message"
            style={{ padding: '10px', margin: '10px', width: '300px' }}
          />
          <button onClick={sendMessage} style={{ padding: '10px 20px', cursor: 'pointer' }}>
            Send Message
          </button>
          <p>HTTP Backend Response: {response}</p>
        </div>

        <hr style={{ width: '80%' }} />

        {/* 接收消息部分 */}
        <div>
          <h2>Received Messages (via WebSocket)</h2>
          <div style={{ border: '1px solid #ccc', padding: '10px', minHeight: '100px', width: '400px', overflowY: 'auto' }}>
            {receivedMessages.length === 0 ? (
              <p>No messages received yet...</p>
            ) : (
              <ul>
                {receivedMessages.map((msg, index) => (
                  <li key={index}>{msg}</li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </header>
    </div>
  );
}

export default App;
