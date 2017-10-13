package com.synnefx.cqms.event.sqlite;

import android.content.ContentValues;
import android.database.Cursor;

import com.synnefx.cqms.event.core.modal.IncidentType;
import com.synnefx.cqms.event.core.modal.Specialty;
import com.synnefx.cqms.event.core.modal.Unit;
import com.synnefx.cqms.event.core.modal.event.PersonInvolved;
import com.synnefx.cqms.event.core.modal.event.ReportedBy;
import com.synnefx.cqms.event.core.modal.event.drugreaction.AdverseDrugEvent;
import com.synnefx.cqms.event.core.modal.event.drugreaction.DrugInfo;
import com.synnefx.cqms.event.core.modal.event.incident.IncidentReport;
import com.synnefx.cqms.event.core.modal.event.medicationerror.MedicationError;
import com.synnefx.cqms.event.util.TimeUtil;

import java.util.Calendar;
import java.util.TimeZone;

import static com.synnefx.cqms.event.sqlite.DatabaseHelper.Columns.KEY_CREATED_ON;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.Columns.KEY_HOSPITAL_ID;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.Columns.KEY_ID;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.Columns.KEY_INCIDENTTYPE_REF;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.Columns.KEY_NAME;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.Columns.KEY_SERVER_ID;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.Columns.KEY_SPECIALTY_REF;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.Columns.KEY_STATUS_CODE;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.Columns.KEY_UNIT_REF;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.Columns.KEY_UPDATED_ON;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_ADMITTED_POST_REACTION;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_COMMENTS;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_DATE_CEASED;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_DATE_DEATH;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_DATE_RECOVERY;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_DATE_STARTED;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_DOSE;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_DRUG;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_FREQUENCY;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_REACTION_ADDED_CASESHEET;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_REACTION_DATE;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_REACTION_OUTCOME_CODE;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_ROUTE;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_SUSPECTED_DRUG;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_CONSUTANT;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_CORRECTIVE_ACTION;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_DEPARTMENT;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_DESCRIPTION;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_DESIGNATION;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_DIAGNOSIS;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_EVENT_REPORT_REF;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_FNAME;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_GENDER;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_HEIGHT;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_HOSPITAL_NUMBER;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_INCIDENT_LOCATION;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_INCIDENT_NUMBER;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_INCIDENT_TIME;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_LNAME;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_PATIENT_TYPE;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_PERSONNEL_TYPE_CODE;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_PERSON_DOB;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_PERSON_INVOLVED_REF;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_PERSON_NAME;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_REPORTED_BY_REF;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_REPORTED_ON;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_STAFF_ID;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_WEIGHT;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.IncidentReportKey.KEY_INCIDENT_LEVEL;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.IncidentReportKey.KEY_INCIDENT_TYPE_REF;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.IncidentReportKey.KEY_MEDICAL_REPORT;

/**
 * Created by Josekutty on 1/31/2017.
 */
public class SqliteDataMapper {

    public static ContentValues setUnitContent(Unit unit) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, unit.getName());
        values.put(KEY_UNIT_REF, unit.getId());
        values.put(KEY_HOSPITAL_ID, unit.getHospitalUUID());
        values.put(KEY_CREATED_ON, Calendar.getInstance(TimeZone.getTimeZone("IST")).getTimeInMillis());
        values.put(KEY_STATUS_CODE, unit.getStatusCode());
        return values;
    }


    public static Unit setUnit(Cursor c) {
        Unit unit = new Unit();
        unit.setId(c.getLong((c.getColumnIndex(KEY_ID))));
        unit.setHospitalUUID(c.getString((c.getColumnIndex(KEY_HOSPITAL_ID))));
        unit.setServerId(c.getLong((c.getColumnIndex(KEY_UNIT_REF))));
        unit.setName(c.getString(c.getColumnIndex(KEY_NAME)));
        unit.setStatusCode(c.getInt(c.getColumnIndex(KEY_STATUS_CODE)));
        return unit;
    }


    public static ContentValues setSpecialtyContent(Specialty specialty) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, specialty.getSpecialityName());
        values.put(KEY_SPECIALTY_REF, specialty.getId());
        values.put(KEY_HOSPITAL_ID, specialty.getHospitalUUID());
        values.put(KEY_CREATED_ON, Calendar.getInstance().getTimeInMillis());
        values.put(KEY_STATUS_CODE, specialty.getStatusCode());
        return values;
    }

    public static Specialty setSpeciality(Cursor c) {
        Specialty specialty = new Specialty();
        specialty.setId(c.getLong((c.getColumnIndex(KEY_ID))));
        specialty.setHospitalUUID(c.getString((c.getColumnIndex(KEY_HOSPITAL_ID))));
        specialty.setServerId(c.getLong((c.getColumnIndex(KEY_SPECIALTY_REF))));
        specialty.setSpecialityName(c.getString(c.getColumnIndex(KEY_NAME)));
        specialty.setStatusCode(c.getInt(c.getColumnIndex(KEY_STATUS_CODE)));
        return specialty;
    }


    public static ContentValues setIncidentTypeContent(IncidentType incidentType) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, incidentType.getIncidentType());
        values.put(KEY_INCIDENTTYPE_REF, incidentType.getServerId());
        values.put(KEY_HOSPITAL_ID, incidentType.getHospitalUUID());
        values.put(KEY_CREATED_ON, Calendar.getInstance(TimeZone.getTimeZone("IST")).getTimeInMillis());
        values.put(KEY_STATUS_CODE, incidentType.getStatusCode());
        return values;
    }


    public static IncidentType setIncidentType(Cursor c) {
        IncidentType incidentType = new IncidentType();
        incidentType.setId(c.getLong((c.getColumnIndex(KEY_ID))));
        incidentType.setHospitalUUID(c.getString((c.getColumnIndex(KEY_HOSPITAL_ID))));
        incidentType.setServerId(c.getLong((c.getColumnIndex(KEY_INCIDENTTYPE_REF))));
        incidentType.setIncidentType(c.getString(c.getColumnIndex(KEY_NAME)));
        incidentType.setStatusCode(c.getInt(c.getColumnIndex(KEY_STATUS_CODE)));
        return incidentType;
    }


    //Incident report
    public static ContentValues setIncidentReportContent(IncidentReport report) {
        ContentValues values = new ContentValues();

        values.put(KEY_STATUS_CODE, report.getStatusCode());
        values.put(KEY_SERVER_ID, report.getServerId());
        if (null != report.getCreatedOn())
            values.put(KEY_CREATED_ON, report.getCreatedOn().getTimeInMillis());
        if (null != report.getUpdated())
            values.put(KEY_UPDATED_ON, report.getUpdated().getTimeInMillis());

        values.put(KEY_UNIT_REF, report.getUnitRef());
        values.put(KEY_INCIDENT_TYPE_REF, report.getIncidentTypeRef());
        if (null != report.getIncidentTime())
            values.put(KEY_INCIDENT_TIME, report.getIncidentTime().getTimeInMillis());

        values.put(KEY_INCIDENT_NUMBER, report.getIncidentNumber());
        values.put(KEY_INCIDENT_LEVEL, report.getIncidentLevelCode());
        values.put(KEY_DESCRIPTION, report.getDescription());
        values.put(KEY_CORRECTIVE_ACTION, report.getCorrectiveActionTaken());
        values.put(KEY_INCIDENT_LOCATION, report.getIncidentLocation());
        values.put(KEY_MEDICAL_REPORT, report.getMedicalReport());

        return values;
    }

    public static IncidentReport setIncidentReport(Cursor c) {
        IncidentReport report = new IncidentReport();
        report.setId(c.getLong((c.getColumnIndex(KEY_ID))));
        report.setHospital(c.getString((c.getColumnIndex(KEY_HOSPITAL_ID))));
        report.setServerId(c.getLong(c.getColumnIndex(KEY_SERVER_ID)));
        report.setStatusCode(c.getInt(c.getColumnIndex(KEY_STATUS_CODE)));

        report.setDepartment(c.getString(c.getColumnIndex("unitName")));
        report.setUnitRef(c.getLong((c.getColumnIndex(KEY_UNIT_REF))));
        report.setUnit(report.getUnitRef());

        report.setIncidentTypeName(c.getString(c.getColumnIndex("incidentTypeName")));
        report.setIncidentTypeRef(c.getLong((c.getColumnIndex(KEY_INCIDENT_TYPE_REF))));
        report.setIncidentType(report.getIncidentTypeRef());

        report.setIncidentNumber(c.getString(c.getColumnIndex(KEY_INCIDENT_NUMBER)));

        report.setMedicalReport(c.getString(c.getColumnIndex(KEY_MEDICAL_REPORT)));
        report.setIncidentLevelCode(c.getInt(c.getColumnIndex(KEY_INCIDENT_LEVEL)));
        report.setDescription(c.getString(c.getColumnIndex(KEY_DESCRIPTION)));
        report.setCorrectiveActionTaken(c.getString(c.getColumnIndex(KEY_CORRECTIVE_ACTION)));

        report.setIncidentLocation(c.getString(c.getColumnIndex(KEY_INCIDENT_LOCATION)));

        Long incidentTimeMill = c.getLong(c.getColumnIndex(KEY_INCIDENT_TIME));
        if (null != incidentTimeMill && 0 < incidentTimeMill) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(incidentTimeMill);
            report.setIncidentTime(cal);
        }

        report.setPersonInvolvedRef(c.getLong(c.getColumnIndex(KEY_PERSON_INVOLVED_REF)));
        report.setReportedByRef(c.getLong(c.getColumnIndex(KEY_REPORTED_BY_REF)));
        if (null != report.getReportedByRef()) {
            ReportedBy reportedBy = new ReportedBy();
            reportedBy.setId(report.getReportedByRef());
            reportedBy.setFirstName(c.getString(c.getColumnIndex(KEY_FNAME)));
            reportedBy.setLastName(c.getString(c.getColumnIndex(KEY_LNAME)));
            report.setReportedBy(reportedBy);
        }
        //created time
        Long createdTimeMill = c.getLong(c.getColumnIndex(KEY_CREATED_ON));
        if (null != createdTimeMill && 0 < createdTimeMill) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(createdTimeMill);
            report.setCreatedOn(cal);
        }
        Long updatedTimeMill = c.getLong(c.getColumnIndex(KEY_UPDATED_ON));
        if (null != updatedTimeMill && 0 < updatedTimeMill) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(updatedTimeMill);
            report.setUpdated(cal);
        }
        return report;
    }

    public static ContentValues setPersonInvolvedContent(PersonInvolved personInvolved) {
        ContentValues values = new ContentValues();

        //values.put(KEY_STATUS_CODE, personInvolved.getStatusCode());
        //values.put(KEY_SERVER_ID, personInvolved.getServerId());
        values.put(KEY_EVENT_REPORT_REF, personInvolved.getEventRef());
        values.put(KEY_PERSONNEL_TYPE_CODE, personInvolved.getPersonnelTypeCode());
        values.put(KEY_PATIENT_TYPE, personInvolved.getPatientTypeCode());
        values.put(KEY_PERSON_NAME, personInvolved.getName());
        values.put(KEY_DESIGNATION, personInvolved.getDesignation());
        values.put(KEY_STAFF_ID, personInvolved.getStaffId());
        values.put(KEY_HOSPITAL_NUMBER, personInvolved.getHospitalNumber());
        if (null != personInvolved.getDateOfBirthIndividual())
            values.put(KEY_PERSON_DOB, personInvolved.getDateOfBirthIndividual().getTimeInMillis());
        values.put(KEY_HEIGHT, personInvolved.getHeight());
        values.put(KEY_WEIGHT, personInvolved.getWeight());
        values.put(KEY_GENDER, personInvolved.getGenderCode());
        values.put(KEY_CONSUTANT, personInvolved.getConsultantName());
        values.put(KEY_DIAGNOSIS, personInvolved.getDiagnosis());
        return values;
    }

    public static PersonInvolved setPersonInvolved(Cursor c) {
        PersonInvolved personInvolved = new PersonInvolved();
        personInvolved.setId(c.getLong((c.getColumnIndex(KEY_ID))));
        personInvolved.setEventRef(c.getLong(c.getColumnIndex(KEY_EVENT_REPORT_REF)));
        personInvolved.setPersonnelTypeCode(c.getInt(c.getColumnIndex(KEY_PERSONNEL_TYPE_CODE)));
        personInvolved.setPatientTypeCode(c.getInt(c.getColumnIndex(KEY_PATIENT_TYPE)));
        personInvolved.setName(c.getString(c.getColumnIndex(KEY_PERSON_NAME)));
        personInvolved.setDesignation(c.getString(c.getColumnIndex(KEY_DESIGNATION)));
        personInvolved.setStaffId(c.getString(c.getColumnIndex(KEY_STAFF_ID)));
        personInvolved.setHospitalNumber(c.getString(c.getColumnIndex(KEY_HOSPITAL_NUMBER)));
        personInvolved.setGenderCode(c.getInt(c.getColumnIndex(KEY_GENDER)));

        Long dobTimeMill = c.getLong(c.getColumnIndex(KEY_PERSON_DOB));
        if (null != dobTimeMill && 0 < dobTimeMill) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(dobTimeMill);
            personInvolved.setDateOfBirthIndividual(cal);
        }

        personInvolved.setWeight(c.getDouble(c.getColumnIndex(KEY_WEIGHT)));
        personInvolved.setHeight(c.getDouble(c.getColumnIndex(KEY_HEIGHT)));
        personInvolved.setConsultantName(c.getString(c.getColumnIndex(KEY_CONSUTANT)));
        personInvolved.setDiagnosis(c.getString(c.getColumnIndex(KEY_DIAGNOSIS)));

        return personInvolved;
    }

    public static ContentValues setReportedByContent(ReportedBy reportedBy) {
        ContentValues values = new ContentValues();
        values.put(KEY_EVENT_REPORT_REF, reportedBy.getEventRef());
        //values.put(KEY_STATUS_CODE, personInvolved.getStatusCode());
        //values.put(KEY_SERVER_ID, personInvolved.getServerId());
        values.put(KEY_LNAME, reportedBy.getLastName());
        values.put(KEY_FNAME, reportedBy.getFirstName());
        values.put(KEY_DESIGNATION, reportedBy.getDesignation());
        values.put(KEY_DEPARTMENT, reportedBy.getDepartment());
        if (null != reportedBy.getReportedOn())
            values.put(KEY_REPORTED_ON, reportedBy.getReportedOn().getTimeInMillis());

        return values;
    }

    public static ReportedBy setReportedBy(Cursor c) {
        ReportedBy reportedBy = new ReportedBy();
        reportedBy.setId(c.getLong((c.getColumnIndex(KEY_ID))));
        reportedBy.setEventRef(c.getLong(c.getColumnIndex(KEY_EVENT_REPORT_REF)));
        reportedBy.setLastName(c.getString(c.getColumnIndex(KEY_LNAME)));
        reportedBy.setFirstName(c.getString(c.getColumnIndex(KEY_FNAME)));
        reportedBy.setDesignation(c.getString(c.getColumnIndex(KEY_DESIGNATION)));
        reportedBy.setDepartment(c.getString(c.getColumnIndex(KEY_DEPARTMENT)));

        Long reportedTimeMill = c.getLong(c.getColumnIndex(KEY_REPORTED_ON));
        if (null != reportedTimeMill && 0 < reportedTimeMill) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(reportedTimeMill);
            reportedBy.setReportedOn(cal);
        }
        return reportedBy;
    }


    /**
     * Medication Error report
     **/

    public static ContentValues setMedicationErrorContent(MedicationError report) {
        ContentValues values = new ContentValues();

        values.put(KEY_STATUS_CODE, report.getStatusCode());
        values.put(KEY_SERVER_ID, report.getServerId());
        if (null != report.getCreatedOn())
            values.put(KEY_CREATED_ON, report.getCreatedOn().getTimeInMillis());
        if (null != report.getUpdated())
            values.put(KEY_UPDATED_ON, report.getUpdated().getTimeInMillis());
        values.put(KEY_UNIT_REF, report.getUnitRef());
        if (null != report.getIncidentTime())
            values.put(KEY_INCIDENT_TIME, report.getIncidentTime().getTimeInMillis());
        values.put(KEY_INCIDENT_NUMBER, report.getIncidentNumber());
        values.put(KEY_INCIDENT_LEVEL, report.getIncidentLevelCode());
        values.put(KEY_DESCRIPTION, report.getDescription());
        values.put(KEY_CORRECTIVE_ACTION, report.getCorrectiveActionTaken());
        values.put(KEY_INCIDENT_LOCATION, report.getIncidentLocation());
        values.put(KEY_MEDICAL_REPORT, report.getMedicalReport());

        return values;
    }

    public static MedicationError setMedicationError(Cursor c) {
        MedicationError report = new MedicationError();
        report.setId(c.getLong((c.getColumnIndex(KEY_ID))));
        report.setHospital(c.getString((c.getColumnIndex(KEY_HOSPITAL_ID))));
        report.setServerId(c.getLong(c.getColumnIndex(KEY_SERVER_ID)));
        report.setStatusCode(c.getInt(c.getColumnIndex(KEY_STATUS_CODE)));

        report.setDepartment(c.getString(c.getColumnIndex("unitName")));
        report.setUnitRef(c.getLong((c.getColumnIndex(KEY_UNIT_REF))));
        report.setUnit(report.getUnitRef());

        report.setIncidentNumber(c.getString(c.getColumnIndex(KEY_INCIDENT_NUMBER)));

        report.setMedicalReport(c.getString(c.getColumnIndex(KEY_MEDICAL_REPORT)));
        report.setIncidentLevelCode(c.getInt(c.getColumnIndex(KEY_INCIDENT_LEVEL)));
        report.setDescription(c.getString(c.getColumnIndex(KEY_DESCRIPTION)));
        report.setCorrectiveActionTaken(c.getString(c.getColumnIndex(KEY_CORRECTIVE_ACTION)));

        report.setIncidentLocation(c.getString(c.getColumnIndex(KEY_INCIDENT_LOCATION)));

        Long incidentTimeMill = c.getLong(c.getColumnIndex(KEY_INCIDENT_TIME));
        if (null != incidentTimeMill && 0 < incidentTimeMill) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(incidentTimeMill);
            report.setIncidentTime(cal);
        }

        report.setPersonInvolvedRef(c.getLong(c.getColumnIndex(KEY_PERSON_INVOLVED_REF)));
        report.setReportedByRef(c.getLong(c.getColumnIndex(KEY_REPORTED_BY_REF)));
        if (null != report.getReportedByRef()) {
            ReportedBy reportedBy = new ReportedBy();
            reportedBy.setId(report.getReportedByRef());
            reportedBy.setFirstName(c.getString(c.getColumnIndex(KEY_FNAME)));
            reportedBy.setLastName(c.getString(c.getColumnIndex(KEY_LNAME)));
            report.setReportedBy(reportedBy);
        }
        //created time
        Long createdTimeMill = c.getLong(c.getColumnIndex(KEY_CREATED_ON));
        if (null != createdTimeMill && 0 < createdTimeMill) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(createdTimeMill);
            report.setCreatedOn(cal);
        }
        Long updatedTimeMill = c.getLong(c.getColumnIndex(KEY_UPDATED_ON));
        if (null != updatedTimeMill && 0 < updatedTimeMill) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(updatedTimeMill);
            report.setUpdated(cal);
        }
        return report;
    }

    /**Adverse drug event rteaction**/
    public static ContentValues setDrugInfoContent(DrugInfo drugInfo) {
        ContentValues values = new ContentValues();
        values.put(KEY_EVENT_REPORT_REF, drugInfo.getEventRef());
        values.put(KEY_DRUG, drugInfo.getDrug());
        values.put(KEY_DOSE, drugInfo.getDose());
        values.put(KEY_FREQUENCY, drugInfo.getFrequency());
        values.put(KEY_ROUTE, drugInfo.getRoute());

        if (null != drugInfo.getDateStarted())
            values.put(KEY_DATE_STARTED, drugInfo.getDateStarted().getTimeInMillis());
        if (null != drugInfo.getDateCeased())
            values.put(KEY_DATE_CEASED, drugInfo.getDateCeased().getTimeInMillis());

        values.put(KEY_SUSPECTED_DRUG, drugInfo.isSuspectedDrug()?1:0);

        return values;
    }

    public static DrugInfo setDrugIngo(Cursor c) {
        DrugInfo drugInfo = new DrugInfo();
        drugInfo.setId(c.getLong((c.getColumnIndex(KEY_ID))));
        drugInfo.setEventRef(c.getLong(c.getColumnIndex(KEY_EVENT_REPORT_REF)));
        drugInfo.setDrug(c.getString(c.getColumnIndex(KEY_DRUG)));
        drugInfo.setDose(c.getString(c.getColumnIndex(KEY_DOSE)));
        drugInfo.setFrequency(c.getString(c.getColumnIndex(KEY_FREQUENCY)));
        drugInfo.setRoute(c.getString(c.getColumnIndex(KEY_ROUTE)));

        Long startedTimeMill = c.getLong(c.getColumnIndex(KEY_DATE_STARTED));
        if (null != startedTimeMill && 0 < startedTimeMill) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(startedTimeMill);
            drugInfo.setDateStarted(cal);
        }

        Long ceasedTimeMill = c.getLong(c.getColumnIndex(KEY_DATE_CEASED));
        if (null != ceasedTimeMill && 0 < ceasedTimeMill) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(ceasedTimeMill);
            drugInfo.setDateCeased(cal);
        }
        Integer suspectedDrugFlag = c.getInt(c.getColumnIndex(KEY_SUSPECTED_DRUG));
        if(null ==suspectedDrugFlag || 0 == suspectedDrugFlag){
            drugInfo.setIsSuspectedDrug(false);
        }else{
            drugInfo.setIsSuspectedDrug(true);
        }
        return drugInfo;
    }

    public static ContentValues setAdverseDrugEventContent(AdverseDrugEvent report) {
        ContentValues values = new ContentValues();

        values.put(KEY_STATUS_CODE, report.getStatusCode());
        values.put(KEY_SERVER_ID, report.getServerId());
        if (null != report.getCreatedOn())
            values.put(KEY_CREATED_ON, report.getCreatedOn().getTimeInMillis());
        if (null != report.getUpdated())
            values.put(KEY_UPDATED_ON, report.getUpdated().getTimeInMillis());
        values.put(KEY_UNIT_REF, report.getUnitRef());

        if (null != report.getIncidentTime())
            values.put(KEY_INCIDENT_TIME, report.getIncidentTime().getTimeInMillis());

        values.put(KEY_INCIDENT_NUMBER, report.getIncidentNumber());
        values.put(KEY_DESCRIPTION, report.getDescription());
        values.put(KEY_CORRECTIVE_ACTION, report.getCorrectiveActionTaken());
        values.put(KEY_INCIDENT_LOCATION, report.getIncidentLocation());

        if(null != report.getReactionDate())
            values.put(KEY_REACTION_DATE, report.getReactionDate().getTimeInMillis());

        values.put(KEY_REACTION_OUTCOME_CODE, report.getActionOutcomeCode());
        values.put(KEY_ADMITTED_POST_REACTION, report.getActionOutcomeCode());
        values.put(KEY_REACTION_ADDED_CASESHEET, report.isReactionAddedToCasesheet()?1:0);
        values.put(KEY_COMMENTS, report.getComments());

        if(null != report.getDateOfRecovery())
            values.put(KEY_DATE_RECOVERY, report.getDateOfRecovery().getTimeInMillis());
        if(null != report.getDateOfDeath())
            values.put(KEY_DATE_DEATH, report.getDateOfDeath().getTimeInMillis());
        return values;
    }

    public static AdverseDrugEvent setAdverseDrugEvent(Cursor c) {
        AdverseDrugEvent report = new AdverseDrugEvent();
        report.setId(c.getLong((c.getColumnIndex(KEY_ID))));
        report.setHospital(c.getString((c.getColumnIndex(KEY_HOSPITAL_ID))));
        report.setServerId(c.getLong(c.getColumnIndex(KEY_SERVER_ID)));
        report.setStatusCode(c.getInt(c.getColumnIndex(KEY_STATUS_CODE)));

        report.setDepartment(c.getString(c.getColumnIndex("unitName")));
        report.setUnitRef(c.getLong((c.getColumnIndex(KEY_UNIT_REF))));
        report.setUnit(report.getUnitRef());
        report.setIncidentNumber(c.getString(c.getColumnIndex(KEY_INCIDENT_NUMBER)));
        report.setDescription(c.getString(c.getColumnIndex(KEY_DESCRIPTION)));
        report.setCorrectiveActionTaken(c.getString(c.getColumnIndex(KEY_CORRECTIVE_ACTION)));
        int caseSheetAdded = c.getInt(c.getColumnIndex(KEY_REACTION_ADDED_CASESHEET));
        if (caseSheetAdded ==1){
            report.setReactionAddedToCasesheet(true);
        }else{
            report.setReactionAddedToCasesheet(false);
        }


        report.setIncidentLocation(c.getString(c.getColumnIndex(KEY_INCIDENT_LOCATION)));

        Long incidentTimeMill = c.getLong(c.getColumnIndex(KEY_INCIDENT_TIME));
        if (null != incidentTimeMill && 0 < incidentTimeMill) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(incidentTimeMill);
            report.setIncidentTime(cal);
        }

        Long reactionTimeMill = c.getLong(c.getColumnIndex(KEY_REACTION_DATE));
        if (null != reactionTimeMill && 0 < reactionTimeMill) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(reactionTimeMill);
            report.setReactionDate(cal);
            report.setReactionDateStr(TimeUtil.getDate(cal));
        }

        report.setPersonInvolvedRef(c.getLong(c.getColumnIndex(KEY_PERSON_INVOLVED_REF)));
        if(null != report.getPersonInvolvedRef()){
            PersonInvolved person = new PersonInvolved();
            person.setId(report.getPersonInvolvedRef());
            person.setName(c.getString(c.getColumnIndex(KEY_PERSON_NAME)));
            person.setHospitalNumber(c.getString(c.getColumnIndex(KEY_HOSPITAL_NUMBER)));
            person.setPatientTypeCode(c.getInt(c.getColumnIndex(KEY_PATIENT_TYPE)));
            report.setPersonInvolved(person);
        }

        report.setReportedByRef(c.getLong(c.getColumnIndex(KEY_REPORTED_BY_REF)));
        if (null != report.getReportedByRef()) {
            ReportedBy reportedBy = new ReportedBy();
            reportedBy.setId(report.getReportedByRef());
            reportedBy.setFirstName(c.getString(c.getColumnIndex(KEY_FNAME)));
            reportedBy.setLastName(c.getString(c.getColumnIndex(KEY_LNAME)));
            report.setReportedBy(reportedBy);
        }
        //created time
        Long createdTimeMill = c.getLong(c.getColumnIndex(KEY_CREATED_ON));
        if (null != createdTimeMill && 0 < createdTimeMill) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(createdTimeMill);
            report.setCreatedOn(cal);
        }
        Long updatedTimeMill = c.getLong(c.getColumnIndex(KEY_UPDATED_ON));
        if (null != updatedTimeMill && 0 < updatedTimeMill) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(updatedTimeMill);
            report.setUpdated(cal);
        }

        report.setActionOutcomeCode(c.getInt(c.getColumnIndex(KEY_REACTION_OUTCOME_CODE)));
        report.setComments(c.getString(c.getColumnIndex(KEY_COMMENTS)));

        Integer admittedPostReaction = c.getInt(c.getColumnIndex(KEY_ADMITTED_POST_REACTION));
        if(null == admittedPostReaction || 0 == admittedPostReaction){
            report.setAdmittedPostReaction(false);
        }else{
            report.setAdmittedPostReaction(true);
        }
        Integer reactionAddedToCasesheet = c.getInt(c.getColumnIndex(KEY_REACTION_OUTCOME_CODE));
        report.setActionOutcomeCode(reactionAddedToCasesheet);

        Long recoveredTimeMill = c.getLong(c.getColumnIndex(KEY_DATE_RECOVERY));
        if (null != recoveredTimeMill && 0 < recoveredTimeMill) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(recoveredTimeMill);
            report.setDateOfRecovery(cal);
            report.setReactionDateStr(TimeUtil.getDate(cal));
        }

        Long deathTimeMill = c.getLong(c.getColumnIndex(KEY_DATE_DEATH));
        if (null != deathTimeMill && 0 < deathTimeMill) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(deathTimeMill);
            report.setDateOfDeath(cal);
            report.setDateOfDeathStr(TimeUtil.getDate(cal));
        }
        return report;
    }
}
