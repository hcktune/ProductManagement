package labs.data;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ProductManager {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = lock.writeLock();
    private final Lock readLock = lock.readLock();
    private static final ProductManager pm = new ProductManager();
    private Map<Product, List<Review>> products = new HashMap<Product, List<Review>>();
    private static final Logger logger = Logger.getLogger(ProductManager.class.getName());
    //private RessourceFormatter formater;
    private final ResourceBundle conf = ResourceBundle.getBundle("conf");

    private final Path reportFolder = Path.of(conf.getString("reports.folder"));
    private final Path dataFolder = Path.of(conf.getString("data.folder"));
    private final Path tempFolder = Path.of(conf.getString("temp.folder"));

    private final MessageFormat reviewFormat = new MessageFormat(conf.getString("review.data.format"));
    private  final MessageFormat productFormat = new MessageFormat(conf.getString("product.data.format"));

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
    public static ProductManager getInstance(){
        return pm;
    }
    private void dumpData(){
        try {
            if (Files.notExists(tempFolder)){
                Files.createDirectory(tempFolder);
            }
            Path tempFile = tempFolder.resolve(MessageFormat.format(conf.getString("temp.file"),  "first"));
                try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(tempFile, StandardOpenOption.CREATE))){
                 out.writeObject(products);
                 products = new HashMap<>();
                }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error dumping data" + e.getMessage(), e);
        }
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
    public RessourceFormatter changeLocale(String langTag){
        return  formatters.getOrDefault(langTag, formatters.get("us_US"));
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
            Product food = null;
            try{
                writeLock.lock();
                food = new Food(id, name, price, rating, bestBefore);
                products.putIfAbsent(food, new ArrayList<Review>());
            }
            catch (Exception e){
                logger.log(Level.INFO, "Error adding foodProduct" + e.getMessage());
            }
            finally {
                writeLock.unlock();
            }

        return food;
    }


    private ProductManager() {
//            changeLocale(languageTag);
            loadAllData();
    }
    @SuppressWarnings("Unchecked")
    private void restoreData(){
            try{
            Path tempFile = Files.list(tempFolder)
                    .filter(path -> path.getFileName().toString().endsWith("tmp"))
                    .findFirst().orElseThrow();
                try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(tempFile, StandardOpenOption.DELETE_ON_CLOSE))){
                    products = (HashMap) in.readObject();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            catch (Exception e){
                logger.log(Level.SEVERE, "restoring data failed " + e.getMessage(), e);
            }
    }

//    public ProductManager(Locale locale){
//            this(locale.toLanguageTag());
//    }

    public Product createProductDrink(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
            Product drink = null;
            try{
                drink = new Drink(id, name, price, rating, bestBefore);
                products.putIfAbsent(drink, new ArrayList<Review>());
            }
            catch (Exception e){
                logger.log(Level.INFO, "Error creating Product Drink" + e.getMessage());
            }
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


    public void printProductReport(Product product, String languageTag, String client) throws IOException{
        RessourceFormatter formatter = changeLocale(languageTag);
        List<Review> reviews = products.get(product);
        Collections.sort(reviews);

        Path productFile = reportFolder.resolve(MessageFormat.format(conf.getString("reports.file"),  product.getId(), client));

        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(productFile, StandardOpenOption.CREATE), StandardCharsets.UTF_8))) {
            out.append(formatter.formatProduct(product) + System.lineSeparator());
            if (reviews.isEmpty()){
                out.append(formatter.getText("no.reviews"));
            }else {
                out.append(reviews.stream()
                        .map(r -> formatter.formatReview(r) + System.lineSeparator())
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
            try{
                readLock.lock();
                return products.keySet()
                        .stream()
                        .filter((product -> product.getId() == id))
                        .findFirst()
                        .orElseThrow( () -> new ProductManagerException("Product with the id" + id + "not found"));
            }
            finally {
                readLock.unlock();
            }
//        Product result = null;
//        for (Product product : products.keySet()){
//            if (product.getId() == id) { //                result = product;
//                break;
//            }
//        }
//        return result;

    }



    private Product reviewProduct(int id, Rating rating, String comments) {
        try{
            writeLock.lock();
            return reviewProduct(findProduct(id), rating, comments);
        }
        catch (ProductManagerException e){
            logger.log(Level.INFO, e.getMessage());
        }
        finally {
            writeLock.unlock();
        }
        return null;
    }

    public void printProductReport(int id, String languageTag, String client) {
        try{
            readLock.lock();
            printProductReport(findProduct(id), languageTag, client );
        }
        catch (ProductManagerException e) {
            logger.log(Level.INFO, e.getMessage());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error printing product report");
        }
        finally {
        readLock.unlock();
        }

    }

    public void printProducts(Predicate<Product> filter, Comparator<Product> sorter, String languageTag){
            try{
                readLock.lock();
                RessourceFormatter formatter = changeLocale(languageTag);
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
                products.keySet().stream().sorted(sorter).filter(filter).forEach(product -> txt.append(formatter.formatProduct(product) + '\n'));
                System.out.println(txt);
            }
           catch (Exception e){
                logger.log(Level.SEVERE, "Error printing products " + e.getMessage()); }
            finally {
           readLock.unlock();
            }
    }

    public Map<Object, String> getDiscounts(String languageTag){
            try {
                readLock.lock();
                RessourceFormatter formatter = changeLocale(languageTag);
                return products.keySet()
                        .stream()
                        .collect(
                                Collectors.groupingBy(product -> product.getRating().getStars(),
                                        Collectors.collectingAndThen(
                                                Collectors.summingDouble(prodcut -> prodcut.getDiscount().doubleValue()),
                                                discount -> formatter.moneyFormat.format(discount)
                                        ))
                        );
            }
            catch (Exception e){
                logger.log(Level.SEVERE, "Error getting discournts" + e.getMessage());
            }
            finally {
            readLock.unlock();
            }
        return Map.of();
    }
}
