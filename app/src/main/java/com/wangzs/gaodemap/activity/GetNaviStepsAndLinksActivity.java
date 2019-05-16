package com.wangzs.gaodemap.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapNaviGuide;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.wangzs.gaodemap.R;
import com.wangzs.gaodemap.activity.view.LBSGuideGroup;
import com.wangzs.gaodemap.activity.view.NaviGuideWidget;

import java.util.ArrayList;
import java.util.List;


public class GetNaviStepsAndLinksActivity extends BaseActivity {


    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    private NaviGuideWidget guideWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_navi_1);
        mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);

        guideWidget = (NaviGuideWidget) findViewById(R.id.route_select_guidelist);

        initDrawerLayout();
    }

    private void initDrawerLayout() {
        drawerLayout = (DrawerLayout) super.findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(Color.TRANSPARENT);
    }

    public void openDetailRoute(View view) {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)){
            drawerLayout.closeDrawer(GravityCompat.END);
        } else{
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    @Override
    public void onInitNaviSuccess() {
        super.onInitNaviSuccess();
        /**
         * 方法: int strategy=mAMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, multipleroute); 参数:
         *
         * @congestion 躲避拥堵
         * @avoidhightspeed 不走高速
         * @cost 避免收费
         * @hightspeed 高速优先
         * @multipleroute 多路径
         *
         *  说明: 以上参数都是boolean类型，其中multipleroute参数表示是否多条路线，如果为true则此策略会算出多条路线。
         *  注意: 不走高速与高速优先不能同时为true 高速优先与避免收费不能同时为true
         */
        int strategy = 0;
        try {
            //再次强调，最后一个参数为true时代表多路径，否则代表单路径
            strategy = mAMapNavi.strategyConvert(true, false, false, false, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAMapNavi.calculateDriveRoute(sList, eList, mWayPointList, strategy);

    }

    @Override
    public void onCalculateRouteSuccess(int[] ids) {
        super.onCalculateRouteSuccess(ids);
        mAMapNavi.startNavi(NaviType.EMULATOR);
        //概览
        List<AMapNaviGuide> guides = mAMapNavi.getNaviGuideList();
        for (int i = 0; i < guides.size(); i++) {
            //guide step相生相惜，指的是大导航段
            AMapNaviGuide guide = guides.get(i);
            Log.d("wlx", "AMapNaviGuide 路线经纬度:" + guide.getCoord() + "");
            Log.d("wlx", "AMapNaviGuide 路线名:" + guide.getName() + "");
            Log.d("wlx", "AMapNaviGuide 路线长:" + guide.getLength() + "m");
            Log.d("wlx", "AMapNaviGuide 路线耗时:" + guide.getTime() + "s");
            Log.d("wlx", "AMapNaviGuide 路线IconType" + guide.getIconType() + "");
            Log.d("wlx", "AMapNaviGuide 路段step开始索引" + guide.getStartSegId() + "");
            Log.d("wlx", "AMapNaviGuide 路段step数量" + guide.getSegCount() + "");
        }

        guideWidget.setGuideData("新发地", "首都国际机场", getGuideGroup(mAMapNavi));


    }

    private List<LBSGuideGroup> getGuideGroup(AMapNavi mAapNavi) {
        List<LBSGuideGroup> steps = new ArrayList();
        try {
            List<AMapNaviGuide> aMapNaviGuides = mAapNavi.getNaviGuideList();
            AMapNaviPath path = mAapNavi.getNaviPath();
            List<AMapNaviStep> aMapNaviSteps = path.getSteps();

            for (int j = 0; j < aMapNaviGuides.size(); j++) {
                AMapNaviGuide g = aMapNaviGuides.get(j);
                LBSGuideGroup group = new LBSGuideGroup();
                group.setGroupIconType(g.getIconType());
                group.setGroupLen(g.getLength());
                group.setGroupName(g.getName());
                group.setGroupToll(g.getToll());
                int count = g.getSegCount();
                int startSeg = g.getStartSegId();
                int traffics = 0;
                for (int i = startSeg; i < count + startSeg; i++) {
                    AMapNaviStep step = aMapNaviSteps.get(i);
                    traffics += step.getTrafficLightNumber();
                    String roadName = "";
                    if (i == (count + startSeg - 1) && j == aMapNaviGuides.size() - 1) {
                        roadName = "终点";
                    } else if (i == (count + startSeg - 1) && j + 1 < aMapNaviGuides.size() - 1) {
                        AMapNaviGuide ag = aMapNaviGuides.get(j + 1);
                        roadName = ag.getName();
                    } else {
                        roadName = step.getLinks().get(0).getRoadName();
                    }

                    LBSGuideGroup.LBSGuidStep lbsGuidStep = new LBSGuideGroup.LBSGuidStep(step.getIconType(), roadName, step.getLength());
                    group.getSteps().add(lbsGuidStep);
                }
                group.setGroupTrafficLights(traffics);
                steps.add(group);
            }
            //        for (LBSGuideGroup guide : steps) {
//            JLog.i("SHIXIN90", "Name=" + guide.getGroupName() +//
//                    ",Length=" + guide.getGroupLen() +//
//                    ",Toll=" + guide.getGroupToll() +//
//                    ",TrafficLights=" + guide.getGroupTrafficLights() +//
//                    ",IconType=" + guide.getGroupIconType());
//
//            for (LBSGuideGroup.LBSGuidStep step : guide.getSteps()) {
//                JLog.e("SHIXIN90", "IconType=" + step.getStepIconType() +//
//                        ",roadName=" + step.getStepRoadName() +//
//                        ",Length=" + step.getStepDistance());
//            }
//        }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return steps;
    }
}
