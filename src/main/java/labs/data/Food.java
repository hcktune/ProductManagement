package labs.data;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class Food extends Product{
    private LocalDate bestBefore;
    private Rating newRating;

    public Rating getNewRating() {
        return newRating;
    }

    Food(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        super(id, name, price, rating);
        this.bestBefore = bestBefore;
    }
    public LocalDate getBestBefore()
    {
        return this.bestBefore;
    }

    @Override
    public String toString() {
        return "Food{" +
                "bestBefore=" + bestBefore +
                "} " + super.toString();
    }

    @Override
    public BigDecimal getDiscount() {
        return bestBefore.equals(LocalDate.now()) ? super.getDiscount() : BigDecimal.ZERO;
    }

    @Override
    public Product applyRating(Rating rating) {
        return new Food(super.getId(), super.getName(), super.getPrice(), newRating, bestBefore);
    }
}
