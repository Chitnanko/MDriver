package mtaxi.cumonywa.com;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CustomerArriveDl extends DialogFragment {
    View v;
    AcceptReject acceptReject;
    private TextView txt_start,txt_end,txttake_kilometer,txttotal_cost;
    private Button btnCancel,btnOk;
    CustomerRequest customerRequest;


    public CustomerArriveDl(){

    }
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        v= inflater.inflate(R.layout.driver_custom_dialog, container, false);
        txt_start=(TextView)v.findViewById(R.id.txtPlace);
        txt_end=(TextView)v.findViewById(R.id.txtEnd);
        btnOk=(Button)v.findViewById(R.id.btnOk);
        btnCancel=(Button)v.findViewById(R.id.btnCancel);
        txttake_kilometer=(TextView)v.findViewById(R.id.txtTakeKilo);
        txttotal_cost=(TextView)v.findViewById(R.id.txtTotalcost);
        getDialog().setTitle("Customer Information");
        getDialog().setCancelable(false);

        txttake_kilometer.setText(customerRequest.getDistance().toString());
        txttotal_cost.setText(customerRequest.getTotalCost().toString());
        txt_start.setText(customerRequest.getStartAddress());
        txt_end.setText(customerRequest.getStopAddress());

        setCancelable(false);




        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                acceptReject.acceptJob();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                acceptReject.rejectJob();
            }
        });



        return v;
    }


    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        //super.onDismiss(dialog);

        Dialog d=getDialog();
        if(d!=null)
        d.dismiss();
    }

    public void show(FragmentManager fmg, String title, CustomerRequest customerRequest){

        this.customerRequest=customerRequest;
        if(isAdded()){
            Log.e("fragmentManger:","fragment added/////////////////////////");
            return;
        }
        show(fmg,title);


    }
    public void setAcceptReject(AcceptReject acceptReject) {
        this.acceptReject = acceptReject;
    }

}
