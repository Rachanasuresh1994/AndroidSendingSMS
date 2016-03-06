package pulku.messagesending.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import okhttp3.Response;
import pulku.messagesending.R;


public class MessageSendingActivity extends AppCompatActivity{

    private String[] numbers;
    private List<String> mOriginatorsTitles;
    private String selectedTitle;
    private Spinner mTitles;
    String api_key = "";

    String api_secret = "";



    String base_api_url = "http://api.globalhaberlesme.com";

    String originatorUrl = "";


    @Bind(R.id.numbersText) TextView mNumbers;
    @Bind(R.id.messageText) TextView mMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_sending);

        ButterKnife.bind(this);

        Intent intent = getIntent();

        StringBuilder stringBuilder = new StringBuilder();
        numbers = intent.getStringArrayExtra(MainActivity.CONTACTS_PHONES);

        if(numbers.length > 10) {
            for (int i = 0; i < 11; i++) {
                if (i < 10) {
                    stringBuilder.append(numbers[i] + " ");
                } else {
                    stringBuilder.append("...");
                }
            }
        } else {
            for(int i = 0; i < numbers.length; i++){
                stringBuilder.append(numbers[i] + " ");
            }
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        api_key = prefs.getString("apikey_preference", "");
        api_secret = prefs.getString("apisecret_preference", "");
        originatorUrl = base_api_url + "/originator/list?key=" + api_key + "&secret=" +
                api_secret;

        mNumbers.setText(stringBuilder);
        mTitles = (Spinner) findViewById(R.id.spinner);

        getOriginatorData();

        mTitles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // On selecting a spinner item
                selectedTitle = parent.getItemAtPosition(position).toString();

                // Showing selected spinner item
                Toast.makeText(parent.getContext(), "Selected: " + selectedTitle, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTitle = parent.getItemAtPosition(0).toString();
            }
        });




    }


    private void getOriginatorData() {
        if(isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(originatorUrl)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("Callback onFailure: ", "Callback başarısız");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("Callback onResponse: ", "Callback başarılı");

                    try {
                        String jsonData = response.body().string();
                        Log.v("Tag", jsonData);
                        if(response.isSuccessful()) {
                            mOriginatorsTitles = parseOriginatorDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException | JSONException e) {
                        Log.e("TAG", "Exception caught: ", e);
                    }
                }
            });
        }
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    private List<String> parseOriginatorDetails(String jsonData) throws JSONException {
        JSONObject originator = new JSONObject(jsonData);
        JSONArray data = originator.getJSONArray("data");

        List<String> originatorsTitles = new ArrayList<>();
        for(int i = 0; i < data.length(); i++) {
            JSONObject jsonOriginatorData = data.getJSONObject(i);
            originatorsTitles.add(jsonOriginatorData.getString("title"));
        }
        return originatorsTitles;

    }


    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo =  manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    private void updateDisplay() {
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout
                .simple_spinner_item, mOriginatorsTitles);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTitles.setAdapter(dataAdapter);

    }


    @OnClick(R.id.smsButton)
    public void smsButtonActivity() {
        if(isNetworkAvailable()) {
            String messageText = mMessage.getText() + "";
            DialogFragment alert = MyAlertDialogFragment.newInstance(numbers, messageText, selectedTitle);
            alert.show(getFragmentManager(), "alert_message");
        }  else {
        Toast.makeText(this, getString(R.string.network_unavailable_message), Toast
                .LENGTH_SHORT)
                .show();
        }
    }


    public static class MyAlertDialogFragment extends DialogFragment {

        String api_key = "";
        String api_secret = "";
        String base_api_url = "http://api.globalhaberlesme.com";


        static MyAlertDialogFragment newInstance(String[] numbers, String messageText, String
                selectedTitle) {

            MyAlertDialogFragment fragment = new MyAlertDialogFragment();

            Bundle bundle = new Bundle();
            bundle.putStringArray("numbers", numbers);
            bundle.putString("message", messageText);
            bundle.putString("selectedTitle", selectedTitle);
            fragment.setArguments(bundle);
            return fragment;
        }



        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            api_key = prefs.getString("apikey_preference", "");
            api_secret = prefs.getString("apisecret_preference", "");

            Context context = getActivity();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.alert_title));
            builder.setMessage(context.getString(R.string.alert_message));
            builder.setPositiveButton(context.getString(R.string.alert_ok_button_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            final String[] phoneNumbers = getArguments().getStringArray("numbers");
                            final String messageText = getArguments().getString("message");
                            final String selectedTitle = getArguments().getString("selectedTitle");

                            String turkish_character = checkingMessageForTurkishCharacters(messageText) + "";
                            String numbers = numbersArrayToString(phoneNumbers);

                            Date date = new Date();
                            String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);

                            if (turkish_character.equals("0")) {

                                base_api_url = base_api_url + "/sms/send/single?key=" + api_key + "&secret=" + api_secret;
                                String url = base_api_url + "&text=" + messageText + "&numbers="
                                        + numbers + "&originator=" + selectedTitle;
                                sendSMS(url);

                            } else {
                                base_api_url = base_api_url + "/sms/send/single?key=" + api_key + "&secret=" + api_secret;
                                String url = base_api_url + "&text=" + messageText + "&numbers="
                                        + numbers + "&originator=" + selectedTitle +
                                        "&turkish_character=" + turkish_character;
                                sendSMS(url);
                            }
                        }
                    }

            );
            builder.setNegativeButton("İPTAL", null);
            return builder.create();

        }


        private int checkingMessageForTurkishCharacters(String messageText) {
            int turkish_character = 0;
            for (int i = 0; i < messageText.length(); i++) {
                if (messageText.charAt(i) == 'ü' ||
                        messageText.charAt(i) == 'Ü' ||
                        messageText.charAt(i) == 'Ö' ||
                        messageText.charAt(i) == 'ö' ||
                        messageText.charAt(i) == 'Ğ' ||
                        messageText.charAt(i) == 'ğ' ||
                        messageText.charAt(i) == 'Ç' ||
                        messageText.charAt(i) == 'ç' ||
                        messageText.charAt(i) == 'Ş' ||
                        messageText.charAt(i) == 'ş' ||
                        messageText.charAt(i) == 'ı' ||
                        messageText.charAt(i) == 'İ') {
                    turkish_character = 1;
                }
            }
            return turkish_character;
        }

        private String numbersArrayToString(String[] phoneNumbers) {
            String numbers = "";
            if(phoneNumbers.length == 1) {
                numbers = phoneNumbers[0] + "";
            } else {
                for(int i = 0; i < phoneNumbers.length; i++) {
                    if(i < phoneNumbers.length - 1) {
                        numbers += phoneNumbers[i].replace("-", "").replace("(", "").replace(")", "")
                                + ",";
                    } else {
                        numbers += phoneNumbers[i].replace("-", "").replace("(", "").replace(")", "");
                    }
                }
            }
            return numbers;
        }

        private void sendSMS(String url) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("TAG", "CallBack başarısız");
                    AlertDialogFragment dialog = new AlertDialogFragment();
                    dialog.show(getFragmentManager(), "error_dialog");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        if (response.isSuccessful()) {
                            Log.d("jsonData ", jsonData);
                            Log.d("response success: ", "response is successfull");
                        } else {
                            AlertDialogFragment dialog = new AlertDialogFragment();
                            dialog.show(getFragmentManager(), "error_dialog");
                        }
                    } catch (IOException e) {
                        Log.e("Tag", e.getLocalizedMessage());
                    }
                }
            });
        }

    }



}
