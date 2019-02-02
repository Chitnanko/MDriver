package mtaxi.cumonywa.com;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CheckConnection extends BaseActivity{
    Button btn_TryAgain;
    FirebaseUser driver= FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference driverReference= FirebaseDatabase.getInstance().getReference("Users").child("Drivers");

    @Override
    protected void onStart() {
        super.onStart();
        if(CheeckInternetConnection.getInstance(this).isOnline()){
           // Toast.makeText(getApplicationContext(),"Connected",Toast.LENGTH_LONG).show();
            checkState();
        }
        else{
            Toast.makeText(getApplicationContext(),"Require Connection",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connectionfail);
        btn_TryAgain=(Button) findViewById(R.id.btn_tryAgain);
        btn_TryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CheeckInternetConnection.getInstance(getApplicationContext()).isOnline()){
                    //Toast.makeText(getApplicationContext(),"Connected",Toast.LENGTH_LONG).show();
                    /*Intent intent=new Intent(getApplicationContext(),PhoneAuthActivity.class);
                    startActivity(intent);
                    finish();*/
                    checkState();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Require Connection",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void checkState(){

        if(driver!=null){

            showProgressDialog();

            driverReference.child(driver.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    boolean active=dataSnapshot.child("active").getValue(Boolean.class);

                    Log.e("driverActive:",Boolean.toString(active)+"$$$$$$$$$$$$$$$$");

                    if(active){
                        hideProgressDialog();
                        Intent activeActivity=new Intent(getApplicationContext(),MainActivity.class);
                        activeActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(activeActivity);

                        finish();

                    }else {

                        hideProgressDialog();
                        Intent inactiveActivity=new Intent(getApplicationContext(),Inactive.class);
                        inactiveActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(inactiveActivity);
                        driverReference.removeEventListener(this);
                        finish();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }else {
            hideProgressDialog();
            Intent intent=new Intent(getApplicationContext(),PhoneAuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

    }

}
