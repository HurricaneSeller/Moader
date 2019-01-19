package com.example.moader.cache;

import android.graphics.Bitmap;

public class MoaderCache extends Cache {
    private static MoaderCache INSTANCE = null;

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    private MoaderCache(int maxSize) {
        super(maxSize);
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
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) != null) {
            INSTANCE.put(key, bitmap);
        }
    }

    @Override
    public Bitmap getBitmapFromMemCache(String key) {
        return INSTANCE.get(key);
    }
}
