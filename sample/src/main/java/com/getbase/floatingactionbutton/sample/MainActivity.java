package com.getbase.floatingactionbutton.sample;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {

  private DrawerLayout mDrawerLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    mDrawerLayout.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
              .add(R.id.fragment, new HomeFragment()).commit();
    }

    navigationView.setCheckedItem(R.id.home);
  }

  NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
      mDrawerLayout.closeDrawer(GravityCompat.START);

      Fragment fragment = null;
      final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      switch (item.getItemId()) {
        case R.id.home:
          fragment = new HomeFragment();
          break;
        case R.id.menus:
          fragment = new MenusFragment();
          break;
        case R.id.progress:
          fragment = new ProgressFragment();
          break;
      }

      ft.replace(R.id.fragment, fragment).commit();
      return true;
    }
  };

  @Override
  public void onBackPressed() {
    if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
      mDrawerLayout.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }
}