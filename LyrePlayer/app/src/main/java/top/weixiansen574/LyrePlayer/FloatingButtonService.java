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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import top.weixiansen574.LyrePlayer.adapter.FLoatMusicListAdapter;
import top.weixiansen574.LyrePlayer.midi.Note;
import top.weixiansen574.LyrePlayer.util.NoteListStorage;


public class FloatingButtonService extends Service {
    public static boolean isStarted = false;
    boolean isRun = false;
    boolean isPause = false;
    long currentTime_ms = 0;
    ArrayList<Note> noteList;

    SharedPreferences keyCoordinates;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    FloatListManager floatListManager;
    private Button stop;
    private Button start;
    private Button list;
    private TextView midiName;
    private TextView currentTime;
    private ProgressBar progressBar;
    private TextView totalTime;
    private String name;

    private Button close;

    Thread playing;
    Thread timer;

    float speed = 1;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();
        name = intent.getStringExtra("name");
        midiName.setText(name);
        noteList = NoteListStorage.getNoteList(intent.getLongExtra("noteListKey",0));
        System.out.println("noteListSize："+noteList.size());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;
        floatListManager = new FloatListManager(this);
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

    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            //从xml布局文件中读取布局
            final View view = View.inflate(this, R.layout.float_layout, null);
            windowManager.addView(view, layoutParams);
            midiName = view.findViewById(R.id.midi_name);
            currentTime = view.findViewById(R.id.current_time);
            progressBar = view.findViewById(R.id.progressBar);
            totalTime = view.findViewById(R.id.total_time);
            final Handler currentTimeHand = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    currentTime.setText(new SimpleDateFormat("mm:ss").format(new Date(msg.what)));
                }
            };

            final Handler progressBarHand = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    progressBar.setProgress(msg.what);
                }
            };
            final Handler totalTimeHand = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    totalTime.setText(new SimpleDateFormat("mm:ss").format(new Date(msg.what)));
                }
            };
            final Handler toastHand = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 0:
                            Toast.makeText(FloatingButtonService.this, getString(R.string.End_of_performance), Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            };
            //初始化播放器
            resetPlaying(currentTimeHand, progressBarHand, totalTimeHand, toastHand);
            //从加载出的布局中找出按钮，并设置监听器
            start = view.findViewById(R.id.float_start);
            view.setOnTouchListener(new FloatingOnTouchListener());
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isRun) {
                        isRun = true;
                        start.setText("  ▎▎");
                        resetPlaying(currentTimeHand, progressBarHand, totalTimeHand, toastHand);
                        playing.start();
                    } else if (!isPause) {
                        isPause = true;
                        start.setText("▶");
                        Toast.makeText(FloatingButtonService.this, getString(R.string.yzt), Toast.LENGTH_SHORT).show();
                        //Toast.makeText(FloatingButtonService.this, getString(R.string.Do_not_click_repeatedly_to_play),Toast.LENGTH_SHORT).show();
                    } else {
                        isPause = false;
                        start.setText("  ▎▎");
                        Toast.makeText(FloatingButtonService.this, getString(R.string.zzbf), Toast.LENGTH_SHORT).show();
                    }
                }

            });
            start.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(FloatingButtonService.this, getString(R.string.xfcygb), Toast.LENGTH_LONG).show();
                    windowManager.removeViewImmediate(view);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(10000);
                                isRun = true;
                                playing.start();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    return false;
                }
            });
            stop = view.findViewById(R.id.float_stop);
            stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view_) {
                    if (playing != null) {
                        playing.interrupt();
                        timer.interrupt();
                        progressBar.setProgress(0);
                        currentTimeHand.sendEmptyMessage(0);
                        isRun = false;
                        start.setText("▶");
                    }
                    Toast.makeText(FloatingButtonService.this, getString(R.string.stops_running), Toast.LENGTH_SHORT).show();
                }
            });
            close = view.findViewById(R.id.close);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view_) {
                    if (playing != null) {
                        playing.interrupt();
                        if (timer != null) {
                            timer.interrupt();
                        }
                        isRun = false;
                        Toast.makeText(FloatingButtonService.this, getString(R.string.Suspended_window_closed), Toast.LENGTH_SHORT).show();
                    }
                    windowManager.removeViewImmediate(view);
                }
            });
            list = view.findViewById(R.id.float_list);
            list.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isRun) {
                        Toast.makeText(FloatingButtonService.this, getString(R.string.qxtzbf), Toast.LENGTH_SHORT).show();
                    } else {
                        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                        } else {
                            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                        }
                        layoutParams.format = PixelFormat.RGBA_8888;
                        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
                        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                        //设置此窗口后面的所有内容变暗的量
                        layoutParams.dimAmount = 0.6f;
                        final View musicListView = View.inflate(FloatingButtonService.this, R.layout.float_music_list, null);
                        windowManager.addView(musicListView, layoutParams);

                        final Handler closeMusicListHand = new Handler(Looper.myLooper()) {
                            @Override
                            public void handleMessage(Message msg) {
                                stopSelf();
                                windowManager.removeView(musicListView);
                            }
                        };

                        TextView b = musicListView.findViewById(R.id.button);
                        RecyclerView music_list = musicListView.findViewById(R.id.music_list);
                        ArrayList<String> musicNames = floatListManager.getMusicNames();
                        FLoatMusicListAdapter adapter = new FLoatMusicListAdapter(FloatingButtonService.this,musicNames);
                        adapter.setOnItemClickListener(new FLoatMusicListAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View v, int position) {
                                Toast.makeText(FloatingButtonService.this, musicNames.get(position), Toast.LENGTH_SHORT).show();
                                noteList = floatListManager.getFloatMusicBean(musicNames.get(position)).noteList;
                                speed = 1;
                                TextView textView = view.findViewById(R.id.midi_name);
                                textView.setText(musicNames.get(position));
                                closeMusicList(closeMusicListHand);
                            }
                        });
                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                closeMusicList(closeMusicListHand);
                            }
                        });
                        music_list.setAdapter(adapter);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FloatingButtonService.this);
                        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
                        music_list.setLayoutManager(linearLayoutManager);
                    }
                }
            });
        }
    }

    private void resetPlaying(final Handler currentTimeHand, final Handler progressBarHand, final Handler totalTimeHand, final Handler toastHand) {
        playing = new Thread(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(FloatingButtonService.this, "演奏开始!", Toast.LENGTH_SHORT).show();
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

                totalTimeHand.sendEmptyMessage((int) (noteList.get(noteList.size() - 1).getTick() * speed));
                System.out.println(((int) (noteList.get(noteList.size() - 1).getTick() * speed)));
                ArrayList<Note> ASetOfNotes = new ArrayList<>(10);
                timer = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (; isRun; currentTime_ms += 100) {
                            //判断是否被中断
                            if (Thread.currentThread().isInterrupted()) {
                                break;
                            }
                            currentTimeHand.sendEmptyMessage((int) currentTime_ms);
                            try {
                                Thread.sleep(100);

                                while (isPause) {
                                    Thread.sleep(100);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                currentTime_ms = 0;
                                break;
                            }
                        }
                    }
                });
                timer.start();
                for (int n = 0; n < noteList.size() && isRun; n++) {
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
                            if (ASetOfNotes.get(i).getValue() <= 7) {
                                x = input_x[ASetOfNotes.get(i).getValue() - 1];
                                y = input_y[0];
                            } else if (ASetOfNotes.get(i).getValue() >= 8 && ASetOfNotes.get(i).getValue() <= 14) {
                                x = input_x[ASetOfNotes.get(i).getValue() - 8];
                                y = input_y[1];
                            } else if (ASetOfNotes.get(i).getValue() >= 15 && ASetOfNotes.get(i).getValue() <= 21) {
                                x = input_x[ASetOfNotes.get(i).getValue() - 15];
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
                                //同步当前音符的时间戳
                                currentTime_ms = (long) (note.getTick() * speed);
                                currentTimeHand.sendEmptyMessage((int) currentTime_ms);
                                System.out.println("tick===" + note.getTick() + "  nextTick===" + nextTick);
                                Thread.sleep((long) ((float) (nextTick - note.getTick()) * speed));
                                while (isPause) {
                                    Thread.sleep(100);
                                }
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
            }
        });
    }

    private void closeMusicList(final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(0);
            }
        }).start();
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

    public void click(int[] xs, int[] ys, long time) {
        System.out.println("点击器已执行");
        GestureDescription.Builder gd = new GestureDescription.Builder();
        for (int i = 0; i < xs.length; i++) {
            Path path = new Path();
            path.moveTo(xs[i], ys[i]);
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

class clickThread implements Runnable {
    @Override
    public void run() {

    }
}