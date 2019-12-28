/**
 * 
 */
package com.example.h3.job;

import com.baidu.android.common.logging.Log;
import com.byc.qq.R;
import util.BackgroundMusic;
import com.example.h3.Config;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import accessibility.AccessibilityHelper;
import com.example.h3.FloatingWindowPic;
import util.SpeechUtil;

/**
 * @author byc
 *
 */
public class LuckyMoneyReceiveJob {
	private static LuckyMoneyReceiveJob current;
	private Context context;
	private SpeechUtil speaker ;
	
	private static final String WECHAT_LUCKY_SEND="����һ�������������";//
	private static final String DIGITAL="0123456789";//
	private String[] mReceiveInfo={"","","","",""};//�����Ϣ��
	private int mIntInfo=0;//��Ϣ����
	private boolean bClicked=false;//�� ��������
	private boolean bNeedClick=false;//�Ƿ������
	private boolean bRecycled=false;//�Ƿ��˳�ѭ��
	public int mLuckyMoneyType=0;//�������Ѳ���������������װ���
	private BackgroundMusic mBackgroundMusic;
	private FloatingWindowPic fwp;
	
	private LuckyMoneyReceiveJob(Context context) {
		this.context = context;
		speaker=SpeechUtil.getSpeechUtil(context);
		mBackgroundMusic=BackgroundMusic.getInstance(context);
		fwp=FloatingWindowPic.getFloatingWindowPic(context,R.layout.float_click_delay_show);
		int w=Config.screenWidth-200;
		int h=Config.screenHeight-200;
		fwp.SetFloatViewPara(100, 200,w,h);
		//���չ㲥��Ϣ
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.ACTION_DISPLAY_SUCCESS);
        filter.addAction(Config.ACTION_DISPLAY_FAIL);
        context.registerReceiver(ClickLuckyMoneyReceiver, filter);
	}
    public static synchronized LuckyMoneyReceiveJob getLuckyMoneyReceiveJob(Context context) {
        if(current == null) {
            current = new LuckyMoneyReceiveJob(context);
        }
        return current;
        
    }
    /*�ж��Ƿ��Ǻ�����괰��*/
    public boolean isNoLuckyMoneyUI(AccessibilityNodeInfo rootNode){
    	AccessibilityNodeInfo aNode=AccessibilityHelper.findNodeInfosByText(rootNode,"����һ����������~",0);
    	if(aNode!=null&&aNode.getText()!=null){
    		if("����һ����������~".equals(aNode.getText().toString()))return true;else return false;
    	}
    	return false;
    }
    //��ʾ�ɹ���ʧ�ܽ��棺
    public void showAnimation(boolean bSuc,String money) {
    	String say="";
    	if(!bSuc){
    		mBackgroundMusic.playBackgroundMusic( "ao.mp3", false);
    		fwp.ShowFloatingWindow(); 
    		fwp.c=20;
    		fwp.mSendMsg=Config.ACTION_DISPLAY_FAIL;
    		fwp.mShowPicType=FloatingWindowPic.SHOW_PIC_FAIL;
    		fwp.StartSwitchPics();
    		if(Config.bReg)
    			say="�����ӳ٣�û������";//��Ȩ
    		else
    			say="û���������ð��ٶȽ������빺�����棬�����ٶȼӿ�100����";//δ��Ȩ
    	}else{
    		fwp.ShowFloatingWindow(); 
    		fwp.c=20;
    		fwp.mSendMsg=Config.ACTION_DISPLAY_SUCCESS;
    		fwp.mShowPicType=FloatingWindowPic.SHOW_PIC_SUC;
    		fwp.StartSwitchPics();
    		if(money==null)
    			say="��ϲ����������ˣ�";//δ��Ȩ
    		else
    			say="��ϲ���������"+money+"Ԫ��";
    	}
    	//��ʾ��
    	if(!say.equals("")){
    		Toast.makeText(context,say, Toast.LENGTH_LONG).show();
    		speaker.speak(say);	
    	}
    	//fwp.StopSwitchPics();
    	//fwp.RemoveFloatingWindowPic();
    	//mBackgroundMusic.stopBackgroundMusic();
    } 
    private BroadcastReceiver ClickLuckyMoneyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            //Log.d(TAG, "receive-->" + action);
            if(Config.ACTION_DISPLAY_SUCCESS.equals(action)) {
            	
            }
            if(Config.ACTION_DISPLAY_FAIL.equals(action)) {
            	mBackgroundMusic.stopBackgroundMusic();
            }
        }
    };
    
    /*��ʾ��ϸ��Ϣ*/
    public  boolean unpackLuckyMoneyShow(AccessibilityNodeInfo rootNode){
    	AccessibilityNodeInfo LuckyMoneyNode=AccessibilityHelper.findNodeInfosByText(rootNode,"�Ѵ������",0);
    	if(LuckyMoneyNode==null)return false;
    	UnpackLuckyMoneyShowDigital();//����������ʾ��
    	//��ȡ���ͺ���
    	bRecycled=false;
    	mIntInfo=1;
    	recycleGetJeAndSay(rootNode);
    	//��ܳɹ���������3.0Ԫ����ֵΪ��7�����׳ɹ���
    	String sMoneyValue=mReceiveInfo[1];
    	//String say="��ϲ�����������"+sMoneyValue+"��͸�ӳɹ���";
    	//Toast.makeText(context,say, Toast.LENGTH_LONG).show();
    	//speaker.speak(say);
    	showAnimation(true,sMoneyValue);
    	return false;
    }
    //�����ʾ���֣�
    private void UnpackLuckyMoneyShowDigital() {
    	
    	speaker.speak("����Ϊ��������");
    	float f=(float) (Math.random()*10000);
    	String s=String.valueOf(f);
    	Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
    private void recycleGetJeAndSay(AccessibilityNodeInfo info) {
    	if(bRecycled)return;
		if (info.getChildCount() == 0) {
			if(info.getText()==null)return;
			if(Config.DEBUG)Log.i(Config.TAG, mIntInfo+info.getText().toString());
			//ȡ��Ϣ
			if(mIntInfo==1)mReceiveInfo[2]=info.getText().toString();//��������ˣ�
			if(mIntInfo==5)mReceiveInfo[0]=info.getText().toString();//�����
			if(mIntInfo==2)mReceiveInfo[1]=info.getText().toString();//���
			mIntInfo=mIntInfo+1;
			
		} else {
			for (int i = 0; i < info.getChildCount(); i++) {
				if(info.getChild(i)!=null){
					recycleGetJeAndSay(info.getChild(i));
				}
			}
		}
    }
    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void recycle(AccessibilityNodeInfo info) {
    	if(bRecycled)return;
		if (info.getChildCount() == 0) {
			//ȡ��Ϣ
			if(!bNeedClick){
				//�����ˣ�
				if(mIntInfo==1){
					mReceiveInfo[mIntInfo]=info.getText().toString();
					if(!mReceiveInfo[mIntInfo].equals(WECHAT_LUCKY_SEND)){
						mLuckyMoneyType=Config.TYPE_LUCKYMONEY_NONE;//�����ˣ�
						bRecycled=true;
						return;
					}else{
						mLuckyMoneyType=Config.TYPE_LUCKYMONEY_THUNDER;
						bRecycled=true;
						return;
					}
				}
				/*
				mReceiveInfo[mIntInfo]=info.getText().toString();
				if(mIntInfo==2){
					mLuckyMoneyType=CheckLuckyMoneyType(mReceiveInfo[1],mReceiveInfo[2]);
					bRecycled=true;
					return;
				}
				*/
				mIntInfo=mIntInfo+1;
			}
			//Log.i(TAG, "child widget----------------------------" + info.getClassName());
			//Log.i(TAG, "showDialog:" + info.canOpenPopup());
			//Log.i(TAG, "Text��" + info.getText());
			//Log.i(TAG, "windowId:" + info.getWindowId());
			//Log.i(TAG, "ResouceId:" + info.getViewIdResourceName());
			//Log.i(TAG, "isClickable:" + info.isClickable());
			if(info.isClickable()){
				
				if(!bClicked)info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				bClicked=true;
			}
			
		} else {
			for (int i = 0; i < info.getChildCount(); i++) {
				if(info.getChild(i)!=null){
					recycle(info.getChild(i));
				}
			}
		}
	}
    public int CheckLuckyMoneyType(String LuckyMoneySend,String LuckyMoneySay){
    	if(LuckyMoneySend.equals(WECHAT_LUCKY_SEND)){
    		String LuckyMoneySayTail=LuckyMoneySay.substring(LuckyMoneySay.length()-1,LuckyMoneySay.length());
    		if(DIGITAL.indexOf(LuckyMoneySayTail)==-1)
    			return Config.TYPE_LUCKYMONEY_WELL;
    		else
    			return Config.TYPE_LUCKYMONEY_THUNDER;
    	}else{
    		return Config.TYPE_LUCKYMONEY_NONE;
    	}
    }
    public void OpenLuckyMoney(AccessibilityNodeInfo info){
    	bNeedClick=true;
    	bClicked=false;
    	bRecycled=false;
    	mIntInfo=0;
    	recycle(info);
    	return ;
    }
    public int IsLuckyMoneyReceive(AccessibilityNodeInfo info){
    	bNeedClick=false;
    	mIntInfo=0;
    	bRecycled=false;
    	recycle(info);
    	return mLuckyMoneyType;
    }
}
