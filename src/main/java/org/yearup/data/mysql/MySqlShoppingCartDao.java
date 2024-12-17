package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {

    @Autowired
    public MySqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);  // Calls the constructor of MySqlDaoBase with DataSource
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart cart = new ShoppingCart();
        cart.setUserId(userId);

        String query = """
                SELECT sc.product_id, sc.quantity,
                p.name, p.price
                FROM shopping_cart sc
                JOIN products p ON sc.product_id = p.product_id
                WHERE sc.user_id = ?
                """;
        try (Connection connection = getConnection()) {  // Using the getConnection method from MySqlDaoBase
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();

            Map<Integer, ShoppingCartItem> items = new HashMap<>();
            while (rs.next()) {
                // Retrieve product details.
                int productId = rs.getInt("product_id");
                String productName = rs.getString("name");
                java.math.BigDecimal productPrice = rs.getBigDecimal("price");
                int quantity = rs.getInt("quantity");

                // Create Product object
                Product product = new Product();
                product.setProductId(productId);
                product.setName(productName);
                product.setPrice(productPrice);

                // Create ShoppingCartItem object
                ShoppingCartItem item = new ShoppingCartItem();
                item.setProduct(product);
                item.setQuantity(quantity);

                // Add the item to the cart
                items.put(productId, item);
            }
            cart.setItems(items);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cart;
    }

    @Override
    public void saveOrUpdate(ShoppingCart shoppingCart) {
        String deleteQuery = "DELETE FROM shopping_cart WHERE user_id = ?";
        String insertQuery = """
                INSERT INTO shopping_cart (user_id, product_id, quantity)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = getConnection()) {  // Using the getConnection method from MySqlDaoBase
            // Clearing existing cart items for the user:
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
                deleteStatement.setInt(1, shoppingCart.getUserId());
                deleteStatement.executeUpdate();
            }

            // Insert updated items
            try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                for (ShoppingCartItem item : shoppingCart.getItems().values()) {
                    insertStatement.setInt(1, shoppingCart.getUserId());
                    insertStatement.setInt(2, item.getProductId());
                    insertStatement.setInt(3, item.getQuantity());
                    insertStatement.addBatch();
                }
                insertStatement.executeBatch();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeItem(int userId, int productId) {
        String query = "DELETE FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        try (Connection connection = getConnection()) {  // Using the getConnection method from MySqlDaoBase
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, productId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearCart(int userId) {
        String query = "DELETE FROM shopping_cart WHERE user_id = ?";
        try (Connection connection = getConnection()) {  // Using the getConnection method from MySqlDaoBase
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
