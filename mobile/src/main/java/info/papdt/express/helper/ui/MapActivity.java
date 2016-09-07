package info.papdt.express.helper.ui;

import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;

import java.util.ArrayList;

import info.papdt.express.helper.R;
import info.papdt.express.helper.ui.common.AbsActivity;

/**
 * Created by hao on 2016-08-31.
 */
public class MapActivity extends AbsActivity implements GeocodeSearch.OnGeocodeSearchListener {

    int i1 = 0;
    private MapView mapView;
    private AMap aMap;
    private GeocodeSearch geocoderSearch;
    private LatLng mLatLng;
    private ArrayList<LatLng> mLatLngsArray = new ArrayList<>();
    private Marker geoMarker;

    private String mPackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        geocoderSearch = new GeocodeSearch(MapActivity.this);
        geocoderSearch.setOnGeocodeSearchListener(this);

        mPackage = getIntent().getStringExtra("Package");
        JSONObject mPackageObj = JSON.parseObject(mPackage);
        String data = mPackageObj.getString("data");
        JSONArray dataArray = JSON.parseArray(data);
        for (int i = 0; i < dataArray.size(); i++) {
            String dataI = dataArray.get(i).toString();
            JSONObject dataobj = JSON.parseObject(dataI);
            String context = dataobj.getString("context");
            getLatlon(context);
        }


    }

    /**
     * 响应地理编码
     */
    public void getLatlon(final String name) {
//        showDialog();

        GeocodeQuery query = new GeocodeQuery(name, null);// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode，
        geocoderSearch.getFromLocationNameAsyn(query);// 设置同步地理编码请求
    }

    /**
     * 地理编码查询回调
     */
    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
//        dismissDialog();
        if (rCode == 1000) {
            if (result != null && result.getGeocodeAddressList() != null
                    && result.getGeocodeAddressList().size() > 0) {
                GeocodeAddress address = result.getGeocodeAddressList().get(0);
                LatLonPoint latLonPoint = address.getLatLonPoint();
                mLatLng = new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
                mLatLngsArray.add(i1, mLatLng);
                i1++;

                geoMarker = aMap.addMarker(new MarkerOptions()
                        .title(address.getFormatAddress())
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                geoMarker.setPosition(mLatLng);

                if (i1 == 1) {
                    geoMarker.showInfoWindow();
                } else if (i1 == mLatLngsArray.size()) {
                    for (int j = 0; j < mLatLngsArray.size() - 1; j++) {
                        aMap.addPolyline((new PolylineOptions())
                                .add(mLatLngsArray.get(j), mLatLngsArray.get(j + 1))
                                .geodesic(true).setDottedLine(true));

                    }
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            mLatLngsArray.get(0), 7));
                }
            }
        }
    }


    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {

    }

    @Override
    protected void setUpViews() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


}
