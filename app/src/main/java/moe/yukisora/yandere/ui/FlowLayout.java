package moe.yukisora.yandere.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class FlowLayout extends ViewGroup {
    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getWidth();
        int lines = 0;
        int lineHeight = getLineHeight();

        // setup layout
        for (int i = 0; i < getChildCount(); ) {
            int lineWidth = 0;

            while (i < getChildCount()) {
                View child = getChildAt(i);
                MarginLayoutParams layoutParams = (MarginLayoutParams)child
                        .getLayoutParams();
                int childWidth = child.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;

                if (childWidth + lineWidth > width) {
                    break;
                }

                // calculate left, top, right, bottom
                int lc = lineWidth + layoutParams.leftMargin;
                int tc = lines * lineHeight + layoutParams.topMargin;
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();

                child.layout(lc, tc, rc, bc);

                lineWidth += childWidth;
                i++;
            }
            lines++;
        }
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // only work for width:match_parent, height:wrap_content
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int lines = 0;

        // calculate how many lines
        for (int i = 0; i < getChildCount(); ) {
            int lineWidth = 0;

            while (i < getChildCount()) {
                View child = getChildAt(i);
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                MarginLayoutParams layoutParams = (MarginLayoutParams)child
                        .getLayoutParams();
                int childWidth = child.getMeasuredWidth() + layoutParams.leftMargin
                        + layoutParams.rightMargin;

                if (childWidth + lineWidth > width) {
                    break;
                }

                lineWidth += childWidth;
                i++;
            }
            lines++;
        }

        setMeasuredDimension(width, getLineHeight() * lines);
    }

    private int getLineHeight() {
        if (getChildCount() > 0) {
            View child = getChildAt(0);

            MarginLayoutParams layoutParams = (MarginLayoutParams)child
                    .getLayoutParams();
            return child.getMeasuredHeight() + layoutParams.topMargin
                    + layoutParams.bottomMargin;
        }

        return 0;
    }

    public static class LayoutParams extends MarginLayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }
}
