package com.zlotluk.mapindicat;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.provider.Settings;
import android.view.View;

import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;

import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import logic.DBuild;
import logic.SQLiteDbEvent;
import logic.SQLiteDbFlag;
import model.Eventt;
import model.Flag;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 1;
    private LocationManager locationManager;
    private Location loc;

    private RequestQueue mRequestQue;
    private String URL = "https://fcm.googleapis.com/fcm/send", t = "t2";
    private TextView errorText;
    private Button notif;
    private WebView wv;
    private ListView zdList;
    private RadioGroup radioMapGroup;
    private RadioButton gog, mcz, mczp;
    private List<Coor> coor;
    private SQLiteDbFlag sqL;
    private SQLiteDbEvent sqLe;
    private CheckBox chbx;
    private static final String staLink = "\"https://mapy.cz/?planovani-trasy&rc=9TiZgyOWIweI-N0&rs=coor&rs=coor&ri=&ri=&mrp={\\\"c\\\":111}&mrp={\\\"c\\\":111}\"";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        sqL = new SQLiteDbFlag(this);
        sqLe = new SQLiteDbEvent(this);

        setContentView(R.layout.activity_main);

        loc = new Location("JRG1");
        loc.setLatitude(53.1358759);
        loc.setLongitude(23.16667);

        chbx = (CheckBox) findViewById(R.id.checkBox);
        if (sqL.getAllLabels().size() > 0) {
            if (sqL.getAllFlag().get(0).getDial() == 1) {
                chbx.setChecked(true);
            } else {
                chbx.setChecked(false);
            }
        }

        errorText = (TextView) findViewById(R.id.textView2);

        notif = (Button) findViewById(R.id.notif);

        zdList = (ListView) findViewById(R.id.zdlist);
        if (MyFirebaseMessagingService.isMf()) {
            zdList.setEnabled(false);
        }
        setUpCoor();
        zdList.setAdapter(new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, sqLe.getAllLabels()));

        wv = (WebView) findViewById(R.id.webview);
        wv.setWebChromeClient(new WebChromeClient());
        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadUrl("file:///android_asset/index.html");
        wv.setVisibility(View.GONE);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (MyFirebaseMessagingService.isMf() || MyFirebaseMessagingService.getM().length() > 1) {
                    if (haveNetworkConnection()) {
                        if (sqL.getAllFlag().get(0).getDial() == 1) {
                            zdList.setEnabled(false);
                            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                buildAlertMessageNoGps();
                            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                getLocation();
                            }
                            wybor2(MyFirebaseMessagingService.getOpis(), MyFirebaseMessagingService.getLng(), MyFirebaseMessagingService.getLat());
                            MyFirebaseMessagingService.setMf(false);
                        } else {
                            if (MyFirebaseMessagingService.getM().equals("c")) {
                                zdList.setEnabled(false);
                                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                    buildAlertMessageNoGps();
                                } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                    getLocation();
                                }
                                try {
                                    TimeUnit.MILLISECONDS.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                perform2(Double.toString(loc.getLongitude()), Double.toString(loc.getLatitude()), MyFirebaseMessagingService.getLng(), MyFirebaseMessagingService.getLat(), sqL.getAllFlag().get(0).getMap());
                                MyFirebaseMessagingService.setMf(false);
                            } else {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MyFirebaseMessagingService.getM()));
                                intent.setPackage("com.google.android.apps.maps");
                                startActivity(intent);
                                MyFirebaseMessagingService.setM("m");
                            }
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Brak połączenia z internetem!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        if (haveNetworkConnection()) {
            new Async().execute();
        } else {
            Toast.makeText(getApplicationContext(), "Brak połączenia z internetem!", Toast.LENGTH_LONG).show();
        }


        if (getIntent().hasExtra("lat")) {
            int id = (int) getIntent().getLongExtra("id", 11111);
            String op = getIntent().getStringExtra("op");
            String latt = getIntent().getStringExtra("lat");
            String lngg = getIntent().getStringExtra("lng");
            String uri = "http://maps.google.com/maps?daddr=" + latt + "," + lngg +
                    " (" + op + ")";
            if (sqLe.getAllId().contains(id)) {
                sqLe.update(new Eventt(id, op, latt, lngg));
            } else {
                sqLe.create(new Eventt(id, op, latt, lngg));
            }
            setUpCoor();
            zdList.setAdapter(new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, sqLe.getAllLabels()));
            if (sqL.getAllFlag().get(0).getDial() == 1) {
                zdList.setEnabled(false);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();
                } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    getLocation();
                }
                wybor2(op, lngg, latt);
            } else {
                if (sqL.getAllFlag().get(0).getMap() == 1) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);
                } else {
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        buildAlertMessageNoGps();
                    } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        getLocation();
                    }
                    perform2(Double.toString(loc.getLongitude()), Double.toString(loc.getLatitude()), lngg, latt, sqL.getAllFlag().get(0).getMap());
                }
            }
        }

        mRequestQue = Volley.newRequestQueue(this);
        FirebaseMessaging.getInstance().subscribeToTopic("news");

        chbx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    try {
                        sqL.update(new Flag(1, sqL.getAllFlag().get(0).getMap(), 1));
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        sqL.update(new Flag(1, sqL.getAllFlag().get(0).getMap(), 0));
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        radioMapGroup = (RadioGroup) findViewById(R.id.mwRadioGroup);
        gog = (RadioButton) findViewById(R.id.gog);
        mcz = (RadioButton) findViewById(R.id.mcz);
        mczp = (RadioButton) findViewById(R.id.mczp);

        if (sqL.getAllLabels().size() > 0) {
            if (sqL.getAllFlag().get(0).getMap() == 1) {
                gog.setChecked(true);
            } else if (sqL.getAllFlag().get(0).getMap() == 2) {
                mcz.setChecked(true);
            } else {
                mczp.setChecked(true);
            }
        } else {
            gog.setChecked(true);
        }
        radioMapGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.gog) {
                    try {
                        sqL.update(new Flag(1, 1, sqL.getAllFlag().get(0).getDial()));
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                } else if (checkedId == R.id.mcz) {
                    try {
                        sqL.update(new Flag(1, 2, sqL.getAllFlag().get(0).getDial()));
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        sqL.update(new Flag(1, 3, sqL.getAllFlag().get(0).getDial()));
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        zdList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                t = "t2";
                if (haveNetworkConnection()) {
                    zdList.setEnabled(false);
                    if (sqL.getAllFlag().get(0).getDial() == 1) {
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            buildAlertMessageNoGps();
                        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            getLocation();
                        }
                        wybor(coor.get(position).getOpis(), position);
                    } else {
                        if (sqL.getAllFlag().get(0).getMap() == 1) {
                            String uri = "http://maps.google.com/maps?daddr=" + coor.get(position).getLat() + "," + coor.get(position).getLng() + " (" + coor.get(position).getOpis() + ")" + "&sensor=false&mode-driving&alternatives=false";
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            intent.setPackage("com.google.android.apps.maps");
                            startActivity(intent);
                            zdList.setEnabled(true);
                        } else {
                            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                buildAlertMessageNoGps();
                            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                getLocation();
                            }
                            perform(Double.toString(loc.getLongitude()), Double.toString(loc.getLatitude()), coor.get(position).getLng(), coor.get(position).getLat(), position, sqL.getAllFlag().get(0).getMap());
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Brak połączenia z internetem!", Toast.LENGTH_LONG).show();
                }
            }
        });

        zdList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("USUŃ");
                alertDialog.setMessage("Czy chcesz usunąć zdarzenie " + sqLe.getAllEve().get(position).getOpis() + "?");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "TAK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                sqLe.delete(sqLe.getAllEve().get(position));
                                zdList.setAdapter(new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, sqLe.getAllLabels()));
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "NIE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                return true;
            }
        });

        notif.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
                } else {
                    intent.putExtra("app_package", getPackageName());
                    intent.putExtra("app_uid", getApplicationInfo().uid);
                }


                startActivity(intent);
            }

        });
    }

    public void wybor(String opis, int pos) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                zdList.setEnabled(true);
            }
        });
        alertDialog.setTitle("Wybór mapy:");
        alertDialog.setMessage("Zdarzenie: " + opis);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Google Maps",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String uri = "http://maps.google.com/maps?daddr=" + coor.get(pos).getLat() + "," + coor.get(pos).getLng() + " (" + opis + ")" + "&sensor=false&mode-driving&alternatives=false";
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        intent.setPackage("com.google.android.apps.maps");
                        startActivity(intent);
                        zdList.setEnabled(true);
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Mapy.cz",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            buildAlertMessageNoGps();
                        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            getLocation();
                        }
                        perform(Double.toString(loc.getLongitude()), Double.toString(loc.getLatitude()), coor.get(pos).getLng(), coor.get(pos).getLat(), pos, 2);
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Mapy.cz (pieszo)",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            buildAlertMessageNoGps();
                        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            getLocation();
                        }
                        perform(Double.toString(loc.getLongitude()), Double.toString(loc.getLatitude()), coor.get(pos).getLng(), coor.get(pos).getLat(), pos, 3);
                    }
                });
        alertDialog.show();
    }

    public void wybor2(String opis, String lng2, String lat2) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                zdList.setEnabled(true);
            }
        });
        alertDialog.setTitle("Wybór mapy:");
        alertDialog.setMessage("Zdarzenie: " + opis);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Google Maps",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String uri = "http://maps.google.com/maps?daddr=" + lat2 + "," + lng2 + " (" + opis + ")" + "&sensor=false&mode-driving&alternatives=false";
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        intent.setPackage("com.google.android.apps.maps");
                        startActivity(intent);
                        zdList.setEnabled(true);
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Mapy.cz",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        perform2(Double.toString(loc.getLongitude()), Double.toString(loc.getLatitude()), lng2, lat2, 2);
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Mapy.cz (pieszo)",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        perform2(Double.toString(loc.getLongitude()), Double.toString(loc.getLatitude()), lng2, lat2, 3);
                    }
                });
        alertDialog.show();
    }

    public void perform(String lng1, String lat1, String lng2, String lat2, int position, int m) {
        Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            public void run() {
                genURL(lng1, lat1, lng2, lat2, position);
                if (t.length() < 5 || t.equals(staLink)) {
                    handler.postDelayed(this, 300);
                    genURL(lng1, lat1, lng2, lat2, position);
                } else {
                    String url;
                    if (m == 2) {
                        url = "https://mapy.cz/?planovani-trasy&rc=" + StringUtils.substringBetween(t, "&rc=", "&rs=") + "&rs=coor&rs=coor&ri=&ri=&mrp={\"c\":113}&mrp={\"c\":113}\"";
                    } else {
                        url = "https://mapy.cz/?planovani-trasy&rc=" + StringUtils.substringBetween(t, "&rc=", "&rs=") + "&rs=coor&rs=coor&ri=&ri=&mrp={\"c\":131}&mrp={\"c\":131}\"";
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        wv.evaluateJavascript("javascript:load(8.3247722, 59.4887319,8.3147714,59.4887319);", null);
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    zdList.setEnabled(true);
                }
            }
        };
        handler.post(runnable);
    }

    public void genURL(String lng1, String lat1, String lng2, String lat2, int po) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            wv.evaluateJavascript("javascript:load(" + lng1 + "," + lat1 + "," + lng2 + "," + lat2 + ");", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    t = s;
                }
            });
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void perform2(String lng1, String lat1, String lng2, String lat2, int m) {
        Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            public void run() {
                genURL2(lng1, lat1, lng2, lat2);
                if (t.length() < 5 || t.equals(staLink)) {
                    handler.postDelayed(this, 300);
                    genURL2(lng1, lat1, lng2, lat2);
                } else {
                    zdList.setEnabled(false);
                    String url;
                    if (m == 2) {
                        url = "https://mapy.cz/?planovani-trasy&rc=" + StringUtils.substringBetween(t, "&rc=", "&rs=") + "&rs=coor&rs=coor&ri=&ri=&mrp={\"c\":113}&mrp={\"c\":113}\"";
                    } else {
                        url = "https://mapy.cz/?planovani-trasy&rc=" + StringUtils.substringBetween(t, "&rc=", "&rs=") + "&rs=coor&rs=coor&ri=&ri=&mrp={\"c\":131}&mrp={\"c\":131}\"";
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        wv.evaluateJavascript("javascript:load(8.3247722, 59.4887319,8.3147714,59.4887319);", null);
                    }
                    startActivity(intent);
                    zdList.setEnabled(true);
                }
            }
        };
        handler.post(runnable);
    }

    public void genURL2(String lng1, String lat1, String lng2, String lat2) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            wv.evaluateJavascript("javascript:load(" + lng1 + "," + lat1 + "," + lng2 + "," + lat2 + ");", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    t = s;
                }
            });
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setUpCoor() {
        coor = new ArrayList<Coor>();
        for (Eventt e : sqLe.getAllEve()) {
            coor.add(new Coor(e.getOpis(), e.getLat(), e.getLng()));
        }
    }

    class Coor {
        private String opis, lat, lng;

        public Coor() {
        }

        public Coor(String opis, String lat, String lng) {
            this.opis = opis;
            this.lat = lat;
            this.lng = lng;
        }

        public String getOpis() {
            return opis;
        }

        public void setOpis(String opis) {
            this.opis = opis;
        }

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLng() {
            return lng;
        }

        public void setLng(String lng) {
            this.lng = lng;
        }
    }


    class Async extends AsyncTask<Void, Void, Void> {

        String records = "", error = "";

        Connection connection;
        Statement statement;
        ResultSet resultSet;

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override

        protected Void doInBackground(Void... voids) {
            SQLiteDbFlag sqLf = new SQLiteDbFlag(getApplicationContext());

            if (sqLf.getAllLabels().size() == 0 && haveNetworkConnection()) {
                try {
                    List tokens = new ArrayList<String>();
                    connection = DBuild.getConnection();
                    statement = connection.createStatement();

                    resultSet = statement.executeQuery("SELECT * FROM Tokenn");

                    while (resultSet.next()) {
                        tokens.add(resultSet.getString(1));
                    }

                    String tok;
                    do {
                        tok = FirebaseInstanceId.getInstance().getToken();
                    } while (tok == null);

                    if (!tokens.contains(tok)) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        statement.execute("INSERT INTO Tokenn VALUES ('" + tok + "', FALSE, '" + sdf.format(new Date()) + "')");
                        sqLf.create(new Flag(1, 1, 0));
                    }
                } catch (Exception e) {
                    error = e.toString();
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if (error != "")
                errorText.setText(error + "\n Pula połaczeń darmowego serwera została obecnie wyczerpana. Spróbuj się zalogować później. ");

            super.onPostExecute(aVoid);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (location != null) {
                loc = location;

            } else if (location1 != null) {
                loc = location1;

            } else if (location2 != null) {
                loc = location2;
            } else {
                Toast.makeText(this, "Nie można ustalić lokalizacji", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Włącz połączenie GPS")
                .setCancelable(false)
                .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Nie", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}