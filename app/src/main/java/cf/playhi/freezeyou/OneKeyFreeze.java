package cf.playhi.freezeyou;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import static cf.playhi.freezeyou.Support.buildAlertDialog;
import static cf.playhi.freezeyou.Support.isDeviceOwner;
import static cf.playhi.freezeyou.Support.oneKeyActionMRoot;
import static cf.playhi.freezeyou.Support.oneKeyActionRoot;
import static cf.playhi.freezeyou.Support.showToast;

public class OneKeyFreeze extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean auto = getIntent().getBooleanExtra("autoCheckAndLockScreen",true);
        Activity activity = this;
        String[] pkgNameList = getApplicationContext().getSharedPreferences(
                "AutoFreezeApplicationList", Context.MODE_PRIVATE).getString("pkgName","").split("\\|\\|");
        if (Build.VERSION.SDK_INT>=21 && isDeviceOwner(activity)){
            oneKeyActionMRoot(activity,activity,true,pkgNameList);
            if (auto){
                checkAndLockScreen(activity,true);
            }
        } else {
            oneKeyActionRoot(activity,activity,true,pkgNameList,true);
            if (auto)
                checkAndLockScreen(activity,false);
        }
    }

    private void checkAndLockScreen(final Context context, final boolean finish){
        switch (PreferenceManager.getDefaultSharedPreferences(context).getString("shortCutOneKeyFreezeAdditionalOptions","nothing")){
            case "nothing":
                doFinish(finish);
                break;
            case "askLockScreen":
                buildAlertDialog(context,R.mipmap.ic_launcher_new_round,R.string.askIfLockScreen,R.string.notice)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                doLockScreen(context);
                                doFinish(finish);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                doFinish(finish);
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                doFinish(finish);
                            }
                        })
                        .create().show();
                break;
            case "lockScreenImmediately":
                doLockScreen(context);
                doFinish(finish);
                break;
            default:
                doFinish(finish);
                break;
        }
    }

    private void doFinish(boolean finish){
        if (finish)
            finish();
    }

    private void doLockScreen(Context context){
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(this, DeviceAdminReceiver.class);
        if (devicePolicyManager!=null){
            if (devicePolicyManager.isAdminActive(componentName)){
                devicePolicyManager.lockNow();
            } else {
                showToast(context,R.string.devicePolicyManagerNotActivated);
            }
        } else {
            showToast(context,R.string.devicePolicyManagerNotFound);
        }
    }
}
