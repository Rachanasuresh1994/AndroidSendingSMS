package pulku.messagesending.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;


import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import pulku.messagesending.R;
import pulku.messagesending.adapter.ContactAdapter;
import pulku.messagesending.model.Contact;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    public static final String CONTACTS_PHONES = "CONTACTS_PHONES";
    private List<Contact> mContacts;
    private ContactAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private CheckBox mCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        ButterKnife.bind(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);


        mContacts = getContacts();

        mAdapter = new ContactAdapter(this, mContacts);
        mRecyclerView.setAdapter(mAdapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);

        mCheckBox = (CheckBox) findViewById(R.id.checkBox);
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                chooseAllContacts(isChecked);
                mAdapter.notifyDataSetChanged();
            }
        });

    }



    public List<Contact> getContacts() {
        ArrayList<Contact> contacts = new ArrayList<>();

        String contactId;
        String contactName;
        String number = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest
                .permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest
                            .permission.SEND_SMS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            // Android version is lesser than 6.0 or the permission is already granted.
        } else {
        ContentResolver contentResolver = getContentResolver();
            try {
                Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null,
                    null, null);
                if (cursor.moveToFirst()) {
                    do {
                        Contact contact = new Contact();

                        int hasPhoneNumber = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        if (hasPhoneNumber > 0) {
                            contactId = cursor.getString(cursor.getColumnIndex(ContactsContract
                                    .Contacts._ID));
                            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract
                                    .Contacts.DISPLAY_NAME));
                            Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone
                                .CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +
                                        "=" + contactId , null, null);



                                if (phoneCursor.moveToFirst()) {
                                    if( phoneCursor.getString(phoneCursor.getColumnIndex
                                            (ContactsContract.CommonDataKinds.Phone.NUMBER))
                                            .replace(" ", "")
                                            .startsWith("+905") || phoneCursor.getString
                                            (phoneCursor.getColumnIndex
                                            (ContactsContract.CommonDataKinds.Phone.NUMBER))
                                            .replace(" ", "")
                                            .startsWith("05") || phoneCursor.getString
                                            (phoneCursor.getColumnIndex
                                                    (ContactsContract.CommonDataKinds.Phone.NUMBER))
                                            .replace(" ", "")
                                            .startsWith("5") ) {
                                        number = phoneCursor.getString(phoneCursor.getColumnIndex
                                                (ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    }
                                } while (phoneCursor.moveToNext());
                                phoneCursor.close();
                            if(!number.equals("")) {
                                contact.setNumber(number);
                                contact.setName(contactName);
                                contacts.add(contact);

                            }
                        }

                    } while (cursor.moveToNext());
                }
                cursor.close();

            }catch(SQLiteException | NullPointerException e) {
                Log.e("HATA", e.getMessage());
            }
        }
        return contacts;
    }



    public List<Contact> chooseAllContacts(boolean isChecked) {
        mContacts = (mAdapter).getContactsList();
        for(int i = 0; i < mContacts.size(); i ++) {
            Contact contact = mContacts.get(i);
            contact.setIsSelected(isChecked);
        }
        return mContacts;
    }

    public String[] getSelectedPhoneNumbers(){

        List<Contact> selectedContacts = new ArrayList<>();
        mContacts = (mAdapter).getContactsList();
        for (int i = 0; i < mContacts.size(); i++) {
            Contact contact = mContacts.get(i);
            if (contact.isSelected()) {
                selectedContacts.add(contact);
            }
        }
        String[] selectedPhones = new String[selectedContacts.size()];
        for(int i = 0; i < selectedContacts.size(); i++){
            selectedPhones[i] = selectedContacts.get(i).getNumber();
        }
        return selectedPhones;
    }

    @OnClick (R.id.button)
    public void startMessageSendingActivity(View view) {
        String selectedPhoneNumbers[] = getSelectedPhoneNumbers();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String api_key = prefs.getString("apikey_preference", "");
        String api_secret = prefs.getString("apisecret_preference", "");
        if(selectedPhoneNumbers.length < 1) {
            NotSelectedContactsAlert alert = new NotSelectedContactsAlert();
            alert.show(getFragmentManager(), "warning_dialog");
        } else if(api_key.equals("") || api_secret.equals("")) {
            ApiDialogFragment alert = new ApiDialogFragment();
            alert.show(getFragmentManager(), "warning_api_dialog");
        } else {
            Intent intent = new Intent(this, MessageSendingActivity.class);
            intent.putExtra(CONTACTS_PHONES, getSelectedPhoneNumbers());
            startActivity(intent);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        MenuItemCompat.setOnActionExpandListener(item,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        // Do something when collapsed
                        mAdapter.setFilter(mContacts);
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        // Do something when expanded
                        return true; // Return true to expand action view
                    }
                });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SetPreferencesActivity.class);
            startActivityForResult(intent, 0);

            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Contact> filteredModelList = filter(mContacts, newText);
        mAdapter.setFilter(filteredModelList);
        return true;
    }

    private List<Contact> filter(List<Contact> mContacts, String query) {
        query = query.toLowerCase();

        final List<Contact> filteredContactList = new ArrayList<>();
        for (Contact contact : mContacts) {
            final String text =contact.getName().toLowerCase();
            if (text.contains(query)) {
                filteredContactList.add(contact);
            }
        }
        return filteredContactList;
    }

}
