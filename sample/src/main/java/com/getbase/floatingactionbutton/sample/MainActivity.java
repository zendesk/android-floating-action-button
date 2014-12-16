package com.getbase.floatingactionbutton.sample;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    findViewById(R.id.pink_icon).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(MainActivity.this, "Clicked pink Floating Action Button", Toast.LENGTH_SHORT).show();
      }
    });

    FloatingActionButton button = (FloatingActionButton) findViewById(R.id.setter);
    button.setSize(FloatingActionButton.SIZE_MINI);
    button.setColorNormalResId(R.color.pink);
    button.setColorPressedResId(R.color.pink_pressed);
    button.setIcon(R.drawable.ic_fab_star);

    FloatingActionButton actionC = new FloatingActionButton(getBaseContext());
    ((FloatingActionsMenu) findViewById(R.id.multiple_actions)).addButton(actionC);

    final FloatingActionButton actionA = (FloatingActionButton) findViewById(R.id.action_a);
    actionA.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        actionA.setTitle("Action A clicked");
      }
    });

    FloatingActionButton setDrawableButton = (FloatingActionButton) findViewById(R.id.setter_drawable);
	setDrawableButton.setDrawable(getResources().getDrawable(R.drawable.ic_fab_star));
  }
}
