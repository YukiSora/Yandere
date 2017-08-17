package moe.yukisora.yandere.adapters;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.YandereApplication;
import moe.yukisora.yandere.fragments.PostFragment;
import moe.yukisora.yandere.modles.ImageData;

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
        ImageData imageData = (fragment.getImageDatas()).get(position);

        holder.layout.getLayoutParams().height = imageData.layout_height;
        holder.imageView.getLayoutParams().width = YandereApplication.getSmallPlaceholderSize() / (int)(YandereApplication.getDpi() / 160f);
        loadImage(holder, imageData);
    }

    @Override
    public int getItemCount() {
        return fragment.getImageDatas().size();
    }

    private void loadImage(final ViewHolder holder, final ImageData imageData) {
        Picasso.with(fragment.getActivity())
                .load(imageData.preview_url)
                .tag(imageData.id)
                .placeholder(R.drawable.progress_animation)
                .error(R.drawable.reload)
                .noFade()
                .into(holder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
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
                    public void onError() {
                        holder.imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                loadImage(holder, imageData);
                            }
                        });
                    }
                });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        RelativeLayout layout;

        public ViewHolder(View view) {
            super(view);

            imageView = view.findViewById(R.id.itemImageView);
            layout = view.findViewById(R.id.itemLayout);
        }
    }
}