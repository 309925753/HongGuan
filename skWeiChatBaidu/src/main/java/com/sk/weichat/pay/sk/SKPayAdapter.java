package com.sk.weichat.pay.sk;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.sk.weichat.MyApplication;
import com.sk.weichat.R;
import com.sk.weichat.bean.CodePay;
import com.sk.weichat.bean.Friend;
import com.sk.weichat.bean.PayCertificate;
import com.sk.weichat.bean.Transfer;
import com.sk.weichat.bean.message.ChatMessage;
import com.sk.weichat.bean.message.XmppMessage;
import com.sk.weichat.bean.redpacket.Recharge;
import com.sk.weichat.bean.redpacket.Withdraw;
import com.sk.weichat.db.dao.FriendDao;
import com.sk.weichat.util.Base64;
import com.sk.weichat.util.TimeUtils;
import com.sk.weichat.util.secure.AES;
import com.sk.weichat.util.secure.chat.SecureChatUtil;

import java.util.ArrayList;
import java.util.List;


public class SKPayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> mChatMessageSource;

    public SKPayAdapter(List<ChatMessage> chatMessages) {
        this.mChatMessageSource = chatMessages;
        if (mChatMessageSource == null) {
            mChatMessageSource = new ArrayList<>();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int resource;
        if (viewType == SKPayType.TRANSFER_BACK) {
            resource = R.layout.item_sk_pay_transfer_back;
            return new TransferBackHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
        } else if (viewType == SKPayType.PAYMENT_SUCCESS) {
            resource = R.layout.item_sk_pay_payment;
            return new PaymentHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
        } else if (viewType == SKPayType.RECEIPT_SUCCESS) {
            resource = R.layout.item_sk_pay_receipt;
            return new ReceiptHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
        } else if (viewType == SKPayType.PAY_CERTIFICATE) {
            resource = R.layout.item_sk_pay_certificate;
            return new PayCertificateHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
        } else if (viewType == SKPayType.SCAN_RECHARGE_CALLBACK) {
            resource = R.layout.item_sk_pay_scan_recharge;
            return new ScanCallbackHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
        } else if (viewType == SKPayType.SCAN_WITHDRAW_CALLBACK) {
            resource = R.layout.item_sk_pay_scan_withdraw;
            return new ScanCallbackHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
        } else {
            resource = R.layout.item_sk_pay_unkonw;
            return new SystemViewHolder(LayoutInflater.from(parent.getContext()).inflate(resource, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = mChatMessageSource.get(position);

        if (!TextUtils.isEmpty(chatMessage.getContent()) || !chatMessage.isDecrypted()) {
            String key = SecureChatUtil.getSymmetricKey(chatMessage.getPacketId());
            try {
                String s = AES.decryptStringFromBase64(chatMessage.getContent(), Base64.decode(key));
                chatMessage.setContent(s);
                chatMessage.setDecrypted(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (holder instanceof TransferBackHolder) {
            Transfer transfer = JSON.parseObject(chatMessage.getContent(), Transfer.class);
            ((TransferBackHolder) holder).mNotifyTimeTv.setText(TimeUtils.f_long_2_str(chatMessage.getTimeSend() * 1000));
            ((TransferBackHolder) holder).mMoneyTv.setText("￥" + transfer.getMoney());
            Friend friend = FriendDao.getInstance().getFriend(transfer.getUserId(), transfer.getToUserId());
            if (friend != null) {
                ((TransferBackHolder) holder).mBackReasonTv.setText(MyApplication.getContext().getString(R.string.transfer_back_reason_out_time,
                        TextUtils.isEmpty(friend.getRemarkName()) ? friend.getNickName() : friend.getRemarkName()));
            }
            ((TransferBackHolder) holder).mBackTimeTv.setText(TimeUtils.f_long_2_str(transfer.getOutTime() * 1000));
            ((TransferBackHolder) holder).mTransferTimeTv.setText(TimeUtils.f_long_2_str(transfer.getCreateTime() * 1000));
        } else if (holder instanceof PaymentHolder) {
            CodePay codePay = JSON.parseObject(chatMessage.getContent(), CodePay.class);
            ((PaymentHolder) holder).mMoneyTv.setText("￥" + codePay.getMoney());
            ((PaymentHolder) holder).mReceiptTimeTv.setText(TimeUtils.f_long_2_str(codePay.getCreateTime() * 1000));
            if (codePay.getType() == 1) {// 付款码
                ((PaymentHolder) holder).mReceiptUserTv.setText(codePay.getToUserName());
            } else {// 二维码收款
                ((PaymentHolder) holder).mReceiptUserTv.setText(codePay.getUserName());
            }
        } else if (holder instanceof ReceiptHolder) {
            CodePay codePay = JSON.parseObject(chatMessage.getContent(), CodePay.class);
            ((ReceiptHolder) holder).mMoneyTv.setText("￥" + codePay.getMoney());
            ((ReceiptHolder) holder).mPaymentTimeTv.setText(TimeUtils.f_long_2_str(codePay.getCreateTime() * 1000));
            if (codePay.getType() == 1) {// 付款码
                ((ReceiptHolder) holder).mPaymentUserTv.setText(codePay.getUserName());
            } else {// 二维码收款
                ((ReceiptHolder) holder).mPaymentUserTv.setText(codePay.getToUserName());
            }
        } else if (holder instanceof PayCertificateHolder) {
            PayCertificate payCertificate = JSON.parseObject(chatMessage.getContent(), PayCertificate.class);
            ((PayCertificateHolder) holder).mMoneyTv.setText("￥" + payCertificate.getMoney());
            ((PayCertificateHolder) holder).mReceiptUserTv.setText(payCertificate.getName());
        } else if (holder instanceof ScanCallbackHolder) {
            if (chatMessage.getType() == XmppMessage.TYPE_SCAN_RECHARGE) {
                Recharge recharge = JSON.parseObject(chatMessage.getContent(), Recharge.class);
                ((ScanCallbackHolder) holder).mMoneyTv.setText("￥" + recharge.getMoney());
                ((ScanCallbackHolder) holder).mPayType.setText(recharge.getType());
                ((ScanCallbackHolder) holder).mPayState.setText(recharge.getStatus());
                ((ScanCallbackHolder) holder).mPayTimeTv.setText(TimeUtils.f_long_2_str(recharge.getCreateTime() * 1000));
            } else {
                Withdraw withdraw = JSON.parseObject(chatMessage.getContent(), Withdraw.class);
                ((ScanCallbackHolder) holder).mMoneyTv.setText("￥" + withdraw.getMoney());
                if (withdraw.getWithdrawAccount() != null) {
                    ((ScanCallbackHolder) holder).mPayType.setText(withdraw.getWithdrawAccount().getType());
                }
                ((ScanCallbackHolder) holder).mPayState.setText(withdraw.getStatus());
                ((ScanCallbackHolder) holder).mPayTimeTv.setText(TimeUtils.f_long_2_str(withdraw.getCreateTime() * 1000));
            }
        } else {
            ((SystemViewHolder) holder).mSystemTv.setText(chatMessage.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return mChatMessageSource.size();
    }

    @Override
    public int getItemViewType(int position) {
        int type = mChatMessageSource.get(position).getType();
        if (type == XmppMessage.TYPE_TRANSFER_BACK) {
            return SKPayType.TRANSFER_BACK;
        } else if (type == XmppMessage.TYPE_PAYMENT_OUT || type == XmppMessage.TYPE_RECEIPT_OUT) {
            return SKPayType.PAYMENT_SUCCESS;
        } else if (type == XmppMessage.TYPE_PAYMENT_GET || type == XmppMessage.TYPE_RECEIPT_GET) {
            return SKPayType.RECEIPT_SUCCESS;
        } else if (type == XmppMessage.TYPE_PAY_CERTIFICATE) {
            return SKPayType.PAY_CERTIFICATE;
        } else if (type == XmppMessage.TYPE_SCAN_RECHARGE) {
            return SKPayType.SCAN_RECHARGE_CALLBACK;
        } else if (type == XmppMessage.TYPE_SCAN_WITHDRAW) {
            return SKPayType.SCAN_WITHDRAW_CALLBACK;
        } else {
            return SKPayType.UN_KNOW;
        }
    }

    class TransferBackHolder extends RecyclerView.ViewHolder {
        TextView mNotifyTimeTv;
        TextView mMoneyTv;
        TextView mBackReasonTv;
        TextView mBackTimeTv;
        TextView mTransferTimeTv;

        public TransferBackHolder(View itemView) {
            super(itemView);
            mNotifyTimeTv = itemView.findViewById(R.id.sk_pay_transfer_notify_time_tv);
            mMoneyTv = itemView.findViewById(R.id.sk_pay_transfer_money_tv);
            mBackReasonTv = itemView.findViewById(R.id.sk_pay_transfer_reason);
            mBackTimeTv = itemView.findViewById(R.id.sk_pay_transfer_back_time_tv);
            mTransferTimeTv = itemView.findViewById(R.id.sk_pay_transfer_transfer_time);
        }
    }

    class PaymentHolder extends RecyclerView.ViewHolder {

        TextView mMoneyTv;
        TextView mReceiptUserTv;
        TextView mReceiptTimeTv;

        public PaymentHolder(View itemView) {
            super(itemView);
            mMoneyTv = itemView.findViewById(R.id.sk_pay_payment_money_tv);
            mReceiptUserTv = itemView.findViewById(R.id.sk_pay_payment_receipt_user_tv);
            mReceiptTimeTv = itemView.findViewById(R.id.sk_pay_payment_receipt_time_tv);
        }
    }

    class ReceiptHolder extends RecyclerView.ViewHolder {

        TextView mMoneyTv;
        TextView mPaymentUserTv;
        TextView mPaymentTimeTv;

        public ReceiptHolder(View itemView) {
            super(itemView);
            mMoneyTv = itemView.findViewById(R.id.sk_pay_receipt_money_tv);
            mPaymentUserTv = itemView.findViewById(R.id.sk_pay_receipt_payment_user_tv);
            mPaymentTimeTv = itemView.findViewById(R.id.sk_pay_receipt_payment_time_tv);
        }
    }

    class PayCertificateHolder extends RecyclerView.ViewHolder {

        TextView mMoneyTv;
        TextView mReceiptUserTv;

        public PayCertificateHolder(View itemView) {
            super(itemView);
            mMoneyTv = itemView.findViewById(R.id.sk_pay_payment_money_tv);
            mReceiptUserTv = itemView.findViewById(R.id.sk_pay_payment_receipt_user_tv);
        }
    }

    class ScanCallbackHolder extends RecyclerView.ViewHolder {
        TextView mMoneyTv;
        TextView mPayType;
        TextView mPayState;
        LinearLayout mFailReasonLl;
        TextView mFailReasonTv;
        TextView mPayTimeTv;

        public ScanCallbackHolder(View itemView) {
            super(itemView);
            mMoneyTv = itemView.findViewById(R.id.sk_scan_money_tv);
            mPayType = itemView.findViewById(R.id.sk_scan_type_tv);
            mPayState = itemView.findViewById(R.id.sk_scan_state_tv);
            mFailReasonLl = itemView.findViewById(R.id.sk_scan_fail_ll);
            mFailReasonTv = itemView.findViewById(R.id.sk_scan_fail_tv);
            mPayTimeTv = itemView.findViewById(R.id.sk_scan_time_tv);
        }
    }

    class SystemViewHolder extends RecyclerView.ViewHolder {

        TextView mSystemTv;

        public SystemViewHolder(View itemView) {
            super(itemView);
            mSystemTv = itemView.findViewById(R.id.chat_content_tv);
        }
    }

}
