package hu.thesis.sample.shapes;

public abstract class AbstractShape implements Shape {
    static int created;
    static {
        created = 0;
    }

    // Példányinicializáló: a fordító minden konstruktorba bemásolja
    {
        created++;
    }

    protected AbstractShape() {
    }

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    public static int createdCount() {
        return created;
    }
}
