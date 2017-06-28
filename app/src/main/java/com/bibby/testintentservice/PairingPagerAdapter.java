package com.bibby.testintentservice;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PairingPagerAdapter extends FragmentPagerAdapter {

    private final int pageCount = 2;

    public PairingPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return PairingFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return pageCount;
    }

}
