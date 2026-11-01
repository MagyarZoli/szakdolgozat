package hu.thesis.sample;

import hu.thesis.sample.shapes.AbstractShape;
import hu.thesis.sample.shapes.Circle;
import hu.thesis.sample.shapes.Shape;
import hu.thesis.sample.shapes.Square;

import java.util.List;

/** A mintaprogram belépési pontja; minden vizsgált nyelvi elemet meghív. */
public final class Main {

    /**
     * Belépési pont: minden vizsgált nyelvi elemet meghív, és kiírja az eredményeket, így a
     * program futtatásával is ellenőrizhető, hogy a mintakód helyes.
     */
    public static void main(String[] args) {
        List<Shape> shapes = List.of(new Circle(1), new Square(2));
        double total = 0;
        for (Shape s : shapes) {
            total += area(s);
            System.out.println(s.describe());
        }
        System.out.println("total=" + total + ", created=" + AbstractShape.createdCount());
    }

    static double area(Shape s) {
        return s.area();
    }
}
