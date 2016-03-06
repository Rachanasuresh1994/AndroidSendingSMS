package pulku.messagesending.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import pulku.messagesending.R;

public class MyPreferencesFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
