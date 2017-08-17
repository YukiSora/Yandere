package moe.yukisora.yandere.interfaces;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

public abstract class RecyclerViewOnScrollListener extends RecyclerView.OnScrollListener implements OnBottomListener {
    private int lastPosition;

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);

        StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager)recyclerView.getLayoutManager();
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        if ((visibleItemCount > 0 && newState == RecyclerView.SCROLL_STATE_IDLE &&
                (lastPosition) >= totalItemCount - 1)) {
            onBottom();
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager)recyclerView.getLayoutManager();
        int[] lastPositions = new int[layoutManager.getSpanCount()];
        layoutManager.findLastVisibleItemPositions(lastPositions);
        lastPosition = lastPositions[0];
        for (int i : lastPositions) {
            if (i > lastPosition) {
                lastPosition = i;
            }
        }
    }
}
