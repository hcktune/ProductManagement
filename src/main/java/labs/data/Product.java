package labs.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public sealed abstract  class Product
        implements Rateable<Product> permits Drink, Food{
    private final int id;
    private final String name;
    private final BigDecimal price;
    private LocalDate bestBefore;
    public static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.5);
    private  Rating rating;


    public LocalDate getBestBefore()
    {
        return this.bestBefore;
    }
    Product(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.rating = rating;
        this.bestBefore = bestBefore;
    }

//    public Product(int id, String name, BigDecimal price){
//        this(id, name, price, Rating.NOT_RATED);
//
//    }

//    public Product(){
//        this(0, "please enter the product's name", BigDecimal.ZERO, Rating.NOT_RATED);
//    }
    @Override
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
        return id == product.id;}
        return  false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
