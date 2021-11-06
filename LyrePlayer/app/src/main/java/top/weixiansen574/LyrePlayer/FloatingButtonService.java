package top.weixiansen574.LyrePlayer;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import top.weixiansen574.LyrePlayer.midi.Note;


public class FloatingButtonService extends Service {
    public static boolean isStarted = false;
    boolean isRun = false;

    SharedPreferences keyCoordinates;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private Button stop;
    private Button start;
    private TextView midiName;
    private TextView currentTime;
    private ProgressBar progressBar;
    private TextView totalTime;

    private Button close;

    Thread playing;
    Thread timer;

    float speed;

    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = 505;
        layoutParams.height = 280;
        layoutParams.x = 30;
        layoutParams.y = 100;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            //读取之前的midi信息
            final SharedPreferences midi_info = getSharedPreferences("midi_info", Context.MODE_PRIVATE);

            //从xml布局文件中读取布局
            final View view = View.inflate(this, R.layout.float_layout, null);
            windowManager.addView(view, layoutParams);
            midiName = view.findViewById(R.id.midi_name);
            midiName.setText(midi_info.getString("midi_name","错误"));
            currentTime = view.findViewById(R.id.current_time);
            progressBar = view.findViewById(R.id.progressBar);
            totalTime = view.findViewById(R.id.total_time);
            final Handler currentTimeHand = new Handler(Looper.myLooper()){
              @Override
              public void handleMessage(Message msg) {
                  currentTime.setText(new SimpleDateFormat("mm:ss").format(new Date(msg.what)));
              }
            };

            final Handler progressBarHand = new Handler(Looper.myLooper()){
                @Override
                public void handleMessage(Message msg) {
                    progressBar.setProgress(msg.what);
                }
            };
            final Handler totalTimeHand = new Handler(Looper.myLooper()){
                @Override
                public void handleMessage(Message msg) {
                    totalTime.setText(new SimpleDateFormat("mm:ss").format(new Date(msg.what)));
                }
            };
            final Handler toastHand = new Handler(Looper.myLooper()){
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 0 :
                            Toast.makeText(FloatingButtonService.this, getString(R.string.End_of_performance), Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            };
            //从加载出的布局中找出按钮，并设置监听器 我小白不知道还有view.findViewById这方法，随意.一下出来了 瞎猫碰到死耗子哈哈哈哈哈
            start = view.findViewById(R.id.float_start);
            midiName = view.findViewById(R.id.midi_name);
            view.setOnTouchListener(new FloatingOnTouchListener());
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!isRun) {
                        isRun = true;
                        playing = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //Toast.makeText(FloatingButtonService.this, "演奏开始!", Toast.LENGTH_SHORT).show();
                                File cache = getCacheDir();
                                cache = new File(cache, "lyreNotes");
                                try {
                                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cache));
                                    ArrayList<Note> noteList = (ArrayList<Note>) ois.readObject();
                                    ois.close();

                                    keyCoordinates = getSharedPreferences("key_coordinates", Context.MODE_PRIVATE);
                                    int[] input_x = {
                                            keyCoordinates.getInt("x1", 0),
                                            keyCoordinates.getInt("x2", 0),
                                            keyCoordinates.getInt("x3", 0),
                                            keyCoordinates.getInt("x4", 0),
                                            keyCoordinates.getInt("x5", 0),
                                            keyCoordinates.getInt("x6", 0),
                                            keyCoordinates.getInt("x7", 0)};
                                    int[] input_y = {
                                            keyCoordinates.getInt("y3", 0),
                                            keyCoordinates.getInt("y2", 0),
                                            keyCoordinates.getInt("y1", 0)};
                                    long nextTick;

                                    speed = midi_info.getFloat("speed", 1);
                                    totalTimeHand.sendEmptyMessage((int) (noteList.get(noteList.size() - 1).getTick() * speed));
                                    ArrayList<Note> ASetOfNotes = new ArrayList<>(10);
                                    timer = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            for (long currentTime_ms = 0; true; currentTime_ms += 1000) {
                                                //判断是否被中断
                                                if (Thread.currentThread().isInterrupted()) {
                                                    break;
                                                }
                                                currentTimeHand.sendEmptyMessage((int) currentTime_ms);
                                                try {
                                                    Thread.sleep(1000);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                    break;
                                                }
                                            }
                                        }
                                    });
                                    timer.start();
                                    for (int n = 0; n < noteList.size(); n++) {
                                        Note note = noteList.get(n);
                                        //搜索下一个音符的tick
                                        if (n + 1 < noteList.size()) {
                                            nextTick = noteList.get(n + 1).getTick();
                                        } else {
                                            nextTick = -1;
                                        }
                                        //如果下个时间等于现在的时间
                                        if (nextTick == note.getTick()) {
                                            ASetOfNotes.add(note);
                                        } else {
                                            ASetOfNotes.add(note);
                                            int[] xs = new int[ASetOfNotes.size()];
                                            int[] ys = new int[ASetOfNotes.size()];
                                            //遍历同时按下的一组音符
                                            for (int i = 0; i < ASetOfNotes.size(); i++) {
                                                int x = 0;
                                                int y = 0;
                                                if (ASetOfNotes.get(i).getNote() <= 7) {
                                                    x = input_x[ASetOfNotes.get(i).getNote() - 1];
                                                    y = input_y[0];
                                                } else if (ASetOfNotes.get(i).getNote() >= 8 && ASetOfNotes.get(i).getNote() <= 14) {
                                                    x = input_x[ASetOfNotes.get(i).getNote() - 8];
                                                    y = input_y[1];
                                                } else if (ASetOfNotes.get(i).getNote() >= 15 && ASetOfNotes.get(i).getNote() <= 21) {
                                                    x = input_x[ASetOfNotes.get(i).getNote() - 15];
                                                    y = input_y[2];
                                                }
                                                xs[i] = x;
                                                ys[i] = y;
                                            }
                                            click(xs, ys, 10);
                                            ASetOfNotes.clear();
                                            if (nextTick != -1) {
                                                progressBarHand.sendEmptyMessage((int) (100 * ((float) note.getTick() / (float) noteList.get(noteList.size() - 1).getTick())));
                                                //currentTimeHand.sendEmptyMessage((int) (note.getTick() * speed));
                                                try {
                                                    Thread.sleep((long) ((float) (nextTick - note.getTick()) * speed));
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                    break;
                                                }
                                            } else {
                                                progressBarHand.sendEmptyMessage(100);
                                                timer.interrupt();
                                                isRun = false;
                                                toastHand.sendEmptyMessage(0);
                                            }
                                        }
                                        System.out.println(note.toString());

                                    }
                                } catch (IOException | ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        playing.start();
                    }else {
                        Toast.makeText(FloatingButtonService.this, getString(R.string.Do_not_click_repeatedly_to_play),Toast.LENGTH_SHORT).show();
                    }
                }

            });
            stop = view.findViewById(R.id.float_stop);
            stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view_) {
                    if(playing != null) {
                        playing.interrupt();
                        timer.interrupt();
                        progressBar.setProgress(0);
                        currentTimeHand.sendEmptyMessage(0);
                        isRun = false;
                    }
                    Toast.makeText(FloatingButtonService.this, getString(R.string.stops_running),Toast.LENGTH_SHORT).show();
                }
            });
            close = view.findViewById(R.id.close);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view_) {
                    if(playing != null) {
                        playing.interrupt();
                        timer.interrupt();
                        isRun = false;
                        Toast.makeText(FloatingButtonService.this, getString(R.string.Suspended_window_closed),Toast.LENGTH_SHORT).show();
                    }
                    windowManager.removeViewImmediate(view);
                }
            });


        }
    }

    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                default:
                    break;
            }
            return false;
        }
    }
    public void click(int[] xs,int[] ys,long time){
        System.out.println("点击器已执行");
        GestureDescription.Builder gd = new GestureDescription.Builder();
        for (int i = 0; i < xs.length; i++) {
            Path path = new Path();
            path.moveTo(xs[i],ys[i]);
            gd.addStroke(new GestureDescription.StrokeDescription(path, 0, time));
        }

        ClickService.mService.dispatchGesture(gd.build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);

            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
            }
        }, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}