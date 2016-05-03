package ggamzang.airinfo;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Created by 미진앤찬섭 on 2016-04-29.
 */
public class TimePickerPreference extends DialogPreference {

    String mNewValue = "";
    String mCurrentValue = "";

    TimePicker tp = null;

    public TimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.timepicker_dialog);
        setPositiveButtonText("설정");
        setNegativeButtonText("취소");

        setDialogIcon(null);
    }

    @Override
    protected void onBindDialogView(View view) {
        tp = (TimePicker)view.findViewById(R.id.tpTime);
        super.onBindDialogView(view);
    }

    @Override
    // for android:defaultValue
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        super.onSetInitialValue(restorePersistedValue, defaultValue);
        if(restorePersistedValue){
            //restore existing value. when it's true, defaultValue is null
            mCurrentValue = this.getPersistedString("00:00");
        }
        else{
            // set default state from the XML attr
            mCurrentValue = (String)defaultValue;
            persistString(mCurrentValue);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if(positiveResult){
            int hour = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hour = tp.getHour();
            }
            else{
                hour = tp.getCurrentHour().intValue();
            }
            int min = 0;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                min = tp.getMinute();
            }
            else{
                min = tp.getCurrentMinute().intValue();
            }

            Toast.makeText(getContext(), "hour:"+hour+", min:"+min,Toast.LENGTH_LONG).show();
            persistString(mNewValue);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        // Check whether this Preference is persistent (continually saved)
        if (isPersistent()) {
            // No need to save instance state since it's persistent,
            // use superclass state
            return superState;
        }

        // Create instance of custom BaseSavedState
        final SavedState myState = new SavedState(superState);
        // Set the state's value with the class member that holds current
        // setting value
        myState.value = mNewValue;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Check whether we saved the state in onSaveInstanceState
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // Cast state to custom BaseSavedState and pass to superclass
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        // Set this Preference's widget to reflect the restored state
        //mNumberPicker.setValue(myState.value);
    }

    private static class SavedState extends BaseSavedState {
        // Member that holds the setting's value
        // Change this data type to match the type saved by your Preference
        String value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            // Get the current preference's value
            value = source.readString();  // Change this to read the appropriate data type
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            // Write the preference's value
            dest.writeString(value);  // Change this to write the appropriate data type
        }

        // Standard creator object using an instance of this class
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
