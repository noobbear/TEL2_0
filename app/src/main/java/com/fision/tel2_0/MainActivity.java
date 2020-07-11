package com.fision.tel2_0;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fision.beans.BakUtil;
import com.fision.beans.FileUtil;
import com.fision.beans.MyContact;
import com.fision.dao.ContactDAO;

import org.zeroturnaround.zip.commons.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements View.OnLongClickListener {
    private static final int CHOOSEPHOTO = 1;
    private static final int TAKEPHOTO = 3;
    private static final int REQUESTPERSSION = 2;
    private LinearLayout layout;
    private ContactDAO dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dao = new ContactDAO(this, "mycontact.db", null, 1);
        dao.getWritableDatabase();
        Init();
        checkOrGetPermission();
    }

    private void Init() {
        layout = (LinearLayout) findViewById(R.id.linearlayout);
        List<MyContact> list = dao.findAll();
        //list.addAll(SystemContactUtil.readContacts(getContentResolver())); // 添加通讯录联系人
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        for (MyContact c : list) {
            Log.v("contact", c.toString());
            ImageView imageView = new ImageView(this);
            if (c.getTx().startsWith("content://com.android.contacts")) {
                imageView.setImageURI(Uri.parse(c.getTx()));
            } else {
                imageView.setImageBitmap(BitmapFactory.decodeFile(c.getTx()));
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            //设置居中显示：
            params.gravity = Gravity.CENTER;
            //设置它的上下左右的margin：4个参数按顺序分别是左上右下
            params.setMargins(0, 0, 0, 0);
            imageView.setTag(c.getPhonenum());
            imageView.setOnLongClickListener(this);
            imageView.setLayoutParams(params);
            imageView.setMaxHeight(dm.widthPixels);
            imageView.setAdjustViewBounds(true);
            layout.addView(imageView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.action_settings:
                intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.id_item_add:
                intent = new Intent(MainActivity.this, Item_add_activity.class);
                startActivity(intent);
                break;
            case R.id.id_item_import:
                intent = new Intent(MainActivity.this, ImportContactActivity.class);
                startActivity(intent);
                break;
            case R.id.id_item_delete:
                intent = new Intent(MainActivity.this, UpdateAndDeleteContactActivity.class);
                startActivity(intent);
                break;
            case R.id.id_about:
                intent = new Intent(MainActivity.this, aboutActivity.class);
                startActivity(intent);
                break;
            case R.id.id_export_bak:
                creatBakZip();
                break;
            case R.id.id_import_bak:
                openZipChooser();
                break;
            default:
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openZipChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.setType(“image/*”);//选择图片
        //intent.setType(“audio/*”); //选择音频
        //intent.setType(“video/*”); //选择视频 （mp4 3gp 是android支持的视频格式）
        //intent.setType(“video/*;image/*”);//同时选择视频和图片
        intent.setType("application/zip");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "请选择备份好的压缩文件"), 3);
    }

    private void importBakZip(String zip) {
        File file = new File(zip);
        if (file.exists() && file.getName().toLowerCase(Locale.ENGLISH).endsWith(".zip")) {
            boolean rs = BakUtil.importBakZip(file, this);
            Toast.makeText(this, rs ? "导入成功" : "导入失败", Toast.LENGTH_LONG).show();
            if (rs) {
                Init();
            }
        } else {
            Toast.makeText(this, "不支持的文件类型", Toast.LENGTH_LONG).show();
        }
    }

    private void creatBakZip() {
        boolean creatBakZip = BakUtil.creatBakZip(this);
        File file = new File(BakUtil.BAK_DIR, "tmp");
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, creatBakZip ? "备份成功" : "备份失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onCreate(null);
    }

    @Override
    public boolean onLongClick(View v) {
        String num = v.getTag().toString();
        Toast.makeText(this, num, Toast.LENGTH_SHORT).show();
        call(num);
        return true;
    }

    private void call(String num) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);//不直接拨打
            //Intent intent = new Intent(Intent.ACTION_CALL);//直接拨打
            intent.setData(Uri.parse("tel:" + num));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void checkOrGetPermission() {
        List<String> ps = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ps.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            //ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUESTPERSSION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ps.add(Manifest.permission.CALL_PHONE);
        }
        if (ps.size() > 0)
            ActivityCompat.requestPermissions(this, (String[]) ps.toArray(new String[0]), REQUESTPERSSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUESTPERSSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "你拒绝了权限申请", Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 3:
                if (resultCode == 0) {
                    return;
                }
                String path;
                Uri uri = data.getData();
                path = FileUtil.getPath(this, uri);
                if (path != null && path.length() > 0) {
                    importBakZip(path);
                } else {
                    Toast.makeText(this, "不支持的文件类型:" + uri.getAuthority() + "--" + uri.getScheme(), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
