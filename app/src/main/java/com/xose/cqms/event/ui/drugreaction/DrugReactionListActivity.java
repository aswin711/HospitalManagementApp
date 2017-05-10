package com.xose.cqms.event.ui.drugreaction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.xose.cqms.event.BootstrapApplication;
import com.xose.cqms.event.R;
import com.xose.cqms.event.ui.base.BootstrapFragmentActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DrugReactionListActivity extends BootstrapFragmentActivity {

    @Bind(R.id.new_incident_fab)
    protected FloatingActionButton newIncidentBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BootstrapApplication.component().inject(this);
        setContentView(R.layout.activity_incident_report_list);
        ButterKnife.bind(this);
        initScreen();
        newIncidentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Hello Snackbar", Snackbar.LENGTH_LONG).show();
                final Intent i = new Intent(DrugReactionListActivity.this, DrugReactionActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_incident_report_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected Activity getActivity() {
        return DrugReactionListActivity.this;
    }


    private void initScreen() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.incident_list_container, new DrugReactionListFragment())
                .commit();
    }
}
