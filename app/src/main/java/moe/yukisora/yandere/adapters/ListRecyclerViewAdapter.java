package moe.yukisora.yandere.adapters;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import moe.yukisora.yandere.R;
import moe.yukisora.yandere.activities.ImageActivity;
import moe.yukisora.yandere.fragments.ListFragment;
import moe.yukisora.yandere.modles.ImageData;

public class ListRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ListFragment fragment;

    public ListRecyclerViewAdapter(Fragment fragment) {
        this.fragment = (ListFragment)fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder)holder).bindData((fragment.getImageDatas()).get(position));
    }

    @Override
    public int getItemCount() {
        return fragment.getImageDatas().size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        RelativeLayout layout;

        ViewHolder(View view) {
            super(view);

            imageView = view.findViewById(R.id.itemImageView);
            layout = view.findViewById(R.id.itemLayout);
        }

        void bindData(ImageData imageData) {
            layout.getLayoutParams().height = imageData.layout_height;
            imageView.getLayoutParams().width = fragment.getActivity().getResources().getDimensionPixelSize(R.dimen.small_loading_size);
            loadImage(imageData);
        }

        private void loadImage(final ImageData imageData) {
            Picasso.with(fragment.getActivity())
                    .load(imageData.preview_url)
                    .tag(imageData.id)
                    .placeholder(R.drawable.animated_loading)
                    .error(R.drawable.ic_refresh)
                    .noFade()
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            layout.setOnClickListener(null);
                            imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(fragment.getActivity(), ImageActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("imageData", imageData);
                                    intent.putExtras(bundle);
                                    ActivityOptionsCompat options = ActivityOptionsCompat.
                                            makeSceneTransitionAnimation(fragment.getActivity(), imageView, "image");

                                    fragment.startActivity(intent, options.toBundle());
                                }
                            });
                        }

                        @Override
                        public void onError() {
                            layout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    loadImage(imageData);
                                }
                            });
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    loadImage(imageData);
                                }
                            });
                        }
                    });
        }
    }
}
