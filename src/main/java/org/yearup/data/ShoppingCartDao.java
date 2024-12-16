package org.yearup.data;

import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao {
    ShoppingCart getByUserId(int userId);
    // adding save, remove, clear
    void saveOrUpdate(ShoppingCart shoppingCart);

    void removeItem(int userId, int productId);

    void clearCart(int userId);
}
