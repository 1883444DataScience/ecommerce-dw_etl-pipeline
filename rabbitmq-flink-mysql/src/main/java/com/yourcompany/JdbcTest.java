import java.sql.*;

public class JdbcTest {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://127.0.0.1:3306/ecommerce?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "your_password";

        Connection conn = DriverManager.getConnection(url, user, password);
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO orders (order_id, user_id, product_id, quantity, unit_price, total_amount, order_status, create_time, update_time) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())"
        );
        ps.setString(1, "order_test");
        ps.setString(2, "user_test");
        ps.setString(3, "product_test");
        ps.setInt(4, 1);
        ps.setBigDecimal(5, new java.math.BigDecimal("10.00"));
        ps.setBigDecimal(6, new java.math.BigDecimal("10.00"));
        ps.setString(7, "NEW");

        int rows = ps.executeUpdate();
        System.out.println("Inserted rows: " + rows);

        ps.close();
        conn.close();
    }
}
