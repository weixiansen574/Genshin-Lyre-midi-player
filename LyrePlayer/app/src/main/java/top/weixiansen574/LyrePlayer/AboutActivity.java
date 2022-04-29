package top.weixiansen574.LyrePlayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {
    TextView versionName,github,bilibili,youtube;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //工具栏返回上一级按钮
        if (item.getItemId() == 16908332){
            finish();
        }
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        versionName = findViewById(R.id.version_name);
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(),0);
            versionName.setText(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        github = findViewById(R.id.github);
        bilibili = findViewById(R.id.bilibili);
        youtube = findViewById(R.id.youtube);

        SpannableString spannableString1 = new SpannableString("github");
        spannableString1.setSpan(new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                Intent intent = new Intent();
                intent.setData(Uri.parse("https://github.com/weixiansen574/Genshin-Lyre-midi-player"));
                startActivity(intent);
            }
        }, 0, github.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        github.setText(spannableString1);
        github.setMovementMethod(LinkMovementMethod.getInstance());

        SpannableString spannableString2 = new SpannableString("bilibili");
        spannableString2.setSpan(new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                Intent intent = new Intent();
                intent.setData(Uri.parse("https://space.bilibili.com/503113802"));
                startActivity(intent);
            }
        }, 0, bilibili.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        bilibili.setText(spannableString2);
        bilibili.setMovementMethod(LinkMovementMethod.getInstance());

        SpannableString spannableString3 = new SpannableString("YouTube");
        spannableString3.setSpan(new ClickableSpan() {

            @Override
            public void onClick(View widget) {
                Intent intent = new Intent();
                intent.setData(Uri.parse("https://www.youtube.com/channel/UCbCv9JlpYxGMj-fv5xiX4vA"));
                startActivity(intent);
            }
        }, 0, youtube.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        youtube.setText(spannableString3);
        youtube.setMovementMethod(LinkMovementMethod.getInstance());


    }

}