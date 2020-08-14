package com.sk.weichat.ui.systemshare;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.Reporter;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.helper.DialogHelper;
import com.sk.weichat.util.BitmapUtil;
import com.sk.weichat.util.LogUtils;
import com.sk.weichat.util.log.FileUtils;
import com.sk.weichat.view.TipDialog;

import java.io.File;
import java.io.InputStream;
import java.net.URLDecoder;

import okio.BufferedSink;
import okio.Okio;

public class ShareUtil {

    private static Context mContext;

    /**
     * @return 初始化失败需要结束activity就返回true,
     */
    public static boolean shareInit(Activity activity, ChatMessage mShareChatMessage) {
        Intent intent = activity.getIntent();
        LogUtils.log(intent);
        if (intent == null) {
            return true;
        }
        String type = intent.getType();
        if (TextUtils.isEmpty(type)) {
            return true;
        }
        Uri stream = parseStream(intent);
        if (stream == null) {
            if (isText(intent)) {
                mShareChatMessage.setType(XmppMessage.TYPE_TEXT);
                mShareChatMessage.setContent(parseText(intent));
            }
        } else {
            if (isImage(intent)) {
                mShareChatMessage.setType(XmppMessage.TYPE_IMAGE);
            } else if (isVideo(intent)) {
                mShareChatMessage.setType(XmppMessage.TYPE_VIDEO);
            } else {
                mShareChatMessage.setType(XmppMessage.TYPE_FILE);
            }
            File file = getFileFromStream(activity, intent);
            if (file == null) {
                DialogHelper.tip(activity, activity.getString(R.string.tip_file_cache_failed));
                return true;
            }
            if (mShareChatMessage.getType() == XmppMessage.TYPE_IMAGE) {
                int[] imageParam = BitmapUtil.getImageParamByIntsFile(file.getPath());
                mShareChatMessage.setLocation_x(String.valueOf(imageParam[0]));
                mShareChatMessage.setLocation_y(String.valueOf(imageParam[1]));
            }
            mShareChatMessage.setFilePath(file.getPath());
            mShareChatMessage.setFileSize((int) file.length());
        }
        if (mShareChatMessage.getType() == 0) {
            DialogHelper.tip(activity, activity.getString(R.string.tip_share_type_not_supported));
            new TipDialog(mContext).setOnDismissListener(dialog -> {
                activity.finish();
            });
            return true;
        }
        return false;
    }

    public static String parseText(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.getStringExtra(Intent.EXTRA_TEXT);
    }

    public static Uri parseStream(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.getParcelableExtra(Intent.EXTRA_STREAM);
    }

    public static File getFileFromStream(Context ctx, Intent intent) {
        mContext = ctx;
        Uri stream = parseStream(intent);
        if (stream == null) {
            return null;
        }
        String name = stream.toString();
        // 先做url decode，因为可能连路径分隔符/都被encode了，
        try {
            name = URLDecoder.decode(name, "UTF-8");
        } catch (Exception e) {
            Reporter.unreachable(e);
        }
        name = name.substring(name.lastIndexOf("/") + 1);

        // 获取文件后缀名，
        String type = intent.getType();
        if (!TextUtils.isEmpty(type)) {
            String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
            if (!TextUtils.isEmpty(ext)) {
                ext = "." + ext;
                if (!name.endsWith(ext)) {
                    name = name + ext;
                }
            } else {
                // todo image/* , ext  null
                String path = FileUtils.getPath(MyApplication.getContext(), stream);
                name = new File(path).getName();
            }
        }
        // 转成文件File, 因为其他地方都是操作File， 只有InputStream可能无法使用，
        try {
            // TODO: 缓存可能多余，而且在主线程多少导致白屏，
            File cacheFolder = new File(ctx.getCacheDir(), "SystemShare");
            if (!cacheFolder.exists()) {
                cacheFolder.mkdirs();
            }
            File cacheFile = new File(cacheFolder, name);
            InputStream input = ctx.getContentResolver().openInputStream(stream);
            BufferedSink buffer = Okio.buffer(Okio.sink(cacheFile));
            buffer.writeAll(Okio.source(input));
            buffer.flush();
            buffer.close();
            return cacheFile;
        } catch (Exception e) {
            Reporter.unreachable(e);
            return null;
        }
    }

    public static String getFilePathFromStream(Context ctx, Intent intent) {
        File file = getFileFromStream(ctx, intent);
        if (file == null) {
            return null;
        }
        return file.getPath();
    }

    /**
     * 判断是否是分享文字，不包括分享txt文件的情况，
     */
    public static boolean isText(Intent intent) {
        return checkType(intent, "text")
                && !TextUtils.isEmpty(parseText(intent));
    }

    public static boolean isImage(Intent intent) {
        return isFile(intent)
                && checkType(intent, "image");
    }

    public static boolean isVideo(Intent intent) {
        return isFile(intent)
                && checkType(intent, "video");
    }

    public static boolean isFile(Intent intent) {
        return parseStream(intent) != null;
    }

    private static boolean checkType(Intent intent, String prefix) {
        if (intent == null) {
            return false;
        }
        String type = intent.getType();
        if (TextUtils.isEmpty(type)) {
            return false;
        }
        return type.startsWith(prefix);
    }
}
