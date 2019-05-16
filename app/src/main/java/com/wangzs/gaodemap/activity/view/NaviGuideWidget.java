package com.wangzs.gaodemap.activity.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;


import java.util.List;

/**
 * Created by hongming.wang on 2017/6/22.
 */

public class NaviGuideWidget extends ExpandableListView {

    public NaviGuideWidget(Context context) {
        super(context);
    }

    public NaviGuideWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NaviGuideWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //设置参数
    private void setParams() {
        //隐藏分割线
        setDivider(null);
        //去掉默认的箭头
        setGroupIndicator(null);
    }

    public void setGuideData(String start, String end, List<LBSGuideGroup> dataList) {
        setParams();
        if (null != dataList && dataList.size() > 0) {
            int size = dataList.size();
            LBSGuideGroup header = new LBSGuideGroup();
            header.setGroupIconType(-1);
            header.setGroupName(start);
            dataList.add(0, header);

            LBSGuideGroup footer = new LBSGuideGroup();
            footer.setGroupIconType(-2);
            footer.setGroupName(end);
            dataList.add(dataList.size(), footer);

            NaviGuideAdapter adapter = new NaviGuideAdapter(getContext(), dataList);
            setAdapter(adapter);
        } else {
            return;
        }
    }

}
