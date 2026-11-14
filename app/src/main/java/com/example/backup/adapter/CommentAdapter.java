package com.example.backup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.backup.R;
import com.example.backup.model.Comment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final Context context;
    private List<Comment> comments;

    public CommentAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    public void updateData(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        String uName = (comment.getUsername() != null && !comment.getUsername().isEmpty())
                ? comment.getUsername() : "Khách";

        holder.tvUsername.setText(uName);
        holder.tvContent.setText(comment.getContent());
        holder.tvTime.setText(comment.getCreatedAt() != null ? comment.getCreatedAt() : "");

        // Set initial letter
        String initial = uName.substring(0, 1).toUpperCase();
        holder.tvAvatarInitial.setText(initial);
    }

    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarInitial, tvUsername, tvContent, tvTime;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarInitial = itemView.findViewById(R.id.tvAvatarInitial);
            tvUsername      = itemView.findViewById(R.id.tvUsername);
            tvContent       = itemView.findViewById(R.id.tvContent);
            tvTime          = itemView.findViewById(R.id.tvTime);
        }
    }
}
