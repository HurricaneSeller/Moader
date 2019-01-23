package com.example.moader.cache;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.moader.utils.BitmapTransformer;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import static com.example.moader.utils.Utils.checkIfOnUitThread;
import static com.example.moader.utils.Utils.hashKeyFromUri;

public class MoaderCache extends Cache {
    private static MoaderCache INSTANCE = null;
    private static final String TAG = "MoaderCache";
    public static final int DISK_CACHE_INDEX = 0;
    private final BitmapTransformer mBitmapTransformer = new BitmapTransformer();

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    private MoaderCache(int maxSize) {
        super(maxSize, INSTANCE);
    }

    public static MoaderCache getInstance() {
        if (INSTANCE == null) {
            int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            int cacheSize = maxMemory / 8;
            synchronized (MoaderCache.class) {
                INSTANCE = new MoaderCache(cacheSize);
            }
        }
        return INSTANCE;
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight() / 1024;
    }

    @Override
    public Bitmap loadBitmapFromDiskCache(DiskLruCache diskLruCache, String uri, int targetWidth, int targetHeight) throws IOException {
        if (checkIfOnUitThread()) {
            Log.w(TAG, "loadBitmapFromDiskCache: not recommend to run this method on UiThread.");
        }
        if (diskLruCache == null) {
            return null;
        }
        Bitmap bitmap = null;
        String key = hashKeyFromUri(uri);
        DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
        if (snapshot != null) {
            FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = mBitmapTransformer.decodeSampledBitmapFromFileDescriptor(fileDescriptor, targetWidth,
                    targetHeight);
            if (bitmap != null) {
                INSTANCE.addBitmapToMemCache(key, bitmap);
            }
        }
        return bitmap;
    }

    @Override
    public Bitmap loadBitmapFromMemCache(String uri) {
        final String key = hashKeyFromUri(uri);
        return INSTANCE.getBitmapFromMemCache(key);
    }
}
