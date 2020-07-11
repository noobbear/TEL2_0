package com.fision.tel2_0;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.fision.beans.MyContact;
import com.fision.beans.MyContactAdapter;
import com.fision.dao.ContactDAO;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class UpdateAndDeleteContactActivity extends Activity {
    private ListView listView;
    private ContactDAO dao;
    private View view;
    private List<MyContact> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_and_delete_contact);
        dao = new ContactDAO(this, "mycontact.db", null, 1);
        dao.getWritableDatabase();
        Init();
    }

    public void Init() {
        listView = (ListView) findViewById(R.id.lv);
        list = dao.findAll();
        listView.setAdapter(new MyContactAdapter(list, getBaseContext()));
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ShowDelMyDialog(list.get((int) view.getTag()));
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ShowInfoMyDialog(list.get((int) view.getTag()));
            }
        });
    }

    private final void ShowInfoMyDialog(final MyContact c) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        String[] s = new String[]{"姓名：" + c.getName(), "电话：" + c.getPhonenum()};
        dialog.setTitle("联系人")
                .setItems(s, null)
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    private final void ShowDelMyDialog(final MyContact c) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("删除联系人")
                .setMessage("您是否要删除" + c.getName() + "？\n该操作无法恢复！");
        DialogInterface.OnClickListener li = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteContact(c);
            }
        };
        dialog.setPositiveButton("删除", li)
                .setNegativeButton("取消", null)
                .create()
                .show();
    }


    private void deleteContact(MyContact c) {
        dao.delete(c);
        try {
            if (c.getTx().startsWith(this.getExternalFilesDir(null).getCanonicalPath()) && !dao.queryByTx(c)) {
                if (deleteTX(c.getTx()))
                    Toast.makeText(this, "删除完毕", Toast.LENGTH_SHORT);
            } else {
                Toast.makeText(this, "删除完毕", Toast.LENGTH_SHORT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        onCreate(null);
    }

    private boolean deleteTX(String path) {
        File file = new File(path);
        return file.delete();
    }
}
