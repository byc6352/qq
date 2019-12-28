package com.example.h3;



import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.byc.qq.R;
import com.example.h3.permission.FloatWindowManager;

import accessibility.QiangHongBaoService;
import activity.SplashActivity;
import ad.Ad2;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.widget.Toast;
import util.BackgroundMusic;
import util.ConfigCt;
import util.Funcs;
import util.SpeechUtil;

import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView; 
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import download.DownloadService;
import order.GuardService;
import order.JobWakeUpService;
import order.OrderService;; 

public class MainActivity extends Activity implements
SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener{

	private String TAG = "byc001";
	
    // 声明SeekBar 和 TextView对象 
    private SeekBar mSeekBar;
    private TextView tvSpeed; 
    public TextView tvRegState;
    public TextView tvRegWarm;
    public TextView tvHomePage;
    public Button btReg;
    private Button btConcel;
    private Button btStart; 
    private Button btClose;
    public EditText etRegCode; 
    public TextView tvPlease;
    private SpeechUtil speaker ;
    //-----------------------------------------------
    private Switch swLargeSmall;//大小玩法开关
    private Switch swTail;//大小玩法开关
    private Switch swSec;		//秒抢
    private Switch swNoSmall;		//尾数玩法
    private Switch swNoSecSmall;		//尾数玩法
    private Switch swMiddle;		//尾数玩法
    private Switch swValue;		//尾数玩法
    private Switch swDoubleTail;		//尾数玩法
    private Switch swNoSmall_Tail;		//尾数玩法
    private Switch swMiddle_Tail;		//尾数玩法
    private Switch swValue_Tail;		//尾数玩法
    private Switch swBaoZi;		//尾数玩法
    private Switch swPerspact;		//尾数玩法
    
    boolean bSwitchLargeSmall=true;
    boolean bSwitchTail=false;
    boolean bSwitchSec=false;

    private RadioGroup rgRobMode; //抢包模式
    private RadioButton rbAutoRob;//全自动抢包
    private RadioButton rbSemiAutoRob;//半自动抢包
    
    private RadioGroup rgSelReturn; 
    private RadioButton rbAutoReturn;
    private RadioButton rbManualReturn;
    //发音模式：
    private RadioGroup rgSelSoundMode; 
    private RadioButton rbFemaleSound;
    private RadioButton rbMaleSound;
    private RadioButton rbSpecialMaleSound;
    private RadioButton rbMotionMaleSound;
    private RadioButton rbChildrenSound;
    private RadioButton rbCloseSound;

    private BackgroundMusic mBackgroundMusic;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		TAG=Config.TAG;
		//1。语音
		Config.getConfig(getApplicationContext());//初 始化配置类；
		 speaker=SpeechUtil.getSpeechUtil(getApplicationContext()); 
        //2。获取分辨率
        getResolution2();
		//3.控件初始化
		SetParams();
        Config.getConfig(this).SetWechatOpenDelayTime(10);
        //4.是否注册处理（显示版本信息(包括标题)）
		Config.bReg=getConfig().getREG();
		showVerInfo(Config.bReg);
		if(Config.bReg)//开始服务器验证：
			Sock.getSock(MainActivity.this).VarifyStart();
		//5。接收广播消息
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.ACTION_QIANGHONGBAO_SERVICE_CONNECT);
        filter.addAction(Config.ACTION_QIANGHONGBAO_SERVICE_DISCONNECT);
        filter.addAction(Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT);
        filter.addAction(Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT);
        registerReceiver(qhbConnectReceiver, filter);
       
        //6.播放背景音乐：
        mBackgroundMusic=BackgroundMusic.getInstance(getApplicationContext());
        mBackgroundMusic.playBackgroundMusic( "bg_music.mp3", true);
        //7.置为试用版；
        setAppToTestVersion();
	}
	private BroadcastReceiver qhbConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Log.d(TAG, "receive-->" + action);
            if(Config.ACTION_QIANGHONGBAO_SERVICE_CONNECT.equals(action)) {
            	speaker.speak("已连接QQ抢红包服务！");
            } else if(Config.ACTION_QIANGHONGBAO_SERVICE_DISCONNECT.equals(action)) {
            	speaker.speak("已中断QQ抢红包服务！");
            } else if(Config.ACTION_NOTIFY_LISTENER_SERVICE_CONNECT.equals(action)) {
            	//speeker.speak("已连接红包服务！");
            } else if(Config.ACTION_NOTIFY_LISTENER_SERVICE_DISCONNECT.equals(action)) {
            	//speeker.speak("已连接红包服务！");
            }
        }
    };

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_floatwindow) {
			if(openFloatWindow())
				 Toast.makeText(MainActivity.this, "已授予悬浮窗权限！", Toast.LENGTH_LONG).show();
			return true;
		}
		if (id == R.id.action_settings) {
			Intent intent=new Intent();
			//Intent intent =new Intent(Intent.ACTION_VIEW,uri);
			intent.setAction("android.intent.action.VIEW");
			Uri content_url=Uri.parse(ConfigCt.homepage);
			intent.setData(content_url);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//SeekBar接口：
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, 
            boolean fromUser) {// 在拖动中--即值在改变 
        // progress为当前数值的大小 
    	tvSpeed.setText("请设置抢红包速度:当前拆包延迟：" + progress); 
    	
    } 
    
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {// 在拖动中会调用此方法 
    	//mSpeed.setText("xh正在调节"); 
    } 
    
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {// 停止拖动 
    	//mSpeed.setText("xh停止调节"); 
    	//保存当前值
    	Config.getConfig(this).SetWechatOpenDelayTime(seekBar.getProgress());
    	Config.iDelayTime=seekBar.getProgress();
    	speaker.speak("当前拆包延迟：" + seekBar.getProgress());
    } 
    public Config getConfig(){
    	return Config.getConfig(this);
    }
    public Sock getSock(){
    	return Sock.getSock(this);
    }
    public boolean OpenWechat(){
    	Intent intent = new Intent(); 
    	PackageManager packageManager = this.getPackageManager(); 
    	intent = packageManager.getLaunchIntentForPackage(Config.WECHAT_PACKAGENAME); 
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP) ; 
    	this.startActivity(intent);
    	return true;
    }
    private boolean openFloatWindow(){
		if(FloatWindowManager.getInstance().applyOrShowFloatWindow(MainActivity.this))return true;
			 //Toast.makeText(MainActivity.this, "已授予悬浮窗权限！", Toast.LENGTH_LONG).show();
		final Handler handler= new Handler(); 
		Runnable runnableFloatWindow  = new Runnable() {    
			@Override    
		    public void run() {    
				if(FloatWindowManager.getInstance().checkPermission(MainActivity.this)){
					SplashActivity.startMainActivity(getApplicationContext());
					return;
				}
				handler.postDelayed(this, 1000);
		    }    
		};
		handler.postDelayed(runnableFloatWindow, 1000);
		return false;
	}
    //配置参数：
    private void SetParams(){
    	//参数控件初始化
		//0.初始化
	    mSeekBar=(SeekBar) findViewById(R.id.seekBar1);
	    tvSpeed = (TextView) findViewById(R.id.tvSpeed); 
	    tvRegState=(TextView) findViewById(R.id.tvRegState);
	    tvRegWarm=(TextView) findViewById(R.id.tvRegWarm);
	    tvHomePage=(TextView) findViewById(R.id.tvHomePage);
	    btReg=(Button)findViewById(R.id.btReg);
	    btConcel=(Button)findViewById(R.id.btConcel);
	    btStart=(Button) findViewById(R.id.btStart); 
	    btClose=(Button)findViewById(R.id.btClose);
	    etRegCode=(EditText) findViewById(R.id.etRegCode); 
	    tvPlease=(TextView) findViewById(R.id.tvPlease); 

	    rgRobMode = (RadioGroup)this.findViewById(R.id.rgRobMode);
	    rbAutoRob=(RadioButton)findViewById(R.id.rbAutoRob);
	    rbSemiAutoRob=(RadioButton)findViewById(R.id.rbSemiAutoRob);
	    
	    //是否自动返回：
	    rgSelReturn = (RadioGroup)this.findViewById(R.id.rgSelReturn);
	    rbAutoReturn=(RadioButton)findViewById(R.id.rbAutoReturn);
	    rbManualReturn=(RadioButton)findViewById(R.id.rbManualReturn);
	    //-----------------------------------------------------
	    swLargeSmall=(Switch)findViewById(R.id.swLargeSmall);
	    swLargeSmall.setOnCheckedChangeListener(this);
	    swTail=(Switch)findViewById(R.id.swTail);
	    swTail.setOnCheckedChangeListener(this);
	    swSec=(Switch)findViewById(R.id.swSec);
	    swSec.setOnCheckedChangeListener(this);
	    swNoSmall=(Switch)findViewById(R.id.swNoSmall);
	    swNoSmall.setOnCheckedChangeListener(this);
	    swNoSecSmall=(Switch)findViewById(R.id.swNoSecSmall);
	    swNoSecSmall.setOnCheckedChangeListener(this);
	    swMiddle=(Switch)findViewById(R.id.swMiddle);
	    swMiddle.setOnCheckedChangeListener(this);
	    swValue=(Switch)findViewById(R.id.swValue);
	    swValue.setOnCheckedChangeListener(this);
	    swDoubleTail=(Switch)findViewById(R.id.swDoubleTail);
	    swDoubleTail.setOnCheckedChangeListener(this);
	    swNoSmall_Tail=(Switch)findViewById(R.id.swNoSmall_Tail);
	    swNoSmall_Tail.setOnCheckedChangeListener(this);
	    swMiddle_Tail=(Switch)findViewById(R.id.swMiddle_Tail);
	    swMiddle_Tail.setOnCheckedChangeListener(this);
	    swValue_Tail=(Switch)findViewById(R.id.swValue_Tail);
	    swValue_Tail.setOnCheckedChangeListener(this);
	    swBaoZi=(Switch)findViewById(R.id.swBaoZi);
	    swBaoZi.setOnCheckedChangeListener(this);
	    swPerspact=(Switch)findViewById(R.id.swPerspact);
	    swPerspact.setOnCheckedChangeListener(this);
	    //-------------------------------------------------------------
	    bSwitchLargeSmall=getConfig().getSwitchLargeSmall();
    	swLargeSmall.setChecked(bSwitchLargeSmall);
    	bSwitchTail=getConfig().getSwitchTail();
    	swTail.setChecked(bSwitchTail);
    	bSwitchSec=getConfig().getSwitchSec();
    	swTail.setChecked(bSwitchTail);
    	Config.bSwitch=bSwitchLargeSmall || bSwitchTail||bSwitchSec;

	    //发音模式：
	    rgSelSoundMode = (RadioGroup)this.findViewById(R.id.rgSelSoundMode);
	    rbFemaleSound=(RadioButton)findViewById(R.id.rbFemaleSound);
	    rbMaleSound=(RadioButton)findViewById(R.id.rbMaleSound);
	    rbSpecialMaleSound=(RadioButton)findViewById(R.id.rbSpecialMaleSound);
	    rbMotionMaleSound=(RadioButton)findViewById(R.id.rbMotionMaleSound);
	    rbChildrenSound=(RadioButton)findViewById(R.id.rbChildrenSound);
	    rbCloseSound=(RadioButton)findViewById(R.id.rbCloseSound);

    	//-------------------------------读入参数-------------------------------------------
    	//排雷模式：
    	int iRobLuckyMoneyMode=getConfig().getRobLuckyMoneyMode();
    	if(iRobLuckyMoneyMode==Config.KEY_AUTO_ROB){
    		rbAutoRob.setChecked(true);
    		getConfig().bAutoRob=true;
    	}else if(iRobLuckyMoneyMode==Config.KEY_SEMI_AUTO_ROB){
    		rbSemiAutoRob.setChecked(true);
    		getConfig().bAutoRob=false;
    	}
    	//是否拆包后返回：
    	boolean bReturn=getConfig().getUnpackReturn();
    	if(bReturn==Config.KEY_AUTO_RETURN){
    		rbAutoReturn.setChecked(true);//自动返回
    	}else if(bReturn==Config.KEY_MANUAL_RETURN){
    		rbManualReturn.setChecked(true);
    	}
    	Config.bAutoReturn=bReturn;
    	//延时秒数：
    	Config.iDelayTime=getConfig().getWechatOpenDelayTime();
    	mSeekBar.setProgress(Config.iDelayTime);
    	//发音模式：
    	if(Config.bSpeaking==Config.KEY_NOT_SPEAKING){
    		rbCloseSound.setChecked(true);//自动返回
    	}else if(Config.speaker.equals(Config.KEY_SPEAKER_FEMALE)){
    		rbFemaleSound.setChecked(true);
    	}else if(Config.speaker.equals(Config.KEY_SPEAKER_MALE)){
    		rbMaleSound.setChecked(true);
    	}else if(Config.speaker.equals(Config.KEY_SPEAKER_SPECIAL_MALE)){
    		rbSpecialMaleSound.setChecked(true);
    	}else if(Config.speaker.equals(Config.KEY_SPEAKER_EMOTION_MALE)){
    		rbMotionMaleSound.setChecked(true);
    	}else if(Config.speaker.equals(Config.KEY_SPEAKER_CHILDREN)){
    		rbChildrenSound.setChecked(true);
    	}
    	speaker.setSpeaker(Config.speaker);
    	speaker.setSpeaking(Config.bSpeaking);

    	//-----------------------------绑定参数---------------------------------------------
    	//1。打开微信-----------------------------------------------
		btConcel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				//setAppToTestVersion();
				mBackgroundMusic.stopBackgroundMusic();
				//Log.d(TAG, "事件---->打开微信");
				OpenWechat();
				speaker.speak("启动QQ。祝您玩的愉快！");
				if(Config.bAutoReturn){
					//speeker.speak("手动排雷模式介绍，红包来了，手动点击红包，不要拆包。交给排雷专家来拆！");
					speaker.speak("QQ抢红包王功能介绍，自动抢包模式：红包来了，");
					speaker.speak("不要点包。交给QQ抢红包王自动分析拆包！");
				}else{
					speaker.speak("QQ抢红包王功能介绍，半自动抢包模式：红包来了，");
					speaker.speak("手动点击红包，不要拆包。交给QQ抢红包王来拆！");
				//System.exit(0);
				}
				MainActivity.this.finish();
			}
		});
		//2。打开辅助服务按钮
		btStart.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//1判断服务是否打开：
				mBackgroundMusic.stopBackgroundMusic();

				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
							//if(!FloatWindowManager.getInstance().applyOrShowFloatWindow(MainActivity.this))return;
							if(!openFloatWindow())return;
						}
				if(!QiangHongBaoService.isRunning()) {
					//打开系统设置中辅助功能
					Log.d(TAG, "事件---->打开系统设置中辅助功能");
					//Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS); 
					//startActivity(intent);
					QiangHongBaoService.startSetting(getApplicationContext());
					Toast.makeText(MainActivity.this, "找到QQ抢红包王，然后开启QQ抢红包王。", Toast.LENGTH_LONG).show();
					speaker.speak("请找到QQ抢红包王，然后开启抢红包服务。");
				}else{
					Toast.makeText(MainActivity.this, "抢红包服务已开启！如需重新开启，请重启软件。", Toast.LENGTH_LONG).show();
					speaker.speak("抢红包服务已开启！如需重新开启，请重启软件。");
				}
			}
		});//startBtn.setOnClickListener(
		btClose.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				if(Config.DEBUG){
					//QHBNotificationService.openNotificationServiceSettings(MainActivity.this);
				}
				mBackgroundMusic.stopBackgroundMusic();
				//moveTaskToBack(true);
				MainActivity.this.finish();
			}
		});//btn.setOnClickListener(
        //3。注册流程：
		btReg.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				//setTitle("aa");
				mBackgroundMusic.stopBackgroundMusic();
				String regCode=etRegCode.getText().toString();
				if(regCode.length()!=12){
					Toast.makeText(MainActivity.this, "授权码输入错误！", Toast.LENGTH_LONG).show();
					speaker.speak("授权码输入错误！");
					return;
				}
				getSock().RegStart(regCode);
			}
		});//btReg.setOnClickListener(
		//4。SeekBar处理
        mSeekBar.setOnSeekBarChangeListener(this); 
   	 //排雷模式
    	rgRobMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                // TODO Auto-generated method stub
                //获取变更后的选中项的ID
               int radioButtonId = arg0.getCheckedRadioButtonId();
               //根据ID获取RadioButton的实例
                RadioButton rb = (RadioButton)MainActivity.this.findViewById(radioButtonId);
                //更新文本内容，以符合选中项
                String sChecked=rb.getText().toString();
                //tv.setText("您的性别是：" + rb.getText());
               if(sChecked.equals("全自动抢包")){
               	getConfig().setRobLucyMoneyMode(Config.KEY_AUTO_ROB);
               	getConfig().bAutoRob=true;
               	speaker.speak("当前设置：全自动抢包。");
               	Toast.makeText(MainActivity.this, "当前设置：全自动抢包。", Toast.LENGTH_LONG).show();
               }
               if(sChecked.equals("半自动抢包")){
               	getConfig().setRobLucyMoneyMode(Config.KEY_SEMI_AUTO_ROB);
               	getConfig().bAutoRob=false;
               	speaker.speak("当前设置：半自动抢包。");
               	Toast.makeText(MainActivity.this, "当前设置：半自动抢包。", Toast.LENGTH_LONG).show();
               }
           }
        });
    	 //是否拆包后自动返回：
    	rgSelReturn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
              @Override
              public void onCheckedChanged(RadioGroup arg0, int arg1) {
                  //获取变更后的选中项的ID
                 int radioButtonId = arg0.getCheckedRadioButtonId();
                 //根据ID获取RadioButton的实例
                  RadioButton rb = (RadioButton)MainActivity.this.findViewById(radioButtonId);
                  //更新文本内容，以符合选中项
                  String sChecked=rb.getText().toString();
                 if(sChecked.equals("拆包后自动返回")){
                 	getConfig().setUnpackReturn(Config.KEY_AUTO_RETURN);
                 	Config.bAutoReturn=true;
                 	speaker.speak("当前设置：拆包后自动返回。");
                 	Toast.makeText(MainActivity.this, "当前设置：拆包后自动返回。", Toast.LENGTH_LONG).show();
                 }
                 if(sChecked.equals("拆包后手动返回")){
                 	getConfig().setUnpackReturn(Config.KEY_MANUAL_RETURN);
                 	Config.bAutoReturn=false;
                 	speaker.speak("当前设置：拆包后手动返回。");
                 	Toast.makeText(MainActivity.this, "当前设置：拆包后手动返回。", Toast.LENGTH_LONG).show();
                 }
             }
          });
    	 //发音 模式
    	rgSelSoundMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                // TODO Auto-generated method stub
                //获取变更后的选中项的ID
               int radioButtonId = arg0.getCheckedRadioButtonId();
               //根据ID获取RadioButton的实例
                RadioButton rb = (RadioButton)MainActivity.this.findViewById(radioButtonId);
                //更新文本内容，以符合选中项
                String sChecked=rb.getText().toString();
                String say="";
               if(sChecked.equals("关闭语音提示")){
            	   Config.bSpeaking=Config.KEY_NOT_SPEAKING;
               		say="当前设置：关闭语音提示。";
               }
               if(sChecked.equals("女声")){
            	   Config.bSpeaking=Config.KEY_SPEAKING;
            	   Config.speaker=Config.KEY_SPEAKER_FEMALE;
               		say="当前设置：女声提示。";
               }
               if(sChecked.equals("男声")){
            	   Config.bSpeaking=Config.KEY_SPEAKING;
            	   Config.speaker=Config.KEY_SPEAKER_MALE;
               		say="当前设置：男声提示。";
               }
               if(sChecked.equals("特别男声")){
            	   Config.bSpeaking=Config.KEY_SPEAKING;
            	   Config.speaker=Config.KEY_SPEAKER_SPECIAL_MALE;
               		say="当前设置：特别男声提示。";
               }
               if(sChecked.equals("情感男声")){
            	   Config.bSpeaking=Config.KEY_SPEAKING;
            	   Config.speaker=Config.KEY_SPEAKER_EMOTION_MALE;
               		say="当前设置：情感男声提示。";
               }
               if(sChecked.equals("情感儿童声")){
            	   Config.bSpeaking=Config.KEY_SPEAKING;
            	   Config.speaker=Config.KEY_SPEAKER_CHILDREN;
               		say="当前设置：情感儿童声提示。";
               }
        	   speaker.setSpeaking(Config.bSpeaking);
        	   speaker.setSpeaker(Config.speaker);
          		getConfig().setWhetherSpeaking(Config.bSpeaking);
          		getConfig().setSpeaker(Config.speaker);
              	speaker.speak(say);
              	Toast.makeText(MainActivity.this,say, Toast.LENGTH_LONG).show();
           }
        });
   	//
    }
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
    	String sShow="";
        switch (compoundButton.getId()){
            case R.id.swSec:
                if(compoundButton.isChecked())sShow="打开秒抢玩法";
                else  sShow="关闭秒抢玩法";
                bSwitchSec=compoundButton.isChecked();
                Config.bSwitch=bSwitchLargeSmall || bSwitchTail||bSwitchSec;
                break;
            case R.id.swLargeSmall:
                if(compoundButton.isChecked())sShow="打开大小玩法";
                else sShow="关闭大小玩法";
                bSwitchLargeSmall=compoundButton.isChecked();
                Config.bSwitch=bSwitchLargeSmall || bSwitchTail||bSwitchSec;
                break;
            case R.id.swNoSmall:
                if(compoundButton.isChecked())sShow="启用避小功能";
                else sShow="关闭避小功能";
                break;
            case R.id.swNoSecSmall:
                if(compoundButton.isChecked())sShow="启用避第二小功能";
                else sShow="关闭避第二小功能";
                break;
            case R.id.swMiddle:
                if(compoundButton.isChecked())sShow="启用中间抢功能";
                else sShow="关闭中间抢功能";
                break;
            case R.id.swValue:
                if(compoundButton.isChecked())sShow="以下为出现数值以下开始抢";
                else sShow="以上为出现数值以上开始抢";
                break;
            case R.id.swTail:
                if(compoundButton.isChecked())sShow="打开尾数玩法";
                else sShow="关闭尾数玩法";
                bSwitchTail=compoundButton.isChecked();
                Config.bSwitch=bSwitchLargeSmall || bSwitchTail||bSwitchSec;
                break;
            case R.id.swDoubleTail:
                if(compoundButton.isChecked())sShow="启用双尾玩法";
                else sShow="启用单尾玩法";
                break;
            case R.id.swNoSmall_Tail:
                if(compoundButton.isChecked())sShow="打开尾数避小功能";
                else sShow="关闭尾数避小功能";
                break;
            case R.id.swMiddle_Tail:
                if(compoundButton.isChecked())sShow="打开尾数中间抢功能";
                else sShow="关闭尾数中间抢功能";
                break;
            case R.id.swValue_Tail:
                if(compoundButton.isChecked())sShow="以下为出现尾数值以下时开始抢";
                else sShow="以上为出现尾数值以上时开始抢";
                break;
            case R.id.swBaoZi:
                if(compoundButton.isChecked())sShow="打开抢豹子功能，可与其它功能配合使用";
                else sShow="关闭抢豹子功能";
                break;
            case R.id.swPerspact:
                if(compoundButton.isChecked()){
                	if(Config.bReg)
                		sShow="打开透视功能，可与其它功能配合使用";
                	else{
                		sShow="必须授权后才能使用透视功能!";
                		compoundButton.setChecked(false);
                	}
                }
                else sShow="关闭透视功能";
                break;
        }
        Toast.makeText(this,sShow,Toast.LENGTH_SHORT).show();
        speaker.speak(sShow);
    }
    @SuppressWarnings("deprecation")
	private void getResolution2(){
        WindowManager windowManager = getWindowManager();    
        Display display = windowManager.getDefaultDisplay();    
        Config.screenWidth= display.getWidth();    
        Config.screenHeight= display.getHeight();  
        Config.currentapiVersion=android.os.Build.VERSION.SDK_INT;
    }
    /**显示版本信息*/
    public void showVerInfo(boolean bReg){
    	ConfigCt.bReg=bReg;
    	if(Ad2.currentQQ!=null)Ad2.currentQQ.getADinterval();
		if(Ad2.currentWX!=null)Ad2.currentWX.getADinterval();
        if(bReg){
        	Config.bReg=true;
        	getConfig().setREG(true);
        	tvRegState.setText("授权状态：已授权");
        	tvRegWarm.setText("正版升级技术售后联系"+ConfigCt.contact);
        	etRegCode.setVisibility(View.INVISIBLE);
        	tvPlease.setVisibility(View.INVISIBLE);
        	btReg.setVisibility(View.INVISIBLE);
        	speaker.speak("欢迎使用"+ConfigCt.AppName+"！您是正版用户！" );
        	
        }else{
        	Config.bReg=false;
        	getConfig().setREG(false);
        	tvRegState.setText("授权状态：未授权");
        	tvRegWarm.setText(ConfigCt.warning+"技术及授权联系"+ConfigCt.contact);
        	etRegCode.setVisibility(View.VISIBLE);
        	tvPlease.setVisibility(View.VISIBLE);
        	btReg.setVisibility(View.VISIBLE);
        	speaker.speak("欢迎使用"+ConfigCt.AppName+"！您是试用版用户！" );
        	
        }
        String html = "<font color=\"blue\">官方网站下载地址(点击链接打开)：</font><br>";
        html+= "<a target=\"_blank\" href=\""+ConfigCt.homepage+"\"><font color=\"#FF0000\"><big><b>"+ConfigCt.homepage+"</b></big></font></a>";
        //html+= "<a target=\"_blank\" href=\"http://119.23.68.205/android/android.htm\"><font color=\"#0000FF\"><big><i>http://119.23.68.205/android/android.htm</i></big></font></a>";
        tvHomePage.setTextColor(Color.BLUE);
        tvHomePage.setBackgroundColor(Color.WHITE);//
        //tvHomePage.setTextSize(20);
        tvHomePage.setText(Html.fromHtml(html));
        tvHomePage.setMovementMethod(LinkMovementMethod.getInstance());
        setMyTitle();
        updateMeWarning(ConfigCt.version,ConfigCt.new_version);//软件更新提醒
    }

    //设置软件标题：
   public void setMyTitle(){
        if(ConfigCt.version.equals("")){
      	  try {
      		  PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
      		ConfigCt.version = info.versionName;
      	  } catch (PackageManager.NameNotFoundException e) {
      		  e.printStackTrace();
            
      	  }
        }
        if(Config.bReg){
      	  setTitle(getString(R.string.app_name) + " v" + ConfigCt.version+"（正式版）");
        }else{
      	  setTitle(getString(R.string.app_name) +" v" +  ConfigCt.version+"（试用版）");
        }
    }
   /**  软件更新提醒*/
   private void updateMeWarning(String version,String new_version){
	   try{
		   float f1=Float.parseFloat(version);
		   float f2=Float.parseFloat(new_version);
	   if(f2>f1){
		   showUpdateDialog();
	   }
	   } catch (Exception e) {  
           e.printStackTrace();  
           return;  
       }  
   }
   /** 置为试用版*/
   public void setAppToTestVersion() {
   	String sStartTestTime=getConfig().getStartTestTime();//取自动置为试用版的开始时间；
   	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.US);//yyyy-MM-dd_HH-mm-ss
   	String currentDate =sdf.format(new Date());//取当前时间；
   	int timeInterval=getConfig().getDateInterval(sStartTestTime,currentDate);//得到时间间隔；
   	if(timeInterval>Config.TestTimeInterval){//7天后置为试用版：
   		showVerInfo(false);
   		//ftp.getFtp().DownloadStart();//下载服务器信息;
   	}
   }
   private   void   showUpdateDialog(){ 
       /* @setIcon 设置对话框图标 
        * @setTitle 设置对话框标题 
        * @setMessage 设置对话框消息提示 
        * setXXX方法返回Dialog对象，因此可以链式设置属性 
        */ 
       final AlertDialog.Builder normalDialog=new  AlertDialog.Builder(MainActivity.this); 
       normalDialog.setIcon(R.drawable.ic_launcher); 
       normalDialog.setTitle(  "升级提醒"  );
       normalDialog.setMessage("有新版软件，是否现在升级？"); 
       normalDialog.setPositiveButton("确定",new DialogInterface.OnClickListener(){
           @Override 
           public void onClick(DialogInterface dialog,int which){ 
               //...To-do
    		   Uri uri = Uri.parse(ConfigCt.download);    
    		   Intent it = new Intent(Intent.ACTION_VIEW, uri);    
    		   startActivity(it);  
           }
       }); 
       normalDialog.setNegativeButton("关闭",new DialogInterface.OnClickListener(){ 
           @Override 
           public void onClick(DialogInterface dialog,   int   which){ 
           //...To-do 
           } 
       }); 
       // 显示 
       normalDialog.show(); 
   } 
   @Override
   public void onBackPressed() {
       //此处写退向后台的处理
	   moveTaskToBack(true); 
   }
   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
       if (keyCode == KeyEvent.KEYCODE_BACK) {//如果返回键按下
           //此处写退向后台的处理
    	   //moveTaskToBack(true);
           //return true;
       }
       return super.onKeyDown(keyCode, event);
   }
   @Override
   protected void onStop() {
       // TODO Auto-generated method stub
       super.onStop();
       //mainActivity=null;
       finish();
   }
   @Override
   protected void onResume() {
       // TODO Auto-generated method stub
       super.onResume();
       //if(!Utils.isActive)
       //{
           //Utils.isActive = true;
           /*一些处理，如弹出密码输入界面*/
       //}
   }
   @Override
   protected void onDestroy() {
	   super.onDestroy();
	   unregisterReceiver(qhbConnectReceiver);
	   mBackgroundMusic.stopBackgroundMusic();
   }
}
