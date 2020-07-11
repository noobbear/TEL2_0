package com.fision.dao;

import java.util.ArrayList;
import java.util.List;

import com.fision.beans.MyContact;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class ContactDAO extends SQLiteOpenHelper {

    private Context mContext;
    private static final String CREATDB = "create table mycontact("
            + "id integer primary key autoincrement, "
            + "name text, "
            + "phonenum text, "
            + "tx text)";

    public ContactDAO(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATDB);
        Toast.makeText(mContext, "Create DB Successfully！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Toast.makeText(mContext, "update DB failed！ No SQL Executed", Toast.LENGTH_SHORT).show();
    }

    public boolean insert(MyContact c) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", c.getName());
        values.put("phonenum", c.getPhonenum());
        if (c.getTx() != null) {
            values.put("tx", c.getTx());
        }
        long i = db.insert("mycontact", null, values);
        return i != -1;
    }

    public boolean delete(MyContact c) {
        SQLiteDatabase db = this.getWritableDatabase();
        long i = db.delete("mycontact", "id = ?", new String[]{String.valueOf(c.getId())});
        return i == 1;
    }

    public boolean queryByTx(MyContact c) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query("mycontact", new String[]{"id"}, "tx = ?", new String[]{String.valueOf(c.getTx())}, null, null, null);
        return cursor.getCount() > 0;
    }

    public boolean update(MyContact c) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", c.getName());
        values.put("phonenum", c.getPhonenum());
        if (c.getTx() != null) {
            values.put("tx", c.getTx());
        }
        long i = db.update("mycontact", values, "id = ? ", new String[]{c.getId() + ""});
        return i == 1;
    }

    public List<MyContact> findAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<MyContact> list = new ArrayList<MyContact>();
        Cursor cursor = db.query("mycontact", null, null, null, null, null, null);
        MyContact contact;
        if (cursor.moveToFirst()) {
            do {
                contact = new MyContact();
                contact.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex("id"))));
                contact.setName(cursor.getString(cursor.getColumnIndex("name")));
                contact.setPhonenum(cursor.getString(cursor.getColumnIndex("phonenum")));
                contact.setTx(cursor.getString(cursor.getColumnIndex("tx")));
                list.add(contact);
            } while (cursor.moveToNext());
        }
        return list;
    }

    public MyContact findById(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query("mycontact", null, "id = ?", new String[]{id}, null, null, null);
        MyContact contact = null;
        if (cursor.moveToFirst()) {
            contact = new MyContact();
            contact.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex("id"))));
            contact.setName(cursor.getString(cursor.getColumnIndex("name")));
            contact.setPhonenum(cursor.getString(cursor.getColumnIndex("phonenum")));
            contact.setTx(cursor.getString(cursor.getColumnIndex("tx")));
        }
        return contact;
    }
}
