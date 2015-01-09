package com.getbase.floatingactionbutton.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

public class TestActivity extends Activity implements FloatingActionsMenu.OnFloatingActionsMenuUpdateListener {
	private FloatingActionsMenu floatingActionsMenu;
	private int menuCheck = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

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
