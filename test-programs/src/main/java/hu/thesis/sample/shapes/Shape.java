package hu.thesis.sample.shapes;

public interface Shape {
    double area();

    String name();

    default String describe() {
        return name() + " area=" + area();
    }
}
