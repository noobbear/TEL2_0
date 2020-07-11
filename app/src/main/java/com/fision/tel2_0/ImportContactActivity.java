package com.fision.tel2_0;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.fision.beans.MyContact;
import com.fision.beans.MyContactAdapter;
import com.fision.beans.SystemContactUtil;

import java.util.List;

public class ImportContactActivity extends Activity {
    private List<MyContact> contactList;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_contact);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
        } else {
            ContentResolver contentResolver = getContentResolver();
            contactList = SystemContactUtil.readContacts(contentResolver);
            listView = (ListView) findViewById(R.id.system_contacts_view);
            listView.setAdapter(new MyContactAdapter(contactList, getBaseContext()));
            //listView.setAdapter(new ArrayAdapter<MyContact>(this, android.R.layout.simple_list_item_1, contactList));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.import_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    contactList = SystemContactUtil.readContacts(getContentResolver());
                } else {
                    Toast.makeText(this, "你拒绝了读取联系人权限！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                ;
        }
    }
}
