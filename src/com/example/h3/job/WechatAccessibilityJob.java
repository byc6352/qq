/**
 * 
 */
package com.example.h3.job;



import util.BackgroundMusic;
import com.example.h3.Config;

import accessibility.QiangHongBaoService;
import notification.IStatusBarNotification;
import notification.NotifyHelper;



import accessibility.BaseAccessibilityJob;

import com.byc.qq.R;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;


import accessibility.AccessibilityHelper;

import com.example.h3.FloatingWindowPic;

import util.SpeechUtil;

/**
 * @author byc
 *
 */
public class WechatAccessibilityJob extends BaseAccessibilityJob  {
	
	private static WechatAccessibilityJob current;
	//-------------------------------拆包延时---------------------------------------------
	//private int mWechatOpenDelayTime=0;//拆包延时
	private SpeechUtil speaker ;


	//------------------------------拆包信息------------------------------------------------
	
	public int mLuckyMoneyType=0;//红包类别：已拆过，福利包，有雷包；
	private LuckyMoneyReceiveJob mReceiveJob;
	private LuckyMoneyDetailJob mDetailJob;
	private LuckyMoneyLauncherJob mLauncherJob;
	private LuckyMoney mLuckyMoney;//红包对象；
	
	private String mCurrentUI="";//当前界面
	private AccessibilityNodeInfo mRootNode; //窗体根结点
	private FloatingWindowPic fwp;
	private boolean bDel=false;//删除广告语
	private BackgroundMusic mBackgroundMusic;
	public WechatAccessibilityJob(){
		super(new String[]{Config.WECHAT_PACKAGENAME});
	}

    @Override
    public void onCreateJob(QiangHongBaoService service) {
        super.onCreateJob(service);
        EventStart();
        mReceiveJob=LuckyMoneyReceiveJob.getLuckyMoneyReceiveJob(context);
        mDetailJob=LuckyMoneyDetailJob.getLuckyMoneyDetailJob(context);
        speaker=SpeechUtil.getSpeechUtil(context);
        mLauncherJob=LuckyMoneyLauncherJob.getLuckyMoneyLauncherJob(context);
        mLuckyMoney=LuckyMoney.getLuckyMoney(context);//红包对象；
        //浮动窗口：
		fwp=FloatingWindowPic.getFloatingWindowPic(context,R.layout.float_click_delay_show);
		int w=Config.screenWidth-200;
		int h=Config.screenHeight-200;
		fwp.SetFloatViewPara(100, 200,w,h);
		//接收广播消息
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.ACTION_CLICK_LUCKY_MONEY);
        context.registerReceiver(ClickLuckyMoneyReceiver, filter);

        mBackgroundMusic=BackgroundMusic.getInstance(context);
     
    }
    @Override
    public void onStopJob() {
    	super.onStopJob();
    	
    }
    public static synchronized WechatAccessibilityJob getJob() {
        if(current == null) {
            current = new WechatAccessibilityJob();
        }
        return current;
    }
    //----------------------------------------------------------------------------------------
    @Override
    public void onReceiveJob(AccessibilityEvent event) {
    	super.onReceiveJob(event);
    	if(!mIsEventWorking)return;
    	if(!mIsTargetPackageName)return;
    	//本程序处理：
    	//if(!event.getPackageName().toString().equals(Config.WECHAT_PACKAGENAME))return;
    	int eventType = event.getEventType();
    	if(event.getClassName()==null)return;
    	String sClassName=event.getClassName().toString();
    	if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
    		mCurrentUI=sClassName;
    	}
    	debug(event);
		//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
		//+++++++++++++++++++++++++++++++++窗口改变+++++++++++++++++++++++++++++++++++++++++++++++
		if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

			//-------------------------点红包信息界面----------------------------------------------------
			if(mCurrentUI.equals(Config.WINDOW_LUCKYMONEY_LAUNCHER_UI)){
				Config.JobState=Config.STATE_NONE;
			}//if(mCurrentUI.equals(Config.WINDOW_LUCKYMONEY_LAUNCHER_UI)){
			
			//-------------------------打开红包界面----------------------------------------------------
			if(mCurrentUI.equals(Config.WINDOW_LUCKYMONEY_RECEIVEUI)){
				if(Config.JobState==Config.STATE_NONE)return;
				AccessibilityNodeInfo rootNode=event.getSource();
				if(rootNode==null)return;
				if(rootNode.getChildCount()==0)return;
				if(Config.wv>=818&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)return;
				processUnpackLuckyMoneyShow(rootNode);
			}//if(mCurrentUI.equals(Config.WINDOW_LUCKYMONEY_RECEIVEUI)){

		}//if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) 
		//+++++++++++++++++++++++++++++++++内容改变+++++++++++++++++++++++++++++++++++++++++++++++
		//+++++++++++++++++++++++++++++++++内容改变+++++++++++++++++++++++++++++++++++++++++++++++
				if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
					
					if(Config.JobState==Config.STATE_CLICK_LUCKYMONEY){
						//-------------------------打开红包界面----------------------------------------------------
						if(mCurrentUI.equals(Config.WINDOW_LUCKYMONEY_RECEIVEUI)){
							AccessibilityNodeInfo rootNode=event.getSource();
							if(rootNode==null)return;
							rootNode=AccessibilityHelper.getRootNode(rootNode);
							if(rootNode.getChildCount()==0)return;
							processUnpackLuckyMoneyShow(rootNode);
						}
						return;
					}
					if(mCurrentUI.equals(Config.WINDOW_LUCKYMONEY_LAUNCHER_UI)){
						AccessibilityNodeInfo rootNode=event.getSource();
						if(rootNode==null)return;
						rootNode=AccessibilityHelper.getRootNode(rootNode);
						
						//---------------------------------------------------------------------------------------
						if(mLauncherJob.isMemberChatUi(rootNode)){
							
							if(!Config.bSwitch)return;//总开关关闭；
							if(clickLuckyMoney3(rootNode)){Config.JobState=Config.STATE_CLICK_LUCKYMONEY;return;}	
						}
						//if(Config.getConfig(context).bAutoClearThunder)clickLuckyMoney();
					}//if(mCurrentUI.equals(Config.WINDOW_LUCKYMONEY_LAUNCHER_UI)){
				}//if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) 
    }
    //红包来了，点击：
    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean clickLuckyMoney3(AccessibilityNodeInfo rootNode){

    	if(mCurrentUI.equals(Config.WINDOW_LUCKYMONEY_LAUNCHER_UI)){
    		
    		mLuckyMoney.LuckyMoneyNode=mLuckyMoney.getLastLuckyMoneyNode(rootNode);//取最后一个红包；
    		mLuckyMoney.RobbedLuckyMoneyInfoNode=mLuckyMoney.getLastReceivedLuckyMoneyInfoNode(rootNode);
    		if(mLuckyMoney.isNewLuckyMoney(mLuckyMoney.LuckyMoneyNode, mLuckyMoney.RobbedLuckyMoneyInfoNode)){
    		
    					//进入抢包状态：
    					Config.JobState=Config.STATE_CLICK_LUCKYMONEY;
    					mLuckyMoney.WhoRobbed=LuckyMoney.TYPE_LUCKYMONEY_NO_ROBBED;
    					mLuckyMoney.LuckyMoneyType=LuckyMoney.TYPE_LUCKYMONEY_PERSONALITY;
    					mLuckyMoney.LuckyMoneyDefined=LuckyMoney.TYPE_LUCKYMONEY_THUNDER;
    					ClickLuckyMoneyDelay();//点包延时显示；
    					return true;
    				
    			}
    	}
        return false;
    }
    /*
     * 显抢到红包示详情
     */
    private void processUnpackLuckyMoneyShow(AccessibilityNodeInfo rootNode){
    	if(rootNode==null)return;
		if(rootNode.getChildCount()==0)return;
    	if(mDetailJob.isDetailUI(rootNode)){//详细信息界面：
			Config.JobState=Config.STATE_NONE;
		}else{
				if(mReceiveJob.isNoLuckyMoneyUI(rootNode)){//红包领完
					mLuckyMoney.WhoRobbed=LuckyMoney.TYPE_LUCKYMONEY_YOU_ROBBED;
					mReceiveJob.showAnimation(false,null);
					//say="红包完了！没抢到！";//
		    		//Toast.makeText(context,say, Toast.LENGTH_LONG).show();
		    		//speaker.speak(say);	
				}else{
					mLuckyMoney.WhoRobbed=LuckyMoney.TYPE_LUCKYMONEY_ME_ROBBED;//红包抢到了;
					//显示信息：
					if(Config.bShowMoney){
						mReceiveJob.unpackLuckyMoneyShow(rootNode);
					}else{
						mReceiveJob.showAnimation(true,null);
						//say="抢到红包了！";//
						//Toast.makeText(context,say, Toast.LENGTH_LONG).show();
			    		//speaker.speak(say);	
					}
				}
				Config.JobState=Config.STATE_NONE;
				AccessibilityHelper.performBack(service);//返回Lancher_UI;
				//mReceiveJob.closeWindow(rootNode);
				mCurrentUI=Config.WINDOW_LUCKYMONEY_LAUNCHER_UI;
		}//if(mDetailJob.isDetailUI(mRootNode)){//详细信息界面：
    }
    //红包来了，点击：
    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean clickLuckyMoney(AccessibilityNodeInfo rootNode){

    	if(mCurrentUI.equals(Config.WINDOW_LUCKYMONEY_LAUNCHER_UI)){
    		
    		mLuckyMoney.LuckyMoneyNode=mLuckyMoney.getLastLuckyMoneyNode(rootNode);//取最后一个红包；
    	

    		if(mLuckyMoney.LuckyMoneyNode!=null){
    			//if(mLauncherJob.isLuckyMoneyLei(mLauncherJob.LuckyMoneyNode)){
    					//进入抢包状态：
    					Config.JobState=Config.STATE_CLICK_LUCKYMONEY;
    					mLuckyMoney.WhoRobbed=LuckyMoney.TYPE_LUCKYMONEY_NO_ROBBED;
    					mLuckyMoney.LuckyMoneyType=LuckyMoney.TYPE_LUCKYMONEY_COMMON;
    					ClickLuckyMoneyDelay();//点包延时显示；
    					return true;
    			//}
    		}
    	}
        return false;
    }
    //个性红包来了，点击：
    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean clickLuckyMoney2(AccessibilityNodeInfo rootNode){

    	if(mCurrentUI.equals(Config.WINDOW_LUCKYMONEY_LAUNCHER_UI)){
    		
    		mLuckyMoney.LuckyMoneyNode=mLuckyMoney.getLastLuckyMoneyNode2(rootNode);
    	
    		mLuckyMoney.RobbedLuckyMoneyInfoNode=mLuckyMoney.getLastReceivedLuckyMoneyInfoNode(rootNode);
    		if(mLuckyMoney.isNewLuckyMoney(mLuckyMoney.LuckyMoneyNode, mLuckyMoney.RobbedLuckyMoneyInfoNode)){
    					//进入抢包状态：
    					Config.JobState=Config.STATE_CLICK_LUCKYMONEY;
    					mLuckyMoney.WhoRobbed=LuckyMoney.TYPE_LUCKYMONEY_NO_ROBBED;
    					mLuckyMoney.LuckyMoneyType=LuckyMoney.TYPE_LUCKYMONEY_PERSONALITY;
    					ClickLuckyMoneyDelay();//点包延时显示；
    					return true;
    		}
    	}
        return false;
    }
    //点包延时：
    private void ClickLuckyMoneyDelay() {
    	mBackgroundMusic.playBackgroundMusic( "ml.wav", true);
    	if(Config.iDelayTime>0)speaker.speak("正在预备抢包：");
		fwp.ShowFloatingWindow(); 
    	fwp.c=Config.iDelayTime;
    	fwp.mSendMsg=Config.ACTION_CLICK_LUCKY_MONEY;
    	fwp.mShowPicType=FloatingWindowPic.SHOW_PIC_GREEN;
    	fwp.StartSwitchPics();

    }



	private BroadcastReceiver ClickLuckyMoneyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(Config.ACTION_CLICK_LUCKY_MONEY.equals(action)) {
            	//点击红包：
            	mBackgroundMusic.stopBackgroundMusic();
            	if(!mCurrentUI.equals(Config.WINDOW_LUCKYMONEY_LAUNCHER_UI))return;
            	if(mLuckyMoney.ClickLuckyMoney(mLuckyMoney.LuckyMoneyNode))
            		Config.JobState=Config.STATE_CLICK_LUCKYMONEY;
            	else
            		Config.JobState=Config.STATE_NONE;
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)

    public void onNotificationPosted(IStatusBarNotification sbn) {
        Notification nf = sbn.getNotification();
        String text = String.valueOf(sbn.getNotification().tickerText);
        notificationEvent(text, nf);
    }
    /** 通知栏事件*/
    private void notificationEvent(String ticker, Notification nf) {
        String text = ticker;
        int index = text.indexOf(":");
        if(index != -1) {
            text = text.substring(index + 1);
        }
        text = text.trim();
        //transferAccounts.notificationEvent(ticker, nf);
        //if(text.contains(TransferAccounts.WX_TRANSFER_ACCOUNTS_ORDER)) { //红包消息
        //    newHongBaoNotification(nf);
        //}
    }

    /**打开通知栏消息*/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void newHongBaoNotification(Notification notification) {
    	//TransferAccounts.mWorking = true;
        //以下是精华，将微信的通知栏消息打开
        PendingIntent pendingIntent = notification.contentIntent;
        boolean lock = NotifyHelper.isLockScreen(getContext());

        if(!lock) {
            NotifyHelper.send(pendingIntent);
        } else {
            //NotifyHelper.showNotify(getContext(), String.valueOf(notification.tickerText), pendingIntent);
        }

        if(lock) {
           // NotifyHelper.playEffect(getContext(), getConfig());
        }
    }
    /*
    *
    */
   private void debug(AccessibilityEvent event){
     	if(Config.DEBUG){
       	final int eventType = event.getEventType();
       	if(event.getClassName()==null)return;
       	String sClassName=event.getClassName().toString();
   			Log.i("byc002", "mCurrentUI="+mCurrentUI);
   			if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
   				Log.i("byc002", "eventType=TYPE_WINDOW_STATE_CHANGED");
   			if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
   				Log.i("byc002", "eventType=TYPE_WINDOW_CONTENT_CHANGED");
   			Log.i("byc002", "Config.JobState="+Config.JobState);
   			
   			
   			if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
   				Log.i(TAG, "事件--------------------->TYPE_WINDOW_STATE_CHANGED");
   				Log.i(TAG, "窗体--------------------->"+sClassName);
   			}
   			else{
   				Log.i(TAG, "事件--------------------->TYPE_WINDOW_CONTENT_CHANGED");
   				Log.i(TAG, "ClassName--------------------->"+sClassName);
   				AccessibilityNodeInfo rootNode=event.getSource();
   				if(rootNode==null)return;
   				Log.i(TAG, "getSource--------------------->"+rootNode.getText());
   			}
   			Log.i(TAG, "包名--------------------->"+event.getPackageName());
   				
   			AccessibilityNodeInfo rootNode=event.getSource();
   			if(rootNode==null)return;
   			rootNode=AccessibilityHelper.getRootNode(rootNode);
   			AccessibilityHelper.recycle(rootNode);
   		}
   }
	/*
	 * (刷新处理流程)
	 * @see accessbility.AccessbilityJob#onWorking()
	 */
	@Override
	public void onWorking(){
		//Log.i(TAG2, "onWorking");
		//installApp.onWorking();
	}
}

