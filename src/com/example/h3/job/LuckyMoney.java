package com.example.h3.job;


import com.example.h3.Config;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.view.accessibility.AccessibilityNodeInfo;
import accessibility.AccessibilityHelper;

/*
 * 红包类；标识红包尾性，红包处理方法。
 */
public class LuckyMoney {
	
	private static LuckyMoney current;
	private Context context;
	private static String TAG = "byc001";
	public static final int TYPE_LUCKYMONEY_ME_ROBBED=1;//自己领走的红包；
	public static final int TYPE_LUCKYMONEY_YOU_ROBBED=2;//别人领走的红包；
	public static final int TYPE_LUCKYMONEY_NO_ROBBED=0;//未领过的红包；
	public static final int TYPE_LUCKYMONEY_COMMON=1;//普通的红包；
	public static final int TYPE_LUCKYMONEY_PERSONALITY=2;//个性的红包；
	public static final int TYPE_LUCKYMONEY_THUNDER=11;//雷的红包；
	public static final int TYPE_LUCKYMONEY_WELFARE=12;//福利的红包；
	public static final int TYPE_LUCKYMONEY_PAY_FOR=13;//赔付的红包；
	/*
	 * 属性1：领取的红包情况：默认未领取，自己领取的，别人领取的；谁抢到了红包；
	 */
	public int WhoRobbed=TYPE_LUCKYMONEY_NO_ROBBED;//未领取
	/*
	 * 属性2：个性红包还是普通红包：默认普通红包；
	 */
	public int LuckyMoneyType=TYPE_LUCKYMONEY_COMMON;
	/*
	 * 属性3：赔付包，福利包还是雷包；默认雷包;
	 */
	public int LuckyMoneyDefined=TYPE_LUCKYMONEY_THUNDER;
	/*
	 * 标识要处理的红包：
	 */
	public AccessibilityNodeInfo LuckyMoneyNode;
	/*
	 * 已获取红包信息；
	 */
	public AccessibilityNodeInfo RobbedLuckyMoneyInfoNode;
	/*
	 * 构造方法
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
     * 获取最后一个未获取的普通红包
     */
    //获取最后一个红包：判断红包是否来了：
    public AccessibilityNodeInfo getLastLuckyMoneyNode(AccessibilityNodeInfo rootNode){
    	//return AccessibilityHelper.findNodeInfosByText(rootNode, "点击拆开", -1);
    	if(Config.wv<786)
    		return AccessibilityHelper.findNodeInfosByText(rootNode, "QQ红包", -1);
    	else{
    		AccessibilityNodeInfo nodeInfo=AccessibilityHelper.findNodeInfosByText(rootNode, "QQ红包", -1);
    		if(nodeInfo==null)return null;
    		nodeInfo=AccessibilityHelper.findNodeInfosByText(rootNode, "点击查看详情", -1);
    		if(nodeInfo==null)return null;
    		if(nodeInfo.getClassName().equals("android.widget.RelativeLayout")&&nodeInfo.isClickable()==true)
    			return nodeInfo;
    	}
    	return null;
    }
    //获取最后一个已拆开的普通红包：
    public AccessibilityNodeInfo getLastLuckyMoneyNoded(AccessibilityNodeInfo rootNode){
    	return AccessibilityHelper.findNodeInfosByText(rootNode, "已拆开", -1);
    }
    //获取最后一个个性红包：
    public AccessibilityNodeInfo getLastLuckyMoneyNode2(AccessibilityNodeInfo rootNode){
    	return AccessibilityHelper.findNodeInfosByText(rootNode, "QQ红包个性版", -1);
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
    //获取已领取红包信息
    public AccessibilityNodeInfo getLastReceivedLuckyMoneyInfoNode(AccessibilityNodeInfo nodeInfo){
    	//(nodeInfo,"com.tencent.mm:id/ho",-1)
        if (nodeInfo == null)return null;
        //return AccessibilityHelper.findNodeInfosByText(nodeInfo, "{\"icon\":\"qqwallet_custom_tips_icon.png\",\"alt\":\"\"}你领取了",-1);  
        return AccessibilityHelper.findNodeInfosByText(nodeInfo, "你领取了",-1);
    }
    //判断是否新红包：
    public boolean isNewLuckyMoney(AccessibilityNodeInfo LuckyMoneyNode,AccessibilityNodeInfo ReceivedLuckyMoneyInfoNode){
    	//无红包
    	if(LuckyMoneyNode==null)return false;
    	//没有领取信息
    	if(ReceivedLuckyMoneyInfoNode==null)return true;
    	//判断：
    	boolean bNewLuckyMoney=false;
    	Rect outBounds1=new Rect();
    	Rect outBounds2=new Rect();
    	LuckyMoneyNode.getBoundsInScreen(outBounds1);
    	ReceivedLuckyMoneyInfoNode.getBoundsInScreen(outBounds2);
    	if(outBounds2.top>outBounds1.top)bNewLuckyMoney=false;else bNewLuckyMoney=true;
    	return bNewLuckyMoney;
    }
}
