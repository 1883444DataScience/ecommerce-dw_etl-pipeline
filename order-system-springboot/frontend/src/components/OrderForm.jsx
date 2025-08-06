import React, { useState } from 'react';
import axios from 'axios';

function OrderForm() {
  const [form, setForm] = useState({
    orderId: '',
    userId: '',
    productId: '',
    quantity: 1,
    unitPrice: 0.0,
  });

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const totalAmount = form.quantity * form.unitPrice;

    const orderData = {
      ...form,
      totalAmount,
      orderStatus: 'PENDING',
    };

    try {
      await axios.post('http://localhost:8080/api/orders', orderData);
      alert('订单已提交');
    } catch (err) {
      alert('提交失败');
      console.error(err);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input name="orderId" placeholder="Order ID" onChange={handleChange} />
      <input name="userId" placeholder="User ID" onChange={handleChange} />
      <input name="productId" placeholder="Product ID" onChange={handleChange} />
      <input name="quantity" type="number" onChange={handleChange} />
      <input name="unitPrice" type="number" step="0.01" onChange={handleChange} />
      <button type="submit">提交订单</button>
    </form>
  );
}

export default OrderForm;
