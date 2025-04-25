package labs.pm.app;

import labs.data.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

/**
* {@code Shop} this is an application that manage products
 * @version 0.1.0
 * @author aymane
* */
public class Shop {
    public static void main(String[] args) {
        ProductManager pm = new ProductManager(Locale.getDefault());
        Product coffee = pm.createProduct(0, "coffee", BigDecimal.valueOf(200), Rating.NOT_RATED, LocalDate.now().plusDays(1));
        pm.reviewProduct(coffee, Rating.TWO_STAR, "this coffe is good");
//
//        Product p6 = pm.createProduct(3, "burger", BigDecimal.valueOf(2), Rating.ONE_STAR, LocalDate.now().plusDays(2));
//        System.out.println(p6.applyRating(Rating.TWO_STAR));
//        System.out.println(p6);
//        pm.createProduct(104, "chocolate", BigDecimal.valueOf(2.99), Rating.FIVE_STAR);
//        pm.createProduct(104, "chocolate", BigDecimal.valueOf(2.99), Rating.FIVE_STAR, LocalDate.now().plusDays(2));
        pm.printProductReport();

    }
}
