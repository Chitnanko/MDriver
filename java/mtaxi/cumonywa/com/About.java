package mtaxi.cumonywa.com;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

public class About extends Activity {

    MenuItem actionSetting;
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        actionSetting=(MenuItem)findViewById(R.id.action_settings);
    }
}
