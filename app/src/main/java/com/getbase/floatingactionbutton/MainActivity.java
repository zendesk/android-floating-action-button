package com.getbase.floatingactionbutton;

import android.app.ListActivity;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;

public class MainActivity extends ListActivity {

  private static final String TAG = MainActivity.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, Cheeses.sCheeseStrings));

    final Drawable background = new MaterialBlurFactory().getBackground(this);
    final View button = findViewById(R.id.le_view);
    final LayoutParams params = button.getLayoutParams();
    params.height = background.getMinimumHeight();
    params.width = background.getMinimumWidth();
    button.setBackground(background);
  }

  @Override
  protected void onPause() {
    super.onPause();

    final View button = findViewById(R.id.le_view);
    final int[] coords = new int[2];
    button.getLocationOnScreen(coords);
    final Rect rect = new Rect(coords[0], coords[1], coords[0] + button.getWidth(), coords[1] + button.getHeight());

    Log.d(TAG, rect.toString());
  }
}
