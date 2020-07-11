package com.fision.tel2_0;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.fision.beans.MyContact;
import com.fision.dao.ContactDAO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class Item_add_activity extends Activity implements OnClickListener{

	private static final int CHOOSEPHOTO= 1;
	private static final int REQUESTPERSSION = 2;
	private ImageView tx;
	private EditText text_name,text_num;
	private Button save;
	private String newImagePath=null;//复制后的图片路径
	private String imagePath=null;//原图路径
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_add_activity);
		tx = (ImageView) findViewById(R.id.tx_image);
		save = (Button) findViewById(R.id.bt_save);
		text_name=(EditText) findViewById(R.id.t_name);
		text_num=(EditText) findViewById(R.id.t_num);
		tx.setOnClickListener(this);
		save.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.item_add_activity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		Intent intent;
		switch (id) {
		case R.id.action_settings:
			Toast.makeText(Item_add_activity.this, "Setting", Toast.LENGTH_SHORT).show();
			break;
		default:
			Toast.makeText(Item_add_activity.this, "Error", Toast.LENGTH_SHORT).show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.tx_image:
			openGallery();
			break;
		case R.id.bt_save:
			if (imagePath==null) {
				Toast.makeText(this, "必须为联系人提供一张图片", Toast.LENGTH_SHORT).show();
				openGallery();
			} else {
			   if(saveContact()){
				Toast.makeText(this, "保存联系人成功", Toast.LENGTH_SHORT).show();
                   this.finish();
			   }else {
                   Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
               }
			}
			break;
		default: Toast.makeText(this, "unexcepted error:clicked ?", Toast.LENGTH_LONG).show();
			break;
		}
		
	}

	/**
	 * 打开图库
	 */
	private void openGallery() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		startActivityForResult(intent, CHOOSEPHOTO);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case CHOOSEPHOTO:
			if (resultCode==RESULT_OK) {
				if (Build.VERSION.SDK_INT>=19) {
					handleImageOnKitKat(data);
				} else {
					handleImageBeforeKitKat(data);
				}
			}
			break;
		default:
			Toast.makeText(this, "必须为联系人提供一张图片", Toast.LENGTH_SHORT).show();
			break;
		}
	}
	
	private void handleImageBeforeKitKat(Intent data) {
		Uri uri=data.getData();
		imagePath=getImagePath(uri,null);
		displayImage(imagePath);//显示原图
	}

	@TargetApi(19)
	private void handleImageOnKitKat(Intent data) {
		Uri uri=data.getData();
		if (DocumentsContract.isDocumentUri(this, uri)) {
			//处理Document类型的uri
			String docid=DocumentsContract.getDocumentId(uri);
			if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
				String id=docid.split(":")[1];
				String selection = MediaStore.Images.Media._ID + "=" + id;
				newImagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
			} else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
				Uri contenturi=ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docid));
				newImagePath=getImagePath(contenturi, null);
			}else {
				Toast.makeText(this, "Error:authority:"+uri.getAuthority(), Toast.LENGTH_LONG).show();
			}
		} else if("content".equalsIgnoreCase(uri.getScheme())){
			imagePath=getImagePath(uri, null);
		}else if ("file".equalsIgnoreCase(uri.getScheme())) {
			imagePath=uri.getPath();
		}else{
			Toast.makeText(this, "Error: uri is not the type of Document or content or file:"+uri.getAuthority()+"--"+uri.getScheme(), Toast.LENGTH_LONG).show();
		}
		//displayImage(newImagePath);//显示新图
		displayImage(imagePath);//显示原图
	}
	
	private String getImagePath(Uri uri, String selection) {
		String path = null;
		Cursor cursor=getContentResolver().query(uri, null, selection, null, null);
		if (cursor!=null) {
		    if (cursor.moveToFirst()){
                path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
		    }
            cursor.close();
		}
		return path;
	}

	/**
	 * 移动文件到指定路径/data/data/包名/file并更名
	 * @param oldPath 旧文件路径
	 * @param newName 新文件名
	 * @return 新文件路径
	 */
	private String MoveToExternalFilesDir(String oldPath,String newName){
		File old = new File(oldPath);
		//把文件放到SD卡
		File newFile = new File(this.getExternalFilesDir(null),newName);
		//File newFile = new File(this.getFilesDir(),newName);
		//old.renameTo(newFile);
		//如果复制失败，取原图
		if (!copyFile(old,newFile)){
			return oldPath;
		}
		Log.w("path","path="+newFile.getPath());
		return  newFile.getPath();
	}


	//复制图片
	private boolean copyFile(File oldFile,File newFile){
		if (oldFile==null||newFile==null)
			return  false;
		try{
			InputStream in= new FileInputStream(oldFile);
			OutputStream out = new FileOutputStream(newFile);
			byte[] data=new byte[1024];
			while (in.read(data)!=-1){
				out.write(data);
			}
			in.close();
			out.close();
		}catch (Exception e){
			//e.printStackTrace();
			return false;
		}
		return  true;
	}

	//凭借原图片完整路径，使用UUID生成唯一的文件名
	private String getNewName(String oldPath){
		String newName="";
		Log.w("path","oldPath="+oldPath);
		UUID uuid=UUID.randomUUID();
		String e=oldPath.substring(oldPath.lastIndexOf('.')+1);
		newName=uuid.toString()+e;
		Log.w("path","newName="+newName);
		return newName;
	}

	//显示图片
	private void displayImage(String path){
		if (path!=null) {
			Bitmap bitmap = BitmapFactory.decodeFile(path);
			tx.setImageBitmap(bitmap);
		}else {
			Toast.makeText(Item_add_activity.this, "获取图片失败", Toast.LENGTH_SHORT).show();
		}
	}

	private boolean saveContact(){
        MyContact contact = new MyContact();
        if (text_name!=null&& (!"".equals(text_name.getText().toString()))){
            contact.setName(text_name.getText().toString());
        }else {
            Toast.makeText(this,"联系人不能为空",Toast.LENGTH_SHORT).show();
            return false;
        }
        if (text_num!=null&& (!"".equals(text_num.getText().toString()))){
            contact.setPhonenum(text_num.getText().toString());
        }else {
            Toast.makeText(this,"电话号码不能为空",Toast.LENGTH_SHORT).show();
            return false;
        }
        //复制图片到SD卡
		newImagePath=MoveToExternalFilesDir(imagePath,getNewName(imagePath));
        if (newImagePath!=null&& (!"".equals(newImagePath))){
            contact.setTx(newImagePath);
        }else {
            Toast.makeText(this,"头像不能为空",Toast.LENGTH_SHORT).show();
            return false;
        }
        ContactDAO dao=new ContactDAO(this,"mycontact.db",null,1);
        return dao.insert(contact);
    }
}
