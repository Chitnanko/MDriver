package mtaxi.cumonywa.com;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends BaseActivity implements View.OnClickListener{

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private TextView txtEditPhone;
    private EditText verify_code,cuphone,driver_name,car_no,car_type;
    private Button verify_btn,resend_btn,continue_btn;
    String name,phone,carNo,carType,code;
    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    ImageView driverPhoto;
    LinearLayout layout_phone;
    boolean isDatilLayout=true;
    LinearLayout layout_code;
    private Button btn_phone;
    private FirebaseUser driver=FirebaseAuth.getInstance().getCurrentUser();;
    private Uri photoUrl;

    DatabaseReference driverReference=FirebaseDatabase.getInstance().getReference("Users").child("Drivers");

    @Override
    protected void onStart() {
        super.onStart();
        //checkState();

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_phone);
        verify_code=(EditText)findViewById(R.id.edit_code);
        verify_btn=(Button) findViewById(R.id.btn_verify);
        resend_btn=(Button) findViewById(R.id.btn_resend);
        continue_btn=(Button) findViewById(R.id.btn_continue);
        driver_name=(EditText)findViewById(R.id.edit_name);
        cuphone=(EditText)findViewById(R.id.edit_phone);
        car_no=(EditText)findViewById(R.id.edit_carNo);
        car_type=(EditText)findViewById(R.id.edit_carType);

        Intent intent=getIntent();
        //phone=intent.getStringExtra("phone");
        //txtEditPhone=findViewById(R.id.txtEditPhone);
        //txtEditPhone.setOnClickListener(this);
        driverPhoto=(ImageView)findViewById(R.id.driver_photo);
        verify_btn.setOnClickListener(this);
        resend_btn.setOnClickListener(this);

        continue_btn.setOnClickListener(this);
        /*Toast.makeText(getApplicationContext(),name+phone+email,Toast.LENGTH_LONG).show();
        Log.i("###########",name+"**"+phone+"**"+email);*/
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
                //Log.d(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                showProgressDialog();
                verify_code.setText(credential.getSmsCode());
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
               // updateUI(STATE_VERIFY_SUCCESS, credential);
                // [END_EXCLUDE]
                //if(credential!=null)
                    signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                //Log.w(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                   // mPhoneNumberField.setError("Invalid phone number.");
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Snackbar.make(findViewById(android.R.id.content), e.getMessage().toString(),
                            Snackbar.LENGTH_LONG).show();
                    // [END_EXCLUDE]
                }

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
               // Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // [START_EXCLUDE]
                // Update UI
                //updateUI(STATE_CODE_SENT);
                // [END_EXCLUDE]
            }
        };

    }

    public void selectImage(View view){
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Photo"),0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0){
            if(resultCode==RESULT_OK){
                //Toast.makeText(getApplicationContext(),"Ok OK OK OK OK OK...................",Toast.LENGTH_LONG).show();
                //Bitmap bitmap=getPath(data.getData());
                //driverPhoto.setImageBitmap(bitmap);
                try {
                    Glide.with(this)
                            .load(new File(getPath(data.getData())))
                            .apply(RequestOptions.circleCropTransform())
                            .into(driverPhoto);
                }catch (Exception e){
                    Log.e("glide error:",e.getMessage()+"////////////////////////////////////");
                }
            }
        }
    }

    private String getPath(Uri data) {

        String[] projection={MediaStore.Images.Media.DATA};
        Cursor cursor=managedQuery(data,projection,null,null,null);
        int column_index=cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String file_Path=cursor.getString(column_index);
        //Bitmap bitmap= BitmapFactory.decodeFile(file_Path);
        return file_Path;
    }

    @Override
    public void onBackPressed() {
        if (isDatilLayout){
            this.moveTaskToBack(true);
        }else {
            layout_phone.setVisibility(View.VISIBLE);
            layout_code.setVisibility(View.GONE);
            isDatilLayout=false;


        }
        //startActivity(new Intent(getApplicationContext(),PhoneInfoActivity.class));

    }


    private void startPhoneNumberVerification(String phoneNumber) {
        Log.i("**************","**");
        Toast.makeText(getApplicationContext(),"Code Sending...................",Toast.LENGTH_LONG).show();
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }
    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            writeNewUser();
                            // [END_EXCLUDE]
                        } else {
                            hideProgressDialog();
                            Toast.makeText(getApplicationContext(),"UnSuccessful..******************************.................",Toast.LENGTH_LONG).show();
                            // [END_EXCLUDE]
                        }
                    }
                });
    }

    private void writeNewUser() {
        final String driverId=mAuth.getCurrentUser().getUid();
        final FirebaseStorage storage=FirebaseStorage.getInstance();
        String imageName=cuphone.getText().toString().substring(1)+".jpg";
        final StorageReference storageReference=storage.getReference("images");
        StorageReference storageReference1=storageReference.child(driverId).child(imageName);
        driverPhoto.setDrawingCacheEnabled(true);

        driverPhoto.buildDrawingCache();
        Bitmap bitmap;

        bitmap = ((BitmapDrawable) driverPhoto.getDrawable()).getBitmap();


            continue_btn.setEnabled(true);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            byte[] data = outputStream.toByteArray();

            UploadTask uploadTask = storageReference1.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideProgressDialog();
                    Toast.makeText(getApplication(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {


                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    photoUrl = task.getResult().getUploadSessionUri();
                    Log.e("photo url", photoUrl.toString() + "photo download url//////////////////////////////////////////");


                    Driver driver = new Driver();
                    driver.setActive(false);
                    driver.setDriverId(driverId);
                    driver.setName(name);
                    driver.setPhone(phone);
                    driver.setCarNo(carNo);
                    driver.setCarType(carType);
                    driver.setDriverPhotoUrl(photoUrl.toString());


                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId);
                    databaseReference.setValue(driver);

                    checkState();

                    /*Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    hideProgressDialog();
                    finish();*/
                }
            });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_continue:
                name = driver_name.getText().toString();
                phone = cuphone.getText().toString();
                carNo = car_no.getText().toString();
                carType = car_type.getText().toString();

                String validph=null;
                String str=phone.substring(0,1);
                if(Integer.parseInt(str)==0){
                   validph=phone.substring(1,11);
                }
                validph="+95"+validph;
                Toast.makeText(getApplicationContext(),name+phone+carNo+carType,Toast.LENGTH_LONG).show();

                if(driverPhoto.getDrawable()==null){

                    Toast.makeText(getApplication(),"No image selected",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(name.equals("")||phone.equals("")||carNo.equals("")||carType.equals("")) {
                    Toast.makeText(getApplicationContext(),"Please complete fill your data.",Toast.LENGTH_LONG).show();
                    driver_name.setError("fill data");
                    cuphone.setError("fill phone no");
                    car_no.setError("fill car no");
                    car_type.setError("fill car type");

                    }else {
                         phone=validph.toString();
                         layout_phone = findViewById(R.id.phone_layout);
                        layout_phone.setVisibility(View.GONE);
                         layout_code = findViewById(R.id.code_layout);
                        layout_code.setVisibility(View.VISIBLE);
                        isDatilLayout=false;
                        startPhoneNumberVerification(phone);
                    }

                break;
            case R.id.btn_verify:
                     code = verify_code.getText().toString();
                    if (TextUtils.isEmpty(code)) {
                        verify_code.setError("Cannot be empty.");
                        return;
                    }

                    verifyPhoneNumberWithCode(mVerificationId, code);
                    showProgressDialog();

                break;
            /*case R.id.txtEditPhone:
                LinearLayout layout_phone=findViewById(R.id.phone_layout);
                layout_phone.setVisibility(View.VISIBLE);
                LinearLayout layout_code=findViewById(R.id.code_layout);
                layout_code.setVisibility(View.GONE);
                break;*/
            case R.id.btn_resend:
                resendVerificationCode(cuphone.getText().toString(), mResendToken);
                break;
        }
    }

    private boolean isValidatePhone() {
        phone=cuphone.getText().toString();

        if (phone==null || phone.startsWith("0")){
            cuphone.setError("Invalid");
            return false;
        }

        return true;
    }

    private void checkState(){

        driver= FirebaseAuth.getInstance().getCurrentUser();

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
                        startActivity(activeActivity);

                       finish();

                    }else {

                        hideProgressDialog();
                        Intent inactiveActivity=new Intent(getApplicationContext(),Inactive.class);
                        startActivity(inactiveActivity);
                        finish();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }else {
        }

    }

}
