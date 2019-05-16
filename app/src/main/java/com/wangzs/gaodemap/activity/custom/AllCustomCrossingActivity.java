package com.wangzs.gaodemap.activity.custom;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.CrossOverlay;
import com.amap.api.maps.model.CrossOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviLocation;
import com.wangzs.gaodemap.R;
import com.wangzs.gaodemap.activity.custom.view.CarOverlay;
import com.wangzs.gaodemap.activity.custom.view.ZoomInIntersectionView;
import com.wangzs.gaodemap.util.DensityUtils;
import com.autonavi.ae.gmap.gloverlay.GLCrossVector;

import java.io.InputStream;

/**
 * 包名： com.amap.navi.demo.activity.custom
 * <p>
 * 创建时间：2018/4/24
 * 项目名称：NaviDemo
 *
 * @author guibao.ggb
 * @email guibao.ggb@alibaba-inc.com
 * <p>
 * 类说明：自定义路口放大图的示例
 */
public class AllCustomCrossingActivity extends BaseCustomActivity {

    private ZoomInIntersectionView mZoomInIntersectionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_custom_crossing_view);
        mAMapNaviView = (TextureMapView) findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);


        mZoomInIntersectionView = (ZoomInIntersectionView) findViewById(R.id.myZoomInIntersectionView);

        mAMapNaviView.getMap().getUiSettings().setZoomControlsEnabled(false);
        mAMapNaviView.getMap().setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                mAMapNaviView.getMap().setPointToCenter(mAMapNaviView.getWidth()/2, mAMapNaviView.getHeight()/2);

                mAMapNaviView.getMap().moveCamera(CameraUpdateFactory.zoomTo(16f));
                carOverlay = new CarOverlay(AllCustomCrossingActivity.this, mAMapNaviView);

            }
        });


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

    /** ----- start 路口放大图 start ------- */
    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {
        mZoomInIntersectionView.setVisibility(View.VISIBLE);
        mZoomInIntersectionView.setIntersectionBitMap(aMapNaviCross);
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
            attr.stAreaRect = new Rect(0, DensityUtils.dp2px(getApplicationContext(),48),
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mZoomInIntersectionView != null) {
            mZoomInIntersectionView.recycleResource();
        }

        if (crossOverlay != null) {
            crossOverlay.remove();
        }

    }
}
