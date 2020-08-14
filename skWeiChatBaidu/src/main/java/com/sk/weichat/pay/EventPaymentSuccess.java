package com.sk.weichat.pay;

public class EventPaymentSuccess {
    private String receiptName;// 收款方昵称

    public EventPaymentSuccess(String receiptName) {
        this.receiptName = receiptName;
    }

    public String getReceiptName() {
        return receiptName;
    }

    public void setReceiptName(String receiptName) {
        this.receiptName = receiptName;
    }
}
