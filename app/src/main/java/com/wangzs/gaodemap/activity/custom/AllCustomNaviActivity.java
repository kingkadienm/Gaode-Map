package com.wangzs.gaodemap.activity.custom;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.CrossOverlay;
import com.amap.api.maps.model.CrossOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapTrafficStatus;
import com.amap.api.navi.model.NaviInfo;
import com.wangzs.gaodemap.R;
import com.wangzs.gaodemap.activity.custom.view.AMapCameraOverlay;
import com.wangzs.gaodemap.activity.custom.view.CarOverlay;
import com.wangzs.gaodemap.activity.custom.view.DriveWayLinear;
import com.wangzs.gaodemap.activity.custom.view.NextTurnTipView;
import com.wangzs.gaodemap.activity.custom.view.TrafficProgressBar;
import com.wangzs.gaodemap.activity.custom.view.ZoomInIntersectionView;
import com.wangzs.gaodemap.util.DensityUtils;
import com.wangzs.gaodemap.util.NaviUtil;
import com.autonavi.ae.gmap.gloverlay.GLCrossVector;

import java.io.InputStream;
import java.util.List;

/**
 * 包名： com.amap.navi.demo.activity.custom
 * <p>
 * 创建时间：2018/4/24
 * 项目名称：NaviDemo
 *
 * @author guibao.ggb
 * @email guibao.ggb@alibaba-inc.com
 * <p>
 * 类说明：各组件整合的导航示例
 */
public class AllCustomNaviActivity extends BaseCustomActivity {

    private TrafficProgressBar mTrafficBarView;
    private DriveWayLinear mDriveWayView;
    private ZoomInIntersectionView mZoomInIntersectionView;
    private AMapCameraOverlay cameraOverlay;

    private TextView textNextRoadDistance;
    private TextView textNextRoadName;
    private NextTurnTipView nextTurnTipView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_custom_navi_view);
        mAMapNaviView = (TextureMapView) findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);


        mTrafficBarView = (TrafficProgressBar) findViewById(R.id.myTrafficBar);
        mDriveWayView = (DriveWayLinear) findViewById(R.id.myDriveWayView);
        mZoomInIntersectionView = (ZoomInIntersectionView) findViewById(R.id.myZoomInIntersectionView);

        textNextRoadDistance = (TextView) findViewById(R.id.text_next_road_distance);
        textNextRoadName = (TextView) findViewById(R.id.text_next_road_name);
        nextTurnTipView = (NextTurnTipView) findViewById(R.id.icon_next_turn_tip);

        cameraOverlay = new AMapCameraOverlay(this);


        mAMapNaviView.getMap().getUiSettings().setZoomControlsEnabled(false);
        mAMapNaviView.getMap().setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                mAMapNaviView.getMap().setPointToCenter(mAMapNaviView.getWidth()/2, mAMapNaviView.getHeight()/2);

                mAMapNaviView.getMap().moveCamera(CameraUpdateFactory.zoomTo(16f));
                carOverlay = new CarOverlay(AllCustomNaviActivity.this, mAMapNaviView);

            }
        });


    }

    /** ------- 导航基本信息的回调 ----- */
    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {
        super.onNaviInfoUpdate(naviInfo);
        int allLength = mAMapNavi.getNaviPath().getAllLength();

        /**
         * 导航路况条 更新
         */
        List<AMapTrafficStatus> trafficStatuses = mAMapNavi.getTrafficStatuses(0, 0);
        mTrafficBarView.update(allLength, naviInfo.getPathRetainDistance(), trafficStatuses);

        /**
         * 更新路口转向图标
         */
        nextTurnTipView.setIconType(naviInfo.getIconType());

        /**
         * 更新下一路口 路名及 距离
         */
        textNextRoadName.setText(naviInfo.getNextRoadName());
        textNextRoadDistance.setText(NaviUtil.formatKM(naviInfo.getCurStepRetainDistance()));

        /**
         * 绘制转弯的箭头
         */
        drawArrow(naviInfo);

    }

    /**
     * GPS 定位信息的回调函数
     * @param location 当前定位的GPS信息
     */
    @Override
    public void onLocationChange(AMapNaviLocation location) {

        if (carOverlay != null && location != null) {
            carOverlay.draw(mAMapNaviView.getMap(), new LatLng(location.getCoord().getLatitude(), location.getCoord().getLongitude()), location.getBearing());
        }
    }

    /** ----- start 车道信息的回调 start ------- */
    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {
        mDriveWayView.setVisibility(View.VISIBLE);
        mDriveWayView.buildDriveWay(aMapLaneInfo);

    }

    @Override
    public void hideLaneInfo() {
        mDriveWayView.hide();
    }
    /** ----- ebd 车道信息的回调 end -------*/


    /** ----- start 路口放大图 start ------- */
    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {
        mZoomInIntersectionView.setIntersectionBitMap(aMapNaviCross);
        mZoomInIntersectionView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideCross() {
        mZoomInIntersectionView.setVisibility(View.GONE);
    }

    private CrossOverlay crossOverlay;
    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {
        try {
            GLCrossVector.AVectorCrossAttr attr = new GLCrossVector.AVectorCrossAttr();
            // 设置显示区域
            attr.stAreaRect = new Rect(0, DensityUtils.dp2px(getApplicationContext(),50),
                    DensityUtils.getScreenWidth(getApplicationContext()), DensityUtils.dp2px(getApplicationContext(), 300));


            //        attr.stAreaRect = new Rect(0, dp2px(48), nWidth, dp2px(290));
            attr.stAreaColor = Color.argb(217, 95, 95, 95);/* 背景颜色 */
            attr.fArrowBorderWidth = DensityUtils.dp2px(getApplicationContext(), 22);/* 箭头边线宽度 */
            attr.stArrowBorderColor = Color.argb(0, 0, 50, 20);/* 箭头边线颜色 */
            attr.fArrowLineWidth = DensityUtils.dp2px(getApplicationContext(),18);/* 箭头内部宽度 */
            attr.stArrowLineColor = Color.argb(255, 255, 253, 65);/* 箭头内部颜色 */
            attr.dayMode = false;
            attr.fArrowLineWidth = 18;/* 箭头内部宽度 */
            attr.stArrowLineColor = Color.argb(255, 255, 253, 65);/* 箭头内部颜色 */
            attr.dayMode = true;


            InputStream inputStream = getResources().getAssets().open("vector3d_arrow_in.png");
            crossOverlay = mAMapNaviView.getMap().addCrossOverlay(new CrossOverlayOptions().setAttribute(attr).setRes(BitmapFactory.decodeStream(inputStream)));

            crossOverlay.setData(aMapModelCross.getPicBuf1());
            crossOverlay.setVisible(true);
            inputStream.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void hideModeCross() {
        if (crossOverlay != null) {
            crossOverlay.setVisible(false);
        }
    }

    /** ----- end 路口放大图 end ------- */

    /** ----- start 电子眼  start --------*/
    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapCameraInfos) {

        if (cameraOverlay != null) {
            cameraOverlay.draw(mAMapNaviView.getMap(), aMapCameraInfos);
        }

    }

    /** ----- end 电子眼  end --------*/

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mZoomInIntersectionView != null) {
            mZoomInIntersectionView.recycleResource();
        }

        if (crossOverlay != null) {
            crossOverlay.remove();
        }

        if (cameraOverlay != null) {
            cameraOverlay.destroy();
        }

    }
}
