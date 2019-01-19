package com.example.moader.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.FileDescriptor;

import androidx.annotation.NonNull;


/**
 * can extend this class to give more method of transforming the bitmap.
 */
public class BitmapTransformer {
    /**
     * @param resources    bitmap
     * @param resourceId   bitmap-id
     * @param targetWidth  targeted width
     * @param targetHeight targeted height
     * @return return the best-fixed bitmap
     */
    public Bitmap decodeBitmapFromResource(Resources resources, int resourceId, int targetWidth,
                                           int targetHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resourceId, options);
        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, resourceId, options);
    }

    /**
     * @param fileDescriptor file-descriptor
     * @param targetWidth    targeted width
     * @param targetHeight   targeted height
     * @return return the best-fixed bitmap
     */
    public Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fileDescriptor, int targetWidth,
                                                        int targetHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

    }

    private int calculateInSampleSize(BitmapFactory.Options options, int targetWidth, int targetHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > targetHeight || width > targetWidth) {
            final int halfWidth = width / 2;
            final int halfHeight = height / 2;
            while ((halfHeight / inSampleSize) > targetHeight && (halfWidth / inSampleSize) > targetWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


}
