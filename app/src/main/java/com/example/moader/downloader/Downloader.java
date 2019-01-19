package com.example.moader.downloader;

import android.graphics.Bitmap;

import java.io.IOException;
import java.io.OutputStream;

public interface Downloader {
    Bitmap downloadBitmapFromUri(String uri);
    Bitmap loadBitmapFromHttp(String uri, int targetWidth, int targetHeight) throws IOException;
    Bitmap loadBitmapFromDiskCache(String uri, int targetWidth, int targetHeight) throws IOException;
    Bitmap loadBitmapFromMemCache(String uri);
    boolean downloadUrlToStream(String uri, OutputStream outputStream);
}
