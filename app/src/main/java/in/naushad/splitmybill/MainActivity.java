package in.naushad.splitmybill;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class MainActivity extends AppCompatActivity {

    EditText etBillValue;
    SeekBar sbNoPeople;
    TextView tvSeekbarCounter,tvIndividualAmount;
    Button btExit;
    Toast EnterAmountToast,ExitToast;
    String currency;
    float BillAmount=0;
    float IndividualShare=0;
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    static{
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    @Override
    public void onBackPressed()
    {
        if(EnterAmountToast!=null)
        {
            EnterAmountToast.cancel();
        }

        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
        {
            ExitToast.cancel();
            super.onBackPressed();
            return;
        }
        else {
            ExitToast.show();
        }
        mBackPressed = System.currentTimeMillis();
    }

    @Override
    protected void onPostResume() {
        SharedPreferences getPrefs= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        currency = getPrefs.getString("currency", "₹");
        calculateIndividualShare();
        super.onPostResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initXML();

        SharedPreferences getPrefs= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        currency = getPrefs.getString("currency", "₹");

        if(etBillValue.getText().toString().matches(""))
        {
            EnterAmountToast.show();
            sbNoPeople.setEnabled(false);
            sbNoPeople.setProgress(1);
        }
        etBillValue.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                sbNoPeople.setEnabled(true);
                sbNoPeople.setProgress(1);
                calculateIndividualShare();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        tvSeekbarCounter.setText(sbNoPeople.getProgress() + "");

        sbNoPeople.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) {
                    sbNoPeople.setProgress(1);
                } else {
                    tvSeekbarCounter.setText(progress + "");
                    calculateIndividualShare();
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        btExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initXML() {
        getSupportActionBar().setTitle("Split My Bill!");
        etBillValue = (EditText) findViewById(R.id.etBillValue);
        sbNoPeople = (SeekBar) findViewById(R.id.sbNoPeople);
        tvSeekbarCounter = (TextView) findViewById(R.id.tvSeekbarCounter);
        tvIndividualAmount =(TextView) findViewById(R.id.tvIndividualAmount);
        btExit =(Button) findViewById(R.id.btExit);

        EnterAmountToast = Toast.makeText(MainActivity.this, "Enter the bill amount!", Toast.LENGTH_SHORT);
        ExitToast = Toast.makeText(getBaseContext(), "Tap Again to Exit", Toast.LENGTH_SHORT);

        // Loading the banner ad in listing menu
        AdView avListingMenu = (AdView) findViewById(R.id.avListingMenu);
        AdRequest adRequestListingMenu = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("A851D03B6D976CAA2BDAABFC232841DC")  // My Xiaomi Redmi 1s test phone
                .addTestDevice("0BCA7BDB8AE649D01EE271E0F9A34C19") //Nexus 7
                .build();
        avListingMenu.loadAd(adRequestListingMenu);
    }

    private void calculateIndividualShare(){
        try {
            BillAmount = Float.valueOf(etBillValue.getText().toString());
            IndividualShare = BillAmount / sbNoPeople.getProgress();

            tvIndividualAmount.setText(currency + " " + String.format("%.2f", IndividualShare));
        }catch (NumberFormatException ne){
            tvIndividualAmount.setText("");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle the click on the back arrow click
        switch (item.getItemId()) {
            case R.id.action_night_theme_auto:
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                recreate();
                return true;
            case R.id.action_night_theme_off:
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                recreate();
                return true;
            case R.id.action_night_theme_on:
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                recreate();
                return true;
            case R.id.action_email_dev:
                startActivity(new Intent(MainActivity.this, Email_Dev.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, settings.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
