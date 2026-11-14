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
import com.example.backup.model.User;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    public interface OnUserActionListener {
        void onToggleVip(User user);
        void onResetPassword(User user);
        void onDelete(User user);
    }

    private final Context context;
    private final List<User> users;
    private final OnUserActionListener listener;

    public AdminUserAdapter(Context context, List<User> users, OnUserActionListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User u = users.get(position);

        holder.tvUsername.setText(u.getUsername());
        holder.tvEmail.setText(u.getEmail() != null && !u.getEmail().isEmpty() ? u.getEmail() : "Chưa cập nhật email");

        String initial = (!u.getUsername().isEmpty()) ? u.getUsername().substring(0, 1).toUpperCase() : "U";
        holder.tvAvatarLetter.setText(initial);

        String dateStr = (u.getCreatedAt() != null && u.getCreatedAt().length() >= 10)
                ? u.getCreatedAt().substring(0, 10) : "Gần đây";
        holder.tvId.setText("ID: #" + u.getId() + " • Tham gia: " + dateStr);

        if (u.isVip()) {
            holder.tvBadge.setText("👑 VIP");
            holder.tvBadge.setVisibility(View.VISIBLE);
            holder.btnToggleVip.setText("Hủy VIP");
        } else {
            holder.tvBadge.setVisibility(View.GONE);
            holder.btnToggleVip.setText("👑 Lên VIP");
        }

        // Không cho phép xóa chính tài khoản admin gốc
        if ("admin".equalsIgnoreCase(u.getUsername())) {
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.btnDelete.setVisibility(View.VISIBLE);
        }

        holder.btnToggleVip.setOnClickListener(v -> {
            if (listener != null) listener.onToggleVip(u);
        });

        holder.btnResetPass.setOnClickListener(v -> {
            if (listener != null) listener.onResetPassword(u);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(u);
        });
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarLetter, tvUsername, tvBadge, tvEmail, tvId;
        Button btnToggleVip, btnResetPass, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarLetter = itemView.findViewById(R.id.tvAdminUserAvatarLetter);
            tvUsername = itemView.findViewById(R.id.tvAdminUsername);
            tvBadge = itemView.findViewById(R.id.tvAdminUserBadge);
            tvEmail = itemView.findViewById(R.id.tvAdminUserEmail);
            tvId = itemView.findViewById(R.id.tvAdminUserId);
            btnToggleVip = itemView.findViewById(R.id.btnAdminToggleVip);
            btnResetPass = itemView.findViewById(R.id.btnAdminResetPass);
            btnDelete = itemView.findViewById(R.id.btnAdminDeleteUser);
        }
    }
}
