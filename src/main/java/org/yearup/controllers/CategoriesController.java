package org.yearup.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.yearup.data.CategoryDao;
import org.yearup.data.ProductDao;
import org.yearup.models.Category;
import org.yearup.models.Product;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.yearup.models.Profile;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.authentication.LoginDto;
import org.yearup.models.authentication.LoginResponseDto;
import org.yearup.models.authentication.RegisterUserDto;
import org.yearup.models.User;
import org.yearup.security.jwt.JWTFilter;
import org.yearup.security.jwt.TokenProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin
@PreAuthorize("permitAll()")
@RequestMapping("/categories")

public class CategoriesController {

    private final CategoryDao categoryDao;
    private final ProductDao productDao;

    // Constructor-based Dependency Injection
    @Autowired
    public CategoriesController(CategoryDao categoryDao, ProductDao productDao) {
        this.categoryDao = categoryDao;
        this.productDao = productDao;
    }

    // GET method to retrieve all categories
    @GetMapping
    public List<Category> getAll() {
        // Retrieve and return all categories
        return categoryDao.getAllCategories();
    }

    //Added mapping to ensure user finds it through localhost../id
    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable int id) {
        Category category = categoryDao.getById(id);
        if (category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // get the category by id
        return ResponseEntity.ok(category);
    }

    // the url to return all products in category 1 would look like this ✔
    // https://localhost:8080/categories/1/products
    @GetMapping("/{categoryId}/products")
    public ResponseEntity<List<Product>> getProductsByCategoryId(@PathVariable int categoryId) {
        // get a list of product by categoryId
        List<Product> products = productDao.listByCategoryId(categoryId);
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); //404 if no products
        }
        return ResponseEntity.ok(products); //200 OK and list
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") //Only an admin can create categories
    public ResponseEntity<Category> addCategory(@RequestBody Category category)
    {
        // insert the category
        Category savedCategory = categoryDao.create(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory); //Returns 201 if created :D
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") //Only an admin can update a category
    public ResponseEntity<Category> updateCategory(@PathVariable int id, @RequestBody Category category) {
        //Authorizing if category already exists:
        Category existingCategory = categoryDao.getById(id);
        if (existingCategory == null) {
            //if it doesn't exist, return 404
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        // update the category by id
        category.setCategoryId(id); //Make sure the category ID stays the same
        Category updatedCategory = categoryDao.update(id, category);

        //Return updated category with status 200 (OK)
        return ResponseEntity.ok(updatedCategory);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") //Only admin can DELETE
    public ResponseEntity<Void> deleteCategory(@PathVariable int id) {
       //Verify category exists:
        Category existingCategory = categoryDao.getById(id);

        if (existingCategory == null) {
            //if it does not exist, error:
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        //delete category by ID
        categoryDao.delete(id);

        //Return 204 No Content status to show category was deleted successfully
        return ResponseEntity.noContent().build();
    }
}
