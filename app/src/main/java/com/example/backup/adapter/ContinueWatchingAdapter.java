package com.example.backup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.backup.R;
import com.example.backup.model.Movie;

import java.util.List;

public class ContinueWatchingAdapter extends RecyclerView.Adapter<ContinueWatchingAdapter.ViewHolder> {

    public interface OnContinueWatchingListener {
        void onPlay(Movie movie);
        void onRemove(Movie movie);
    }

    private final Context context;
    private final List<Movie> movieList;
    private final OnContinueWatchingListener listener;

    public ContinueWatchingAdapter(Context context, List<Movie> movieList, OnContinueWatchingListener listener) {
        this.context = context;
        this.movieList = movieList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_continue_watching, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.tvTitle.setText(movie.getTitle());

        String progressText = "Đã xem " + movie.getFormattedWatchProgress();
        if (movie.getWatchDuration() > 0) {
            progressText += " • " + movie.getRemainingTimeText();
        }
        holder.tvProgressText.setText(progressText);
        holder.pbProgress.setProgress(movie.getWatchProgressPercent());

        Glide.with(context)
                .load(movie.getPosterPath())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.imgPoster);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPlay(movie);
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemove(movie);
        });
    }

    @Override
    public int getItemCount() {
        return movieList != null ? movieList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView tvTitle, tvProgressText;
        ProgressBar pbProgress;
        ImageButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgCwPoster);
            tvTitle = itemView.findViewById(R.id.tvCwTitle);
            tvProgressText = itemView.findViewById(R.id.tvCwProgressText);
            pbProgress = itemView.findViewById(R.id.pbCwProgress);
            btnRemove = itemView.findViewById(R.id.btnCwRemove);
        }
    }
}
