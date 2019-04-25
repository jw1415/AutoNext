package com.jw.autonest;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.jw.autonest.util.AccessibilityHelper;
import com.jw.autonest.util.SpfUtil;

import java.util.Random;

public class AutoNextService extends AccessibilityService {


    private static final String TAG = "autoService";
    private Handler mHandler = new Handler();
    private long nextTime;
    private String mAnswer;
    private boolean mIsRefresh;
    private AccessibilityNodeInfo mPracticeNode;

    private String activityName;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.w(TAG, "auto service is enable");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        info.packageNames = new  String[]{"com.bjtongan.anjia365.llpx"};

        setServiceInfo(info);

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindow is null");
            return;
        }

        String aName = event.getClassName().toString();
        if(aName.endsWith("Activity")){
            activityName = aName;
        }

        if("com.bjta.media.view.VideoAdsViewActivity".equals(event.getClassName())){
            mPracticeNode = null;
        }

        if("com.bjta.media.view.mpexam.MdPlayerPracticeActivity".equals(event.getClassName())){
            if(SpfUtil.getInstance(this).getData("auto")) {
                mPracticeNode = nodeInfo;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        boolean isSubmit = autoSubmit(nodeInfo);
                        if (nodeInfo == mPracticeNode) {
                            if (!isSubmit) {
                                mHandler.removeCallbacks(this);
                                mHandler.postDelayed(this, nextTime - System.currentTimeMillis() + 1000);
                            }
                        }
                    }
                }, 1000);
            }
        }else{
            autoNext(nodeInfo);
        }


    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "auto service on interrupt");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.w(TAG, "auto service on Unbind");
        return super.onUnbind(intent);

    }

    private void autoNext(AccessibilityNodeInfo nodeInfo){
        nextRunnable.run();
    }

    private Runnable nextRunnable = new Runnable() {
        @Override
        public void run() {
            final AccessibilityNodeInfo nextNode = AccessibilityHelper.findNodeInfosById(getRootInActiveWindow(), "com.bjtongan.anjia365.llpx:id/next_btn");

            if(nextNode != null && nextNode.isVisibleToUser() && (nextTime < System.currentTimeMillis())){
                clickNode(nextNode,6,"next button");
            }
            if(SpfUtil.getInstance(getApplicationContext()).getData("auto2")) {
                mHandler.removeCallbacks(this);
                mHandler.postDelayed(this,3000);
            }
        }
    };

    private boolean autoSubmit(AccessibilityNodeInfo nodeInfo ){
        Log.w(TAG, "find submit node");
        AccessibilityNodeInfo submitNode = AccessibilityHelper.findNodeInfosById(nodeInfo, "com.bjtongan.anjia365.llpx:id/btn_next_que");
        if(submitNode != null && submitNode.isVisibleToUser()){
            if(submitNode.getText().toString().contains("提交")){
                clickNode(submitNode,2,"submit node");
                return true;
            }
        }
        Log.w(TAG, "find redo node");
        AccessibilityNodeInfo redoNode = AccessibilityHelper.findNodeInfosById(nodeInfo, "com.bjtongan.anjia365.llpx:id/btnRedo");
        if(redoNode != null && redoNode.isVisibleToUser() && mAnswer == null){
            AccessibilityNodeInfo answerNode = AccessibilityHelper.findNodeInfosById(nodeInfo, "com.bjtongan.anjia365.llpx:id/tv_sdanswer");
            if(answerNode != null){
                String text = answerNode.getText().toString();
                if(text.length() > 1){
                    mAnswer = text.substring(0,1);
                    Log.w(TAG, "answer is "+mAnswer+" will click it next");
                }
            }
            clickNode(redoNode,2,"redo node,will try right answer:"+mAnswer);
            return false;
        }

        if(mAnswer != null){
            Log.w(TAG, "find right answer node");
            AccessibilityNodeInfo answerNode = AccessibilityHelper.findNodeInfosByText(nodeInfo, mAnswer);
            mAnswer = null;
            if(answerNode != null && answerNode.isVisibleToUser()){
                clickNode(answerNode.getParent(),1," right answer node");
                return false;
            }
        }

        Log.w(TAG, "find try node");
        String tryAnswer = new Random().nextInt(2) == 1?"A":"B";
        AccessibilityNodeInfo answerNode = AccessibilityHelper.findNodeInfosByText(nodeInfo, tryAnswer);
        if(answerNode != null && answerNode.isVisibleToUser()){
            clickNode(answerNode.getParent(),8," try answer  node : "+tryAnswer);
            return false;
        }
        return false;
    }

    private void clickNode(final AccessibilityNodeInfo targetNode,int maxTime, String desc){
        Random r = new Random();
        int delay = (r.nextInt(maxTime)+3)*1000 + r.nextInt(10)*100 + r.nextInt(10)*10 + r.nextInt(10);
        Log.w(TAG, "find "+desc+" is visiable , will click "+delay+" ms later");
        Toast.makeText(this,"find "+desc+",will click "+delay+" ms later",Toast.LENGTH_LONG).show();
        mIsRefresh = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIsRefresh = false;
                if(targetNode != null && targetNode.isVisibleToUser()) {
                    AccessibilityHelper.performClick(targetNode);
                }
            }
        },delay);
        nextTime = System.currentTimeMillis()+delay+1000;
    }
}
