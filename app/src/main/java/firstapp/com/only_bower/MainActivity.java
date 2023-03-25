package firstapp.com.only_bower;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private WebView mw;
    private Button mbt1;
    private Button bt1;
    private Button bt2;
    private Button bt3;
    private Button bt4;
    private EditText et1;
    private ProgressBar progressBar;
    private ListView listView;

    private List<webUrlItem> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
        setContentView(R.layout.activity_main);
        mw = (WebView) findViewById(R.id.myweb);
        mbt1 = (Button) findViewById(R.id.mbt1);
        et1 = (EditText) findViewById(R.id.mtv);
        bt1 = (Button) findViewById(R.id.bt1);
        bt2 = (Button) findViewById(R.id.bt2);
        bt3 = (Button) findViewById(R.id.bt3);
        bt4 = (Button) findViewById(R.id.bt4);
        progressBar = (ProgressBar) findViewById(R.id.pro);
        listView = (ListView) findViewById(R.id.list_item);
        list = new ArrayList<>();

        init();

        webUrlAdapter adapter = new webUrlAdapter(MainActivity.this, R.layout.item, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                webUrlItem wb = list.get(position);
                mw.loadUrl(wb.getUrl());
            }
        });

        addmwFeature();

        mbt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = et1.getText().toString();
                mw.loadUrl(url);
            }
        });
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mw.canGoBack()){
                    mw.goBack();
                }
            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mw.canGoForward()){
                    mw.goForward();
                }
            }
        });
        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mw.reload();
            }
        });
        bt4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listView.getVisibility()!=View.VISIBLE)
                    listView.setVisibility(View.VISIBLE);
                else
                    listView.setVisibility(View.GONE);
            }
        });

        mw.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress<100){
                    if(progressBar.getVisibility()==View.GONE || progressBar.getVisibility()==View.INVISIBLE){
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    progressBar.setProgress(newProgress);
                }
                else{
                    progressBar.setVisibility(View.GONE);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        mw.setWebViewClient(new WebViewClient(){
            public void onProgressChanged(WebView view, int progress)
            {
                // update the progressBar
                MainActivity.this.setProgress(progress * 100);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(!url.startsWith("http://") && !url.startsWith("https://")){
                    return;
                }
                //添加网页信息到list
                String title = view.getTitle();
                SharedPreferences pref = getSharedPreferences("urldata",MODE_PRIVATE);
                if(pref.contains(url)){
                    pref.edit().remove(url);
                    for(int i=0;i<list.size();i++){
                        webUrlItem wb = list.get(i);
                        if(wb.getUrl().equals(url)){
                            list.remove(i);
                            break;
                        }
                    }
                }
                list.add(0, new webUrlItem(url, title));
                adapter.notifyDataSetChanged();
                boolean isf = pref.edit().putString(url, title).commit();
                Log.d("llgh", isf+" "+pref.getAll().size());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try{
                    if(!url.startsWith("http://") && !url.startsWith("https://")){
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        new AlertDialog.Builder(MainActivity.this).setTitle("三方应用").setMessage("是否打开第三方应用")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(intent);
                                    }
                                }).setNegativeButton("取消",null).show();
                        return true;
                    }
                }catch (Exception e) {//防止crash (如果手机上没有安装处理某个scheme开头的url的APP, 会导致crash)
                    return true;//没有安装该app时，返回true，表示拦截自定义链接，但不跳转，避免弹出上面的错误页面
                }
                // 重写WebViewClient的shouldOverrideUrlLoading()方法
                //使用WebView加载显示url
                et1.setText(url);
                view.loadUrl(url);
                listView.setVisibility(View.GONE);
                //返回true
                return true;
            }
        });


    }

    void init() {
        SharedPreferences pref = getSharedPreferences("urldata", MODE_PRIVATE);
        Set<? extends Map.Entry<String, ?>> set = pref.getAll().entrySet();
        Log.d("llgh", pref.getAll().toString());
        for(Map.Entry<String, ?> s : set){
            Log.d("llgh", s.getKey());
            String url = s.getKey();
            String title = (String) s.getValue();
            list.add(0, new webUrlItem(url, title));
        }
    }

    void addmwFeature(){
        WebSettings webSettings = mw.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
    }

    final int PERMISSIONS_RESULT_CODE = 121;// 自定义
    //申请权限
    private void checkPermission() {
        final String[] permissions = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
        };


        // SDK版本 大于或等于23 动态申请权限
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        // 检查 未授权 的 权限
        List<String> pl = new ArrayList<>();
        for(String permission : permissions){
            if(ContextCompat.checkSelfPermission(this,permission)
                    != PackageManager.PERMISSION_GRANTED){
                pl.add(permission);
            }
        }
        // 申请权限
        if(pl.size() > 0){
            ActivityCompat.requestPermissions(this, pl.toArray(new String[0]),PERMISSIONS_RESULT_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSIONS_RESULT_CODE && grantResults.length > 0){
            // 判断 是否获得 权限
            for(int i=0;i < grantResults.length;i++){
                // 未得到 授权 的权限
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    if(!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])){
                        Log.i("WY",permissions[i]+" 未授权且不再询问");
                    }else{
                        Log.i("WY",permissions[i]+" 未授权");
                    }
                }else{
                    Log.i("WY",permissions[i]+" 已授权");
                }
            }
        }
    }

}