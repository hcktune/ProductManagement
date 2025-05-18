package labs.data;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class Food extends Product{





    Food(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        super(id, name, price, rating, bestBefore);
    }


    @Override
    public String toString() {
        return "Food{" +
                "bestBefore=" + getBestBefore() +
                "} " + super.toString();
    }

    @Override
    public BigDecimal getDiscount() {
        return getBestBefore().equals(LocalDate.now()) ? super.getDiscount() : BigDecimal.ZERO;
    }

    @Override
    public Product applyRating(Rating rating) {
        return new Food(getId(), getName(), getPrice(), rating, getBestBefore());
    }
}
