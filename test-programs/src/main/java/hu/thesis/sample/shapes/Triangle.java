package hu.thesis.sample.shapes;

public class Triangle extends AbstractShape {
    private final double b;
    private final double h;

    public Triangle(double b, double h) {
        this.b = b;
        this.h = h;
    }

    @Override
    public double area() {
        return b * h / 2;
    }
}
