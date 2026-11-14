package com.example.backup.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * Tiện ích chuẩn hóa video (Defragment MP4):
 * Chuyển đổi các file MP4 phân mảnh (fMP4) thành MP4 tiêu chuẩn có đầy đủ bảng mẫu (stbl, stss)
 * Giúp Android MediaPlayer / VideoView có thể tua (seek) đến bất kỳ giây nào và đọc đúng thời lượng 100%.
 */
public class VideoRemuxer {
    private static final String TAG = "VideoRemuxer";

    public interface RemuxCallback {
        void onReady(Uri uri, int durationMs);
    }

    public static void prepareVideoUri(Context context, Uri sourceUri, int rawResId, RemuxCallback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            try {
                // Đặt tên file cache chuẩn hóa
                String cacheFileName = (rawResId != 0 
                        ? "standard_raw_" + rawResId 
                        : "standard_url_" + Math.abs(sourceUri.toString().hashCode())) + ".mp4";
                File cacheFile = new File(context.getCacheDir(), cacheFileName);

                // Nếu file chuẩn hóa đã tồn tại trong cache, sử dụng ngay lập tức
                if (cacheFile.exists() && cacheFile.length() > 50000) {
                    mainHandler.post(() -> callback.onReady(Uri.fromFile(cacheFile), 0));
                    return;
                }

                MediaExtractor extractor = new MediaExtractor();
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
                    extractor.release();
                    mainHandler.post(() -> callback.onReady(sourceUri, 0));
                    return;
                }

                File tempFile = new File(context.getCacheDir(), "temp_" + cacheFileName);
                if (tempFile.exists()) tempFile.delete();

                MediaMuxer muxer = new MediaMuxer(tempFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                int[] trackMap = new int[trackCount];
                boolean hasValidTracks = false;
                long maxTrackDurationUs = 0;

                for (int i = 0; i < trackCount; i++) {
                    MediaFormat format = extractor.getTrackFormat(i);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    if (mime != null && (mime.startsWith("video/") || mime.startsWith("audio/"))) {
                        trackMap[i] = muxer.addTrack(format);
                        extractor.selectTrack(i);
                        hasValidTracks = true;
                        if (format.containsKey(MediaFormat.KEY_DURATION)) {
                            maxTrackDurationUs = Math.max(maxTrackDurationUs, format.getLong(MediaFormat.KEY_DURATION));
                        }
                    } else {
                        trackMap[i] = -1;
                    }
                }

                if (!hasValidTracks) {
                    muxer.release();
                    extractor.release();
                    mainHandler.post(() -> callback.onReady(sourceUri, 0));
                    return;
                }

                muxer.start();

                ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024);
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                long lastSampleTimeUs = 0;

                while (true) {
                    int trackIndex = extractor.getSampleTrackIndex();
                    if (trackIndex < 0) {
                        break;
                    }

                    int muxerTrackIndex = trackMap[trackIndex];
                    if (muxerTrackIndex >= 0) {
                        bufferInfo.offset = 0;
                        bufferInfo.size = extractor.readSampleData(buffer, 0);
                        bufferInfo.presentationTimeUs = extractor.getSampleTime();
                        bufferInfo.flags = extractor.getSampleFlags();

                        if (bufferInfo.presentationTimeUs > lastSampleTimeUs) {
                            lastSampleTimeUs = bufferInfo.presentationTimeUs;
                        }

                        muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo);
                    }

                    extractor.advance();
                }

                muxer.stop();
                muxer.release();
                extractor.release();

                int calculatedDurationMs = (int) (Math.max(maxTrackDurationUs, lastSampleTimeUs) / 1000);

                if (tempFile.length() > 10000) {
                    if (cacheFile.exists()) cacheFile.delete();
                    tempFile.renameTo(cacheFile);
                    mainHandler.post(() -> callback.onReady(Uri.fromFile(cacheFile), calculatedDurationMs));
                } else {
                    mainHandler.post(() -> callback.onReady(sourceUri, calculatedDurationMs));
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi chuẩn hóa MP4: " + e.getMessage(), e);
                // Nếu có bất kỳ sự cố nào, fallback về nguồn gốc an toàn
                mainHandler.post(() -> callback.onReady(sourceUri, 0));
            }
        }).start();
    }
}
