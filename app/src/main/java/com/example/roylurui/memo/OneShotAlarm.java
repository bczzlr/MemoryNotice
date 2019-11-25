package com.example.roylurui.memo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.sfzhang.memo.R;

import org.litepal.crud.DataSupport;

/**
 * Created by sf roylurui on 2019/11/21.
 */
//做一个广播接收器
public class OneShotAlarm extends BroadcastReceiver {

    private int alarmId;
    int BIG_NUM_FOR_ALARM=100;

    @Override
    public void onReceive(Context context, Intent intent) {
        //showMemo(context);

        alarmId=intent.getIntExtra("alarmId",0);

        Toast.makeText(context,"Time UP!",Toast.LENGTH_LONG).show();

        //设置震动
        Vibrator vb =(Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vb.vibrate(300);

        showNotice(context);
    }

    //显示通知，并且提供点击功能
    private void showNotice(Context context) {
        int num=alarmId-BIG_NUM_FOR_ALARM;
        Log.d("MainActivity","alarmNoticeId "+num);


        Intent intent=new Intent(context,Edit.class);

        Memo record= getMemoWithId(num);
        deleteTheAlarm(num);//or num

        transportInformationToEdit(intent,record);

        //用来设置点击通知之后跳转到对应edit页面
        PendingIntent pi=PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);//PendingIntent.FLAG_UPDATE_CURRENT is very important which caused a bug and troubles me for a long time

        //通知
        NotificationManager manager=(NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        //设置通知的属性
        Notification notification=new NotificationCompat.Builder(context)
                .setContentTitle(record.getTextDate()+" "+record.getTextTime())
                .setContentText(record.getMainText())
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.icon))
                .setContentIntent(pi)
                .setAutoCancel(true)
                //.setStyle(new NotificationCompat.BigTextStyle().bigText(record.getMainText()))
                .setLights(Color.GREEN,1000,1000)
                .build();
        manager.notify(num,notification);
    }

    //删除数据库对应id的alarm（alarm为第二列，存储的是通知日期时间的字符串）
    private void deleteTheAlarm(int num) {
        ContentValues temp = new ContentValues();
        temp.put("alarm", "");
        String where = String.valueOf(num);
        DataSupport.updateAll(Memo.class, temp, "id = ?", where);
    }

    /**
     * 存储数据到intent中去
     * @param it
     * @param record
     */
    private void transportInformationToEdit(Intent it, Memo record) {
        it.putExtra("num",record.getNum());
        it.putExtra("tag",record.getTag());
        it.putExtra("textDate",record.getTextDate());
        it.putExtra("textTime",record.getTextTime());
        record.setAlarm("");
        it.putExtra("alarm","");
        it.putExtra("mainText",record.getMainText());
    }

    private Memo getMemoWithId(int num) {
        String whereArgs = String.valueOf(num);
        Memo record= DataSupport.where("id = ?", whereArgs).findFirst(Memo.class);
        return record;
    }
}
