package fm.jiecao.jcvideoplayer_lib;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class SavaVideoDialog extends Dialog implements View.OnClickListener {

    private TextView tv1;

    private OnSavaVideoDialogClickListener mOnSavaVideoDialogClickListener;

    public SavaVideoDialog(Context context, OnSavaVideoDialogClickListener mOnSavaVideoDialogClickListener) {
        super(context, R.style.BottomDialog);
        this.mOnSavaVideoDialogClickListener = mOnSavaVideoDialogClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_sava_video);
        setCanceledOnTouchOutside(true);
        initView();
    }

    private void initView() {
        tv1 = (TextView) findViewById(R.id.tv1);

        tv1.setOnClickListener(this);


        Window o = getWindow();
        WindowManager.LayoutParams lp = o.getAttributes();
        // x/y坐标
        // lp.x = 100;
        // lp.y = 100;
        lp.width = ScreenUtil.getScreenWidth(getContext());
        o.setAttributes(lp);
        this.getWindow().setGravity(Gravity.BOTTOM);
        this.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.tv1) {
            mOnSavaVideoDialogClickListener.tv1Click();

        }
    }


    public interface OnSavaVideoDialogClickListener {
        void tv1Click();
    }
}
