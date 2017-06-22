package com.synnefx.cqms.event.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.ui.base.BootstrapPagerAdapter;
import com.synnefx.cqms.event.ui.base.FragmentListener;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Fragment which houses the View pager.
 */
public class CarouselFragment extends Fragment {

    //@Bind(R.id.tpi_header)
    //protected TitlePageIndicator indicator;

    @Bind(R.id.vp_pages)
    protected ViewPager pager;

    private FragmentListener fragmentListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_carousel, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        ButterKnife.bind(this, getView());

        Bundle bundle = this.getArguments();

        pager.setAdapter(new BootstrapPagerAdapter(getResources(), getChildFragmentManager()));
        //indicator.setViewPager(pager);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                fragmentListener.viewFragment(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if(bundle != null){
            int pos = bundle.getInt("id",1);
            pager.setCurrentItem(pos);
        }else{
            pager.setCurrentItem(1);
        }


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentListener){
            fragmentListener = (FragmentListener) context;
        }
    }
}