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
import com.example.backup.model.ChatMessage;
import com.example.backup.model.Movie;
import com.example.backup.view.MovieDetailActivity;

import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 1;
    private static final int TYPE_AI = 2;

    private final Context context;
    private final List<ChatMessage> messageList;

    public ChatAdapter(Context context, List<ChatMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).isUser() ? TYPE_USER : TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_USER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_ai, parent, false);
            return new AiViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        if (holder instanceof UserViewHolder) {
            UserViewHolder userHolder = (UserViewHolder) holder;
            userHolder.tvText.setText(message.getText());
            userHolder.tvTime.setText(message.getTime());
        } else if (holder instanceof AiViewHolder) {
            AiViewHolder aiHolder = (AiViewHolder) holder;
            aiHolder.tvText.setText(message.getText());
            aiHolder.tvTime.setText(message.getTime());

            Movie movie = message.getSuggestedMovie();
            if (movie != null) {
                aiHolder.cardMovie.setVisibility(View.VISIBLE);
                aiHolder.tvMovieTitle.setText(movie.getTitle());
                aiHolder.tvMovieRating.setText(String.format(Locale.US, "★ %.1f TMDB • Bấm để xem", movie.getVoteAverage()));

                String imgUrl = (movie.getBackdropPath() != null && !movie.getBackdropPath().isEmpty())
                        ? movie.getBackdropPath() : movie.getPosterPath();

                Glide.with(context)
                        .load(imgUrl)
                        .placeholder(R.drawable.gradient_banner_overlay)
                        .into(aiHolder.imgPoster);

                aiHolder.cardMovie.setOnClickListener(v -> {
                    Intent intent = new Intent(context, MovieDetailActivity.class);
                    intent.putExtra("movie_id", movie.getId());
                    intent.putExtra("movie", movie);
                    context.startActivity(intent);
                });
            } else {
                aiHolder.cardMovie.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvTime;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvUserChatText);
            tvTime = itemView.findViewById(R.id.tvUserChatTime);
        }
    }

    static class AiViewHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvTime;
        View cardMovie;
        ImageView imgPoster;
        TextView tvMovieTitle, tvMovieRating;

        AiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText          = itemView.findViewById(R.id.tvAiChatText);
            tvTime          = itemView.findViewById(R.id.tvAiChatTime);
            cardMovie       = itemView.findViewById(R.id.cardSuggestedMovie);
            imgPoster       = itemView.findViewById(R.id.imgSuggestedPoster);
            tvMovieTitle    = itemView.findViewById(R.id.tvSuggestedTitle);
            tvMovieRating   = itemView.findViewById(R.id.tvSuggestedRating);
        }
    }
}
