import java.awt.*;
import java.text.ParseException;
import java.util.HashMap;

/**
 * Created by jmuia on 2016-03-14.
 */
public class WhiteboardPoint extends Point {
    private Color color;

    public WhiteboardPoint(Color color) {
        this.color = color;
    }

    public WhiteboardPoint(Point p, Color color) {
        super(p);
        this.color = color;
    }

    public WhiteboardPoint(int x, int y, Color color) {
        super(x, y);
        this.color = color;
    }

    public WhiteboardPoint(String s) throws ParseException {
        super();

        HashMap<String, String> properties = new HashMap<>();

        String[] propertyTokens = s.split("&");
        if (propertyTokens.length != 3) { throw new ParseException("Could not parse string", -1); }

        for (String propertyToken: propertyTokens) {
            String[] keyValueTokens = propertyToken.split("=");
            if (keyValueTokens.length != 2) { throw new ParseException("Could not parse string", -1); }

            properties.put(keyValueTokens[0], keyValueTokens[1]);
        }

        int x = Integer.parseInt(properties.get("x"));
        int y = Integer.parseInt(properties.get("y"));
        int rgb = Integer.parseInt(properties.get("rgb"));

        this.x = x;
        this.y = y;
        this.color = new Color(rgb);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String serialize() {
        String s = "x=%d&y=%d&rgb=%d";
        return String.format(s, x, y, color.getRGB());
    }
}
