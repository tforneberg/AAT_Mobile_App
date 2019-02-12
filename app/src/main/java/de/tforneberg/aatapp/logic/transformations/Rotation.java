package de.tforneberg.aatapp.logic.transformations;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.util.Util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;

/**
 * Caching save implementation of a BitmapTransformation that rotates the bitmap by a given angle.
 * Create object with "new Rotation(rotationAngle)" and call .transform(bitmapPool, toTransform, w, h).
 */
public class Rotation extends BitmapTransformation {
    //static identifier values used to evaluate if a cached version of a bitmap can be used or not
    private static final String ID = "de.tforneberg.aatapp.logic.transformations.Rotation";
    private static final byte[] ID_BYTES = ID.getBytes(Charset.forName("UTF-8"));

    private int rotationAngle;

    public Rotation(int rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);

        int newWidth = toTransform.getWidth();
        int newHeight = toTransform.getHeight();

        return Bitmap.createBitmap(toTransform, 0, 0, newWidth, newHeight, matrix, true);
    }

    @Override
    public int hashCode() {
        return Util.hashCode(ID.hashCode(), Util.hashCode(rotationAngle));
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Rotation) {
            Rotation other = (Rotation) obj;
            return rotationAngle == other.rotationAngle;
        }
        return false;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
        byte[] objData = ByteBuffer.allocate(4).putInt(rotationAngle).array();
        messageDigest.update(objData);
    }
}
