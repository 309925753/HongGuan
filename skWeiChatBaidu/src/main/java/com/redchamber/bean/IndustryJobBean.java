package com.redchamber.bean;

import java.util.List;

public class IndustryJobBean {

    public String profession;
    public List<Job> items;
    public boolean isSelect;

    public static class Job {

        public String name;

        public Job(String name) {
            this.name = name;
        }
    }

}
