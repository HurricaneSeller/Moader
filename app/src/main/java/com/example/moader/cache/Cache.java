package com.example.moader.cache;

import android.graphics.Bitmap;
import android.util.LruCache;

import java.io.IOException;
import java.io.OutputStream;

import androidx.annotation.NonNull;

import static com.example.moader.cache.MoaderCache.DISK_CACHE_INDEX;
import static com.example.moader.utils.Utils.hashKeyFromUri;

public abstract class Cache extends LruCache<String, Bitmap> {
    private Cache mCache;

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    Cache(int maxSize, @NonNull Cache cache) {
        super(maxSize);
        this.mCache = cache;
    }

    final void addBitmapToMemCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) != null) {
            mCache.put(key, bitmap);
        }
    }

    final Bitmap getBitmapFromMemCache(String key) {
        return mCache.get(key);
    }

    public abstract Bitmap loadBitmapFromDiskCache(DiskLruCache diskLruCache, String uri, int targetWidth,
                                                   int targetHeight) throws IOException;

    public abstract Bitmap loadBitmapFromMemCache(String uri);


}
