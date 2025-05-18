package labs.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public final  class Drink extends Product{

    Drink(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore){
        super(id, name, price, rating, bestBefore);
    }

    @Override
    public BigDecimal getDiscount() {
        LocalTime now = LocalTime.now();
        return (now.isBefore(LocalTime.of(18, 30))) && now.isAfter(LocalTime.of(17, 30))  ? super.getDiscount() : BigDecimal.ZERO;
    }

    @Override
    public Product applyRating(Rating rating) {
        return new Drink(getId(), getName(), getPrice(), getRating(), getBestBefore());
    }
}
