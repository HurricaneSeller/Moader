package com.example.moader.downloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.util.Log;

import com.example.moader.cache.Cache;
import com.example.moader.cache.DiskLruCache;
import com.example.moader.utils.BitmapTransformer;
import com.example.moader.utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.example.moader.cache.DiskLruCache.IO_BUFFER_SIZE;
import static com.example.moader.utils.Utils.close;
import static com.example.moader.utils.Utils.hashKeyFromUri;

/**
 * The default downloader.
 */
public class MoaderDownloader implements Downloader{
    private static final String TAG = "MoaderDownloader";
    private static final int DISK_CACHE_INDEX = 0;
    private final DiskLruCache mDiskLruCache;
    private final Cache mMemoryCache;
    private final BitmapTransformer mBitmapTransformer;

    public MoaderDownloader(DiskLruCache diskLruCache, Cache memoryCache, BitmapTransformer bitmapTransformer) {
        mDiskLruCache = diskLruCache;
        mMemoryCache = memoryCache;
        mBitmapTransformer = bitmapTransformer;
    }

    @Override
    public Bitmap downloadBitmapFromUri(String uri) {
        Bitmap bitmap = null;
        HttpURLConnection connection = null;
        BufferedInputStream inputStream = null;
        try {
            final URL url = new URL(uri);
            connection = (HttpURLConnection) url.openConnection();
            inputStream = new BufferedInputStream(connection.getInputStream(), IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            Utils.close(inputStream);
        }
        return bitmap;
    }

    @Override
    public Bitmap loadBitmapFromHttp(String uri, int targetWidth, int targetHeight) throws IOException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("can not run time-consuming work on UiThread.");
        }
        if (mDiskLruCache == null) {
            return null;
        }
        String key = hashKeyFromUri(uri);
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if (editor != null) {
            OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
            if (downloadUrlToStream(uri, outputStream)) {
                editor.commit();
            } else {
                editor.abort();
            }
            mDiskLruCache.flush();
        }
        return loadBitmapFromDiskCache(uri, targetWidth, targetHeight);
    }

    @Override
    public Bitmap loadBitmapFromDiskCache(String uri, int targetWidth, int targetHeight) throws IOException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.w(TAG, "loadBitmapFromDiskCache: not recommend to run this method on UiThread.");
        }
        if (mDiskLruCache == null) {
            return null;
        }
        Bitmap bitmap = null;
        String key = hashKeyFromUri(uri);
        DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
        if (snapshot != null) {
            FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = mBitmapTransformer.decodeSampledBitmapFromFileDescriptor(fileDescriptor, targetWidth, targetHeight);
            if (bitmap != null) {
                mMemoryCache.addBitmapToMemoryCache(key, bitmap);
            }
        }
        return bitmap;
    }

    @Override
    public Bitmap loadBitmapFromMemCache(String uri) {
        final String key = hashKeyFromUri(uri);
        return mMemoryCache.getBitmapFromMemCache(key);
    }

    @Override
    public boolean downloadUrlToStream(String uri, OutputStream outputStream) {
        HttpURLConnection connection = null;
        BufferedOutputStream bufferedOutputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            final URL url = new URL(uri);
            connection = (HttpURLConnection) url.openConnection();
            bufferedInputStream = new BufferedInputStream(connection.getInputStream(), IO_BUFFER_SIZE);
            bufferedOutputStream = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
            int b;
            while ((b = bufferedInputStream.read()) != -1) {
                bufferedOutputStream.write(b);
            }
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            close(bufferedOutputStream);
            close(bufferedInputStream);
        }
        return false;
    }
}
