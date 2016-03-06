package pulku.messagesending.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pulku.messagesending.R;
import pulku.messagesending.model.Contact;


public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private List<Contact> mContacts;
    private LayoutInflater inflater;

    public ContactAdapter(Context context, List<Contact> contacts) {
        inflater =  LayoutInflater.from(context);
        mContacts = contacts;
    }


    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.contactslist_item,
                parent, false);
        ContactViewHolder holder = new ContactViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {

        holder.mContactName.setText(mContacts.get(position).getName());
        holder.mContactNumber.setText(mContacts.get(position).getNumber());
        holder.contactIsSelected = mContacts.get(position).isSelected();
        holder.mContactCheckBox.setChecked(mContacts.get(position).isSelected());
        holder.mContactCheckBox.setTag(mContacts.get(position));
        holder.mContactCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                Contact contact = (Contact) checkBox.getTag();
                contact.setIsSelected(checkBox.isChecked());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        public CheckBox mContactCheckBox;
        public TextView mContactName;
        public TextView mContactNumber;
        public boolean contactIsSelected;

        public ContactViewHolder(View itemView) {
            super(itemView);

            mContactName = (TextView) itemView.findViewById(R.id.contactName);
            mContactNumber = (TextView) itemView.findViewById(R.id.contactNumber);
            mContactCheckBox = (CheckBox) itemView.findViewById(R.id.contactCheckBox);

        }


        @Override
        public void onClick(View v) {
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Contact contact = new Contact();
            if(isChecked){
                contactIsSelected = true;
            } else {
                contactIsSelected = false;
            }
            contact.setIsSelected(contactIsSelected);
        }
    }

    public List<Contact> getContactsList() {
        return mContacts;
    }

    public void setFilter(List<Contact> countryModels) {
        mContacts = new ArrayList<>();
        mContacts.addAll(countryModels);
        notifyDataSetChanged();
    }

}
