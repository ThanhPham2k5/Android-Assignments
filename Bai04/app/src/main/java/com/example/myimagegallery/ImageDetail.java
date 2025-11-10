package com.example.myimagegallery;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;

public class ImageDetail extends AppCompatActivity {

    private ViewPager2 viewPager;
    private float startX;
    private ArrayList<ImageItem> imageList;
    private int startPosition;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        viewPager = findViewById(R.id.viewPager);

        // Nhận danh sách ảnh & vị trí hiện tại từ Intent
        imageList = getIntent().getParcelableArrayListExtra("image_list");
        startPosition = getIntent().getIntExtra("position", 0);

        ImagePagerAdapter adapter = new ImagePagerAdapter(imageList);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(startPosition, false);

        // Hiệu ứng chuyển ảnh (ZoomOut)
        viewPager.setPageTransformer(new ZoomOutPageTransformer());

        // Lắng nghe cử chỉ 3 con trỏ
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            private int pointerCount = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                pointerCount = event.getPointerCount();

                if (pointerCount == 3) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getX();
                            break;

                        case MotionEvent.ACTION_UP:
                            float endX = event.getX();
                            if (startX - endX > 200) {
                                // Vuốt trái => ảnh kế
                                if (viewPager.getCurrentItem() < imageList.size() - 1)
                                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                            } else if (endX - startX > 200) {
                                // Vuốt phải => ảnh trước
                                if (viewPager.getCurrentItem() > 0)
                                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                            }
                            break;
                    }
                }
                return false;
            }
        });

        ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
