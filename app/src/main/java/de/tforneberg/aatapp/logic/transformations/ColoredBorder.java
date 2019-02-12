package de.tforneberg.aatapp.logic.transformations;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.util.Util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;

/**
 * Caching save implementation of a BitmapTransformation that adds a colored border to the bitmap.
 * Create object with "new ColoredBorder(color, borderSize)" and call .transform(bitmapPool, toTransform, w, h).
 */
public class ColoredBorder extends BitmapTransformation {
    //static identifier values used to evaluate if a cached version of a bitmap can be used or not
    private static final String ID = "de.tforneberg.aatapp.logic.transformations.ColoredBorder";
    private static final byte[] ID_BYTES = ID.getBytes(Charset.forName("UTF-8"));

    private int color;
    private int borderSize;

    public ColoredBorder(int borderColor, int borderSize) {
        super();
        this.borderSize = borderSize;
        this.color = borderColor;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        int newWidth = toTransform.getWidth()+2*borderSize;
        int newHeight = toTransform.getHeight()+2*borderSize;

        Bitmap newBitmap = Bitmap.createBitmap(newWidth,newHeight, Bitmap.Config.ARGB_8888);

        //draw old bitmap centered into new bitmap/canvas
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(toTransform, borderSize, borderSize, null);

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        canvas.drawRect(0, 0, newWidth, borderSize, paint); //Top border
        canvas.drawRect(0, 0, borderSize, newHeight, paint); //Left border
        canvas.drawRect(newWidth - borderSize, 0, newWidth, newHeight, paint); //Right border
        canvas.drawRect(0, newHeight - borderSize, newWidth, newHeight, paint); //Bottom border

        return newBitmap;
    }

    @Override
    public int hashCode() {
        return Util.hashCode(ID.hashCode(), Util.hashCode(color, Util.hashCode(borderSize)));
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof ColoredBorder) {
            ColoredBorder other = (ColoredBorder) obj;
            return color == other.color && borderSize == other.borderSize;
        }
        return false;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
        byte[] objData = ByteBuffer.allocate(8).putInt(color).putInt(borderSize).array();
        messageDigest.update(objData);
    }
}
