package moe.yukisora.yandere;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private PostFragment fragment;

    public RecyclerViewAdapter(Fragment fragment) {
        this.fragment = (PostFragment)fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ImageData imageData = (fragment.getImageDatas()).get(position);
        holder.imageView.setImageBitmap(imageData.getBitmap());
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("moe.yukisora.yandere.ImageViewActivity");
                Bundle bundle = new Bundle();
                bundle.putSerializable("imageData", imageData);
                intent.putExtras(bundle);
                fragment.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fragment.getImageDatas().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView)((ViewGroup)view).getChildAt(0);
        }
    }
}
