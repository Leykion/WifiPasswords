package com.gmail.ndraiman.wifipasswords.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.gmail.ndraiman.wifipasswords.R;
import com.gmail.ndraiman.wifipasswords.dialogs.HelpDialogFragment;
import com.gmail.ndraiman.wifipasswords.extras.AppCompatPreferenceActivity;
import com.gmail.ndraiman.wifipasswords.extras.MyApplication;
import com.gmail.ndraiman.wifipasswords.extras.RequestCodes;
import com.gmail.ndraiman.wifipasswords.task.TaskCheckPasscode;


public class SettingsActivity extends AppCompatPreferenceActivity {

    private static Toolbar mToolbar;
    private SettingsFragment mSettingsFragment;

    public static final String SETTINGS_FRAGMENT_TAG = "settings_fragment_tag";

    private static boolean mPasscodePrefs = false;
    private static final String PASSCODE_PREFS = "passcode_preferences";
    private static final String TAG = "SettingsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);

        ActionBar sBar = getSupportActionBar();
        if (sBar != null) {
            sBar.setDisplayShowHomeEnabled(true);
            sBar.setDisplayHomeAsUpEnabled(true);
            sBar.setDisplayShowTitleEnabled(true);

        }


        if (savedInstanceState == null) {
            mSettingsFragment = new SettingsFragment();

        } else {
            mSettingsFragment = (SettingsFragment) getFragmentManager().findFragmentByTag(SETTINGS_FRAGMENT_TAG);
            mPasscodePrefs = savedInstanceState.getBoolean(PASSCODE_PREFS, false);
        }

        // Display the fragment as the main content
        getFragmentManager().beginTransaction()
                .replace(R.id.settings_frame, mSettingsFragment, SETTINGS_FRAGMENT_TAG).commit();


        if (mPasscodePrefs) {
            getSupportActionBar().setTitle(getString(R.string.pref_passcode_toolbar_title));
        }
    }


    //Required Method to Override to Validated Fragments
    @Override
    protected boolean isValidFragment(String fragmentName) {
        Log.d(TAG, "isValidFragment() called with: " + "fragmentName = [" + fragmentName + "]");
        return SettingsFragment.class.getName().equals(fragmentName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_help:
                HelpDialogFragment dialog = HelpDialogFragment.getInstance();
                dialog.show(getFragmentManager(), getString(R.string.dialog_about_key));
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");

        if (mPasscodePrefs) {
            mPasscodePrefs = false;
            mSettingsFragment.setPreferenceScreen(null);
            mSettingsFragment.loadGeneralPreferences();

        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState() called with: " + "outState = [" + outState + "]");
        outState.putBoolean(PASSCODE_PREFS, mPasscodePrefs);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        if(MyApplication.mPasscodeActivated && MyApplication.mAppWentBackground) {
            startActivity(new Intent(this, PasscodeActivity.class));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause()");

        if(MyApplication.mPasscodeActivated && !isFinishing()) {
            Log.e(TAG, "executing TaskCheckPasscode()");
            new TaskCheckPasscode(getApplicationContext(), this).execute();
        }
    }


    /***************************************************************/
    /****************** Settings Fragment **************************/
    /***************************************************************/
    public static class SettingsFragment extends PreferenceFragment {

        private static final String TAG = "SettingsFragment";

        private Preference mPasscodeToggle;
        private Preference mPasscodeChange;


        private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener
                = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                getActivity().setResult(RESULT_OK);

                String stringValue = newValue.toString();
                Log.d(TAG, "onPreferenceChange: " + stringValue);

                if (preference instanceof EditTextPreference) {

                    preference.setSummary(stringValue);


                } else if (preference instanceof ListPreference) {

                    preference.setSummary(stringValue);

                    //Toggle Manual location entry according to List Choice
                    if (stringValue.equals(getString(R.string.pref_path_list_manual))) {

                        findPreference(getString(R.string.pref_path_manual_key)).setEnabled(true);
                        findPreference(getString(R.string.pref_reset_manual_key)).setEnabled(true);
                    } else {
                        findPreference(getString(R.string.pref_path_manual_key)).setEnabled(false);
                        findPreference(getString(R.string.pref_reset_manual_key)).setEnabled(false);
                    }

                }

                return true;
            }
        };

        private void bindPreferenceSummaryToValue(Preference preference) {
            Log.d(TAG, "bindPreferenceSummaryToValue");
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "SettingsFragment - onCreate called with: mPasscodePrefs = " + mPasscodePrefs);

            getActivity().setResult(RESULT_CANCELED);

            if(mPasscodePrefs) {
                loadPasscodePreferences();

            } else {
                loadGeneralPreferences();

            }
        }


        //Helper method for onCreate
        public void loadGeneralPreferences() {
            Log.d(TAG, "loadGeneralPreferences()");

            mToolbar.setTitle(getString(R.string.action_settings));

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            findPreference(getString(R.string.pref_reset_manual_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    resetPathPref();
                    return true;
                }
            });

            findPreference(getString(R.string.pref_default_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showResetWarningDialog();
                    return true;
                }
            });

            findPreference(getString(R.string.pref_header_passcode_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    setPreferenceScreen(null);
                    loadPasscodePreferences();
                    mPasscodePrefs = true;

                    return true;
                }
            });

            setupShareWarningPreference();

            //Summary to Value
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_path_manual_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_path_list_key)));

        }


        public void loadPasscodePreferences() {
            Log.d(TAG, "loadPasscodePreferences()");

            mToolbar.setTitle(getString(R.string.pref_passcode_toolbar_title));
            addPreferencesFromResource(R.xml.passcode_prefs);


            mPasscodeToggle = findPreference(getString(R.string.pref_passcode_toggle_key));
            mPasscodeChange = findPreference(getString(R.string.pref_passcode_change_key));


            if(MyApplication.mPasscodeActivated) {
                mPasscodeToggle.setTitle(R.string.pref_passcode_toggle_title_off);
                mPasscodeChange.setEnabled(true);

            } else {
                mPasscodeToggle.setTitle(R.string.pref_passcode_toggle_title_on);
                mPasscodeChange.setEnabled(false);

            }


            mPasscodeToggle.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    if (!MyApplication.mPasscodeActivated) {

                        Intent intent = new Intent(getActivity(), PasscodeActivity.class);
                        intent.putExtra(MyApplication.PASSCODE_REQUEST_CODE, RequestCodes.PASSCODE_PREF_ENABLE);
                        startActivityForResult(intent, RequestCodes.PASSCODE_PREF_ENABLE);


                    } else {
                        Intent intent = new Intent(getActivity(), PasscodeActivity.class);
                        intent.putExtra(MyApplication.PASSCODE_REQUEST_CODE, RequestCodes.PASSCODE_PREF_DISABLE);
                        startActivityForResult(intent, RequestCodes.PASSCODE_PREF_DISABLE);

                    }

                    return true;
                }
            });


            mPasscodeChange.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Intent intent = new Intent(getActivity(), PasscodeActivity.class);
                    intent.putExtra(MyApplication.PASSCODE_REQUEST_CODE, RequestCodes.PASSCODE_PREF_CHANGE);
                    startActivityForResult(intent, RequestCodes.PASSCODE_PREF_CHANGE);
                    return true;
                }
            });


        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            Log.d(TAG, "onActivityResult() called with: " + "requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");

            switch (requestCode) {

                case RequestCodes.PASSCODE_PREF_ENABLE:
                    if(resultCode == RESULT_OK) {
                        mPasscodeToggle.setTitle(R.string.pref_passcode_toggle_title_off);
                        mPasscodeChange.setEnabled(true);
                    }
                    break;


                case RequestCodes.PASSCODE_PREF_DISABLE:
                    if(resultCode == RESULT_OK) {
                        mPasscodeToggle.setTitle(R.string.pref_passcode_toggle_title_on);
                        mPasscodeChange.setEnabled(false);
                    }
                    break;

            }
        }

        //Restore wpa_supplicant path to default
        private void resetPathPref() {
            Log.d(TAG, "resetPathPref");

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(getString(R.string.pref_path_manual_key), getString(R.string.pref_path_default));
            editor.apply();


            findPreference(getString(R.string.pref_path_manual_key)).setSummary(getString(R.string.pref_path_default));

            //Refresh Preference Screen
            setPreferenceScreen(null);
            loadGeneralPreferences();

        }


        private void showResetWarningDialog() {
            Log.d(TAG, "showResetWarningDialog");

            String[] buttons = getResources().getStringArray(R.array.dialog_warning_reset_buttons);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);

            //Send Result Codes to target fragment according to button clicked
            builder.setMessage(R.string.dialog_warning_reset_message)
                    .setTitle(R.string.dialog_warning_reset_title)
                    .setPositiveButton(buttons[0], new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().setResult(RequestCodes.RESET_TO_DEFAULT);
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton(buttons[1], new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Dismiss Dialog
                        }
                    });

            builder.create().show();
        }


        private void setupShareWarningPreference() {

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            final CheckBoxPreference shareWarning = (CheckBoxPreference) findPreference(getString(R.string.pref_share_warning_key));
            shareWarning.setChecked(sharedPreferences.getBoolean(MyApplication.SHARE_WARNING, true));

            shareWarning.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.d(TAG, "onPreferenceChange() called with: " + "preference = [" + preference + "], newValue = [" + newValue + "]");
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

                    if ((boolean) newValue) {
                        Log.d(TAG, "isChecked");
                        preference.setTitle(R.string.pref_share_warning_title_show);
                        sharedPreferences.edit().putBoolean(MyApplication.SHARE_WARNING, true).apply();

                    } else {
                        Log.d(TAG, "Not Checked");
                        preference.setTitle(R.string.pref_share_warning_title_hide);
                        sharedPreferences.edit().putBoolean(MyApplication.SHARE_WARNING, false).apply();

                    }

                    return true;
                }
            });
        }

    }
}
