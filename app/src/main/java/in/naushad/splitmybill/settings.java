package in.naushad.splitmybill;

import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;

/**
 * Created by Naushad on 21/03/2016.
 */
public class settings extends AppCompatPreferenceActivity{

    static {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        getSupportActionBar().setTitle("Settings");
        //getSupportActionBar().setSubtitle("My App,Your Way ;)");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
