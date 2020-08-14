package com.sk.weichat.util.link;

import android.content.Context;
import android.content.Intent;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.sk.weichat.ui.tool.WebViewActivity;
import com.sk.weichat.util.HttpUtil;
import com.sk.weichat.util.LinkMovementClickMethod;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sk.weichat.ui.tool.WebViewActivity.EXTRA_URL;

public class HttpTextView extends TextView {
    // 现在创建 matcher 对象
    Matcher m;
    // 记录网址的list
    LinkedList<String> mStringList;
    // 记录该网址所在位置的list
    LinkedList<UrlInfo> mUrlInfos;
    int flag = Spanned.SPAN_POINT_MARK;
    /*
     * 正则文本
     * ((http|ftp|https)://)(([a-zA-Z0-9\._-]+\.[a-zA-Z]{2,6})|([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\&%_\./-~-]*)?|(([a-zA-Z0-9\._-]+\.[a-zA-Z]{2,6})|([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\&%_\./-~-]*)?
     * */
    private String pattern = HttpUtil.REGEX_URL;
    // 创建 Pattern 对象
    Pattern r = Pattern.compile(pattern);
    private boolean needToRegionUrl = true;//是否开启识别URL，默认开启

    public HttpTextView(Context context) {
        this(context, null);
    }

    public HttpTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HttpTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mStringList = new LinkedList<>();
        mUrlInfos = new LinkedList<>();
    }

    public void setUrlText(CharSequence text) {
        if (needToRegionUrl) {
            SpannableStringBuilderAllVer stringBuilderAllVer = recognUrl(text);
            super.setText(stringBuilderAllVer);
            this.setMovementMethod(LinkMovementClickMethod.getInstance());
        } else {
            super.setText(text);
        }
    }

    public boolean getIsNeedToRegionUrl() {
        return needToRegionUrl;
    }

    public void setOpenRegionUrl(boolean needToRegionUrl) {
        this.needToRegionUrl = needToRegionUrl;
    }

    private SpannableStringBuilderAllVer recognUrl(CharSequence text) {
        mStringList.clear();
        mUrlInfos.clear();

        CharSequence contextText;
        CharSequence clickText;
        text = text == null ? "" : text;
        //以下用于拼接本来存在的spanText
        SpannableStringBuilderAllVer span = new SpannableStringBuilderAllVer(text);
        ClickableSpan[] clickableSpans = span.getSpans(0, text.length(), ClickableSpan.class);
        if (clickableSpans.length > 0) {
            int start = 0;
            int end = 0;
            for (int i = 0; i < clickableSpans.length; i++) {
                start = span.getSpanStart(clickableSpans[0]);
                end = span.getSpanEnd(clickableSpans[i]);
            }
            //可点击文本后面的内容页
            contextText = text.subSequence(end, text.length());
            //可点击文本
            clickText = text.subSequence(start, end);
        } else {
            contextText = text;
            clickText = null;
        }
        m = r.matcher(contextText.toString());
        //匹配成功
        while (m.find()) {
            //得到网址数
            UrlInfo info = new UrlInfo();
            info.start = m.start();
            info.end = m.end();
            mStringList.add(m.group());
            mUrlInfos.add(info);
        }
        return jointText(clickText, contextText);
    }

    /**
     * 拼接文本
     */
    private SpannableStringBuilderAllVer jointText(CharSequence clickSpanText,
                                                   CharSequence contentText) {
        SpannableStringBuilderAllVer spanBuilder;
        if (clickSpanText != null) {
            spanBuilder = new SpannableStringBuilderAllVer(clickSpanText);
        } else {
            spanBuilder = new SpannableStringBuilderAllVer();
        }
        if (mStringList.size() > 0) {
            //只有一个网址
            if (mStringList.size() == 1) {
                String preStr = contentText.toString().substring(0, mUrlInfos.get(0).start);
                spanBuilder.append(preStr);
                String url = mStringList.get(0);
                spanBuilder.append(url, new URLClick(url), flag);
                String nextStr = contentText.toString().substring(mUrlInfos.get(0).end);
                spanBuilder.append(nextStr);
            } else {
                // 有多个网址
                for (int i = 0; i < mStringList.size(); i++) {
                    if (i == 0) {
                        //拼接第1个span的前面文本
                        String headStr =
                                contentText.toString().substring(0, mUrlInfos.get(0).start);
                        spanBuilder.append(headStr);
                    }
                    if (i == mStringList.size() - 1) {
                        //拼接最后一个span的后面的文本
                        spanBuilder.append(mStringList.get(i), new URLClick(mStringList.get(i)),
                                flag);
                        String footStr = contentText.toString().substring(mUrlInfos.get(i).end);
                        spanBuilder.append(footStr);
                    }
                    if (i != mStringList.size() - 1) {
                        //拼接两两span之间的文本
                        spanBuilder.append(mStringList.get(i), new URLClick(mStringList.get(i)), flag);
                        String betweenStr = contentText.toString()
                                .substring(mUrlInfos.get(i).end,
                                        mUrlInfos.get(i + 1).start);
                        spanBuilder.append(betweenStr);
                    }
                }
            }
        } else {
            spanBuilder.append(contentText);
        }

        return spanBuilder;
    }

    class UrlInfo {
        public int start;
        public int end;
    }

    class URLClick extends ClickableSpan {
        private String text;

        public URLClick(String text) {
            this.text = text;
        }

        @Override
        public void onClick(View view) {
            // Toast.makeText(widget.getContext(), text, Toast.LENGTH_SHORT).show();
            Context context = view.getContext();
            Intent intent = new Intent(context, WebViewActivity.class);
            if (text.startsWith("http") || text.startsWith("https") || text.startsWith("ftp")) {
                intent.putExtra(EXTRA_URL, text);
            } else {
                intent.putExtra(EXTRA_URL, "http://" + text);
            }
            context.startActivity(intent);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(0xff517fae);
            ds.setUnderlineText(false);
        }
    }
}
