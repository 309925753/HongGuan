package com.redchamber.bean;

import java.io.Serializable;

public class AccountIndexBean implements Serializable {
//	"maxTimes": 3,
//		"auditGold": 5000,
//		"balance": 8,
//		"minGold": 500,
//		"goldScale": 10
    public int balance;
    public int goldScale;//红豆和现金比率
    public int maxTimes;//每天最大提现次数
    public int auditGold;//需要审核
    public int minGold;//每次最少提现
    public int leftTimes;//剩余提现次数

}
