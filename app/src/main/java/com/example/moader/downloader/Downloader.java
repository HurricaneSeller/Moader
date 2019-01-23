package com.example.moader.downloader;

import android.graphics.Bitmap;

import java.io.IOException;
import java.io.OutputStream;

public interface Downloader {
    Bitmap downloadBitmapFromUri(String uri);
    Bitmap downloadBitmapFromHttp(String uri, int targetWidth, int targetHeight) throws IOException;
}
