package com.example.backup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.backup.R;
import com.example.backup.model.Cast;

import java.util.List;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {

    private final Context context;
    private final List<Cast> castList;

    public CastAdapter(Context context, List<Cast> castList) {
        this.context = context;
        this.castList = castList;
    }

    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cast, parent, false);
        return new CastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
        Cast cast = castList.get(position);
        holder.tvActorName.setText(cast.getActorName());
        holder.tvCharacterName.setText(cast.getCharacterName());

        String path = cast.getProfilePath();
        if (path != null && !path.isEmpty()) {
            Glide.with(context)
                    .load(path)
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_person);
        }
    }

    @Override
    public int getItemCount() {
        return castList != null ? castList.size() : 0;
    }

    static class CastViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvActorName;
        TextView tvCharacterName;

        public CastViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgCastAvatar);
            tvActorName = itemView.findViewById(R.id.tvActorName);
            tvCharacterName = itemView.findViewById(R.id.tvCharacterName);
        }
    }
}
