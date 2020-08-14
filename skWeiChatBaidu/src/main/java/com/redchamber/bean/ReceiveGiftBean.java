package com.redchamber.bean;

/**
 * 收到礼物
 */
public class ReceiveGiftBean {

    /**
     * gift : {"giftId":"5eb4f6fc2d617a5b2d216104","price":20,"name":"测试02","photo":"www.456.png"}
     * fromUserName : 无心法师
     * gold : 20
     * fromUserId : 10000041
     * giftCount : 1
     */

    private GiftBean gift;
    private String fromUserName;
    private int gold;
    private int fromUserId;
    private int giftCount;

    public GiftBean getGift() {
        return gift;
    }

    public void setGift(GiftBean gift) {
        this.gift = gift;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(int fromUserId) {
        this.fromUserId = fromUserId;
    }

    public int getGiftCount() {
        return giftCount;
    }

    public void setGiftCount(int giftCount) {
        this.giftCount = giftCount;
    }

    public static class GiftBean {
        /**
         * giftId : 5eb4f6fc2d617a5b2d216104
         * price : 20
         * name : 测试02
         * photo : www.456.png
         */

        private String giftId;
        private int price;
        private String name;
        private String photo;

        public String getGiftId() {
            return giftId;
        }

        public void setGiftId(String giftId) {
            this.giftId = giftId;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhoto() {
            return photo;
        }

        public void setPhoto(String photo) {
            this.photo = photo;
        }
    }
}
