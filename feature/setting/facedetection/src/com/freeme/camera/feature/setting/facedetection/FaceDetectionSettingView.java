package com.freeme.camera.feature.setting.facedetection;

import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.freeme.camera.R;
import com.freeme.camera.common.debug.LogHelper;
import com.freeme.camera.common.debug.LogUtil;
import com.freeme.camera.common.preference.SwitchPreference;
import com.freeme.camera.common.setting.ICameraSettingView;

public class FaceDetectionSettingView implements ICameraSettingView {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(FaceDetectionSettingView.class.getSimpleName());

    private FaceDetectionSettingView.OnFaceDetectionClickListener mListener;
    private SwitchPreference mPref;
    private boolean mChecked;
    private String mKey;
    private boolean mEnabled;
    private static final int SETTING_ITEM_PRIORITY = 10;

    /**
     * Listener to listen zsd is clicked.
     */
    public interface OnFaceDetectionClickListener {
        /**
         * Callback when zsd item is clicked by user.
         *
         * @param checked True means zsd is opened, false means zsd is closed.
         */
        void onFaceDetectionClicked(boolean checked);
    }

    /**
     * Zsd setting view constructor.
     *
     * @param key The key of setting.
     */
    public FaceDetectionSettingView(String key) {
        mKey = key;
    }

    @Override
    public void loadView(PreferenceFragment fragment) {
        fragment.addPreferencesFromResource(R.xml.face_detection_preference);
        mPref = (SwitchPreference) fragment.findPreference(mKey);
        mPref.setRootPreference(fragment.getPreferenceScreen());
        mPref.setId(R.id.facedetection_setting);
        mPref.setContentDescription(fragment.getActivity().getResources()
                .getString(R.string.face_detection_content_description));
        mPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                boolean checked = (Boolean) o;
                mChecked = checked;
                mListener.onFaceDetectionClicked(checked);
                return true;
            }
        });
        mPref.setEnabled(mEnabled);
    }

    @Override
    public void refreshView() {
        if (mPref != null) {
            mPref.setChecked(mChecked);
            mPref.setEnabled(mEnabled);
        }
    }

    @Override
    public void unloadView() {
        LogHelper.d(TAG, "[unloadView]");
    }

    @Override
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return mEnabled;
    }


    @Override
    public String getKey() {
        return mKey;
    }

    /**
     * Set listener to listen zsd is clicked.
     *
     * @param listener The instance of {@link FaceDetectionSettingView.OnFaceDetectionClickListener}.
     */
    public void setFaceDetectionOnClickListener(FaceDetectionSettingView.OnFaceDetectionClickListener listener) {
        mListener = listener;
    }

    /**
     * Set zsd state.
     *
     * @param checked True means zsd is opened, false means zsd is closed.
     */
    public void setChecked(boolean checked) {
        mChecked = checked;
    }
}

