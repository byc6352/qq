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
	private String[] mReceiveInfo={"","",""};//�����Ϣ��
	private int mIntInfo=0;//��Ϣ����
	private boolean bReg=false;//�Ƿ�ע�᣻
	private boolean bRecycled=false;//�Ƿ��˳�ѭ��
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
    /*�Ƿ�����ϸ��Ϣ����*/
    public boolean isDetailUI(AccessibilityNodeInfo rootNode){
    	AccessibilityNodeInfo aNode=AccessibilityHelper.findNodeInfosByText(rootNode,"����",0);
    	if(aNode==null)return false;
    	aNode=AccessibilityHelper.findNodeInfosByText(rootNode,"QQ���",0);
    	if(aNode==null)return false;
    	aNode=AccessibilityHelper.findNodeInfosByText(rootNode,"�����¼",0);
    	if(aNode==null)return false;
    	return true;
    }
    /*��ʾ��ϸ��Ϣ*/
    public  boolean unpackLuckyMoneyShow(AccessibilityNodeInfo rootNode){
    	AccessibilityNodeInfo LuckyMoneyNode=AccessibilityHelper.findNodeInfosByText(rootNode,"�յ��ĺ���Ѵ������    ���ʹ��",0);
    	if(LuckyMoneyNode!=null&&LuckyMoneyNode.getText()!=null){
    		if("�յ��ĺ���Ѵ������    ���ʹ��".equals(LuckyMoneyNode.getText().toString())){
    			UnpackLuckyMoneyShowDigital();//����������ʾ��
    			//��ȡ���ͺ���
    			bRecycled=false;
    			mIntInfo=1;
    			recycleGetJeAndSay(rootNode);
    	    	//��ܳɹ���������3.0Ԫ����ֵΪ��7�����׳ɹ���
    	    	String sMoneyValue=mReceiveInfo[1];
    	    	String say="��ϲ�����������"+sMoneyValue+"��͸�ӳɹ���";
    	    	Toast.makeText(context,say, Toast.LENGTH_LONG).show();
    	    	speaker.speak(say);
    		}
    	}
    	return false;
    }
    //�����ʾ���֣�
    private void UnpackLuckyMoneyShowDigital() {
    	
    	//speeker.speak("����Ϊ��������");
    	float f=(float) (Math.random()*10000);
    	String s=String.valueOf(f);
    	Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
    private void recycleGetJeAndSay(AccessibilityNodeInfo info) {
      	if(bRecycled)return;
  		if (info.getChildCount() == 0) {
  			//ȡ��Ϣ
  			if(mIntInfo==4){//�����
  				if(info.getText()!=null)
  					mReceiveInfo[0]=info.getText().toString();	
  				else mReceiveInfo[0]="10/1";	
  			}
  			if(mIntInfo==9){//��
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
			//ȡ��Ϣ
			mReceiveInfo[mIntInfo]=info.getText().toString();

			//Log.i(TAG, "child widget----------------------------" + info.getClassName());
			//Log.i(TAG, "Text��" + info.getText());
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
    	//��ܳɹ���������3.0Ԫ����ֵΪ��7�����׳ɹ���
    	String sMoneyValue=mReceiveInfo[2];
    	//1�����׳ɹ��жϣ�
    	String say="��ϲ���������"+sMoneyValue+"Ԫ��͸�ӳɹ���";
    	Toast.makeText(context, say, Toast.LENGTH_LONG).show();
    	speaker.speak(say);
    }

}
