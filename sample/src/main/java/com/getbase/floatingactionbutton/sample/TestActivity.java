package com.getbase.floatingactionbutton.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

public class TestActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

		FloatingActionsMenu floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.inflate);

		floatingActionsMenu.inflate(R.menu.app_menu_second);
		floatingActionsMenu.inflate(R.menu.app_menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.app_menu, menu);
		getMenuInflater().inflate(R.menu.app_menu_second, menu);
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
}
