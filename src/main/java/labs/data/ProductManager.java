package labs.data;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class ProductManager {
    private Locale locale;
    private ResourceBundle resourcess;
    private DateTimeFormatter formatter;
    private NumberFormat moneyFormat;
    private Product product;
    private Review review;
    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        product =  new Food(id, name, price, rating, bestBefore);
        return product;
    }

    public ProductManager(Locale locale) {
        this.locale = locale;
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
        product =  new Drink(id, name, price, rating);
        return  product;
    }

    public Product reviewProduct(Product product, Rating rating, String comments) {
        review = new Review(rating, comments);
        this.product = product.applyRating(rating);
        return this.product;
    }

    public void printProductReport(){
        StringBuilder txt = new StringBuilder();
        txt.append(product);
        txt.append("\n");
        StringBuilder reviewCheck = review != null ? txt.append(review) : txt.append("Not reviewed\n");
        System.out.println(reviewCheck);
    }

}
