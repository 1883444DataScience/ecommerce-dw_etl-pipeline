// react_Nodejs.js 改为 ReactNodejs.js 或 React_Nodejs.js 都行，重要是首字母大写
import React from 'react';

function React_Nodejs() {
  const sendOrder = () => {
    fetch("http://localhost:3001/api/sendOrder", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        orderId: "009",
        userId: "u2",
        productId: "p2",
        quantity: 1,
        unitPrice: "29.99"
      })
    })
      .then(res => res.json())
      .then(data => console.log("Order sent:", data))
      .catch(err => console.error("Error:", err));
  };

  return (
    <div style={{ padding: '2rem' }}>
      <h1>Send Order to RabbitMQ</h1>
      <button onClick={sendOrder}>Send Test Order</button>
    </div>
  );
}

export default React_Nodejs;
