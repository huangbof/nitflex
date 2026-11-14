package com.example.backup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.backup.R;
import com.example.backup.model.Comment;

import java.util.ArrayList;
import java.util.List;

public class AdminCommentAdapter extends RecyclerView.Adapter<AdminCommentAdapter.ViewHolder> {

    public interface OnCommentDeleteListener {
        void onDelete(Comment comment);
    }

    private final Context context;
    private final List<Comment> commentsFull = new ArrayList<>();
    private final List<Comment> commentsDisplay = new ArrayList<>();
    private final OnCommentDeleteListener deleteListener;

    public AdminCommentAdapter(Context context, List<Comment> comments, OnCommentDeleteListener deleteListener) {
        this.context = context;
        this.deleteListener = deleteListener;
        setComments(comments);
    }

    public void setComments(List<Comment> comments) {
        this.commentsFull.clear();
        this.commentsDisplay.clear();
        if (comments != null) {
            this.commentsFull.addAll(comments);
            this.commentsDisplay.addAll(comments);
        }
        notifyDataSetChanged();
    }

    public void filter(String query) {
        commentsDisplay.clear();
        if (query == null || query.trim().isEmpty()) {
            commentsDisplay.addAll(commentsFull);
        } else {
            String lower = query.toLowerCase().trim();
            for (Comment c : commentsFull) {
                boolean matchUser = c.getUsername() != null && c.getUsername().toLowerCase().contains(lower);
                boolean matchMovie = c.getMovieTitle() != null && c.getMovieTitle().toLowerCase().contains(lower);
                boolean matchContent = c.getContent() != null && c.getContent().toLowerCase().contains(lower);
                if (matchUser || matchMovie || matchContent) {
                    commentsDisplay.add(c);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment c = commentsDisplay.get(position);

        String uName = (c.getUsername() != null && !c.getUsername().isEmpty()) ? c.getUsername() : "Người dùng";
        holder.tvUsername.setText(uName);
        holder.tvAvatarLetter.setText(uName.substring(0, 1).toUpperCase());

        String mTitle = (c.getMovieTitle() != null && !c.getMovieTitle().isEmpty()) ? c.getMovieTitle() : "Phim #" + c.getMovieId();
        holder.tvMovieTitle.setText("🎬 " + mTitle);

        holder.tvContent.setText(c.getContent() != null ? c.getContent() : "");

        String dateStr = (c.getCreatedAt() != null && c.getCreatedAt().length() >= 16)
                ? c.getCreatedAt().substring(0, 16)
                : (c.getCreatedAt() != null ? c.getCreatedAt() : "Gần đây");
        holder.tvTime.setText("🕒 " + dateStr);

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(c);
        });
    }

    @Override
    public int getItemCount() {
        return commentsDisplay.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarLetter, tvUsername, tvMovieTitle, tvContent, tvTime;
        Button btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarLetter = itemView.findViewById(R.id.tvCommentUserAvatar);
            tvUsername     = itemView.findViewById(R.id.tvCommentUsername);
            tvMovieTitle   = itemView.findViewById(R.id.tvCommentMovieTitle);
            tvContent      = itemView.findViewById(R.id.tvCommentContent);
            tvTime         = itemView.findViewById(R.id.tvCommentTime);
            btnDelete      = itemView.findViewById(R.id.btnDeleteComment);
        }
    }
}
