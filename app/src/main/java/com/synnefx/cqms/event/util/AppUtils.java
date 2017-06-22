package com.synnefx.cqms.event.util;

import com.synnefx.cqms.event.core.Constants;

/**
 * Created by Josekutty on 8/10/2016.
 */
public class AppUtils {


    public static String getSpecialtyString(Integer code) {
        if (null != code) {
            switch (code) {
                case 1:
                    return "Medical, including dermatology, neurology, haematology, oncology, etc";
                case 2:
                    return "Surgery, including neurosurgery, urology, EENT, ophthalmology, etc.";
                case 3:
                    return "Mixed (medical & surgical), including gynaecology";
                case 4:
                    return "Obstetrics, including related surgery";
                case 5:
                    return "Paediatrics, including related surgery";
                case 6:
                    return "Intensive care & resuscitation";
                case 7:
                    return "Emergency unit";
                case 8:
                    return "Long term care & rehabilitation";
                case 9:
                    return "Ambulatory care, including related surgery";
                case 10:
                    return "Others :";
                default:
                    return Constants.Common.BLANK;
            }
        }
        return Constants.Common.BLANK;
    }

    public static String getProffessionalCategory(Integer code) {
        if (null != code) {
            switch (code) {
                case 1:
                    return "Nurse/Midwife";
                case 4:
                    return "Auxiliary";
                case 5:
                    return "Medical Doctor";
                case 12:
                    return "Other Health-care worker";
                default:
                    return Constants.Common.BLANK;
            }
        }
        return Constants.Common.BLANK;
    }

    public static String getProffessionalSubCategory(Integer code) {
        if (null != code) {
            switch (code) {
                case 1:
                    return "Nurse";
                case 2:
                    return "Midwife";
                case 3:
                    return "Student";
                case 4:
                    return "Auxiliary";
                case 5:
                    return "Internal medicine";
                case 6:
                    return "Surgeon";
                case 7:
                    return "Anaesthetist/Resuscitator/Emergency Physician";
                case 8:
                    return "Paediatrician";
                case 9:
                    return "Gynaecologist";
                default:
                    return Constants.Common.BLANK;
            }
        }
        return Constants.Common.BLANK;
    }

    public static String getIndicationString(Integer code) {
        if (null != code) {
            switch (code) {
                case 1:
                    return "bef.pat: Before touching a patient";
                case 2:
                    return "bef.asept: Before clean/aseptic procedure";
                case 3:
                    return "aft.b.f: After body fluid exposure risk";
                case 4:
                    return "aft.pat: After touching a patient";
                case 5:
                    return "aft.p.surr: After touching patient surroundings";
            }
        }
        return Constants.Common.BLANK;
    }

    public static String formatHHActionString(Integer code) {
        if (null != code) {
            switch (code) {
                case 1:
                    return "Hand Rub";
                case 2:
                    return "Hand Wash";
                case 4:
                    return "Glouse used";
                case 3:
                default:
                    return "No Action";
            }
        }
        return "No Action";
    }
}
