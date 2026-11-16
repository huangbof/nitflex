package com.example.backup.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.backup.R;
import com.example.backup.model.User;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    public interface OnUserActionListener {
        void onToggleVip(User user);
        void onResetPassword(User user);
        void onDelete(User user);
    }

    private final Context context;
    private final List<User> usersFull = new ArrayList<>();
    private final List<User> usersDisplay = new ArrayList<>();
    private final OnUserActionListener listener;

    public AdminUserAdapter(Context context, List<User> users, OnUserActionListener listener) {
        this.context = context;
        this.listener = listener;
        setUsers(users);
    }

    public void setUsers(List<User> list) {
        this.usersFull.clear();
        this.usersDisplay.clear();
        if (list != null) {
            this.usersFull.addAll(list);
            this.usersDisplay.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void filter(String query) {
        usersDisplay.clear();
        if (query == null || query.trim().isEmpty()) {
            usersDisplay.addAll(usersFull);
        } else {
            String lower = query.toLowerCase().trim();
            for (User u : usersFull) {
                boolean matchName = u.getUsername() != null && u.getUsername().toLowerCase().contains(lower);
                boolean matchEmail = u.getEmail() != null && u.getEmail().toLowerCase().contains(lower);
                if (matchName || matchEmail) {
                    usersDisplay.add(u);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User u = usersDisplay.get(position);

        holder.tvUsername.setText(u.getUsername());
        holder.tvEmail.setText(u.getEmail() != null && !u.getEmail().isEmpty() ? u.getEmail() : "Chưa cập nhật email");

        String initial = (!u.getUsername().isEmpty()) ? u.getUsername().substring(0, 1).toUpperCase() : "U";
        holder.tvAvatarLetter.setText(initial);

        String dateStr = (u.getCreatedAt() != null && u.getCreatedAt().length() >= 10)
                ? u.getCreatedAt().substring(0, 10) : "Gần đây";
        holder.tvId.setText("ID: #" + u.getId() + " • Tham gia: " + dateStr);

        boolean isRootAdmin = u.getId() == 1 || "admin".equalsIgnoreCase(u.getUsername());
        if (isRootAdmin) {
            holder.tvBadge.setText("🛡️ ADMIN");
            holder.tvBadge.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnToggleVip.setVisibility(View.GONE);
            holder.btnResetPass.setVisibility(View.VISIBLE);
            holder.btnResetPass.setText("🔑 Đặt lại");
            holder.btnResetPass.getLayoutParams().width = (int) (88 * context.getResources().getDisplayMetrics().density);
        } else {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnToggleVip.setVisibility(View.VISIBLE);
            holder.btnResetPass.setVisibility(View.VISIBLE);
            holder.btnResetPass.setText("🔑");
            holder.btnResetPass.getLayoutParams().width = (int) (40 * context.getResources().getDisplayMetrics().density);
            if (u.isVip()) {
                holder.tvBadge.setText("👑 VIP");
                holder.tvBadge.setVisibility(View.VISIBLE);
                holder.btnToggleVip.setText("Hủy VIP");
                holder.btnToggleVip.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3A3A3C")));
                holder.btnToggleVip.setTextColor(Color.parseColor("#FF5252"));
            } else {
                holder.tvBadge.setVisibility(View.GONE);
                holder.btnToggleVip.setText("👑 Nâng VIP");
                holder.btnToggleVip.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFC107")));
                holder.btnToggleVip.setTextColor(Color.parseColor("#000000"));
            }
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
        return usersDisplay.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarLetter, tvUsername, tvEmail, tvId, tvBadge;
        Button btnToggleVip, btnResetPass, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarLetter = itemView.findViewById(R.id.tvAdminUserAvatarLetter);
            tvUsername = itemView.findViewById(R.id.tvAdminUsername);
            tvEmail = itemView.findViewById(R.id.tvAdminUserEmail);
            tvId = itemView.findViewById(R.id.tvAdminUserId);
            tvBadge = itemView.findViewById(R.id.tvAdminUserBadge);
            btnToggleVip = itemView.findViewById(R.id.btnAdminToggleVip);
            btnResetPass = itemView.findViewById(R.id.btnAdminResetPass);
            btnDelete = itemView.findViewById(R.id.btnAdminDeleteUser);
        }
    }
}
