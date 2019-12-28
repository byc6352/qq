/**
 * 
 */
package com.example.h3.job;

import com.example.h3.Config;
import com.example.h3.MainActivity;

import android.content.Context;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import accessibility.AccessibilityHelper;
import util.SpeechUtil;

/**
 * @author byc
 *
 */
public class LuckyMoneyDetailJob {
	private static LuckyMoneyDetailJob current;
	private Context context;
	private String[] mReceiveInfo={"","",""};//拆包信息；
	private int mIntInfo=0;//信息数；
	private boolean bReg=false;//是否注册；
	private boolean bRecycled=false;//是否退出循环
	private SpeechUtil speaker ;
	
    public static synchronized LuckyMoneyDetailJob getLuckyMoneyDetailJob(Context context) {
    	
        if(current == null) {
            current = new LuckyMoneyDetailJob(context);
        }
        return current;
    }
    private LuckyMoneyDetailJob(Context context){
    	this.context = context;
    	bReg=Config.getConfig(context).getREG();
    	speaker=SpeechUtil.getSpeechUtil(context);
    }
    /*是否是详细信息界面*/
    public boolean isDetailUI(AccessibilityNodeInfo rootNode){
    	AccessibilityNodeInfo aNode=AccessibilityHelper.findNodeInfosByText(rootNode,"返回",0);
    	if(aNode==null)return false;
    	aNode=AccessibilityHelper.findNodeInfosByText(rootNode,"QQ红包",0);
    	if(aNode==null)return false;
    	aNode=AccessibilityHelper.findNodeInfosByText(rootNode,"红包记录",0);
    	if(aNode==null)return false;
    	return true;
    }
    /*显示详细信息*/
    public  boolean unpackLuckyMoneyShow(AccessibilityNodeInfo rootNode){
    	AccessibilityNodeInfo LuckyMoneyNode=AccessibilityHelper.findNodeInfosByText(rootNode,"收到的红包已存入余额    余额使用",0);
    	if(LuckyMoneyNode!=null&&LuckyMoneyNode.getText()!=null){
    		if("收到的红包已存入余额    余额使用".equals(LuckyMoneyNode.getText().toString())){
    			UnpackLuckyMoneyShowDigital();//抢包数字显示；
    			//获取金额和红包语：
    			bRecycled=false;
    			mIntInfo=1;
    			recycleGetJeAndSay(rootNode);
    	    	//躲避成功：抢到金额：3.0元；雷值为：7；避雷成功！
    	    	String sMoneyValue=mReceiveInfo[1];
    	    	String say="恭喜！抢到红包："+sMoneyValue+"。透视成功！";
    	    	Toast.makeText(context,say, Toast.LENGTH_LONG).show();
    	    	speaker.speak(say);
    		}
    	}
    	return false;
    }
    //拆包显示数字：
    private void UnpackLuckyMoneyShowDigital() {
    	
    	//speeker.speak("正在为您分析：");
    	float f=(float) (Math.random()*10000);
    	String s=String.valueOf(f);
    	Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
    private void recycleGetJeAndSay(AccessibilityNodeInfo info) {
      	if(bRecycled)return;
  		if (info.getChildCount() == 0) {
  			//取信息
  			if(mIntInfo==4){//红包语
  				if(info.getText()!=null)
  					mReceiveInfo[0]=info.getText().toString();	
  				else mReceiveInfo[0]="10/1";	
  			}
  			if(mIntInfo==9){//金额：
  				if(info.getText()!=null)
  					mReceiveInfo[1]=info.getText().toString();	
  				else mReceiveInfo[1]="0.01";
  				bRecycled=true;
  			}
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
			//取信息
			mReceiveInfo[mIntInfo]=info.getText().toString();

			//Log.i(TAG, "child widget----------------------------" + info.getClassName());
			//Log.i(TAG, "Text：" + info.getText());
			//Log.i(TAG, "windowId:" + info.getWindowId());
			if(mIntInfo==2){bRecycled=true;return;}
			mIntInfo=mIntInfo+1;
		} else {
			for (int i = 0; i < info.getChildCount(); i++) {
				if(info.getChild(i)!=null){
					recycle(info.getChild(i));
				}
			}
		}
	}
    public void LuckyMoneyDetailShow(AccessibilityNodeInfo info){
    	mIntInfo=0;
    	bRecycled=false;
    	recycle(info);
    	//躲避成功：抢到金额：3.0元；雷值为：7；避雷成功！
    	String sMoneyValue=mReceiveInfo[2];
    	//1。避雷成功判断：
    	String say="恭喜！抢到红包"+sMoneyValue+"元。透视成功！";
    	Toast.makeText(context, say, Toast.LENGTH_LONG).show();
    	speaker.speak(say);
    }

}
