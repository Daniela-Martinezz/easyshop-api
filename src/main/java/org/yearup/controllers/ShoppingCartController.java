package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@CrossOrigin

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

    private User getAuthenticatedUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found.");
        }
        String username = principal.getName();
        User user = userDao.getByUserName(username);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
        }
        return user;
    }

    // each method in this controller requires a Principal object as a parameter
    @GetMapping
    public ShoppingCart getCart(Principal principal) {
        User user = getAuthenticatedUser(principal);
        try {
            // get the currently logged in username
            return shoppingCartDao.getByUserId(user.getId());
            // use the shoppingcartDao to get all items in the cart and return the cart
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // add a POST method to add a product to the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be added
    @PostMapping("/products/{productId}")
    public ResponseEntity<Void> addProductToCart(@PathVariable int productId, @RequestBody ShoppingCartItem item, Principal principal) {
        User user = getAuthenticatedUser(principal);
        ShoppingCart cart = shoppingCartDao.getByUserId(user.getId());
        try {
            if (cart == null) {
                cart = new ShoppingCart();
                cart.setUserId(user.getId());
            }

            Product product = productDao.getById(productId);
            //Check if product exists before adding to cart
            if (product == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
            }

            item.setProduct(product);

            // add or update item in cart
            if (cart.contains(productId)) {
                ShoppingCartItem existingItem = cart.get(productId);
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
            } else {
                cart.add(item);
            }

            shoppingCartDao.saveOrUpdate(cart);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to add product to cart.");
        }
    }
    //PUT method to update quantity of a product in cart:
    @PutMapping("/products/{productId}")
    public void updateProductInCart(@PathVariable int productId, @RequestBody ShoppingCartItem item, Principal principal) {
      User user = getAuthenticatedUser(principal);
        try {
            ShoppingCart cart = shoppingCartDao.getByUserId(user.getId());

            if (cart == null || !cart.contains(productId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
            }

            ShoppingCartItem existingItem = cart.get(productId);
            existingItem.setQuantity(item.getQuantity());
            shoppingCartDao.saveOrUpdate(cart);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to update cart.");
        }
    }
    @DeleteMapping("/products/{productId}")
    public void removeProductFromCart(@PathVariable int productId, Principal principal) {
        User user = getAuthenticatedUser(principal);
        try {
            shoppingCartDao.removeItem(user.getId(), productId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to remove product from cart.");
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(Principal principal) {
        User user = getAuthenticatedUser(principal);
        try {
            shoppingCartDao.clearCart(user.getId());

            //204 no content
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to clear shopping cart.");
        }

    }
}
