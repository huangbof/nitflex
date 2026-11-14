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
import com.example.backup.model.Cast;

import java.util.List;

public class AdminCastAdapter extends RecyclerView.Adapter<AdminCastAdapter.ViewHolder> {

    public interface OnCastActionListener {
        void onDelete(Cast cast);
    }

    private final Context context;
    private final List<Cast> casts;
    private final OnCastActionListener listener;

    public AdminCastAdapter(Context context, List<Cast> casts, OnCastActionListener listener) {
        this.context = context;
        this.casts = casts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_cast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cast c = casts.get(position);

        holder.tvActor.setText(c.getActorName());
        holder.tvCharacter.setText("vai " + (c.getCharacterName() != null ? c.getCharacterName() : ""));

        Glide.with(context)
                .load(c.getProfilePath())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(holder.imgProfile);

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(c);
        });
    }

    @Override
    public int getItemCount() {
        return casts != null ? casts.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile;
        TextView tvActor, tvCharacter;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgAdminCastProfile);
            tvActor = itemView.findViewById(R.id.tvAdminCastActor);
            tvCharacter = itemView.findViewById(R.id.tvAdminCastCharacter);
            btnDelete = itemView.findViewById(R.id.btnAdminDeleteCast);
        }
    }
}
