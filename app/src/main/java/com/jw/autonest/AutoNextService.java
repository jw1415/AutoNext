package com.jw.autonest;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.jw.autonest.util.AccessibilityHelper;
import com.jw.autonest.util.AnswerUtil;
import com.jw.autonest.util.SpfUtil;

import java.util.Random;

public class AutoNextService extends AccessibilityService {


    private static final String TAG = "autoService";
    private Handler mHandler = new Handler();
    private long nextTime;
    private String mAnswer;
    private boolean mIsRefresh;

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



        if(isMdPlayerPracticeActivity()){
            if(SpfUtil.getInstance(this).getData("auto")) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        boolean isSubmit = autoSubmit(nodeInfo);
                        if (isMdPlayerPracticeActivity()) {
                            if (!isSubmit) {
                                postTask(this,0);
                            }
                        }
                    }
                }, 1000);
            }
        }

        if(isVideoAdsViewActivity()){
            autoNext(nodeInfo);
        }

        if(isVideoAdsViewActivity()){
            autoNext(nodeInfo);
        }

        if(isExerciseActivity()){
            autoExercise.run();
        }

    }

    /**
     * 视频答题页
     * @return
     */
    private boolean isMdPlayerPracticeActivity(){
        return "com.bjta.media.view.mpexam.MdPlayerPracticeActivity".equals(activityName);
    }

    /**
     * 视频页
     * @return
     */
    private boolean isVideoAdsViewActivity(){
        return "com.bjta.media.view.VideoAdsViewActivity".equals(activityName);
    }

    /**
     * 答题页
     * @return
     */
    private boolean isExerciseActivity(){
        return "com.bjta.exam.plugin.view.dlg.ExerciseActivity".equals(activityName);
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
            if(isVideoAdsViewActivity() && SpfUtil.getInstance(getApplicationContext()).getData("auto2")) {
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


    private Runnable  autoExercise = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
            if(nodeInfo == null) {
                Log.w(TAG, "rootWindow is null");
                postTask(this,2000);
                return;
            }
            if(!isExerciseActivity()){
                return;
            }

            //先查看 答题正确或失败状态
            AccessibilityNodeInfo answerNode = AccessibilityHelper.findNodeInfosById(nodeInfo, "com.bjtongan.anjia365.llpx:id/tv_sdanswer");
            if(answerNode != null && answerNode.isVisibleToUser()){
                String ans = answerNode.getText().toString();
                if(ans.contains("正确")){
                    //回答正确，等待自动滑屏
                    Toast.makeText(AutoNextService.this,"right,wait for auto next",Toast.LENGTH_LONG).show();
                    postTask(this,3000);
                    return;
                }else{
                    AccessibilityNodeInfo pageNode = AccessibilityHelper.findNodeInfosById(nodeInfo, "com.bjtongan.anjia365.llpx:id/viewPager");
                    if(pageNode != null){
                        //错误状态，主动右滑
                        Toast.makeText(AutoNextService.this,"faild,wait for pluggin next",Toast.LENGTH_LONG).show();
//                        Rect rect = new Rect();
//                        pageNode.getBoundsInScreen(rect);
//                        GestureDescription.Builder builder = new GestureDescription.Builder();
//                        Path path = new Path();
////                        path.moveTo(rect.width()*6/6, rect.centerY());
//                        path.moveTo(rect.width()*5/6,rect.centerY());
//                        path.lineTo(rect.width()*4/6,rect.centerY());
//                        path.lineTo(rect.width()*3/6,rect.centerY());
//                        path.lineTo(rect.width()*2/6,rect.centerY());
//                        path.lineTo(rect.width()*1/6,rect.centerY());
//                        GestureDescription gestureDescription = builder
//                                .addStroke(new GestureDescription.StrokeDescription(path, 100, 500))
//                                .build();
//                        dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
//                            @Override
//                            public void onCompleted(GestureDescription gestureDescription) {
//                                super.onCompleted(gestureDescription);
//                            }
//
//                            @Override
//                            public void onCancelled(GestureDescription gestureDescription) {
//                                super.onCancelled(gestureDescription);
//                            }
//                        }, mHandler);
                        postTask(this,5000);
                        return;
                    }
                }
            }

            //查找题号
            try {
                AccessibilityNodeInfo totalNode = AccessibilityHelper.findNodeInfosById(nodeInfo, "com.bjtongan.anjia365.llpx:id/tv_questionTotal");
                int num = Integer.parseInt(totalNode.getText().toString().split("/")[0]);
                //查找答案
                String answer = AnswerUtil.getAnswer(num);
                //点击选项
                AccessibilityNodeInfo targetNode = AccessibilityHelper.findNodeInfosByText(nodeInfo, answer);
                clickNode(targetNode.getParent(),5,"will click : "+answer +" for "+num);
            } catch (Exception e) {
                Toast.makeText(AutoNextService.this,"error:"+e.getMessage(),Toast.LENGTH_LONG).show();
            }
            postTask(this,0);
        }
    };


    private void postTask(Runnable r,long delay){
        mHandler.removeCallbacks(r);
        if(delay > 0){
            mHandler.postDelayed(r,delay);
        }
        long time = nextTime - System.currentTimeMillis() + 1000;
        if(time <= 0){
            mHandler.postDelayed(r,2000);
        }else{
            mHandler.postDelayed(r,time);
        }
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
