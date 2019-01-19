package com.example.moader.cache;

import android.graphics.Bitmap;
import android.util.LruCache;

public abstract class Cache extends LruCache<String, Bitmap> {
    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    Cache(int maxSize) {
        super(maxSize);
    }
    public abstract void addBitmapToMemoryCache(String key, Bitmap bitmap);
    public abstract Bitmap getBitmapFromMemCache(String key);
}
