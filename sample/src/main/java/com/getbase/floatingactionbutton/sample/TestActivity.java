package com.getbase.floatingactionbutton.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

public class TestActivity extends Activity {

	private FloatingActionsMenu floatingActionsMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

		floatingActionsMenu = (FloatingActionsMenu) findViewById(R.id.inflate);

		floatingActionsMenu.inflate(R.menu.app_menu_second);
		floatingActionsMenu.inflate(R.menu.app_menu);
		
		findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!floatingActionsMenu.isExpanded()) {
					floatingActionsMenu.expand();
				}
				floatingActionsMenu.clear();
			}
		});
		findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				floatingActionsMenu.inflate(R.menu.app_menu_third);
			}
		});
		findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				floatingActionsMenu.clear();
				floatingActionsMenu.inflate(R.menu.app_menu_second);
			}
		});
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
			case R.id.menu_third_1:
				Toast.makeText(this, "Menu third 2", Toast.LENGTH_SHORT).show();
				break;
		}
		return false;
	}
}
