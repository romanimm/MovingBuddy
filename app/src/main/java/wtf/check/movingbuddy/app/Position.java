package wtf.check.movingbuddy.app;

/**
 * Created by roman on 04.03.2015.
 */
public class Position {
    public float x;
    public float y;

    public Position(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public synchronized void add(float x, float y) {
        this.x += x;
        this.y += y;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Deprecated // not needed?
    public static Position diff(Position pointA, Position pointB) {
        return new Position(pointB.x - pointA.x, pointB.y - pointA.y);
    }

    @Deprecated // not needed? use add!
    public synchronized Position sub(int dt) {
        return new Position(x - dt, y - dt);
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
