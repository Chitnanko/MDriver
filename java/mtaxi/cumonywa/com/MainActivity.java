package mtaxi.cumonywa.com;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.support.design.widget.NavigationView;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
//import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mtaxi.cumonywa.com.model.DriverLocation;

public class MainActivity extends BaseActivity implements OnMapReadyCallback, RoutingListener, ServiceCallbacks,
        AcceptReject, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private static int Go = 1;
    private static int Pick_up = 2;

    Toolbar toolbar;
    private FusedLocationProviderClient mFusedLocationClient;
    private SupportMapFragment mapFragment;
    private Switch mWorkingSwitch;
    private String customerId = "";
    private LatLng driverLatLng;
    private LatLng stopLatLong;
    private boolean isWorking = false;
    private boolean bound = false;
    private MService mService;
    private Button btnPhone, btnFinish, btnTake;
    private LinearLayout customArrived;
    private String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private LatLng startLatLng;
    private LatLng stopLatLng;
    CustomerRequest customerRequest;

    private ImageView imgDriver;
    private TextView txtName;
    private TextView txtPhone;



    private double startlat;
    private double startlng;
    private double stoplat;
    private double stoplng;
    private String dvName = "";
    private String dvPhone = "";
    private Driver driver;
    private MenuItem about;
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            MService.MyBinder myBinder = (MService.MyBinder) service;
            mService = myBinder.getServiceSystem();
            bound = true;
            mService.setCallbacks(MainActivity.this);

            mWorkingSwitch.setChecked(mService.isWorking());
            customerId = mService.getCustomerId();

            mService.getDriverDb().child(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    driver=dataSnapshot.getValue(Driver.class);
                    if(driver!=null){

                        setDriverProfile(driver);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            //updateAvailable();

           /* mService.getAcceptJob().removeValue();
            mService.getRefWorking().removeValue();
            mService.getCancelRequest().removeValue();
            mService.getRejectJob().removeValue();
            mService.getFinishJob().removeValue();*/

           /* if(mService.isWorking())
                mService.startWorking();*/

            customerRequest = mService.getCustomerRequest();
            if (customerRequest != null) {

                startlat = Double.parseDouble(customerRequest.getStartLat());
                startlng = Double.parseDouble(customerRequest.getStartLng());
                startLatLng = new LatLng(startlat, startlng);

                stoplat = Double.parseDouble(customerRequest.getStopLat());
                stoplng = Double.parseDouble(customerRequest.getStopLng());
                stopLatLng = new LatLng(stoplat, stoplng);

                /*if(cd==null){
                    start();
                }*/

                //this statment is temporary
                //start();
                if (mService.isAcceptJob()) {
                    updateAvailable();
                    getRouteToMarker(driverLatLng, startLatLng, Go);
                } else {
                    start();
                }
            }else if(cd!=null){
                if(cd.isVisible())
                    cancelRequest();
            }

        }


        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private Button btnPickUp;

    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {

            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, About.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        btnPhone = (Button) findViewById(R.id.btnPhone);
        btnPickUp = findViewById(R.id.btnPickUp);
        btnFinish = findViewById(R.id.btnfinish);
        btnTake = findViewById(R.id.btnTakeaPerson);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View view=navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);


        txtName = view.findViewById(R.id.dName);
        txtPhone = view.findViewById(R.id.dPhone);
        imgDriver = view.findViewById(R.id.imgDriver);

        txtName.setText("jjjjjjjjjjjjj");
        txtPhone.setText("jjjjjjjjjjjjjj");


        btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();

                if(driverLatLng!=null && startLatLng!=null){
                    mMap.clear();
                    getRouteToMarker(driverLatLng,startLatLng,Go);
                }
            }
        });

        btnPickUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                if(startLatLng!=null && stopLatLng!=null)
                    getRouteToMarker(startLatLng,stopLatLng,Pick_up);
            }
        });
        btnPhone.setOnClickListener(new View.OnClickListener() {
            String str="";

            @Override
            public void onClick(View view) {
                mService.getCustomerInfo().child(customerId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user=dataSnapshot.getValue(User.class);

                        if(user!=null)
                            str=user.getPhone().toString();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                Intent intent=new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+str));
                startActivity(intent);
            }
        });



        customArrived=(LinearLayout)findViewById(R.id.rOutInfo);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buildGoogleAPIClient();

        mWorkingSwitch = findViewById(R.id.workingSwitch);


        mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                if (isChecked) {
                    isWorking = true;
                    mWorkingSwitch.setChecked(isWorking);
                    if(mService!=null) {
                        mService.setWorking(true);
                        mService.startWorking();

                        if(intent!=null){
                            startService(intent);
                        }
                        connectDriver();
                    }

                } else {
                    isWorking = false;
                    mWorkingSwitch.setChecked(isWorking);
                    if(mService!=null) {
                        mService.setWorking(false);
                        mService.stopWorking();

                        if(intent!=null)
                            stopService(intent);

                        disconnectDriver();
                    }

                }
            }
        });

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference finishJob=mService.getFinishJob();
                finishJob.child(customerId).setValue(driverId);
                customArrived.setVisibility(View.GONE);
                mService.setCustomerId("");
                customerId="";
                mService.setAcceptJob(false);
                mService.setCustomerRequest(null);
                updateAvailable();
            }
        });




        //getAssignedCustomer();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onStop() {
        super.onStop();
        if(bound) {
            unbindService(connection);
            mService.setCallbacks(null);
        }

        if(mGoogleApiClient!=null)
            mGoogleApiClient.disconnect();

    }

    Intent intent;
    @Override
    protected void onStart() {
        super.onStart();
        intent = new Intent(this, MService.class);
        startService(intent);

        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        if(mGoogleApiClient!=null)
            mGoogleApiClient.connect();
    }

    private void disconnectDriver() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        //mService.stopWorking();

    }


    private void connectDriver() {
        updateAvailable();

    }

    private void updateAvailable() {

        DatabaseReference refAvailable =mService.getDriversAvailable();
        DatabaseReference refWorking =mService.getRefWorking();

        try {
            switch (customerId) {
                case "":
                    mMap.clear();
                    customArrived.setVisibility(View.GONE);
                    refAvailable.child(driverId).setValue(driverId);
                    refWorking.child(driverId).removeValue();
                    break;
                default:
                    customArrived.setVisibility(View.VISIBLE);
                    refAvailable.child(driverId).removeValue();
                    refWorking.child(driverId).setValue(driverId);

            }
        }catch (Exception e){
            Log.e("error",e.getMessage());
        }
    }

    private boolean isReady=false;

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mLastLocation = location;
                    driverLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                }
            }
        }
    };

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }



    public void getRouteToMarker(LatLng beginLatLng,LatLng pickupLatLng,int pickIcon) {
        if (pickupLatLng != null && beginLatLng != null) {
            mMap.clear();
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.WALKING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(beginLatLng, pickupLatLng)
                    .key("AIzaSyB2SjC-TnWoou5KmxS-gSSkg0yhb3Fm9hk")
                    .build();
            routing.execute();

            if(pickIcon==Go){

                mMap.addMarker(new MarkerOptions().position(beginLatLng).title("Start Here")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Stop Here")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.passenger)));

            }else if(pickIcon==Pick_up){

                mMap.addMarker(new MarkerOptions().position(beginLatLng).title("Start Here")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("Stop Here")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.busstop)));


            }

            showProgressDialog();

            CameraPosition cameraPosition = new CameraPosition.Builder().target(beginLatLng)
                    .zoom(17).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }else {
            Toast.makeText(getApplicationContext(),"Enable gps",Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            return;
        }

        mMap.setMyLocationEnabled(true);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mGoogleApiClient.connect();


    }

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark};

    @Override
    public void onRoutingFailure(RouteException e) {

        hideProgressDialog();

        if(e != null) {
            Toast.makeText(this, "Error Message: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines!=null) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

        hideProgressDialog();

    }

    @Override
    public void onRoutingCancelled() {
        hideProgressDialog();
    }


    @Override
    public ComponentName startService(Intent service) {
        return super.startService(service);
    }

    private  boolean dialogShow=false;
    CustomerArriveDl cd;
    FragmentManager fmg;
    @Override
    public void start() {
        //getRouteToMarker(latLng);

        if(fmg==null)
            fmg = getSupportFragmentManager();

        // fmg.re

        if(cd==null)
            cd = new CustomerArriveDl();

        cd.setAcceptReject(MainActivity.this);
        Log.e("cd show", "cd showed ///////////////////////////////////////////////");
        cd.show(fmg, "hhhhh", mService.getCustomerRequest());
        //c dialogShow = true;

    }

    @Override
    public void initJob() {

    }


    @Override
    public void cancelRequest() {
        customerId=mService.getCustomerId();

        Log.e("customerCancel:","//////////////////////");

        if(cd!=null){
            cd.dismiss();
        }

        updateAvailable();


    }

    @Override
    public void acceptJob() {
        //dialogShow = true;
        String cid = mService.getCustomerId();
        DatabaseReference activeJob = mService.getActiveJob();
        activeJob.child(cid).setValue(mService.getDriverId());

        mService.setAcceptJob(true);

        DatabaseReference databaseReference = mService.getAssignedCustomerRef();
        databaseReference.removeValue();

        customerId = mService.getCustomerId();
        updateAvailable();

        customerRequest = mService.getCustomerRequest();

        startlat = Double.parseDouble(customerRequest.getStartLat());
        startlng = Double.parseDouble(customerRequest.getStartLng());
        startLatLng = new LatLng(startlat, startlng);

        stoplat = Double.parseDouble(customerRequest.getStopLat());
        stoplng = Double.parseDouble(customerRequest.getStopLng());
        stopLatLng = new LatLng(stoplat, stoplng);


        getRouteToMarker(driverLatLng, startLatLng, Go);
        mService.listenCancelRequest();
        DatabaseReference reference = mService.getDriverLocaiton();

        DriverLocation driverLocation = new DriverLocation();
        try{

            driverLocation.setDriverLat(driverLatLng.latitude);
            driverLocation.setDriverLng(driverLatLng.longitude);

        }catch(Exception e){
            Log.e("driverLocationE:",e.getMessage()+"///////////////////////////");
        }
        reference.child(customerId).setValue(driverLocation);
        // customArrived.setVisibility(View.VISIBLE);

    }

    @Override
    public void rejectJob() {
        //dialogShow=true;
        String cid=mService.getCustomerId();
        DatabaseReference reject=mService.getRejectJob().child(cid);
        reject.setValue(mService.getDriverId(),cid);
        //reject.removeValue();
        mService.setAcceptJob(false);

        DatabaseReference databaseReference=mService.getAssignedCustomerRef();
        databaseReference.removeValue();

        mService.setCustomerId("");
        mService.setCustomerRequest(null);
        customerId="";
        updateAvailable();

    }

    private synchronized void buildGoogleAPIClient(){

        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,0,this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        // updateLocationUI();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        try {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                checkLocationPermission();
                return;
            }
            Task<Location> locationResult = mFusedLocationClient.getLastLocation();
            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    try {
                        if (task.isSuccessful()) {
                            Location location = task.getResult();
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15);
                            mMap.moveCamera(update);
                            // latLng=null;
                        }
                    }catch (Exception e){


                    }

                }
            });
        }catch (Exception e){
            Log.e("Exception:%s",e.getMessage());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(this,"Check your connection",Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if(id==R.id.nav_help){
            Intent intent=new Intent(MainActivity.this,HelpActivity.class);
            startActivity(intent);
        }
        else if(id==R.id.nav_feedback){
            Intent intent=new Intent(Intent.ACTION_SEND);
            intent.setType("text/email");
            String shareBody="Dear..........,"+" ";
            intent.putExtra(Intent.EXTRA_EMAIL,new String[]{"nylcumy@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT,"Feedback");
            intent.putExtra(Intent.EXTRA_TEXT,shareBody);
            startActivity(Intent.createChooser(intent,"Send Feedback"));

        }
        else if(id==R.id.nav_share){
            Intent intent=new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String shareBody="Here is the share content body!";
            intent.putExtra(Intent.EXTRA_SUBJECT,"Subjects");
            intent.putExtra(Intent.EXTRA_TEXT,shareBody);
            startActivity(Intent.createChooser(intent,"Share via"));

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setDriverProfile(@NonNull Driver driverProfile){

        FirebaseStorage storage=FirebaseStorage.getInstance();
        StorageReference storageReference=storage.getReference("images");
        String imageName=driverProfile.getPhone().toString().trim().substring(3)+".jpg";
        StorageReference imgReference=storageReference.child(driverId).child(imageName);

        txtName.setText(driverProfile.getName());
        txtPhone.setText(driverProfile.getPhone());


        try {

            imgReference.getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            Glide.with(getApplicationContext())
                                    .load(uri.toString())
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(imgDriver);

                            Log.e("error", "photo downloaded/////////////////////////////");

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Log.e("error", e.getMessage() + "/////////////////////////////////////");

                }
            });
        }catch (Exception e){
            Log.e("error",e.getMessage());
        }


    }

}
