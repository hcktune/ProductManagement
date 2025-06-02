package labs.data;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProductManager {
    private Map<Product, List<Review>> products = new HashMap<Product, List<Review>>();
    private static final Logger logger = Logger.getLogger(ProductManager.class.getName());
    private RessourceFormatter formater;
    private ResourceBundle conf = ResourceBundle.getBundle("conf");

    private Path reportFolder = Path.of(conf.getString("reports.folder"));
    private Path dataFolder = Path.of(conf.getString("data.folder"));
    private Path tmpFolder = Path.of(conf.getString("tmp.folder"));

    private MessageFormat reviewFormat = new MessageFormat(conf.getString("review.data.format"));
    private MessageFormat productFormat = new MessageFormat(conf.getString("product.data.format"));

    public Review parseReview(String text){
        Review review = null;
        try {
            Object[] values = reviewFormat.parse(text);
//            reviewProduct(Integer.parseInt((String) values[0]),
//                        Rateable.convert(Integer.parseInt((String) values[1])),
//                                (String) values[2]);
            review = new Review(Rateable.convert(Integer.parseInt((String) values[0])), (String) values[1]);
        } catch (ParseException  | NumberFormatException e )  {
            logger.log(Level.WARNING, "Error parsing review" + text, e.getMessage());
        }
        return review;
    }
    private void loadAllData(){
        try {
            products = Files.list(dataFolder)
                    .filter(file -> file.getFileName().toString().startsWith("product_"))
                    .map(file -> loadProudct(file))
                    .filter(product -> product != null)
                    .collect(Collectors.toMap(product -> product,
                            product -> {
                                try {
                                    return loadReviews(product);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private Product loadProudct(Path file){
        Product p = null;
        try {
            p = parseProduct(Files.lines(dataFolder.resolve(file), Charset.defaultCharset()).findFirst().orElseThrow());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return p;
    }

    private List<Review> loadReviews(Product product) throws IOException {
        List<Review> reviews = null;
        Path f = dataFolder.resolve(MessageFormat.format(conf.getString("reviews.data.file"), product.getId()));

        if (Files.notExists(f)){
            reviews = new ArrayList<>();
        } else{
            reviews = Files.lines(f, Charset.defaultCharset())
                    .map(text -> parseReview(text))
                    .filter(review -> review != null)
                    .collect(Collectors.toList());
        }
        return  reviews;
    }
    public Product parseProduct(String product){
        Product p = null;
        try{
            Object[] values = productFormat.parse(product);

            int id = Integer.parseInt( (String) values[1]);
            String name = (String) values[2];
            BigDecimal price = BigDecimal.valueOf(Double.parseDouble( (String) values[3]));
            Rating rating = Rateable.convert(Integer.parseInt((String) values[4]));
            LocalDate l = LocalDate.parse((String) values[5]);
            switch ((String) values[0]){
                case "D":
                    p = createProductDrink(id, name, price, rating, l);
                    break;
                case "F":
                    p = createProductFood(id, name, price, rating, l);
                    break;
            }
        } catch (ParseException  | NumberFormatException e) {
            logger.log(Level.WARNING, "Error parsing Product", e.getMessage());
        }
        return p;
    }
    public void changeLocale(String langTag){
        formater = formatters.getOrDefault(langTag, formatters.get("us_US"));
    }

    public static Set<String> getSupportedLocales(){
        return formatters.keySet();
    }

    private static final Map<String, RessourceFormatter> formatters =
            Map.of("en-GB", new RessourceFormatter(Locale.UK),
                   "en-US", new RessourceFormatter(Locale.US),
                   "fr-FR", new RessourceFormatter(Locale.FRANCE),
                   "ru-RU", new RessourceFormatter(Locale.of("ru", "RU")),
                    "us-US", new RessourceFormatter(Locale.US));

        private static class RessourceFormatter {
            private Locale locale;
            private ResourceBundle conf;
            private NumberFormat moneyFormat;
            private DateTimeFormatter dateformat;

            public RessourceFormatter(Locale locale){
                this.locale = locale;
                conf = ResourceBundle.getBundle("conf", locale);
                dateformat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
                moneyFormat = NumberFormat.getCurrencyInstance(locale);
            }

            private String formatProduct(Product product){
                String type = switch(product){
                    case Food food -> conf.getString("food");
                    case Drink drink -> conf.getString("drink");
                };

                return MessageFormat.format(conf.getString("product"),
                        product.getName(),
                        moneyFormat.format(product.getPrice()),
                        product.getRating().getStars(),
                        dateformat.format(product.getBestBefore()),
                        type

                        );
            }

            private String formatReview(Review review){
                return MessageFormat.format(conf.getString("review"),
                        review.rating().getStars(),
                        review.comments()
                        );
            }

            private String getText(String key){
                return conf.getString(key);
            }

        }


    public Product createProductFood(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product food =  new Food(id, name, price, rating, bestBefore);
        products.putIfAbsent(food, new ArrayList<Review>());
        return food;
    }


    public ProductManager(String languageTag) {
            changeLocale(languageTag);
            loadAllData();
    }

    public ProductManager(Locale locale){
            this(locale.toLanguageTag());
    }

    public Product createProductDrink(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product drink = new Drink(id, name, price, rating, bestBefore);
        products.putIfAbsent(drink, new ArrayList<Review>());
        return drink;
    }

    public Product reviewProduct(Product product, Rating rating, String comments) throws ProductManagerException {
        //check if not full -> create a copy and increase the size

        List<Review> reviews = products.get(product);
        products.remove(product, reviews);

        reviews.add(new Review(rating, comments));
//        int sum = 0;
//
//        for (Review review : reviews) {
//            sum += review.rating().ordinal();
//        }
//
//        product = product.applyRating(Rateable.convert(Math.round( (float) sum / reviews.size())));

        product = product.applyRating(
                Rateable.convert(
                        (int) Math.round(
                                reviews.stream()
                                        .mapToInt((r) -> r.rating().ordinal())
                                        .average()
                                        .orElse(0)
                        )
                )
        );

        products.put(product, reviews);
        return product;
    }


    public void printProductReport(Product product) throws IOException{
        List<Review> reviews = products.get(product);
        Collections.sort(reviews);

        Path productFile = reportFolder.resolve(MessageFormat.format(conf.getString("reports.file"),  product.getId()));

        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(productFile, StandardOpenOption.CREATE), StandardCharsets.UTF_8))) {
            out.append(formater.formatProduct(product) + System.lineSeparator());
            if (reviews.isEmpty()){
                out.append(formater.getText("no.reviews"));
            }else {
                out.append(reviews.stream()
                        .map(r -> formater.formatReview(r) + System.lineSeparator())
                        .collect(Collectors.joining()));
            }

//        for (Review review : reviews){
//            txt.append(formater.formatReview(review));
//            txt.append("\n");
//
//
//        }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error printing product report : " + e.getMessage(), e);
        }

    }

    public Product findProduct(int id) throws ProductManagerException {
//        Product result = null;
//        for (Product product : products.keySet()){
//            if (product.getId() == id) {
//                result = product;
//                break;
//            }
//        }
//        return result;
        return products.keySet()
                .stream()
                .filter((product -> product.getId() == id))
                .findFirst()
                .orElseThrow( () -> new ProductManagerException("Product with the id" + id + "not found"));
    }



    public Product reviewProduct(int id, Rating rating, String comments) {
        try{
            return reviewProduct(findProduct(id), rating, comments);
        }
        catch (ProductManagerException e){
            logger.log(Level.INFO, e.getMessage());
        }
        return null;
    }

    public void printProductReport(int id) {
        try{
            printProductReport(findProduct(id));
        }
        catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error printing product report");
        }

    }

    public void printProducts(Predicate<Product> filter, Comparator<Product> sorter){
            StringBuilder txt = new StringBuilder();
//        List<Product> productsList = new ArrayList<>(products.keySet());
//        productsList.sort(sorter);
//        StringBuilder txt = new StringBuilder();
//
//        for (Product product : productsList){
//            txt.append(formater.formatProduct(product));
//            txt.append("\n");
//        }
//        System.out.println(txt);
        products.keySet().stream().sorted(sorter).filter(filter).forEach(product -> txt.append(formater.formatProduct(product) + '\n'));
        System.out.println(txt);
    }

    public Map<String, String> getDiscounts(){
            return products.keySet()
                    .stream()
                    .collect(
                            Collectors.groupingBy(product -> product.getRating().getStars(),
                                    Collectors.collectingAndThen(
                                            Collectors.summingDouble(prodcut -> prodcut.getDiscount().doubleValue()),
                                                    discount -> formater.moneyFormat.format(discount)
                                    ))
                    );
    }
}
