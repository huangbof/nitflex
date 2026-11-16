package com.example.backup.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.backup.R;
import com.example.backup.model.Movie;

import java.util.List;
import java.util.Locale;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    private final Context context;
    private List<Movie> movies;
    private final OnMovieClickListener listener;

    public MovieAdapter(Context context, List<Movie> movies, OnMovieClickListener listener) {
        this.context = context;
        this.movies = movies;
        this.listener = listener;
    }

    public MovieAdapter(Context context, List<Movie> movies) {
        this(context, movies, movie -> {
            if (movie != null) {
                Intent intent = new Intent(context, com.example.backup.view.MovieDetailActivity.class);
                intent.putExtra("movie_id", movie.getId());
                intent.putExtra("movie", movie);
                context.startActivity(intent);
            }
        });
    }

    public void updateData(List<Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.tvTitle.setText(movie.getTitle());
        holder.tvRating.setText(String.format(Locale.US, "★ %.1f", movie.getVoteAverage()));
        if (movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4) {
            holder.tvYear.setText(movie.getReleaseDate().substring(0, 4));
        } else {
            holder.tvYear.setText("");
        }

        if (holder.tvVipBadge != null) {
            holder.tvVipBadge.setVisibility(movie.isVip() ? View.VISIBLE : View.GONE);
        }

        Glide.with(context)
                .load(movie.getPosterPath())
                .placeholder(R.drawable.bg_genre_chip)
                .error(R.drawable.bg_genre_chip)
                .into(holder.imgPoster);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMovieClick(movie);
        });
    }

    @Override
    public int getItemCount() {
        return movies != null ? movies.size() : 0;
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView tvTitle, tvRating, tvYear, tvVipBadge;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster  = itemView.findViewById(R.id.imgPoster);
            tvTitle    = itemView.findViewById(R.id.tvTitle);
            tvRating   = itemView.findViewById(R.id.tvRating);
            tvYear     = itemView.findViewById(R.id.tvYear);
            tvVipBadge = itemView.findViewById(R.id.tvVipBadge);
        }
    }
}
