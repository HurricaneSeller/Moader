package com.example.moader.utils;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class LoaderResult {
    private ImageView mImageView;
    private String mUri;
    private Bitmap mBitmap;

    public LoaderResult(ImageView imageView, String uri, Bitmap bitmap) {
        mImageView = imageView;
        mUri = uri;
        mBitmap = bitmap;
    }

    public ImageView getImageView() {
        return mImageView;
    }

    public String getUri() {
        return mUri;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}
