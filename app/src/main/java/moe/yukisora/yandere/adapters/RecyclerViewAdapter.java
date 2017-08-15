package moe.yukisora.yandere.adapters;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import moe.yukisora.yandere.modles.ImageData;
import moe.yukisora.yandere.MainActivity;
import moe.yukisora.yandere.R;
import moe.yukisora.yandere.fragments.PostFragment;

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
        Bitmap bitmap = imageData.getBitmap();

        holder.layout.getLayoutParams().height = imageData.layout_height;
        //if is placeholder image
        if (bitmap.getWidth() == MainActivity.getSmallPlaceholderSize() && bitmap.getHeight() == MainActivity.getSmallPlaceholderSize())
            holder.imageView.getLayoutParams().width = MainActivity.getSmallPlaceholderSize() / (int)(MainActivity.getDpi() / 160f);
        else
            holder.imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        holder.imageView.setImageBitmap(bitmap);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("moe.yukisora.yandere.activities.ImageViewActivity");
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
        RelativeLayout layout;

        public ViewHolder(View view) {
            super(view);

            imageView = (ImageView)view.findViewById(R.id.itemImageView);
            layout = (RelativeLayout)view.findViewById(R.id.itemLayout);
        }
    }
}
