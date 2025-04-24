package labs.pm.app;

import labs.data.Drink;
import labs.data.Food;
import labs.data.Product;
import labs.data.Rating;

import java.beans.beancontext.BeanContext;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
* {@code Shop} this is an application that manage products
 * @version 0.1.0
 * @author aymane
* */
public class Shop {
    public static void main(String[] args) {
        Product p1 = new Food(2, "coffee", BigDecimal.valueOf(200), Rating.FOUR_STAR, LocalDate.now().plusDays(1));
        Product adjusted_p1 = p1.applyRating(Rating.FIVE_STAR);
        System.out.println(adjusted_p1);

        p1 = new Food(3, "burger", BigDecimal.valueOf(2), Rating.ONE_STAR, LocalDate.now().plusDays(2));
        System.out.println(p1);

        Product p6 = new Drink(104, "chocolate", BigDecimal.valueOf(2.99), Rating.FIVE_STAR);
        Product p7 = new Food(104, "chocolate", BigDecimal.valueOf(2.99), Rating.FIVE_STAR, LocalDate.now().plusDays(2));
        System.out.println(p6 = p7);

    }
}
