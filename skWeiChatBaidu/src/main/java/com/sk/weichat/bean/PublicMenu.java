package com.sk.weichat.bean;

import java.util.List;

/**
 * 公众号菜单实体
 */

public class PublicMenu {

    /**
     * id : 681385389583237120
     * menuList : [{"id":681391821095436288,"name":"江山如此多娇","parentId":681385389583237120,"url":"www.js.com","userId":10000104},{"id":681391914045407232,"name":"江山如此多娇2","parentId":681385389583237120,"url":"www.js2.com","userId":10000104}]
     * name : 泥人2003
     * parentId : 0
     * url : http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack
     * userId : 10000104
     */

    private long id;
    private String name;
    private int parentId;
    private String url;
    private int userId;
    private List<MenuListBean> menuList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<MenuListBean> getMenuList() {
        return menuList;
    }

    public void setMenuList(List<MenuListBean> menuList) {
        this.menuList = menuList;
    }

    public static class MenuListBean {
        /**
         * id : 681391821095436288
         * name : 江山如此多娇
         * parentId : 681385389583237120
         * url : www.js.com
         * userId : 10000104
         * menuId
         */

        private long id;
        private String name;
        private long parentId;
        private String url;
        private String menuId;
        private int userId;

        public String getMenuId() {
            return menuId;
        }

        public void setMenuId(String menuId) {
            this.menuId = menuId;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getParentId() {
            return parentId;
        }

        public void setParentId(long parentId) {
            this.parentId = parentId;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }
    }
}
