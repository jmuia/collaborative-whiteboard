import java.awt.*;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jmuia on 2016-03-14.
 */
public class Whiteboard {
    private static Whiteboard instance = new Whiteboard();
    private ConcurrentHashMap<Point, WhiteboardPoint> points;

    private Set<Color> colors;

    private List<WhiteboardListener> listeners;

    private Whiteboard() {
        this.points = new ConcurrentHashMap<>();
        this.colors = Collections.synchronizedSet(new HashSet<Color>());
        this.listeners = Collections.synchronizedList(new ArrayList<WhiteboardListener>());
    }

    public static void addPoint(WhiteboardPoint point) {
        instance.points.put(new Point(point.x, point.y), point);
        synchronized (instance.listeners) {
            Iterator i = instance.listeners.iterator();
            while (i.hasNext())
                ((WhiteboardListener) i.next()).update(point);
        }
    }

    public static Collection<WhiteboardPoint> getPoints() {
        return instance.points.values();
    }

    public static void initialize(String whiteboard) throws ParseException {
        String[] points = whiteboard.split(",");
        List<WhiteboardPoint> wbps = new ArrayList<>();

        for (String point: points) {
            wbps.add(new WhiteboardPoint(point));
        }

        synchronized (instance.points) {
            instance.points = new ConcurrentHashMap<>();
            for (WhiteboardPoint point: wbps) {
                addPoint(point);
            }
        }
    }

    public static void clear() {
        synchronized (instance) {
            instance.points = new ConcurrentHashMap<>();
            instance.colors = Collections.synchronizedSet(new HashSet<Color>());
        }
    }

    public static Color registerColor() {
        Color color;
        double luminosity;
        synchronized (instance.colors) {
            do {
                color = new Color((int)(Math.random() * 0x1000000));
                luminosity = 0.21*color.getRed() + 0.72*color.getGreen() + 0.07*color.getBlue();
            } while (instance.colors.contains(color) || luminosity > 200);
            instance.colors.add(color);
        }

        return color;
    }

    public static void addListener(WhiteboardListener listener) {
        instance.listeners.add(listener);
    }

    public static void removeListener(WhiteboardListener listener) {
        instance.listeners.remove(listener);
    }

    public static String serialize() {
        String s = "";
        synchronized (instance.points) {
            Enumeration<WhiteboardPoint> values = instance.points.elements();
            while (values.hasMoreElements()) {
                WhiteboardPoint point = values.nextElement();
                s += point.serialize();
                if (values.hasMoreElements()) { s += ","; }
            }
        }
        return s;
    }



}
