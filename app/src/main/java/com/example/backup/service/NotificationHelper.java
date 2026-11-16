package com.example.backup.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.backup.R;
import com.example.backup.model.Movie;
import com.example.backup.view.MovieDetailActivity;

public class NotificationHelper {

    public static final String CHANNEL_ID = "nitflex_movies_channel";
    public static final String CHANNEL_NAME = "Phim Mới Nit Flex";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo cập nhật phim mới phát hành trên ứng dụng Nit Flex");
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showMovieNotification(Context context, Movie movie) {
        if (context == null || movie == null) return;

        createNotificationChannel(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        Intent intent = new Intent(context, MovieDetailActivity.class);
        intent.putExtra("movie_id", movie.getId());
        intent.putExtra("movie", movie);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                movie.getId(),
                intent,
                flags
        );

        String overview = (movie.getOverview() != null && !movie.getOverview().isEmpty())
                ? movie.getOverview()
                : "Phim bom tấn đặc sắc đã có mặt trên rạp phim Nit Flex. Chạm vào thông báo để xem ngay!";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_play_arrow)
                .setContentTitle("🎬 PHIM MỚI: " + movie.getTitle())
                .setContentText("Điểm TMDB: ★ " + movie.getVoteAverage() + " • Nhấn để xem ngay!")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle("🎬 " + movie.getTitle() + " (★ " + movie.getVoteAverage() + " TMDB)")
                        .bigText(overview))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        try {
            NotificationManagerCompat.from(context).notify(movie.getId(), builder.build());
        } catch (SecurityException ignored) {

        }
    }
}
