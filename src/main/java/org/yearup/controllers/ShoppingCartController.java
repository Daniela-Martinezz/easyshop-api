package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.security.Principal;

// only logged-in users should have access to these actions
@RestController
@RequestMapping("/cart")
public class ShoppingCartController {
    // a shopping cart requires
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
    private ProductDao productDao;

    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    // each method in this controller requires a Principal object as a parameter
    @GetMapping
    public ShoppingCart getCart(Principal principal) {
        try {
            // get the currently logged in username
            int userId = getUserId(principal);
            return shoppingCartDao.getByUserId(userId);
            // use the shoppingcartDao to get all items in the cart and return the cart
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // add a POST method to add a product to the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be added
    @PostMapping("/products/{productId}")
    public void addProductToCart(@PathVariable int productId, @RequestBody ShoppingCartItem item, Principal principal) {
        try {
            int userId = getUserId(principal);
            ShoppingCart cart = shoppingCartDao.getByUserId(userId);

            if (cart == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopping cart not found.");
            }

            Product product = productDao.getById(productId);
            //Check if product exists before adding to cart
            if (product == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
            }

            item.setProduct(product);
            cart.add(item);
            shoppingCartDao.saveOrUpdate(cart);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to add product to cart.");
        }
    }
    //PUT method to update quantity of a product in cart:
    @PutMapping("/products/{productId}")
    public void updateProductInCart(@PathVariable int productId, @RequestBody ShoppingCartItem item, Principal principal) {
        try {
          int userId = getUserId(principal);
            ShoppingCart cart = shoppingCartDao.getByUserId(userId);

            if (cart == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopping cart not found.");
            }

            if (cart.contains(productId)) {
                ShoppingCartItem existingItem = cart.get(productId);
                existingItem.setQuantity(item.getQuantity());
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
            }

            shoppingCartDao.saveOrUpdate(cart);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to update cart.");
        }
    }
    // Helper method to get the user ID from the principal (logged-in user)
    private int getUserId(Principal principal) {
        String username = principal.getName();
        return userDao.getIdByUsername(username); // retrieve user ID from the username
    }
}
