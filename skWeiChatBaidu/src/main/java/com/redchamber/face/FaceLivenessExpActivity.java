package com.redchamber.face;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.baidu.idl.face.platform.FaceStatusEnum;
import com.baidu.idl.face.platform.ui.FaceLivenessActivity;
import com.redchamber.lib.utils.ToastUtils;

import java.util.HashMap;

public class FaceLivenessExpActivity extends FaceLivenessActivity {

    private AlertDialog mDefaultDialog;
    private HashMap<String, String> base64ImageMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onLivenessCompletion(FaceStatusEnum status, String message, HashMap<String, String> base64ImageMap) {
        super.onLivenessCompletion(status, message, base64ImageMap);
        if (status == FaceStatusEnum.OK && mIsCompletion) {
//            showMessageDialog("人脸识别", "识别成功");
//            this.base64ImageMap = base64ImageMap;
            Intent intent = new Intent();
            if (base64ImageMap != null) {
                String image = base64ImageMap.get("bestImage0");
                intent.putExtra("image", image);
            }
            setResult(RESULT_OK, intent);
            finish();
        } else if (status == FaceStatusEnum.Error_DetectTimeout ||
                status == FaceStatusEnum.Error_LivenessTimeout ||
                status == FaceStatusEnum.Error_Timeout) {
//            showMessageDialog("人脸识别", "采集超时");
            ToastUtils.showToast("采集超时");
            finish();
        }
    }


    private void showMessageDialog(String title, String message) {
        if (mDefaultDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(title).
                    setMessage(message).
                    setNegativeButton("确认",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mDefaultDialog.dismiss();
                                    Intent intent = new Intent();
                                    if (base64ImageMap != null) {
                                        String image = base64ImageMap.get("bestImage0");
                                        intent.putExtra("image", image);
                                    }
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            });
            mDefaultDialog = builder.create();
            mDefaultDialog.setCancelable(true);
        }
        mDefaultDialog.dismiss();
        mDefaultDialog.show();
    }

    @Override
    public void finish() {
        super.finish();
    }

}