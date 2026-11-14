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
import com.example.backup.model.VipPackage;

import java.util.List;

public class AdminVipPackageAdapter extends RecyclerView.Adapter<AdminVipPackageAdapter.ViewHolder> {

    public interface OnVipPackageActionListener {
        void onToggleActive(VipPackage pkg);
        void onEdit(VipPackage pkg);
        void onDelete(VipPackage pkg);
    }

    private final Context context;
    private final List<VipPackage> packages;
    private final OnVipPackageActionListener listener;

    public AdminVipPackageAdapter(Context context, List<VipPackage> packages, OnVipPackageActionListener listener) {
        this.context = context;
        this.packages = packages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_vip_package, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VipPackage p = packages.get(position);

        holder.tvName.setText("👑 " + p.getName());
        holder.tvPrice.setText(p.getFormattedPrice());
        holder.tvDuration.setText(String.format("/ %d ngày (Mã: %s)", p.getDurationDays(), p.getCode()));
        holder.tvDesc.setText(p.getDescription() != null ? p.getDescription() : "");

        if (p.isActive()) {
            holder.tvStatus.setText("ĐANG BẬT");
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.btnToggle.setText("Tắt gói");
        } else {
            holder.tvStatus.setText("ĐÃ TẮT");
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.btnToggle.setText("Bật gói");
        }

        holder.btnToggle.setOnClickListener(v -> {
            if (listener != null) listener.onToggleActive(p);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(p);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(p);
        });
    }

    @Override
    public int getItemCount() {
        return packages != null ? packages.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus, tvPrice, tvDuration, tvDesc;
        Button btnToggle, btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAdminPkgName);
            tvStatus = itemView.findViewById(R.id.tvAdminPkgStatus);
            tvPrice = itemView.findViewById(R.id.tvAdminPkgPrice);
            tvDuration = itemView.findViewById(R.id.tvAdminPkgDuration);
            tvDesc = itemView.findViewById(R.id.tvAdminPkgDesc);
            btnToggle = itemView.findViewById(R.id.btnAdminPkgToggle);
            btnEdit = itemView.findViewById(R.id.btnAdminPkgEdit);
            btnDelete = itemView.findViewById(R.id.btnAdminPkgDelete);
        }
    }
}
