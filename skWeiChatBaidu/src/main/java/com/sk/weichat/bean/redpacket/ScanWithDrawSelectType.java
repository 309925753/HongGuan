package com.sk.weichat.bean.redpacket;

public class ScanWithDrawSelectType {
    /**
     * id :
     * createTime :
     * userId :
     * type :
     * aliPayName :
     * aliPayAccount :
     * cardName :
     * bankCardNo :
     * bankName :
     * bankBranchName :
     * desc :
     */
    private String id;
    private long createTime;
    private String userId;
    private int type;
    private String aliPayName;
    private String aliPayAccount;
    private String cardName;
    private String bankCardNo;
    private String bankName;
    private String bankBranchName;
    private String desc;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getAliPayName() {
        return aliPayName;
    }

    public void setAliPayName(String aliPayName) {
        this.aliPayName = aliPayName;
    }

    public String getAliPayAccount() {
        return aliPayAccount;
    }

    public void setAliPayAccount(String aliPayAccount) {
        this.aliPayAccount = aliPayAccount;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getBankCardNo() {
        return bankCardNo;
    }

    public void setBankCardNo(String bankCardNo) {
        this.bankCardNo = bankCardNo;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankBranchName() {
        return bankBranchName;
    }

    public void setBankBranchName(String bankBranchName) {
        this.bankBranchName = bankBranchName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}