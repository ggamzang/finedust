package ggamzang.airinfo;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by chansub.shin on 2016-04-21.
 */
public class GPSSettingsDialog extends Dialog {
    private Button mBtnSettings = null;
    private Button mBtnCancel   = null;

    public GPSSettingsDialog(Context context){
        super(context);
        setContentView(R.layout.dialog_gps_setting_layout);

        mBtnSettings = (Button)findViewById(R.id.btnSettings);
        mBtnCancel = (Button)findViewById(R.id.btnCancel);
    }

    public void setSettingsClickListener(View.OnClickListener listener){
        if(mBtnSettings != null){
            mBtnSettings.setOnClickListener(listener);
        }
    }

    public void setCancelClickListener(View.OnClickListener listener){
        if(mBtnCancel != null){
            mBtnCancel.setOnClickListener(listener);
        }
    }
}
