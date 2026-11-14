package com.example.backup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.backup.R;
import com.example.backup.model.Movie;

import java.util.List;
import java.util.Locale;

public class AdminMovieAdapter extends RecyclerView.Adapter<AdminMovieAdapter.ViewHolder> {

    public interface OnMovieActionListener {
        void onEdit(Movie movie);
        void onDelete(Movie movie);
    }

    private final Context context;
    private final List<Movie> movies;
    private final OnMovieActionListener listener;

    public AdminMovieAdapter(Context context, List<Movie> movies, OnMovieActionListener listener) {
        this.context = context;
        this.movies = movies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie m = movies.get(position);
        holder.tvTitle.setText(m.getTitle());

        String year = (m.getReleaseDate() != null && m.getReleaseDate().length() >= 4)
                ? m.getReleaseDate().substring(0, 4) : "2024";
        String orig = (m.getOriginalTitle() != null && !m.getOriginalTitle().isEmpty())
                ? m.getOriginalTitle() : m.getTitle();
        holder.tvSubtitle.setText(orig + " • " + year);

        holder.tvStats.setText(String.format(Locale.US, "★ %.1f TMDB • ID: #%d", m.getVoteAverage(), m.getId()));

        // Phân quyền VIP: điểm TMDB > 8.0
        holder.tvVipBadge.setVisibility(m.isVip() ? View.VISIBLE : View.GONE);

        Glide.with(context)
                .load(m.getPosterPath())
                .placeholder(R.drawable.bg_genre_chip)
                .error(R.drawable.bg_genre_chip)
                .into(holder.imgPoster);

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(m);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(m);
        });
    }

    @Override
    public int getItemCount() {
        return movies != null ? movies.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView tvTitle, tvSubtitle, tvStats, tvVipBadge;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgAdminMoviePoster);
            tvTitle = itemView.findViewById(R.id.tvAdminMovieTitle);
            tvSubtitle = itemView.findViewById(R.id.tvAdminMovieSubtitle);
            tvStats = itemView.findViewById(R.id.tvAdminMovieStats);
            tvVipBadge = itemView.findViewById(R.id.tvAdminMovieVipBadge);
            btnEdit = itemView.findViewById(R.id.btnAdminEditMovie);
            btnDelete = itemView.findViewById(R.id.btnAdminDeleteMovie);
        }
    }
}
