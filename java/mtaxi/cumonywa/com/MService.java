package mtaxi.cumonywa.com;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mtaxi.cumonywa.com.DriverStatus;
import mtaxi.cumonywa.com.MainActivity;
import mtaxi.cumonywa.com.R;

public class MService extends Service {
    private boolean isWorking=false;
    private boolean isAcceptJob=false;
    private CustomerRequest customerRequest;
    NotificationManager noti;
    private ServiceCallbacks serviceCallbacks;
    private DatabaseReference driversAvailable;
    private DatabaseReference refWorking;
    private DatabaseReference activeJob;
    private DatabaseReference rejectJob;
    private DatabaseReference cancelRequest;
    private DatabaseReference assignedCustomerRef;
    private DatabaseReference finishJob;
    private DatabaseReference driverDb,customerInfo,connectedRef,
                                operator;



    private DatabaseReference driverLocaiton;

    private String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private String customerId="";
    public boolean isWorking() {
        return isWorking;
    }

    public CustomerRequest getCustomerRequest() {
        return customerRequest;
    }

    public void setCustomerRequest(CustomerRequest customerRequest) {
        this.customerRequest = customerRequest;
    }

    public void setWorking(boolean working) {
        isWorking = working;
    }

    MyBinder binder=new MyBinder();
    MService mService;
    static Context context;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        connectedRef=FirebaseDatabase.getInstance().getReference(".info/connected");
        operator=FirebaseDatabase.getInstance().getReference("operator");
        customerInfo= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");
        driverLocaiton=FirebaseDatabase.getInstance().getReference("driverLocation");
        driversAvailable=FirebaseDatabase.getInstance().getReference("driversAvailable");
        refWorking= FirebaseDatabase.getInstance().getReference("driversWorking");
        activeJob=FirebaseDatabase.getInstance().getReference("activeJob");
        rejectJob=FirebaseDatabase.getInstance().getReference("rejectJob");
        finishJob=FirebaseDatabase.getInstance().getReference("finishJob");
        cancelRequest=FirebaseDatabase.getInstance().getReference("cancelRequest");
        driverDb=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
        assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest");

        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean connected=dataSnapshot.getValue(Boolean.class);
                if(connected){

                    if(!isAcceptJob){
                        driversAvailable.child(driverId).onDisconnect().removeValue();
                        //stopSelf();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*DatabaseReference st=driversAvailable.child(driverId);
        st.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String exitDriver = dataSnapshot.getValue(String.class).;
                Log.d("driverId",exitDriver+"/////////////////////////////////////////////////");
                if (exitDriver != null) {
                    isWorking = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        //startForegroundService();

      /*  activeJob=FirebaseDatabase.getInstance().getReference("activeJob");
        context=getApplication();
        Log.e("service created","service created/////////////////////////////////////");*/

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent,flags,startId);
    }

    private void startForegroundService(){
        Intent in=new Intent(getApplicationContext(),MainActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        noti=(NotificationManager)getSystemService(Service.NOTIFICATION_SERVICE);
        //Notification notification=new Notification();
        PendingIntent pendingIntent=PendingIntent.getService(getApplicationContext(),0,in,0);
        String body="Hello Welcome to M-Taxi";
        String title="Welcome";
        Notification n=new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("MDriver")
                .setContentText("running")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        startForeground(1,n);

    }

    @Override
    public void onDestroy() {

        stopForeground(true);
        super.onDestroy();

    }

    public void stopWorking(){
        Log.e("service destory","service destory//////////////////////////////////////////////////");

        Toast.makeText(this,"service destroyed",Toast.LENGTH_LONG).show();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        try {
            refWorking.child(userId).removeValue();
            driversAvailable.child(userId).removeValue();
        }catch (Exception e){

        }

        //stopSelf();
    }



    public void startWorking(){
        startForegroundService();


        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //status = 1;

                    double startLat = 0, startLng = 0, stopLat = 0, stopLng = 0;

                    for (DataSnapshot customerData : dataSnapshot.getChildren()) {


                        customerRequest=customerData.getValue(CustomerRequest.class);


                        customerId=customerRequest.getCustomerId();

                        if(customerId!=null) {

                            assignedCustomerRef.removeValue();

                            listenCancelRequest();
                            if (serviceCallbacks != null) {
                                //serviceCallbacks.stop(new LatLng(stoplat,stoplng));
                                serviceCallbacks.start();

                            } else {
                                NotificationManager noti = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
                                final int uiqueId = 3343;
                                Intent in = new Intent(getApplicationContext(), MainActivity.class);
                                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, in, 0);
                                String body = "Hello Welcome to M-Taxi";
                                String title = "Welcome";
                                Notification n = new NotificationCompat.Builder(getApplicationContext())
                                        /* .setTicker("bbbb")*/
                                        .setSmallIcon(R.drawable.ic_launcher_background)
                                        .setContentTitle("Monywa")
                                        .setContentText("Customer arrived")
                                        .setContentIntent(pi)
                                        .setAutoCancel(true)
                                        .build();
                                n.defaults = Notification.DEFAULT_ALL;
                                noti.notify(uiqueId, n);
                                // Toast.makeText(getApplicationContext(),"Customer arrived",Toast.LENGTH_LONG).show();
                                //Toast.makeText(getApplicationContext(), "hey customer request arrived", Toast.LENGTH_LONG).show();

                            }
                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public DatabaseReference getRefWorking() {
        return refWorking;
    }

    public void setRefWorking(DatabaseReference refWorking) {
        this.refWorking = refWorking;
    }

    public DatabaseReference getDriversAvailable() {
        return driversAvailable;
    }

    public void setDriversAvailable(DatabaseReference driversAvailable) {
        this.driversAvailable = driversAvailable;
    }

    public DatabaseReference getActiveJob() {
        return activeJob;
    }

    public void setActiveJob(DatabaseReference acceptJob) {
        this.activeJob = acceptJob;
    }

    public DatabaseReference getRejectJob() {
        return rejectJob;
    }


    public void setRejectJob(DatabaseReference rejectJob) {
        this.rejectJob = rejectJob;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public DatabaseReference getAssignedCustomerRef() {
        return assignedCustomerRef;
    }

    public void setAssignedCustomerRef(DatabaseReference assignedCustomerRef) {
        this.assignedCustomerRef = assignedCustomerRef;
    }

    public class MyBinder extends Binder{

        public MService getServiceSystem(){
            return MService.this;
        }
    }

public void listenCancelRequest(){

        operator.child(driverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                try {
                    boolean b = dataSnapshot.getValue(Boolean.class);

                    if (b) {
                        customerId = "";
                        customerRequest = null;
                        isAcceptJob = false;

                        operator.child(driverId).removeValue();

                        if (serviceCallbacks != null) {
                            serviceCallbacks.cancelRequest();
                        } else {
                            updateAvailable();
                            //Toast.makeText(getApplicationContext(),"Customer cancel request",Toast.LENGTH_LONG).show();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    cancelRequest.child(customerId).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            String dId=dataSnapshot.getValue(String.class);
            if(driverId.equals(dId)){
                cancelRequest.child(customerId).removeValue();
                activeJob.child(customerId).removeValue();
                customerId="";
                customerRequest=null;
                isAcceptJob=false;
                assignedCustomerRef.removeValue();

                Toast.makeText(getApplicationContext(),"Customer cancel request",Toast.LENGTH_LONG).show();
                if(serviceCallbacks!=null) {
                    serviceCallbacks.cancelRequest();
                }else{
                    updateAvailable();
                    Toast.makeText(getApplicationContext(),"Customer cancel request",Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });

}

    private void updateAvailable() {


        try {
            switch (customerId) {
                case "":
                    driversAvailable.child(driverId).setValue(driverId);
                    refWorking.child(driverId).removeValue();
                    break;
                default:
                    driversAvailable.child(driverId).removeValue();
                    refWorking.child(driverId).setValue(driverId);

            }
        }catch (Exception e){
            Log.e("error",e.getMessage());
        }
    }

    public DatabaseReference getDriverDb() {
        return driverDb;
    }

    public void setDriverDb(DatabaseReference driverDb) {
        this.driverDb = driverDb;
    }

    public DatabaseReference getFinishJob() {
        return finishJob;
    }

    public void setFinishJob(DatabaseReference finishJob) {
        this.finishJob = finishJob;
    }

    public DatabaseReference getCancelRequest() {
        return cancelRequest;
    }

    public void setCancelRequest(DatabaseReference cancelRequest) {
        this.cancelRequest = cancelRequest;
    }

    public boolean isAcceptJob() {
        return isAcceptJob;
    }

    public void setAcceptJob(boolean acceptJob) {
        isAcceptJob = acceptJob;
    }

    public DatabaseReference getDriverLocaiton() {
        return driverLocaiton;
    }

    public void setDriverLocaiton(DatabaseReference driverLocaiton) {
        this.driverLocaiton = driverLocaiton;
    }

    public DatabaseReference getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(DatabaseReference customerInfo) {
        this.customerInfo = customerInfo;
    }
}
