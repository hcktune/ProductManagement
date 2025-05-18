package labs.data;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

public class ProductManager {
    private Locale locale;
    private ResourceBundle ressources;
    private NumberFormat moneyFormat;
    private Map<Product, List<Review>> products = new HashMap<Product, List<Review>>();
    private DateTimeFormatter dateformat;

    public Product createProductFood(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product food =  new Food(id, name, price, rating, bestBefore);
        products.putIfAbsent(food, new ArrayList<Review>());
        return food;
    }


    public ProductManager(Locale locale) {
        this.locale = locale;
        ressources = ResourceBundle.getBundle("resources", locale);
        dateformat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
        moneyFormat = NumberFormat.getCurrencyInstance(locale);
    }

    public Product createProductDrink(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product drink = new Drink(id, name, price, rating, bestBefore);
        products.putIfAbsent(drink, new ArrayList<Review>());
        return drink;
    }

    public Product reviewProduct(Product product, Rating rating, String comments) {
        //check if not full -> create a copy and increase the size

        List<Review> reviews = products.get(product);
        products.remove(product, reviews);

        reviews.add(new Review(rating, comments));
        int sum = 0;

        for (Review review : reviews) {
            sum += review.rating().ordinal();
        }

        product = product.applyRating(Rateable.convert(Math.round( (float) sum / reviews.size())));
        products.put(product, reviews);
        return product;
    }


    public void printProductReport(Product product){
        StringBuilder txt = new StringBuilder();

        List<Review> reviews = products.get(product);
        Collections.sort(reviews);
        String type = switch (product) {
            case Food food -> ressources.getString("food");
            case Drink drink -> ressources.getString("drink");

        };

        txt.append(MessageFormat.format(ressources.getString("product"),
                product.getName(),
                moneyFormat.format(product.getPrice()),

                product.getRating().getStars(),
                dateformat.format(product.getBestBefore()),
                type));
        txt.append("\n");


        for (Review review : reviews){



            txt.append(MessageFormat.format(ressources.getString("review"),
                    review.rating().getStars(),
                    review.comment(),
                    txt.append("\n")));


            if (reviews.isEmpty()){
                txt.append(ressources.getString("no.reviews"));
                txt.append("\n");
            }
        }
        System.out.println(txt);
    }

    public Product findProduct(int id){
        Product result = null;
        for (Product product : products.keySet()){
            if (product.getId() == id) {
                result = product;
                break;
            }
        }
        return result;
    }

    public Product reviewProduct(int id, Rating rating, String comments){
        return reviewProduct(findProduct(id), rating, comments);
    }

    public void printProductReport(int id){
        printProductReport(findProduct(id));
    }

}
