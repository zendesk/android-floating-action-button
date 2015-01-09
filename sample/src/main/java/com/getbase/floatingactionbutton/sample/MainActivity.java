package com.getbase.floatingactionbutton.sample;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import android.app.Activity;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity implements FloatingActionsMenu.OnFloatingActionsMenuUpdateListener {
	private FloatingActionsMenu floatingActionsMenu;
	private int menuCheck = 0;

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
		button.setStrokeVisible(false);

		final View actionB = findViewById(R.id.action_b);

		FloatingActionButton actionC = new FloatingActionButton(getBaseContext());
		actionC.setTitle("Hide/Show Action B");
		actionC.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				actionB.setVisibility(actionB.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
			}
		});
		((FloatingActionsMenu) findViewById(R.id.multiple_actions)).addButton(actionC);

		ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
		drawable.getPaint().setColor(getResources().getColor(R.color.white));
		((FloatingActionButton) findViewById(R.id.setter_drawable)).setIconDrawable(drawable);

		final FloatingActionButton actionA = (FloatingActionButton) findViewById(R.id.action_a);
		actionA.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				actionA.setTitle("Action A clicked");
			}
		});

		FloatingActionButton setDrawableButton = (FloatingActionButton) findViewById(R.id.setter_drawable);
		setDrawableButton.setIconDrawable(getResources().getDrawable(R.drawable.ic_fab_star));

		floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.inflate);
		floatingActionsMenu.setOnFloatingActionsMenuUpdateListener(this);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (menuCheck % 2 == 0) {
			floatingActionsMenu.inflate(R.menu.app_menu);
		} else {
			floatingActionsMenu.inflate(R.menu.app_menu_second);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu1:
				Toast.makeText(this, "Menu 1", Toast.LENGTH_SHORT).show();
				break;
			case R.id.menu2:
				Toast.makeText(this, "Menu 2", Toast.LENGTH_SHORT).show();
				break;
			case R.id.menu_secondary_1:
				Toast.makeText(this, "Menu secondary 1", Toast.LENGTH_SHORT).show();
				break;
			case R.id.menu_secondary_2:
				Toast.makeText(this, "Menu secondary 2", Toast.LENGTH_SHORT).show();
				break;
		}
		return false;
	}

	@Override
	public void onMenuExpanded() {
	}

	@Override
	public void onMenuCollapsed() {
		menuCheck++;
		invalidateOptionsMenu();
	}
}
