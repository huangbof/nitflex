package com.example.backup.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.backup.R;
import com.example.backup.model.Genre;

import java.util.List;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.GenreViewHolder> {

    public interface OnGenreClickListener {
        void onGenreClick(Genre genre);
    }

    private final Context context;
    private final List<Genre> genres;
    private int selectedPosition = 0;
    private final OnGenreClickListener listener;

    public GenreAdapter(Context context, List<Genre> genres, OnGenreClickListener listener) {
        this.context = context;
        this.genres = genres;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_genre, parent, false);
        return new GenreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
        Genre genre = genres.get(position);
        holder.tvGenreName.setText(genre.getName());

        if (position == selectedPosition) {
            holder.tvGenreName.setBackgroundResource(R.drawable.bg_genre_chip_selected);
            holder.tvGenreName.setTextColor(Color.WHITE);
        } else {
            holder.tvGenreName.setBackgroundResource(R.drawable.bg_genre_chip);
            holder.tvGenreName.setTextColor(ContextCompat.getColor(context, R.color.text_gray_light));
        }

        holder.itemView.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            if (listener != null) listener.onGenreClick(genre);
        });
    }

    @Override
    public int getItemCount() {
        return genres != null ? genres.size() : 0;
    }

    public static class GenreViewHolder extends RecyclerView.ViewHolder {
        TextView tvGenreName;

        public GenreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGenreName = itemView.findViewById(R.id.tvGenreName);
        }
    }
}
