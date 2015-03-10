package wtf.check.movingbuddy.app;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 04.03.2015.
 */
public class Buddy {
    private static Buddy buddy = new Buddy();
    private int drawableId;
    private List<BuddyListener> listeners = new ArrayList<BuddyListener>();
    private Position position;

    private Bitmap image;
    private Bitmap drawableBitmap;
    private int width;
    private int height;

    public Buddy() {
        position = new Position(50, 20);
    }

    public int getDrawableId() {
        return drawableId;
    }

    public void setDrawableId(int drawableId) {
        this.drawableId = drawableId;
        updateImages(ImageType.ResourceId);
    }

    public void setDrawableBitmap(Bitmap drawableBitmap) {
        this.drawableBitmap = drawableBitmap;
        updateImages(ImageType.Bitmap);
    }
    public void addListener(BuddyListener listener) {
        listeners.add(listener);
    }

    public void updateImages(ImageType imagetType) {
        for (BuddyListener listener : listeners) {
            listener.updateImage(imagetType);
        }
    }



    public Bitmap getDrawableBitmap() {
        return drawableBitmap;
    }

    public Position getPosition() {
        return position;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void addToPosition(float x, float y) {
        this.position.x += x;
        this.position.y += y;
    }

    public interface BuddyListener {
        public void updateImage(ImageType imageType);
    }

    public static Buddy getInstance() {
        return buddy;
    }

    @Override
    public String toString() {
        return String.format("Position: %s", position.toString());
    }
}
