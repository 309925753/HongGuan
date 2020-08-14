package com.sk.weichat.pay;

public class EventReceiptSuccess {
    private String paymentName;// 付款方昵称

    public EventReceiptSuccess(String paymentName) {
        this.paymentName = paymentName;
    }

    public String getPaymentName() {
        return paymentName;
    }

    public void setPaymentName(String paymentName) {
        this.paymentName = paymentName;
    }
}
