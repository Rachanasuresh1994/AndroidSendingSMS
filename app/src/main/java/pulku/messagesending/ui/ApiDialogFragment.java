package pulku.messagesending.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import pulku.messagesending.R;

/**
 * Created by pınar on 25.02.2016.
 */
public class ApiDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.api_warning_title))
                .setMessage(context.getString(R.string.api_warning_message))
                .setPositiveButton(context.getString(R.string.alert_ok_button_text), null);
        AlertDialog dialog = builder.create();
        return dialog;

    }
}
