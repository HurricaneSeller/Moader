package com.example.moader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.example.moader.cache.Cache;
import com.example.moader.cache.DiskLruCache;
import com.example.moader.cache.MoaderCache;
import com.example.moader.downloader.Downloader;
import com.example.moader.downloader.MoaderDownloader;
import com.example.moader.executor.MoaderExecutor;
import com.example.moader.utils.BitmapTransformer;
import com.example.moader.utils.LoaderResult;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;

import androidx.annotation.NonNull;

import static com.example.moader.utils.Utils.getDiskCacheDir;
import static com.example.moader.utils.Utils.getUsableSpace;

/**
 * usage:
 * Moader moader = Moader.getInstance(context);
 * <p>
 * or using specific parameters:
 * Moader.Builder builder = new Moader.Builder(context).executor(defined-executor)... ;
 * Moader moader = builder.build();
 */

public class Moader {
    private static final int MESSAGE_POST_RESULT = 1;
    private static final int TAG_KEY_URI = 182;

    private static final long DISK_CACHE_SIZE = 52428800; //1024 * 1024 * 50
    private static final int I0_BUFFER_SIZE = 8192;//1024 * 8
    private static final int DISK_CACHE_INDEX = 0;
    private static final String TAG = "Moader";

    private Executor mExecutor;
    private boolean mIsDiskLruCacheCreated = false;
    private Downloader mDownloader;

    @SuppressLint("StaticFieldLeak")
    private static volatile Moader MOADER = null;

    public static Moader getInstance(@NonNull Context context) {
        if (MOADER == null) {
            synchronized (Moader.class) {
                MOADER = new Builder(context).build();
            }
        }
        return MOADER;
    }


    public final void bindBitmap(@NonNull final String uri, @NonNull final ImageView imageView) {
        bindBitmap(uri, imageView, 0, 0);
    }

    public final void bindBitmap(@NonNull final String uri, @NonNull final ImageView imageView,
                           final int targetWidth,
                           final int targetHeight) {
        imageView.setTag(TAG_KEY_URI, uri);
        Bitmap bitmap = mDownloader.loadBitmapFromMemCache(uri);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }
        Runnable loadBitmapTask = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = null;
                try {
                    bitmap = loadBitmap(uri, targetWidth, targetHeight);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (bitmap != null) {
                    LoaderResult result = new LoaderResult(imageView, uri, bitmap);
                    mMainHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();
                }
            }
        };
        mExecutor.execute(loadBitmapTask);
    }

    private Bitmap loadBitmap(String uri, int targetWidth, int targetHeight) throws IOException {
        Bitmap bitmap = mDownloader.loadBitmapFromMemCache(uri);
        if (bitmap != null) {
            return bitmap;
        }
        bitmap = mDownloader.loadBitmapFromDiskCache(uri, targetWidth, targetHeight);
        if (bitmap != null) {
            return bitmap;
        }
        bitmap = mDownloader.loadBitmapFromHttp(uri, targetWidth, targetHeight);
        if (bitmap == null && !mIsDiskLruCacheCreated) {
            Log.w(TAG, "loadBitmap: error occur");
            bitmap = mDownloader.downloadBitmapFromUri(uri);
        }
        return bitmap;
    }


    private final Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            LoaderResult result = (LoaderResult) msg.obj;
            ImageView imageView = result.getImageView();
            String uri = (String) imageView.getTag(TAG_KEY_URI);
            if (uri.endsWith(result.getUri())) {
                imageView.setImageBitmap(result.getBitmap());
            }
        }
    };


    private Moader(Executor executor, Cache memoryCache, Context context, DiskLruCache diskLruCache,
                   boolean isDiskLruCacheCreated, Downloader downloader) {
        mExecutor = executor;
        mIsDiskLruCacheCreated = isDiskLruCacheCreated;
        mDownloader = downloader;
    }

    public static class Builder {
        private Executor mExecutor;
        private Context mContext;
        private Cache mCache;
        private DiskLruCache mDiskLruCache;
        private boolean mIsDiskLruCacheCreated = false;
        private Downloader mDownloader;
        private BitmapTransformer mBitmapTransformer = new BitmapTransformer();


        Builder(Context context) {
            mContext = context.getApplicationContext();
        }

        /**
         * @param executor set specified executor.
         */
        public final Builder executor(@NonNull Executor executor) {
            if (mExecutor != null) {
                throw new IllegalArgumentException("Executor already be set.");
            }
            mExecutor = executor;
            return this;
        }

        public final Builder cache(@NonNull Cache cache) {
            if (mCache != null) {
                throw new IllegalArgumentException("Lrucache already be set.");
            }
            mCache = cache;
            return this;
        }

        public final Builder downloader(@NonNull Downloader downloader) {
            if (mDownloader != null) {
                throw new IllegalArgumentException("Downloader already be set.");
            }
            mDownloader = downloader;
            return this;
        }

        Moader build() {
            if (mExecutor == null) {
                mExecutor = MoaderExecutor.getInstance();
            }
            if (mCache == null) {
                mCache = MoaderCache.getInstance();
            }

            File diskCacheDir = getDiskCacheDir(mContext, "bitmap");
            if (!diskCacheDir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                diskCacheDir.mkdirs();
            }
            if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
                try {
                    mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
                    mIsDiskLruCacheCreated = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mDownloader == null) {
                mDownloader = new MoaderDownloader(mDiskLruCache, mCache, mBitmapTransformer);
            }
            return new Moader(mExecutor, mCache, mContext, mDiskLruCache, mIsDiskLruCacheCreated, mDownloader);
        }

    }
}
