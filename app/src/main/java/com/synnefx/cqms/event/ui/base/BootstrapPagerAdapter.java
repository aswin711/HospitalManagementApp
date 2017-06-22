package com.synnefx.cqms.event.ui.base;


import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.synnefx.cqms.event.R;
import com.synnefx.cqms.event.ui.drugreaction.DrugReactionListFragment;
import com.synnefx.cqms.event.ui.incident.IncidentReportListFragment;
import com.synnefx.cqms.event.ui.medicationerror.MedicationErrorListFragment;

/**
 * Pager adapter
 */
public class BootstrapPagerAdapter extends FragmentPagerAdapter {

    private final Resources resources;


    /**
     * Create pager adapter
     *
     * @param resources
     * @param fragmentManager
     */
    public BootstrapPagerAdapter(final Resources resources, final FragmentManager fragmentManager) {
        super(fragmentManager);
        this.resources = resources;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Fragment getItem(final int position) {
        final Fragment result;
        switch (position) {
            case 0:
                result = new MedicationErrorListFragment();
                break;
            case 1:
                result = new IncidentReportListFragment();
                break;
            case 2:
                result = new DrugReactionListFragment();
                break;
            default:
                result = null;
                break;
        }
        if (result != null) {
            result.setArguments(new Bundle()); //TODO do we need this?
        }
        return result;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.page_medication_error);
            case 1:
                return resources.getString(R.string.page_incident_reports);
            case 2:
                return resources.getString(R.string.page_adverse_drug_reaction);
            default:
                return null;
        }
    }
}
