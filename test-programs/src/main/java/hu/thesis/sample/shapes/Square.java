package hu.thesis.sample.shapes;

public class Square extends AbstractShape {
    private final double a;

    public Square(double a) {
        this.a = a;
    }

    @Override
    public double area() {
        return a * a;
    }

    @Override
    public String name() {
        return "Square(" + a + ")";
    }
}
