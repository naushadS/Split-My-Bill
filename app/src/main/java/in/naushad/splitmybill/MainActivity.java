package in.naushad.splitmybill;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsService;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    EditText etBillValue;
    SeekBar sbNoPeople;
    TextView tvSeekbarCounter,tvIndividualAmount;
    CheckBox cbMoreThanTen;
    Button btExit;
    Toast EnterAmountToast,ExitToast;
    String currency,peoplecount;
    int peoplecountint;
    float BillAmount=0;
    float IndividualShare=0;
    private static final int TIME_INTERVAL = 2000;
    private static final int REQUEST_CODE_PERMISSION=2;
    private long mBackPressed;

    //Chrome Custom Tabs
    public static final String SourceCodeURL="https://github.com/naushadS/Split-My-Bill";
    public static final String DevsGithubURL="https://github.com/naushadS";
    static final String STABLE_PACKAGE = "com.android.chrome";
    static final String BETA_PACKAGE = "com.chrome.beta";
    static final String DEV_PACKAGE = "com.chrome.dev";
    static final String LOCAL_PACKAGE = "com.google.android.apps.chrome";
    private static String sPackageNameToUse;
    String finalPackageName;

    CustomTabsClient mClient;
    CustomTabsSession mCustomTabsSession;
    CustomTabsServiceConnection mCustomTabsServiceConnection;
    CustomTabsIntent customTabsIntent;

    //permission required list
    String[] mPermission = {Manifest.permission.ACCESS_COARSE_LOCATION};



	static {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_AUTO);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initXML();


        if(etBillValue.getText().toString().matches(""))
        {
            EnterAmountToast.show();
            sbNoPeople.setProgress(1);
        }
        etBillValue.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                calculateIndividualShare();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

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

        initializeChromeCustomTab();
    }

    private void initXML() {
        getSupportActionBar().setTitle("Split My Bill!");
        etBillValue = (EditText) findViewById(R.id.etBillValue);
        sbNoPeople = (SeekBar) findViewById(R.id.sbNoPeople);
        tvSeekbarCounter = (TextView) findViewById(R.id.tvSeekbarCounter);
        tvIndividualAmount =(TextView) findViewById(R.id.tvIndividualAmount);

        SharedPreferences getPrefs= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        currency = getPrefs.getString("currency", "₹");
        peoplecount=getPrefs.getString("peoplecount", "10");
        peoplecountint=Integer.parseInt(peoplecount);
        sbNoPeople.setMax(peoplecountint);

        btExit =(Button) findViewById(R.id.btExit);

        EnterAmountToast = Toast.makeText(MainActivity.this, "Enter the bill amount!", Toast.LENGTH_SHORT);
        ExitToast = Toast.makeText(getBaseContext(), "Tap Again to Exit", Toast.LENGTH_SHORT);

        // Loading the banner ad in listing menu
        AdView avListingMenu = (AdView) findViewById(R.id.avListingMenu);
        AdRequest adRequestListingMenu = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("B35807BFE8860378315CD2ED919F6980")  // Redmi Note 3
                .build();
        avListingMenu.loadAd(adRequestListingMenu);


        //Requesting permissions
        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission[0])
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        mPermission, REQUEST_CODE_PERMISSION);
            }
        } catch (Exception e) {}
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

    private void menuClickActionSourceCode(){
        if(finalPackageName!=null) {
            customTabsIntent.launchUrl(MainActivity.this, Uri.parse(SourceCodeURL));
        }else{
            Bundle basket = new Bundle();
            basket.putString("URLToOpen",SourceCodeURL);
            basket.putString("WebpageTitle", "Split My Bill! (Source Code)");
            basket.putString("WebpageSubtitle", "Hosted by Github!");
            Intent person = new Intent(MainActivity.this,webViewFallback.class);
            person.putExtras(basket);
            startActivity(person);
        }
    }

    private void initializeChromeCustomTab(){
        try {
            finalPackageName = getPackageNameToUse(getBaseContext());
            //cct marlon jones
            if (finalPackageName != null) {
                mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
                    @Override
                    public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                        //warmup
                        mClient = customTabsClient;
                        mClient.warmup(0L);
                        mCustomTabsSession = mClient.newSession(null);

                        //prefetch
                        /*
                        mCustomTabsSession.mayLaunchUrl(Uri.parse(SourceCodeURL), null, null);
                        mCustomTabsSession.mayLaunchUrl(Uri.parse(DevsGithubURL), null, null);
                        */
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        mClient = null;

                    }
                };
                CustomTabsClient.bindCustomTabsService(MainActivity.this, finalPackageName, mCustomTabsServiceConnection);

                customTabsIntent = new CustomTabsIntent.Builder(mCustomTabsSession)
                        .setShowTitle(true)
                        .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setSecondaryToolbarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left)
                        .setExitAnimations(this, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .setCloseButtonIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_back_white_24dp))
                        .build();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getPackageNameToUse(Context context) {
        if (sPackageNameToUse != null)
            return sPackageNameToUse;

        PackageManager pm = context.getPackageManager();

        // Get default VIEW intent handler.
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
        ResolveInfo defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0);
        String defaultViewHandlerPackageName = null;
        if (defaultViewHandlerInfo != null) {
            defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName;
        }

        // Get all apps that can handle VIEW intents.
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        List<String> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName);
            }
        }

        // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
        // and service calls.
        if (packagesSupportingCustomTabs.isEmpty()) {
            sPackageNameToUse = null;
        } else if (packagesSupportingCustomTabs.size() == 1) {
            sPackageNameToUse = packagesSupportingCustomTabs.get(0);
        } else if (!TextUtils.isEmpty(defaultViewHandlerPackageName)
                && !hasSpecializedHandlerIntents(context, activityIntent)
                && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName)) {
            sPackageNameToUse = defaultViewHandlerPackageName;
        } else if (packagesSupportingCustomTabs.contains(STABLE_PACKAGE)) {
            sPackageNameToUse = STABLE_PACKAGE;
        } else if (packagesSupportingCustomTabs.contains(BETA_PACKAGE)) {
            sPackageNameToUse = BETA_PACKAGE;
        } else if (packagesSupportingCustomTabs.contains(DEV_PACKAGE)) {
            sPackageNameToUse = DEV_PACKAGE;
        } else if (packagesSupportingCustomTabs.contains(LOCAL_PACKAGE)) {
            sPackageNameToUse = LOCAL_PACKAGE;
        }
        return sPackageNameToUse;
    }

    private static boolean hasSpecializedHandlerIntents(Context context, Intent intent) {
        try {
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> handlers = pm.queryIntentActivities(
                    intent,
                    PackageManager.GET_RESOLVED_FILTER);
            if (handlers == null || handlers.size() == 0) {
                return false;
            }
            for (ResolveInfo resolveInfo : handlers) {
                IntentFilter filter = resolveInfo.filter;
                if (filter == null) continue;
                if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) continue;
                if (resolveInfo.activityInfo == null) continue;
                return true;
            }
        } catch (RuntimeException e) {

        }
        return false;
    }
	
    @Override
    protected void onResume() {
        SharedPreferences getPrefs= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        currency = getPrefs.getString("currency", "₹");

        peoplecount=getPrefs.getString("peoplecount", "10");
        peoplecountint=Integer.parseInt(peoplecount);
        sbNoPeople.setMax(peoplecountint);

        calculateIndividualShare();
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            MainActivity.this.unbindService(mCustomTabsServiceConnection);
        }catch (Exception e){}
    }

	@Override
    public void onBackPressed() {
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
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                recreate();
                return true;
            case R.id.action_night_theme_off:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                recreate();
                return true;
            case R.id.action_night_theme_on:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                recreate();
                return true;
            case R.id.action_source_code:
                menuClickActionSourceCode();
                break;
            case R.id.action_email_dev:
                startActivity(new Intent(MainActivity.this, Email_Dev.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, settings.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
