package mtaxi.cumonywa.com;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import mtaxi.cumonywa.com.BaseActivity;

public class Inactive extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inactive);
        Button button=findViewById(R.id.btnCallCenter);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str="09975672803";
                Intent intent=new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+str.trim()));
                startActivity(intent);
            }
        });
    }
}
