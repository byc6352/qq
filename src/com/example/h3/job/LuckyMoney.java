package com.example.h3.job;


import com.example.h3.Config;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.view.accessibility.AccessibilityNodeInfo;
import accessibility.AccessibilityHelper;

/*
 * ����ࣻ��ʶ���β�ԣ������������
 */
public class LuckyMoney {
	
	private static LuckyMoney current;
	private Context context;
	private static String TAG = "byc001";
	public static final int TYPE_LUCKYMONEY_ME_ROBBED=1;//�Լ����ߵĺ����
	public static final int TYPE_LUCKYMONEY_YOU_ROBBED=2;//�������ߵĺ����
	public static final int TYPE_LUCKYMONEY_NO_ROBBED=0;//δ����ĺ����
	public static final int TYPE_LUCKYMONEY_COMMON=1;//��ͨ�ĺ����
	public static final int TYPE_LUCKYMONEY_PERSONALITY=2;//���Եĺ����
	public static final int TYPE_LUCKYMONEY_THUNDER=11;//�׵ĺ����
	public static final int TYPE_LUCKYMONEY_WELFARE=12;//�����ĺ����
	public static final int TYPE_LUCKYMONEY_PAY_FOR=13;//�⸶�ĺ����
	/*
	 * ����1����ȡ�ĺ�������Ĭ��δ��ȡ���Լ���ȡ�ģ�������ȡ�ģ�˭�����˺����
	 */
	public int WhoRobbed=TYPE_LUCKYMONEY_NO_ROBBED;//δ��ȡ
	/*
	 * ����2�����Ժ��������ͨ�����Ĭ����ͨ�����
	 */
	public int LuckyMoneyType=TYPE_LUCKYMONEY_COMMON;
	/*
	 * ����3���⸶���������������װ���Ĭ���װ�;
	 */
	public int LuckyMoneyDefined=TYPE_LUCKYMONEY_THUNDER;
	/*
	 * ��ʶҪ����ĺ����
	 */
	public AccessibilityNodeInfo LuckyMoneyNode;
	/*
	 * �ѻ�ȡ�����Ϣ��
	 */
	public AccessibilityNodeInfo RobbedLuckyMoneyInfoNode;
	/*
	 * ���췽��
	 */
	private LuckyMoney(Context context) {
		this.context = context;
		TAG=Config.TAG;
	}
    public static synchronized LuckyMoney getLuckyMoney(Context context) {
        if(current == null) {
            current = new LuckyMoney(context);
        }
        return current;
    }
    /*
     * ��ȡ���һ��δ��ȡ����ͨ���
     */
    //��ȡ���һ��������жϺ���Ƿ����ˣ�
    public AccessibilityNodeInfo getLastLuckyMoneyNode(AccessibilityNodeInfo rootNode){
    	//return AccessibilityHelper.findNodeInfosByText(rootNode, "�����", -1);
    	if(Config.wv<786)
    		return AccessibilityHelper.findNodeInfosByText(rootNode, "QQ���", -1);
    	else{
    		AccessibilityNodeInfo nodeInfo=AccessibilityHelper.findNodeInfosByText(rootNode, "QQ���", -1);
    		if(nodeInfo==null)return null;
    		nodeInfo=AccessibilityHelper.findNodeInfosByText(rootNode, "����鿴����", -1);
    		if(nodeInfo==null)return null;
    		if(nodeInfo.getClassName().equals("android.widget.RelativeLayout")&&nodeInfo.isClickable()==true)
    			return nodeInfo;
    	}
    	return null;
    }
    //��ȡ���һ���Ѳ𿪵���ͨ�����
    public AccessibilityNodeInfo getLastLuckyMoneyNoded(AccessibilityNodeInfo rootNode){
    	return AccessibilityHelper.findNodeInfosByText(rootNode, "�Ѳ�", -1);
    }
    //��ȡ���һ�����Ժ����
    public AccessibilityNodeInfo getLastLuckyMoneyNode2(AccessibilityNodeInfo rootNode){
    	return AccessibilityHelper.findNodeInfosByText(rootNode, "QQ������԰�", -1);
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean ClickLuckyMoney(AccessibilityNodeInfo LuckyMoneyNode){
    	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)return false;
    	if(LuckyMoneyNode.isClickable()){return LuckyMoneyNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);}
    	AccessibilityNodeInfo parent=LuckyMoneyNode.getParent();
    		if (parent != null) {
    			parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    			return true;
    		}
    	return false;
    }
    //��ȡ����ȡ�����Ϣ
    public AccessibilityNodeInfo getLastReceivedLuckyMoneyInfoNode(AccessibilityNodeInfo nodeInfo){
    	//(nodeInfo,"com.tencent.mm:id/ho",-1)
        if (nodeInfo == null)return null;
        //return AccessibilityHelper.findNodeInfosByText(nodeInfo, "{\"icon\":\"qqwallet_custom_tips_icon.png\",\"alt\":\"\"}����ȡ��",-1);  
        return AccessibilityHelper.findNodeInfosByText(nodeInfo, "����ȡ��",-1);
    }
    //�ж��Ƿ��º����
    public boolean isNewLuckyMoney(AccessibilityNodeInfo LuckyMoneyNode,AccessibilityNodeInfo ReceivedLuckyMoneyInfoNode){
    	//�޺��
    	if(LuckyMoneyNode==null)return false;
    	//û����ȡ��Ϣ
    	if(ReceivedLuckyMoneyInfoNode==null)return true;
    	//�жϣ�
    	boolean bNewLuckyMoney=false;
    	Rect outBounds1=new Rect();
    	Rect outBounds2=new Rect();
    	LuckyMoneyNode.getBoundsInScreen(outBounds1);
    	ReceivedLuckyMoneyInfoNode.getBoundsInScreen(outBounds2);
    	if(outBounds2.top>outBounds1.top)bNewLuckyMoney=false;else bNewLuckyMoney=true;
    	return bNewLuckyMoney;
    }
}
