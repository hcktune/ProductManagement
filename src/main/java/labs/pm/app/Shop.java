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
        ProductManager pm = new ProductManager(Locale.UK);
        Product p1 = pm.createProductFood(1, "coffee", BigDecimal.ZERO, Rating.ONE_STAR, LocalDate.now());
        p1 = pm.reviewProduct(1, Rating.THREE_STAR, "Good");
        p1 = pm.reviewProduct(p1, Rating.FOUR_STAR, "Nice");
        p1 = pm.reviewProduct(p1, Rating.FIVE_STAR, "Excellent!");
        p1 = pm.reviewProduct(1, Rating.THREE_STAR, "lets goo jemi");
        p1 = pm.reviewProduct(p1, Rating.FOUR_STAR, "mmmm not good");
        p1 = pm.reviewProduct(p1, Rating.FOUR_STAR, "lets goo ami");
        p1 = pm.reviewProduct(p1, Rating.FOUR_STAR, "Niiice bro this is good");
        p1 = pm.reviewProduct(p1, Rating.ONE_STAR, "yeee");
        pm.printProductReport(p1);



    }
}
