package com.example.myimagegallery;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class ZoomOutPageTransformer implements ViewPager2.PageTransformer {
    // Độ thu phóng
    private static final float MIN_SCALE = 0.85f;
    // Độ mờ
    private static final float MIN_ALPHA = 0.5f;

    @Override
    public void transformPage(@NonNull View view, float position) {
        // Khi ảnh ra khỏi vùng nhìn
        if (position < -1 || position > 1) {
            view.setAlpha(0f);
        } else {
            // Độ thu phóng thấp nhất là 85% hoặc 100%
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            // Thu phóng ngang dọc theo độ thu phóng và kích thước màn hình
            float vertMargin = view.getHeight() * (1 - scaleFactor) / 2;
            float horzMargin = view.getWidth() * (1 - scaleFactor) / 2;

            if (position < 0) {
                view.setTranslationX(horzMargin - vertMargin / 2);
            } else {
                view.setTranslationX(-horzMargin + vertMargin / 2);
            }

            // Áp dụng các hiệu ứng chuyển cảnh
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
            view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
        }
    }
}
