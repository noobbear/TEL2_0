package com.fision.beans;

import android.content.Context;
import android.util.Xml;
import android.widget.Toast;

import com.fision.dao.ContactDAO;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;
import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class BakUtil {

    public static final String REC_DIR = "/sdcard/fision.tel/rec";
    public static final String BAK_DIR = "/sdcard/fision.tel/bak";
    public static final String BAK_XML_DIR = "/sdcard/fision.tel/bak/tmp/";
    public static final String BAK_IMAGE_DIR = "/sdcard/fision.tel/bak/tmp/";

    private static final void initDir(String dir) {
        File xmlDir = new File(BAK_XML_DIR + dir, "xml");
        File imageDir = new File(BAK_IMAGE_DIR + dir, "image");
        if (!xmlDir.exists()) {
            xmlDir.mkdirs();
        }
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
    }

    public static final boolean creatBakZip(Context context) {
        String dir = TimeUtil.getNowTimeStr();
        initDir(dir);
        ContactDAO dao = new ContactDAO(context, "mycontact.db", null, 1);
        List<MyContact> all = dao.findAll();
        boolean rs = createXml(all, dir) && copyImageFilesToBakDir(all, dir);
        if (rs) {
            Toast.makeText(context, "文件压缩中", Toast.LENGTH_SHORT).show();
            String sourcePath = BAK_DIR + File.separator + "tmp" + File.separator + dir;
            String zipPath = BAK_DIR + File.separator + dir + ".zip";
            File source = new File(sourcePath);
            File target = new File(zipPath);
            try {
                if (!target.exists() && !target.createNewFile()) {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            rs = zipDir(source, target);
        }
        return rs;
    }

    private static final boolean createXml(List<MyContact> list, String dir) {
        XmlSerializer serializer = Xml.newSerializer();
        File file = new File(BAK_XML_DIR + dir + File.separator + "xml", "list.xml");
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            serializer.setOutput(fileOutputStream, "utf-8");
            serializer.startDocument("utf-8", true);
            serializer.startTag(null, "list");
            serializer.attribute(null, "size", String.valueOf(list.size()));
            for (MyContact c : list) {
                serializer.startTag(null, "contact");
                serializer.startTag(null, "id");
                serializer.text(String.valueOf(c.getId()));
                serializer.endTag(null, "id");
                serializer.startTag(null, "name");
                serializer.text(c.getName());
                serializer.endTag(null, "name");
                serializer.startTag(null, "number");
                serializer.text(c.getPhonenum());
                serializer.endTag(null, "number");
                serializer.startTag(null, "srcId");
                serializer.text(c.getTx());
                serializer.endTag(null, "srcId");
                serializer.endTag(null, "contact");
            }
            serializer.endTag(null, "list");
            //告诉序列化器文件生成完毕
            serializer.endDocument();
            serializer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static final boolean copyImageFilesToBakDir(List<MyContact> list, String dirName) {
        int count = 0;
        File target;
        File dir = new File(BAK_IMAGE_DIR + dirName, "image");
        if (!dir.exists() && !dir.mkdirs()) {
            return false;
        }
        for (MyContact c : list) {
            File image = new File(c.getTx());
            if (image.exists() && image.canRead()) {
                target = new File(dir, image.getName());
                boolean rename = copyFile(image, target);
                count += rename ? 1 : 0;
            }
        }
        return count == list.size();
    }

    private static final boolean zipDir(File source, File target) {
        ZipUtil.pack(source, target);
        return target.exists() && target.canRead();
    }

    //复制文件
    public static boolean copyFile(File oldFile, File newFile) {
        if (oldFile == null || newFile == null)
            return false;
        try {
            InputStream in = new FileInputStream(oldFile);
            OutputStream out = new FileOutputStream(newFile);
            byte[] data = new byte[1024];
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean importBakZip(File file, Context context) {
        File rec = new File(REC_DIR);
        if (!rec.exists() && !rec.mkdirs()) {
            return false;
        }
        ZipUtil.unpack(file, rec);
        String[] list = rec.list();
        if (list == null || list.length == 0) {
            return false;
        }
        File xml = new File(rec, "xml/list.xml");
        File image = new File(rec, "image");
        if (xml.exists() && image.exists() && image.isDirectory()) {
            List<MyContact> contacts = parseContactFromXml(xml, context);
            File appFilesDir = context.getExternalFilesDir(null);
            int count = copyImageFilesFromBakDir(image.listFiles(), appFilesDir);
            ContactDAO dao = new ContactDAO(context, "mycontact.db", null, 1);
            int insert = 0;
            for (MyContact c : contacts) {
                c.setTx(appFilesDir + File.separator + FileUtil.getFileByPath(c.getTx()).getName());
                if (dao.insert(c)) {
                    insert++;
                }
            }
            Toast.makeText(context, "恢复联系人" + insert + "个，恢复图片" + count + "张", Toast.LENGTH_LONG).show();
            try {
                FileUtils.deleteDirectory(rec);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return false;
        }
        return true;
    }

    private static final List<MyContact> parseContactFromXml(File file, Context context) {
        List<MyContact> list = new ArrayList<>();
        try {
            list.addAll(getMyContacts(new FileInputStream(file)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ArrayList<MyContact> getMyContacts(InputStream xml) throws Exception {
        ArrayList<MyContact> MyContacts = null;
        MyContact MyContact = null;
        // 创建一个xml解析的工厂  
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        // 获得xml解析类的引用  
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(xml, "UTF-8");
        // 获得事件的类型  
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    MyContacts = new ArrayList<MyContact>();
                    break;
                case XmlPullParser.START_TAG:
                    if ("contact".equals(parser.getName())) {
                        MyContact = new MyContact();
                    } else if ("id".equals(parser.getName())) {
                        String id = parser.nextText();// 获取该节点的内容
                        MyContact.setId(Integer.valueOf(id));
                    } else if ("name".equals(parser.getName())) {
                        String name = parser.nextText();// 获取该节点的内容
                        MyContact.setName(name);
                    } else if ("number".equals(parser.getName())) {
                        MyContact.setPhonenum(parser.nextText());
                    } else if ("srcId".equals(parser.getName())) {
                        MyContact.setTx(parser.nextText());
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if ("contact".equals(parser.getName())) {
                        MyContacts.add(MyContact);
                        MyContact = null;
                    }
                    break;
            }
            eventType = parser.next();
        }
        return MyContacts;
    }

    private static final int copyImageFilesFromBakDir(File[] files, File appFilesDir) {
        int count = 0;
        for (File file : files) {
            File newFile = new File(appFilesDir, file.getName());
            boolean copyFile = copyFile(file, newFile);
            if (copyFile) {
                count++;
            }
        }
        return count;
    }
}
