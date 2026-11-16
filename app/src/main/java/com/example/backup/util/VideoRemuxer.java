package com.example.backup.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class VideoRemuxer {
    private static final String TAG = "VideoRemuxer";

    public interface RemuxCallback {
        void onReady(Uri uri, int durationMs);
    }

    public static void clearCache(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.exists()) {
                File[] files = cacheDir.listFiles((dir, name) -> name.startsWith("standard_") || name.startsWith("temp_"));
                if (files != null) {
                    for (File f : files) {
                        f.delete();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi xóa cache: " + e.getMessage());
        }
    }

    public static boolean isFragmentedMp4(Context context, Uri uri, int rawResId) {
        AssetFileDescriptor afd = null;
        InputStream is = null;
        try {
            if (rawResId != 0) {
                afd = context.getResources().openRawResourceFd(rawResId);
                if (afd != null) is = afd.createInputStream();
            } else if (uri != null) {
                if ("android.resource".equals(uri.getScheme())) {
                    afd = context.getContentResolver().openAssetFileDescriptor(uri, "r");
                    if (afd != null) is = afd.createInputStream();
                } else if ("file".equals(uri.getScheme()) || uri.getPath() != null) {
                    File f = new File(uri.getPath());
                    if (f.exists()) is = new FileInputStream(f);
                }
            }
            if (is != null) {
                byte[] buf = new byte[32768];
                int read = is.read(buf);
                if (read > 8) {
                    for (int i = 0; i < read - 4; i++) {
                        if (buf[i] == 'm' && buf[i+1] == 'o' && buf[i+2] == 'o' && buf[i+3] == 'f') {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Lỗi kiểm tra fMP4: " + e.getMessage());
        } finally {
            try { if (is != null) is.close(); } catch (Exception ignored) {}
            try { if (afd != null) afd.close(); } catch (Exception ignored) {}
        }
        return false;
    }

    public static int detectMediaDurationMs(Context context, Uri uri, int rawResId) {

        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            if (rawResId != 0) {
                AssetFileDescriptor afd = context.getResources().openRawResourceFd(rawResId);
                if (afd != null) {
                    retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    afd.close();
                }
            } else if (uri != null) {
                retriever.setDataSource(context, uri);
            }
            String durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            retriever.release();
            if (durStr != null) {
                int d = Integer.parseInt(durStr);
                if (d > 0) return d;
            }
        } catch (Exception ignored) {}

        AssetFileDescriptor afd = null;
        FileInputStream fis = null;
        try {
            if (rawResId != 0) {
                afd = context.getResources().openRawResourceFd(rawResId);
                if (afd != null) fis = afd.createInputStream();
            } else if (uri != null) {
                if ("android.resource".equals(uri.getScheme())) {
                    afd = context.getContentResolver().openAssetFileDescriptor(uri, "r");
                    if (afd != null) fis = afd.createInputStream();
                } else if ("file".equals(uri.getScheme()) || uri.getPath() != null) {
                    File f = new File(uri.getPath());
                    if (f.exists()) fis = new FileInputStream(f);
                }
            }
            if (fis != null) {
                int available = fis.available();
                int maxRead = Math.min(available, 100 * 1024 * 1024);
                byte[] data = new byte[maxRead];
                int totalRead = 0;
                while (totalRead < data.length) {
                    int r = fis.read(data, totalRead, data.length - totalRead);
                    if (r <= 0) break;
                    totalRead += r;
                }

                long timescale = 1000;
                int mdhdIdx = indexOf(data, totalRead, new byte[]{'m', 'd', 'h', 'd'}, 0);
                if (mdhdIdx != -1 && mdhdIdx + 28 <= totalRead) {
                    int ver = data[mdhdIdx + 4] & 0xFF;
                    if (ver == 0) {
                        timescale = readUint32(data, mdhdIdx + 16);
                    } else {
                        timescale = readUint32(data, mdhdIdx + 24);
                    }
                }

                long maxTfdt = 0;
                int pos = 0;
                byte[] tfdtTag = new byte[]{'t', 'f', 'd', 't'};
                while (pos < totalRead - 16) {
                    int found = indexOf(data, totalRead, tfdtTag, pos);
                    if (found == -1) break;
                    int ver = data[found + 4] & 0xFF;
                    long baseTime;
                    if (ver == 0 && found + 12 <= totalRead) {
                        baseTime = readUint32(data, found + 8);
                    } else if (ver == 1 && found + 16 <= totalRead) {
                        baseTime = readUint64(data, found + 8);
                    } else {
                        baseTime = 0;
                    }
                    if (baseTime > maxTfdt) {
                        maxTfdt = baseTime;
                    }
                    pos = found + 4;
                }

                if (timescale > 0 && maxTfdt > 0) {
                    return (int) ((maxTfdt * 1000) / timescale);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Lỗi đọc thời lượng fMP4: " + e.getMessage());
        } finally {
            try { if (fis != null) fis.close(); } catch (Exception ignored) {}
            try { if (afd != null) afd.close(); } catch (Exception ignored) {}
        }

        return 0;
    }

    private static int indexOf(byte[] data, int length, byte[] pattern, int start) {
        if (data == null || pattern == null || start < 0) return -1;
        int limit = Math.min(data.length, length) - pattern.length;
        for (int i = start; i <= limit; i++) {
            boolean match = true;
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }

    private static long readUint32(byte[] data, int offset) {
        return (((long)(data[offset] & 0xFF)) << 24) |
               (((long)(data[offset + 1] & 0xFF)) << 16) |
               (((long)(data[offset + 2] & 0xFF)) << 8) |
               (((long)(data[offset + 3] & 0xFF)));
    }

    private static long readUint64(byte[] data, int offset) {
        long high = readUint32(data, offset);
        long low = readUint32(data, offset + 4);
        return (high << 32) | (low & 0xFFFFFFFFL);
    }

    public static void prepareVideoUri(Context context, Uri sourceUri, int rawResId, RemuxCallback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            MediaExtractor extractor = null;
            MediaMuxer muxer = null;
            File tempFile = null;

            try {

                String cacheFileName = (rawResId != 0
                        ? "standard_v2_raw_" + rawResId
                        : "standard_v2_url_" + Math.abs(sourceUri.toString().hashCode())) + ".mp4";
                File cacheFile = new File(context.getCacheDir(), cacheFileName);

                if (cacheFile.exists() && cacheFile.length() > 500000) {
                    int cachedDurationMs = detectMediaDurationMs(context, Uri.fromFile(cacheFile), 0);
                    mainHandler.post(() -> callback.onReady(Uri.fromFile(cacheFile), cachedDurationMs));
                    return;
                }

                extractor = new MediaExtractor();
                if (rawResId != 0) {
                    AssetFileDescriptor afd = context.getResources().openRawResourceFd(rawResId);
                    extractor.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    afd.close();
                } else if (sourceUri.getScheme() != null && sourceUri.getScheme().equals("android.resource")) {
                    AssetFileDescriptor afd = context.getContentResolver().openAssetFileDescriptor(sourceUri, "r");
                    if (afd != null) {
                        extractor.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        afd.close();
                    } else {
                        extractor.setDataSource(context, sourceUri, null);
                    }
                } else {
                    extractor.setDataSource(context, sourceUri, null);
                }

                int trackCount = extractor.getTrackCount();
                if (trackCount <= 0) {
                    if (extractor != null) extractor.release();
                    int d = detectMediaDurationMs(context, sourceUri, rawResId);
                    mainHandler.post(() -> callback.onReady(sourceUri, d));
                    return;
                }

                tempFile = new File(context.getCacheDir(), "temp_" + cacheFileName);
                if (tempFile.exists()) tempFile.delete();

                muxer = new MediaMuxer(tempFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                int[] trackMap = new int[trackCount];
                boolean[] isAudioTrack = new boolean[trackCount];
                boolean hasAudio = false;
                boolean hasVideo = false;
                long maxTrackDurationUs = 0;

                for (int i = 0; i < trackCount; i++) {
                    MediaFormat format = extractor.getTrackFormat(i);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    if (mime != null) {
                        if (mime.startsWith("video/")) {
                            trackMap[i] = muxer.addTrack(format);
                            extractor.selectTrack(i);
                            hasVideo = true;
                            if (format.containsKey(MediaFormat.KEY_DURATION)) {
                                maxTrackDurationUs = Math.max(maxTrackDurationUs, format.getLong(MediaFormat.KEY_DURATION));
                            }
                        } else if (mime.startsWith("audio/")) {
                            trackMap[i] = muxer.addTrack(format);
                            extractor.selectTrack(i);
                            isAudioTrack[i] = true;
                            hasAudio = true;
                            if (format.containsKey(MediaFormat.KEY_DURATION)) {
                                maxTrackDurationUs = Math.max(maxTrackDurationUs, format.getLong(MediaFormat.KEY_DURATION));
                            }
                        } else {
                            trackMap[i] = -1;
                        }
                    } else {
                        trackMap[i] = -1;
                    }
                }

                if (!hasVideo) {
                    muxer.release();
                    extractor.release();
                    int d = detectMediaDurationMs(context, sourceUri, rawResId);
                    mainHandler.post(() -> callback.onReady(sourceUri, d));
                    return;
                }

                muxer.start();

                ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024);
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                long[] lastTrackTimeUs = new long[trackCount];
                for (int i = 0; i < trackCount; i++) {
                    lastTrackTimeUs[i] = -1;
                }

                int audioSampleCount = 0;
                int videoSampleCount = 0;

                while (true) {
                    int trackIndex = extractor.getSampleTrackIndex();
                    if (trackIndex < 0) {
                        break;
                    }

                    int muxerTrackIndex = trackMap[trackIndex];
                    if (muxerTrackIndex >= 0) {
                        int sampleSize = extractor.readSampleData(buffer, 0);
                        if (sampleSize < 0) {
                            break;
                        }

                        long sampleTime = extractor.getSampleTime();
                        if (sampleTime < 0) sampleTime = 0;

                        if (lastTrackTimeUs[trackIndex] >= 0 && sampleTime <= lastTrackTimeUs[trackIndex]) {
                            sampleTime = lastTrackTimeUs[trackIndex] + 500;
                        }
                        lastTrackTimeUs[trackIndex] = sampleTime;

                        if (sampleTime > maxTrackDurationUs) {
                            maxTrackDurationUs = sampleTime;
                        }

                        bufferInfo.offset = 0;
                        bufferInfo.size = sampleSize;
                        bufferInfo.presentationTimeUs = sampleTime;
                        bufferInfo.flags = extractor.getSampleFlags();

                        muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo);

                        if (isAudioTrack[trackIndex]) {
                            audioSampleCount++;
                        } else {
                            videoSampleCount++;
                        }
                    }

                    extractor.advance();
                }

                muxer.stop();
                muxer.release();
                extractor.release();
                muxer = null;
                extractor = null;

                if (hasAudio && audioSampleCount == 0) {
                    Log.w(TAG, "Không ghi nhận được sample âm thanh, hủy bỏ cache và fallback về nguồn gốc.");
                    if (tempFile != null && tempFile.exists()) tempFile.delete();
                    int d = detectMediaDurationMs(context, sourceUri, rawResId);
                    mainHandler.post(() -> callback.onReady(sourceUri, d));
                    return;
                }

                int calculatedDurationMs = (int) (maxTrackDurationUs / 1000);
                if (calculatedDurationMs <= 0) {
                    calculatedDurationMs = detectMediaDurationMs(context, sourceUri, rawResId);
                }

                if (tempFile != null && tempFile.exists() && tempFile.length() > 500000) {
                    if (cacheFile.exists()) cacheFile.delete();
                    tempFile.renameTo(cacheFile);
                    final int finalDur = calculatedDurationMs;
                    mainHandler.post(() -> callback.onReady(Uri.fromFile(cacheFile), finalDur));
                } else {
                    final int finalDur = calculatedDurationMs;
                    mainHandler.post(() -> callback.onReady(sourceUri, finalDur));
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi chuẩn hóa MP4: " + e.getMessage(), e);
                try {
                    if (muxer != null) muxer.release();
                } catch (Exception ignored) {}
                try {
                    if (extractor != null) extractor.release();
                } catch (Exception ignored) {}
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
                int d = detectMediaDurationMs(context, sourceUri, rawResId);
                mainHandler.post(() -> callback.onReady(sourceUri, d));
            }
        }).start();
    }
}
