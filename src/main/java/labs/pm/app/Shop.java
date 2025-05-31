package labs.pm.app;

import labs.data.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;

/**
* {@code Shop} this is an application that manage products
 * @version 0.1.0
 * @author aymane
* */
public class Shop {
    public static void main(String[] args) {

        ProductManager pm = new ProductManager("us-US");
        pm.parseProduct("F, 1, tea, 10.99, 0, 2021-08-02");
        pm.parseProduct("D, 2, cold orange, 1, 3, 2012-01-01");
        pm.parseReview("1, 2, good");


        pm.printProductReport(1);
//        pm.printProductReport(1);
//        pm.changeLocale("ru-RU");
//        pm.reviewProduct(2, Rating.FIVE_STAR, "Excellent!");
//        pm.reviewProduct(1, Rating.THREE_STAR, "lets goo jemi");
//        p1 = pm.reviewProduct(p1, Rating.FOUR_STAR, "mmmm not good");
//        p1 = pm.reviewProduct(p1, Rating.FOUR_STAR, "lets goo ami");
//        p1 = pm.reviewProduct(p1, Rating.FOUR_STAR, "Niiice bro this is good");
//        p1 = pm.reviewProduct(p1, Rating.ONE_STAR, "yeee");
//        pm.printProductReport(44);
//        Comparator<Product> ratingSorter = ((p1, p2) -> p1.getRating().ordinal() - p2.getRating().ordinal());
//        Comparator<Product> priceSorter = ((p1, p2) -> p1.getPrice().compareTo(p2.getPrice()));
//      pm.printProducts( ((o1, o2) -> o1.getRating().ordinal() - o2.getRating().ordinal()) );
//        pm.printProducts( ((o1, o2) -> o1.getPrice().compareTo(o2.getPrice()) ) );
//       pm.printProducts((p1) -> p1.getPrice().floatValue() < 2, (p1, p2) -> p1.getRating().ordinal() - p2.getRating().ordinal());
//        pm.printProducts(ratingSorter.thenComparing(priceSorter).reversed());

        pm.getDiscounts().forEach( (rating, discount) -> System.out.println(rating + "\t" + discount) );
    }
}
