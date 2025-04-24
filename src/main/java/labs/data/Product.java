package labs.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public abstract class Product {
    private final int id;
    private final String name;
    private final BigDecimal price;
    public static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.1);
    private  Rating rating;


    public Product(int id, String name, BigDecimal price, Rating rating) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.rating = rating;
    }

    public Product(int id, String name, BigDecimal price){
        this(id, name, price, Rating.NOT_RATED);

    }

    public Product(){
        this(0, "please enter the product's name", BigDecimal.ZERO, Rating.NOT_RATED);
    }

    public Rating getRating() {
        return this.rating;
    }


    public int getId(){
        return this.id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }


    /**
     * replicate, adjust, return new Product object
     * {@link applyRating apply rating}
     * @return a {@link Product Product}
     * value of the discount
     * */


    /**
     * Calculate the discount of a product
     * {@link DISCOUNT_RATE discournt rate}
     * @return a {@link BigDecimal BigDecimal}
     * value of the discount
     * */
    public BigDecimal getDiscount(){
        return price.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public abstract Product applyRating(Rating rating);

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", rating=" + rating +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Product product){
        return id == product.id && Objects.equals(name, product.name);}
        return  false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
