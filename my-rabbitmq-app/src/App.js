// src/App.js
import React, { useState } from 'react';
import './App.css'; // 可选，若没有可删除此行

function App() {
  const [message, setMessage] = useState('');
  const [response, setResponse] = useState('');

  // 发送消息到 Node.js 后端 (通过 HTTP)
  const sendMessage = async () => {
    try {
      let payload;

      // 尝试将输入解析为 JSON；如果失败则作为字符串发送
      try {
        payload = JSON.parse(message);
      } catch {
        payload = { message };  // 包装成 JSON 对象，后端更好处理
      }

      const res = await fetch('http://localhost:3001/api/send', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      const data = await res.json();
      setResponse(JSON.stringify(data));
      setMessage('');
    } catch (error) {
      console.error('Error sending message:', error);
      setResponse('Failed to send message');
    }
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>Send Message to RabbitMQ (via HTTP)</h1>

        <input
          type="text"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          placeholder='Enter message or JSON payload'
          style={{ padding: '10px', margin: '10px', width: '300px' }}
        />
        <button onClick={sendMessage} style={{ padding: '10px 20px', cursor: 'pointer' }}>
          Send
        </button>

        <p>Backend Response: {response}</p>
      </header>
    </div>
  );
}

export default App;
