/**
 * Todo  同FilterPerviewDialog
 */
//package com.sk.weichat.video;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.cgfay.cameralibrary.adapter.PreviewFilterAdapter;
//import com.cgfay.cameralibrary.engine.camera.CameraParam;
//import com.cgfay.filterlibrary.glfilter.resource.FilterHelper;
//import com.cgfay.filterlibrary.glfilter.resource.bean.ResourceData;
//import com.cgfay.filterlibrary.glfilter.utils.BitmapUtils;
//import com.sk.weichat.R;
//import com.sk.weichat.ui.base.BaseRecViewHolder;
//import com.sk.weichat.util.ScreenUtil;
//
//import java.util.List;
//
//import static android.support.v7.widget.RecyclerView.HORIZONTAL;
//
///**
// * 滤镜选择器
// * create by xuan
// * time :2018-11-26 17:47:08
// */
//
//public class FilterSelectDialog extends Dialog implements View.OnClickListener {
//
//    private Context mContext;
//    private PreviewFilterAdapter.OnFilterChangeListener mListener;
//    private List<ResourceData> mdatas;
//
//    private RecyclerView mListView;
//    private CommAvatarAdapter mAdapter;
//    private int currt = 0;
//    private LinearLayout mLlBeauty;
//
//    private XSeekBar mSeekBar1;
//    private XSeekBar mSeekBar2;
//    private TextView tvFilter;
//    private TextView tvBeauty;
//    private CameraParam mCameraParam;
//    private OnDismissListener dismissListener;
//    private TextView tv_skin;
//    private TextView tv_whiten;
//
//    public FilterSelectDialog(Context context, PreviewFilterAdapter.OnFilterChangeListener listener) {
//        super(context, R.style.TrillDialog);
//        this.mContext = context;
//        initDatas(context);
//        mListener = listener;
//    }
//
//    private void initDatas(Context context) {
//
//        mdatas = FilterHelper.getFilterList();
//        if (mdatas == null || mdatas.size() == 0) {
//            FilterHelper.initAssetsFilter(context);
//            mdatas = FilterHelper.getFilterList();
//        }
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.dialog_filter_list);
//        setCanceledOnTouchOutside(true);
//
//        mCameraParam = CameraParam.getInstance();
//        initView();
//        initListview();
//    }
//
//    private void initListview() {
//        mLlBeauty = findViewById(R.id.ll_seekbar_beauty);
//        mListView = findViewById(R.id.rv_filter);
//        mListView.setLayoutManager(new LinearLayoutManager(mContext, HORIZONTAL, false));
//        mAdapter = new CommAvatarAdapter();
//        mListView.setAdapter(mAdapter);
//    }
//
//    private void initView() {
//        Window o = getWindow();
//        WindowManager.LayoutParams lp = o.getAttributes();
//        lp.width = ScreenUtil.getScreenWidth(getContext());
//        o.setAttributes(lp);
//        this.getWindow().setGravity(Gravity.BOTTOM);
//        this.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
//
//        mSeekBar1 = findViewById(R.id.bar_volume1);
//        mSeekBar2 = findViewById(R.id.bar_volume2);
//
//        tvFilter = findViewById(R.id.tv_filter);
//        tvFilter.setOnClickListener(this);
//        tvBeauty = findViewById(R.id.tv_beauty);
//        tv_whiten = findViewById(R.id.tv_whiten);
//        tv_skin = findViewById(R.id.tv_skin);
//        tv_whiten.setText(R.string.tv_whiten);
//        tv_skin.setText(R.string.tv_skin);
//        tvBeauty.setText(R.string.video_beauty);
//        tvFilter.setText(R.string.video_filter);
//        tvBeauty.setOnClickListener(this);
//
//        // 磨皮
//        mSeekBar1.addOnChangeListener(new XSeekBar.OnChangeListener() {
//            @Override
//            public void change(int curr) {
//                mCameraParam.beauty.beautyIntensity = curr / 100.0f;
//            }
//        });
//
//        // 美白
//        mSeekBar2.addOnChangeListener(new XSeekBar.OnChangeListener() {
//            @Override
//            public void change(int curr) {
//                mCameraParam.beauty.complexionIntensity = curr / 100.0f;
//            }
//        });
//    }
//
//
//    @Override
//    public void show() {
//        super.show();
//        Log.e("xuan", "show: " + this.mdatas.size());
//    }
//
//    @Override
//    public void dismiss() {
//        super.dismiss();
//        if (dismissListener != null) {
//            dismissListener.dismiss();
//        }
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.tv_beauty:
//                tvFilter.setTextColor(getColor(R.color.action_bar_tittle_color));
//                tvBeauty.setTextColor(getColor(R.color.white));
//                mListView.setVisibility(View.GONE);
//                mLlBeauty.setVisibility(View.VISIBLE);
//                break;
//            case R.id.tv_filter:
//                tvBeauty.setTextColor(getColor(R.color.action_bar_tittle_color));
//                tvFilter.setTextColor(getColor(R.color.white));
//                mListView.setVisibility(View.VISIBLE);
//                mLlBeauty.setVisibility(View.GONE);
//                break;
//        }
//    }
//
//    private int getColor(int rid) {
//        return getContext().getResources().getColor(rid);
//    }
//
//    public void setOnDismissListener(OnDismissListener listener) {
//        dismissListener = listener;
//    }
//
//    public interface OnDismissListener {
//        void dismiss();
//    }
//
//    class CommAvatarAdapter extends RecyclerView.Adapter<FilterInfoHolder> {
//
//        @NonNull
//        @Override
//        public FilterInfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            LayoutInflater inflater = LayoutInflater.from(mContext);
//            View view = inflater.inflate(R.layout.item_filter, null);
//            return new FilterInfoHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull FilterInfoHolder holder, int position) {
//            ResourceData data = mdatas.get(position);
//
//            if (data.thumbPath.startsWith("assets://")) {
//                holder.ivImage.setImageBitmap(BitmapUtils.getImageFromAssetsFile(mContext, data.thumbPath.substring("assets://".length())));
//            } else {
//                holder.ivImage.setImageBitmap(BitmapUtils.getBitmapFromFile(data.thumbPath));
//            }
//
//            String[] filters = getContext().getResources().getStringArray(R.array.filter_effect);
//            holder.tvName.setText(filters[position]);
//
//
//            //            switch (LocaleHelper.getLanguage(mContext)) {
//            //
//            //                case "en":
//            //                    holder.tvName.setText(filters[position]);
//            //
//            //                    break;
//            //                case "TW":
//            //                    holder.tvName.setText(position);
//            //
//            //                    break;
//            //                case "zh":
//            //                    holder.tvName.setText(data.name);
//            //                    break;
//            //
//            //            }
//
//            if (currt == position) {
//                holder.ivSelect.setVisibility(View.VISIBLE);
//            } else {
//                holder.ivSelect.setVisibility(View.GONE);
//            }
//
//        }
//
//        @Override
//        public int getItemCount() {
//            return mdatas.size();
//        }
//    }
//
//    public class FilterInfoHolder extends BaseRecViewHolder {
//        public ImageView ivImage;
//        public ImageView ivSelect;
//        public TextView tvName;
//        public FrameLayout mLlWrap;
//
//        public FilterInfoHolder(View rootView) {
//            super(rootView);
//
//            tvName = rootView.findViewById(R.id.tv_name);
//            ivImage = rootView.findViewById(R.id.iv_image);
//            ivSelect = rootView.findViewById(R.id.iv_select);
//            mLlWrap = rootView.findViewById(R.id.ll_wrap);
//            mLlWrap.setOnClickListener(v -> {
//                Log.e("xuan", "onReply: 选择了 " + getAdapterPosition());
//                mAdapter.notifyItemChanged(currt);
//                currt = getAdapterPosition();
//                mAdapter.notifyItemChanged(currt);
//                if (mListener != null) {
//                    mListener.onFilterChanged(mdatas.get(currt));
//
//                }
//            });
//        }
//    }
//}
