package pulku.messagesending.model;


import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {

    private String mName;
    private String mNumber;
    private boolean isSelected;

    public Contact() {}


    protected Contact(Parcel in) {
        mName = in.readString();
        mNumber = in.readString();
        isSelected = (in.readInt() == 0) ? false : true;
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getNumber() {
        return mNumber;
    }

    public void setNumber(String number) {
        mNumber = number;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mNumber);
        dest.writeInt(isSelected ? 1 : 0);
    }
}
