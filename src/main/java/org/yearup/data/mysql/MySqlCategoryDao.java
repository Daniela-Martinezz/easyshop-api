package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;
import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;


@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao {

    public MySqlCategoryDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<Category> getAllCategories() {
        String sql = "SELECT * FROM categories";
        List<Category> categories = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                categories.add(mapRow(resultSet)); //List of categories from SELECT...
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions appropriately
            throw new RuntimeException("Error fetching categories", e);
        }
        return categories;
    }

    @Override
    public Category getById(int categoryId) {
        // get category by id
        String sql = "SELECT * FROM categories WHERE category_id = ?";
        try(Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            try(ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                   return mapRow(resultSet);
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving category by ID", e);
        }
        return null;
    }

    @Override
    public Category create(Category category) {
        // create a new category
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setCategoryId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating category", e);
        }

            return category;
    }

    @Override
    public Category update(int categoryId, Category category) {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE category_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3, categoryId);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating category", e);
        }

        return category;
    }

    @Override
    public void delete(int categoryId) {
        String sql = "DELETE FROM categories WHERE category_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error deleting category", e);
        }
    }

    // mapRow for mapping or ResultSet to Category Object
    protected static Category mapRow(ResultSet row) throws SQLException {
        int categoryId = row.getInt("category_id"); //getting data from ResultSet (data retrieved from db query
        String name = row.getString("name");
        String description = row.getString("description");

        Category category = new Category();  //creating category object
        category.setCategoryId(categoryId);
        category.setName(name);
        category.setDescription(description);

        return category;
    }
}