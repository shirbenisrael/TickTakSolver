package com.shirbi.ticktaksolver;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ShareImage {
    ShareImage(Activity activity)
    {
        m_activity = activity;
    }

    private Activity m_activity;

    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int REQUEST_READ_STORAGE = 3;

    private void GetPermissionToFiles() {
        boolean hasPermission = (ContextCompat.checkSelfPermission(m_activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        if (!hasPermission) {
            ActivityCompat.requestPermissions(m_activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }

        hasPermission = (ContextCompat.checkSelfPermission(m_activity,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        if (!hasPermission) {
            ActivityCompat.requestPermissions(m_activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READ_STORAGE);
        }

    }

    public void shareView(View view) {
        //GetPermissionToFiles();

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Drawable bgDrawable = view.getBackground();
            if (bgDrawable != null) {
                bgDrawable.draw(canvas);
            } else {
                canvas.drawColor(Color.BLACK);
            }
            view.draw(canvas);

            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
            String strDate = dateFormat.format(date);
            String fileName = "tick_tack_solver_" + strDate + ".jpg";

            File picFile =  new File(m_activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);

            try
            {
                picFile.createNewFile();
                FileOutputStream picOut = new FileOutputStream(picFile);
                boolean saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, picOut);
                picOut.flush();
                picOut.getFD().sync();
                picOut.close();
                MediaStore.Images.Media.insertImage(m_activity.getContentResolver(), picFile.getAbsolutePath(), picFile.getName(), picFile.getName());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            view.destroyDrawingCache();

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("image/*");
            sharingIntent.putExtra(Intent.EXTRA_TEXT,
                    "https://play.google.com/store/apps/details?id=com.shirbi.ticktaksolver");
            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri pictureUri = FileProvider.getUriForFile(m_activity, "com.shirbi.ticktacksolver", picFile);  // use this version for API >= 24
            sharingIntent.putExtra(Intent.EXTRA_STREAM, pictureUri);
            m_activity.startActivity(Intent.createChooser(sharingIntent, "Share via"));
        } else {
            //Error
        }
    }
}
