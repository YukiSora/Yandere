package moe.yukisora.yandere;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ImageViewActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        ((TextView)findViewById(R.id.textView)).setText("Nico Nico Ni");
    }
}
