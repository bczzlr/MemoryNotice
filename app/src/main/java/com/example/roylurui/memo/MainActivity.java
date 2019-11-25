package com.example.roylurui.memo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.sfzhang.memo.R;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {


    //onemeno类型的集合
    private List<OneMemo> memolist = new ArrayList<>();

    //适配器
    MemoAdapter adapter;

    //主list view
    ListView lv;

    //提醒
    int BIG_NUM_FOR_ALARM = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Connector.getDatabase();
        //addDataLitepPal();
        loadHistoryData();

        adapter = new MemoAdapter(MainActivity.this, R.layout.memo_list, memolist);
        lv = (ListView) findViewById(R.id.list);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(this);
        lv.setOnItemLongClickListener(this);

    }

    //创建菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }


    //点击菜单toolbar的加号出发添加事件
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                onAdd();
                break;
            default:
        }
        return true;
    }

    /**
     * 从数据库中取数据
     */
    private void loadHistoryData() {
        List<Memo> memoes = DataSupport.findAll(Memo.class);

        //如果没取出来，从新初始化数据库
        if (memoes.size() == 0) {
            initializeLitePal();
            memoes = DataSupport.findAll(Memo.class);
        }

        //遍历数组
        for (Memo record : memoes) {
            //打印日志
            Log.d("MainActivity", "current num: " + record.getNum());
            Log.d("MainActivity", "id: " + record.getId());
            Log.d("MainActivity", "getAlarm: " + record.getAlarm());
            //从当前memo对象中去除属性，封装为onemenmo添加到onememo集合（防止布尔值存储出现错误）
            int tag = record.getTag();
            String textDate = record.getTextDate();
            String textTime = record.getTextTime();
            boolean alarm = record.getAlarm().length() > 1 ? true : false;
            String mainText = record.getMainText();
            OneMemo temp = new OneMemo(tag, textDate, textTime, alarm, mainText);
            memolist.add(temp);
        }

    }

    //test
    public void testAdd(View v) {
        /*
        Memo record=new Memo();
        record.setNum(1);
        record.setTag(1);
        record.setTextDate("1212");
        record.setTextTime("23:00");
        record.setAlarm("123");
        record.setMainText("hahaha");
        record.save();
        */
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //准备跳转到编辑界面
        Intent it = new Intent(this, Edit.class);

        //获取当前位置对应的memo对象
        Memo record = getMemoWithNum(position);

        //对象信息存储
        transportInformationToEdit(it, record);

        //带着信息跳转
        startActivityForResult(it, position);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //长按删除逻辑

        //获取当前memolist的大小
        int n = memolist.size();

        //如果需要删除memo已经设置闹钟
        //取消它
        if (memolist.get(position).getAlarm()) {
            cancelAlarm(position);
        }
        memolist.remove(position);
        adapter.notifyDataSetChanged();

        String whereArgs = String.valueOf(position); //why not position ?
        DataSupport.deleteAll(Memo.class, "num = ?", whereArgs);

        for (int i = position + 1; i < n; i++) {
            ContentValues temp = new ContentValues();
            temp.put("num", i - 1);
            String where = String.valueOf(i);
            DataSupport.updateAll(Memo.class, temp, "num = ?", where);
        }

        return true;
    }

    /**
     * 从edit activity返回，做刷新activity和list操作
     * @param requestCode
     * @param resultCode
     * @param it
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent it) {
        if (resultCode == RESULT_OK) {
            updateLitePalAndList(requestCode, it);
        }
    }


    //update the database and memolist acccording to the "num" memo that Edit.class return

    /**
     * 更新操作
     * @param requestCode
     * @param it
     */
    private void updateLitePalAndList(int requestCode, Intent it) {

        int num = requestCode;
        int tag = it.getIntExtra("tag", 0);

        Calendar c = Calendar.getInstance();
        String current_date = getCurrentDate(c);
        String current_time = getCurrentTime(c);

        String alarm = it.getStringExtra("alarm");
        String mainText = it.getStringExtra("mainText");

        boolean gotAlarm = alarm.length() > 1 ? true : false;
        OneMemo new_memo = new OneMemo(tag, current_date, current_time, gotAlarm, mainText);

        if ((requestCode + 1) > memolist.size()) {
            //数据库插入
            // add a new memo record into database
            addRecordToLitePal(num, tag, current_date, current_time, alarm, mainText);

            // add a new OneMemo object into memolist and show
            //list添加
            memolist.add(new_memo);
        } else {
            //if the previous has got an alarm clock
            //cancel it first
            if (memolist.get(num).getAlarm()) {
                cancelAlarm(num);
            }

            //update the previous "num" memo
            ContentValues temp = new ContentValues();
            temp.put("tag", tag);
            temp.put("textDate", current_date);
            temp.put("textTime", current_time);
            temp.put("alarm", alarm);
            temp.put("mainText", mainText);
            String where = String.valueOf(num);
            DataSupport.updateAll(Memo.class, temp, "num = ?", where);

            memolist.set(num, new_memo);
        }
        //if user has set up an alarm
        if (gotAlarm) {
            loadAlarm(alarm, requestCode, 0);
        }

        adapter.notifyDataSetChanged();
    }

    //当数据库中没有数据的时候
    /**
     * 初始化数据库
     */
    private void initializeLitePal() {
        Calendar c = Calendar.getInstance();
        String textDate = getCurrentDate(c);
        String textTime = getCurrentTime(c);


        //初始化的数据，插入两条数据
        addRecordToLitePal(0, 0, textDate, textTime, "", "点击是编辑");
        addRecordToLitePal(1, 1, textDate, textTime, "", "长按是删除");
    }

    /**
     * 得到当前日期并格式化,格式：XX/XX
     * @param c 日期对象
     * @return
     */
    private String getCurrentDate(Calendar c) {
        return c.get(Calendar.YEAR) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 得到当前时间并格式化，格式：XX：XX
     * @param c
     * @return
     */
    private String getCurrentTime(Calendar c) {
        String current_time = "";
        if (c.get(Calendar.HOUR_OF_DAY) < 10)
            current_time = current_time + "0" + c.get(Calendar.HOUR_OF_DAY);
        else current_time = current_time + c.get(Calendar.HOUR_OF_DAY);

        current_time = current_time + ":";

        if (c.get(Calendar.MINUTE) < 10) current_time = current_time + "0" + c.get(Calendar.MINUTE);
        else current_time = current_time + c.get(Calendar.MINUTE);

        return current_time;
    }

    /**
     * 根据下列数据将memo对象存入数据库
     * @param num
     * @param tag
     * @param textDate
     * @param textTime
     * @param alarm
     * @param mainText
     */
    private void addRecordToLitePal(int num, int tag, String textDate, String textTime, String alarm, String mainText) {
        Memo record = new Memo();
        record.setNum(num);
        record.setTag(tag);
        record.setTextDate(textDate);
        record.setTextTime(textTime);
        record.setAlarm(alarm);

        record.setMainText(mainText);
        record.save();
    }

    /**
     * 将memo对象中的数据提取出来放进intent中（其实可以javabean实现serializable接口，即可直接传递对象）
     *
     * @param it     inten对象
     * @param record memo对象
     */
    private void transportInformationToEdit(Intent it, Memo record) {
        it.putExtra("num", record.getNum());
        it.putExtra("tag", record.getTag());
        it.putExtra("textDate", record.getTextDate());
        it.putExtra("textTime", record.getTextTime());
        it.putExtra("alarm", record.getAlarm());
        it.putExtra("mainText", record.getMainText());
    }



    /**
     * 添加事件逻辑
     */
    public void onAdd() {
        Intent it = new Intent(this, Edit.class);

        int position = memolist.size();

        Calendar c = Calendar.getInstance();
        String current_date = getCurrentDate(c);
        String current_time = getCurrentTime(c);

        it.putExtra("num", position);
        it.putExtra("tag", 0);
        it.putExtra("textDate", current_date);
        it.putExtra("textTime", current_time);
        it.putExtra("alarm", "");
        it.putExtra("mainText", "");

        startActivityForResult(it, position);
    }

    /**
     * 根据序号从数据库中找到对应序号的memo对象
     *
     * @param num 序号（数据库中index）
     * @return memo对象
     */
    private Memo getMemoWithNum(int num) {
        String whereArgs = String.valueOf(num);
        Memo record = DataSupport.where("num = ?", whereArgs).findFirst(Memo.class);
        return record;
    }

    //***********************************load or cancel alarm************************************************************************************
    //*****************BUG  SOLVED*************************
    //still have a bug as I know:
    //after deleting a memo, the "num" changes, then the cancelAlarm may have some trouble (it do not cancel actually)
    //establishing a hash table may solve this problem
    //SOLVED through adding id
    //******************************************

    //set an alarm clock according to the "alarm"
    private void loadAlarm(String alarm, int num, int days) {

        //根据传入的alarm字符串，解析时间
        int alarm_hour = 0;
        int alarm_minute = 0;
        int alarm_year = 0;
        int alarm_month = 0;
        int alarm_day = 0;

        int i = 0, k = 0;
        while (i < alarm.length() && alarm.charAt(i) != '/') i++;
        alarm_year = Integer.parseInt(alarm.substring(k, i));
        k = i + 1;
        i++;
        while (i < alarm.length() && alarm.charAt(i) != '/') i++;
        alarm_month = Integer.parseInt(alarm.substring(k, i));
        k = i + 1;
        i++;
        while (i < alarm.length() && alarm.charAt(i) != ' ') i++;
        alarm_day = Integer.parseInt(alarm.substring(k, i));
        k = i + 1;
        i++;
        while (i < alarm.length() && alarm.charAt(i) != ':') i++;
        alarm_hour = Integer.parseInt(alarm.substring(k, i));
        k = i + 1;
        i++;
        alarm_minute = Integer.parseInt(alarm.substring(k));

        Memo record = getMemoWithNum(num);

        // When the alarm goes off, we want to broadcast an Intent to our
        // BroadcastReceiver. Here we make an Intent with an explicit class
        // name to have our own receiver (which has been published in
        // AndroidManifest.xml) instantiated and called, and then create an
        // IntentSender to have the intent executed as a broadcast.
        //响铃提醒时
        Intent intent = new Intent(MainActivity.this, OneShotAlarm.class);
        intent.putExtra("alarmId", record.getId() + BIG_NUM_FOR_ALARM);
        PendingIntent sender = PendingIntent.getBroadcast(
                MainActivity.this, record.getId() + BIG_NUM_FOR_ALARM, intent, 0);

        // 我们希望10秒后警报响起。
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        //calendar.add(Calendar.SECOND, 5);

        Calendar alarm_time = Calendar.getInstance();
        alarm_time.set(alarm_year, alarm_month - 1, alarm_day, alarm_hour, alarm_minute);

        int interval = 1000 * 60 * 60 * 24 * days;

        // Schedule the alarm!
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        //if(interval==0)
        am.set(AlarmManager.RTC_WAKEUP, alarm_time.getTimeInMillis(), sender);
    }

    /**
     * 取消当前位置的闹铃
     * @param num 当前位置
     */
    private void cancelAlarm(int num) {
        Memo record = getMemoWithNum(num);

        Intent intent = new Intent(MainActivity.this,
                OneShotAlarm.class);

        PendingIntent sender = PendingIntent.getBroadcast(
                MainActivity.this, record.getId() + BIG_NUM_FOR_ALARM, intent, 0);

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }

    //********************************************************************************************************************************
}

