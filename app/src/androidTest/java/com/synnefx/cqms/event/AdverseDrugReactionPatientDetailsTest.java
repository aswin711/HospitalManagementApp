package com.synnefx.cqms.event;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.synnefx.cqms.event.ui.MainActivity;
import com.synnefx.cqms.event.ui.drugreaction.DrugReactionActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * Created by cedex on 10/5/2017.
 */
@RunWith(AndroidJUnit4.class)
public class AdverseDrugReactionPatientDetailsTest {
    @Rule
    public ActivityTestRule<DrugReactionActivity> activityTestRule
            = new ActivityTestRule<>(DrugReactionActivity.class);

    @Before
    public void init(){
        activityTestRule.getActivity().getSupportFragmentManager();

    }
}
