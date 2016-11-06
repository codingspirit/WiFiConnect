package cn.edu.ouc.wificonnect;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by ALEX.DON.SCOFIELD on 2016/10/29.
 */

public class MyPagerAdapter extends PagerAdapter {
    private List<View> myviews;

    public MyPagerAdapter(List<View> views) {
        this.myviews = views;
    }

    @Override
    public int getCount() {
        return myviews.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(myviews.get(position));
        return myviews.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(myviews.get(position));
    }
}
