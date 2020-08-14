package com.sk.weichat.luo.camfilter;

import com.sk.weichat.R;


public class FilterTypeHelper {

    public static int FilterType2Color(GPUCamImgOperator.GPUImgFilterType filterType) {
        switch (filterType) {
            case NONE:
                return R.color.filter_color_grey_light;
            case COOL:
                return R.color.filter_color_blue_dark;
            case EMERALD:
            case EVERGREEN:
                return R.color.filter_color_blue_dark_dark;
            case NOSTALGIA:
                return R.color.filter_color_green_dark;
            case HEALTHY:
                return R.color.filter_color_red;
            default:
                return R.color.filter_color_grey_light;
        }
    }

    public static int FilterType2Thumb(GPUCamImgOperator.GPUImgFilterType filterType) {
        switch (filterType) {
            case NONE:
                return R.drawable.filter_thumb_original;
            case COOL:
                return R.drawable.filter_thumb_cool;
            case EMERALD:
                return R.drawable.filter_thumb_emerald;
            case EVERGREEN:
                return R.drawable.filter_thumb_evergreen;
            case HEALTHY:
                return R.drawable.filter_thumb_healthy;
            case NOSTALGIA:
                return R.drawable.filter_thumb_nostalgia;
            default:
                return R.drawable.filter_thumb_original;
        }
    }

    public static int FilterType2Name(GPUCamImgOperator.GPUImgFilterType filterType) {
        switch (filterType) {
            case NONE:
                return R.string.filter_none;
            case COOL:
                return R.string.filter_cool;
            case EMERALD:
                return R.string.filter_emerald;
            case EVERGREEN:
                return R.string.filter_evergreen;
            case HEALTHY:
                return R.string.filter_healthy;
            case NOSTALGIA:
                return R.string.filter_nostalgia;
            case CRAYON:
                return R.string.filter_crayon;
            default:
                return R.string.filter_none;
        }
    }


    //				"filter_warm",
//				"filter_antique",
//				"filter_sketch"
    public static String FilterType2FilterName(GPUCamImgOperator.GPUImgFilterType filterType) {
        switch (filterType) {
            case NONE:
                return "filter_none";
            case COOL:
                return "filter_cool";
            case EMERALD:
                return "filter_emerald";
            case EVERGREEN:
                return "filter_evergreen";
            case HEALTHY:
                return "filter_Healthy";
            case NOSTALGIA:
                return "filter_nostalgia";
            case CRAYON:
                return "filter_crayon";
            default:
                return "filter_none";
        }
    }
}
