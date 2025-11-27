package com.example.btquatrinh2;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Xml;

import androidx.core.content.FileProvider;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;

public class XmlHandler {

    // 1. Xuất XML (Đã thêm Note)
    public static void exportAndEmail(Context context, ArrayList<Customer> list) {
        try {
            File file = new File(context.getExternalFilesDir(null), "customers.xml");
            FileOutputStream fos = new FileOutputStream(file);

            XmlSerializer xmlSerializer = Xml.newSerializer();
            StringWriter writer = new StringWriter();
            xmlSerializer.setOutput(writer);
            xmlSerializer.startDocument("UTF-8", true);
            xmlSerializer.startTag(null, "Customers");

            for (Customer c : list) {
                xmlSerializer.startTag(null, "Customer");

                xmlSerializer.startTag(null, "Phone");
                xmlSerializer.text(c.phone != null ? c.phone : "");
                xmlSerializer.endTag(null, "Phone");

                xmlSerializer.startTag(null, "Points");
                xmlSerializer.text(String.valueOf(c.points));
                xmlSerializer.endTag(null, "Points");

                xmlSerializer.startTag(null, "Created");
                xmlSerializer.text(c.createdDate != null ? c.createdDate : "");
                xmlSerializer.endTag(null, "Created");

                xmlSerializer.startTag(null, "Updated");
                xmlSerializer.text(c.updatedDate != null ? c.updatedDate : "");
                xmlSerializer.endTag(null, "Updated");

                // --- MỚI: THÊM GHI CHÚ ---
                xmlSerializer.startTag(null, "Note");
                xmlSerializer.text(c.note != null ? c.note : "");
                xmlSerializer.endTag(null, "Note");

                xmlSerializer.endTag(null, "Customer");
            }

            xmlSerializer.endTag(null, "Customers");
            xmlSerializer.endDocument();
            xmlSerializer.flush();

            fos.write(writer.toString().getBytes());
            fos.close();

            sendEmail(context, file);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ... (Hàm sendEmail giữ nguyên) ...
    private static void sendEmail(Context context, File file) {
        Uri path = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/xml");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Danh sách khách hàng");
        emailIntent.putExtra(Intent.EXTRA_STREAM, path);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(emailIntent, "Gửi email..."));
    }

    // 2. Import XML (Sửa FileInputStream -> InputStream và thêm đọc Note)
    public static ArrayList<Customer> parseXml(InputStream is) { // <-- SỬA Ở ĐÂY
        ArrayList<Customer> list = new ArrayList<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(is, null);

            int eventType = parser.getEventType();
            Customer currentCus = null;
            String text = "";

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagname = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagname.equalsIgnoreCase("Customer")) {
                            // Tạo object tạm (đảm bảo Constructor Customer của bạn có 5 tham số)
                            currentCus = new Customer("", 0, "", "", "");
                        }
                        break;
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if (tagname.equalsIgnoreCase("Customer")) {
                            if (currentCus != null) list.add(currentCus);
                        } else if (currentCus != null) {
                            if (tagname.equalsIgnoreCase("Phone")) {
                                currentCus.phone = text;
                            } else if (tagname.equalsIgnoreCase("Points")) {
                                try { currentCus.points = Integer.parseInt(text); } catch (Exception e) { currentCus.points = 0; }
                            } else if (tagname.equalsIgnoreCase("Created")) {
                                currentCus.createdDate = text;
                            } else if (tagname.equalsIgnoreCase("Updated")) {
                                currentCus.updatedDate = text;
                            } else if (tagname.equalsIgnoreCase("Note")) { // --- MỚI: ĐỌC NOTE ---
                                currentCus.note = text;
                            }
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}