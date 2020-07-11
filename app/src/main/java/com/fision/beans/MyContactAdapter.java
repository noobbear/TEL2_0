package com.fision.beans;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fision.tel2_0.R;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class MyContactAdapter extends BaseAdapter {
    private List<MyContact> list;
    private Context bContext;

    public MyContactAdapter(List<MyContact> list,Context bContext){
        this.list = list;
        this.bContext = bContext;
    }

    @Override
    public int getCount() {
        return this.list.size();
    }

    @Override
    public Object getItem(int position) {
        return this.list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Long.valueOf(String.valueOf(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        MyContact contact = this.list.get(position);
        if (convertView == null)
            v = View.inflate(bContext, R.layout.layout_list_item, null);
        else
            v = convertView;
        TextView tv_name = (TextView) v.findViewById(R.id.item_name);
        tv_name.setText(contact.getName());
        TextView tv_num = (TextView) v.findViewById(R.id.item_num);
        tv_num.setText(contact.getPhonenum());
        if (contact.getTx().startsWith("content://com.android.contacts")){
            Uri uri = Uri.parse(contact.getTx());
            ImageView img = (ImageView) v.findViewById(R.id.img_l);
            try {
                img.setImageURI(uri);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            ImageView img = (ImageView) v.findViewById(R.id.img_l);
            img.setImageBitmap(BitmapFactory.decodeFile(contact.getTx()));
        }
        v.setTag(position);
        return v;
    }
    public static InputStream getContactBitmapFromURI(Context context, Uri uri)
            throws FileNotFoundException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        if (input == null) {
            return null;
        }
        return input;
    }
}
