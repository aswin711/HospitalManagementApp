package com.synnefx.cqms.event.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.annotations.Expose;
import com.synnefx.cqms.event.core.modal.IncidentType;
import com.synnefx.cqms.event.core.modal.Specialty;
import com.synnefx.cqms.event.core.modal.Unit;
import com.synnefx.cqms.event.core.modal.User;
import com.synnefx.cqms.event.core.modal.event.PersonInvolved;
import com.synnefx.cqms.event.core.modal.event.ReportedBy;
import com.synnefx.cqms.event.core.modal.event.drugreaction.AdverseDrugEvent;
import com.synnefx.cqms.event.core.modal.event.drugreaction.DrugInfo;
import com.synnefx.cqms.event.core.modal.event.incident.IncidentReport;
import com.synnefx.cqms.event.core.modal.event.medicationerror.MedicationError;
import com.synnefx.cqms.event.util.ListViewer;
import com.synnefx.cqms.event.util.PrefUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import timber.log.Timber;

import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_DATE_CEASED;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_DATE_STARTED;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_DOSE;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_DRUG;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_FREQUENCY;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_ROUTE;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.DrugReactionKey.KEY_SUSPECTED_DRUG;
import static com.synnefx.cqms.event.sqlite.DatabaseHelper.EventReportKey.KEY_EVENT_REPORT_REF;


/**
 * Created by Josekutty on 9/17/2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper sInstance;

    private static final String LOG = "DatabaseHelper";
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "cqmseventsappdb";

    // Table Names
    private static final String TABLE_USERS = "tbl_authorized_users";
    private static final String TABLE_VERSIONS = "tbl_app_version";
    private static final String TABLE_UNITS = "tbl_units";
    private static final String TABLE_SPECIALTY = "tbl_speciality";
    private static final String TABLE_INCIDENT_TYPE = "tbl_incident_types";
    private static final String TABLE_HOSPITAL = "tbl_hospital";


    // Common column names
    public static final class Columns {

        public static final String KEY_ID = "id";
        public static final String KEY_SERVER_ID = "serverId";
        public static final String KEY_NAME = "name";
        public static final String KEY_CREATED_ON = "createdOn";
        public static final String KEY_UPDATED_ON = "updatedOn";
        public static final String KEY_HOSPITAL_ID = "hospitalID";
        public static final String KEY_REMOVED = "removed";
        public static final String KEY_SORT_ORDER = "sort_order";
        public static final String KEY_STATUS_CODE = "status_code";


        public static final String KEY_USERNAME = "username";
        public static final String KEY_EMAIL = "email";

        public static final String KEY_UNIT_REF = "unitRef";
        public static final String KEY_SPECIALTY_REF = "specialtyRef";
        public static final String KEY_INCIDENTTYPE_REF = "incidentTypeRef";


        public static final String KEY_VERSION_CODE = "version_code";

    }

    private static final String CREATE_TABLE_USERS = "CREATE TABLE IF NOT EXISTS "
            + TABLE_USERS + " ( " + Columns.KEY_ID + " INTEGER PRIMARY KEY, " + Columns.KEY_USERNAME
            + " TEXT, " + Columns.KEY_NAME + " TEXT, " + Columns.KEY_EMAIL
            + " TEXT, "+ Columns.KEY_CREATED_ON + " DATETIME, " + Columns.KEY_HOSPITAL_ID + " TEXT NOT NULL )";

    private static final String CREATE_TABLE_UNITS = "CREATE TABLE IF NOT EXISTS "
            + TABLE_UNITS + " (" + Columns.KEY_ID + " INTEGER PRIMARY KEY, " + Columns.KEY_NAME + " TEXT NOT NULL, " + Columns.KEY_UNIT_REF
            + " INTEGER NOT NULL, " + Columns.KEY_HOSPITAL_ID + " TEXT NOT NULL, " + Columns.KEY_STATUS_CODE + " INTEGER NOT NULL, " + Columns.KEY_CREATED_ON
            + " DATETIME, UNIQUE("+Columns.KEY_UNIT_REF+", "+Columns.KEY_HOSPITAL_ID +") ON CONFLICT REPLACE )";


    private static final String CREATE_TABLE_SPECIALTY = "CREATE TABLE IF NOT EXISTS "
            + TABLE_SPECIALTY + " (" + Columns.KEY_ID + " INTEGER PRIMARY KEY, " + Columns.KEY_NAME + " TEXT NOT NULL, " + Columns.KEY_SPECIALTY_REF
            + " INTEGER NOT NULL, " + Columns.KEY_HOSPITAL_ID + " TEXT NOT NULL, " + Columns.KEY_STATUS_CODE + " INTEGER NOT NULL, " + Columns.KEY_CREATED_ON
            + " DATETIME, UNIQUE ("+Columns.KEY_SPECIALTY_REF+", "+Columns.KEY_HOSPITAL_ID +") ON CONFLICT REPLACE ) ";

    private static final String CREATE_TABLE_INCIDENT_TYPE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_INCIDENT_TYPE + " (" + Columns.KEY_ID + " INTEGER PRIMARY KEY, " + Columns.KEY_NAME + " TEXT NOT NULL, " + Columns.KEY_INCIDENTTYPE_REF
            + " INTEGER NOT NULL, " + Columns.KEY_HOSPITAL_ID + " TEXT NOT NULL, " + Columns.KEY_STATUS_CODE + " INTEGER NOT NULL, " + Columns.KEY_CREATED_ON
            + " DATETIME, UNIQUE("+Columns.KEY_INCIDENTTYPE_REF+", "+Columns.KEY_HOSPITAL_ID +") ON CONFLICT REPLACE ) ";


    private static final String CREATE_TABLE_VERSION_HISTORY = "CREATE TABLE IF NOT EXISTS "
            + TABLE_VERSIONS + "(" + Columns.KEY_ID + " INTEGER PRIMARY KEY, " + Columns.KEY_VERSION_CODE
            + " INTEGER" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_VERSION_HISTORY);
        db.execSQL(CREATE_TABLE_UNITS);
        db.execSQL(CREATE_TABLE_SPECIALTY);
        db.execSQL(CREATE_TABLE_INCIDENT_TYPE);

        db.execSQL(CREATE_TABLE_PERSON_INVOLVED);
        db.execSQL(CREATE_TABLE_REPORTED_BY);
        db.execSQL(CREATE_TABLE_INCIDENT_REPORT);

       // db.execSQL(CREATE_TABLE_MEDICATION_ERR_REPORT);


        db.execSQL(CREATE_TABLE_ADVERSE_DRUGG_REACTION_REPORT);

        db.execSQL(CREATE_TABLE_DRUG_INFO);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UNITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPECIALTY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCIDENT_TYPE);
        onCreate(db);
    }

    public int createVersionEntry(int key) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Columns.KEY_VERSION_CODE, key);
        return (int) db.insert(TABLE_VERSIONS, null, values);
    }


    private <E extends Serializable> E getValue(List<E> list, E o) {
        if (null == o)
            return null;
        if (null != list && list.size() > 0) {
            for (E ob : list) {
                if (null == ob || !ob.equals(o))
                    continue;
                else
                    return ob;
            }
        }
        return null;
    }

    //Configure Units

    public void insertOrUpdateUnits(List<Unit> units, String hospitalRef) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (null != units && units.size() > 0) {
            List<Unit> existingRecords = this.getAllUnitsTypes(hospitalRef);
            db.beginTransaction();
            try {
                for (Unit unit : units) {
                    ContentValues values = SqliteDataMapper.setUnitContent(unit);
                    Unit foundUnit = getValue(existingRecords, unit);
                    existingRecords.remove(foundUnit);
                    if (null != foundUnit) {
                        db.update(TABLE_UNITS, values, Columns.KEY_ID + "=" + foundUnit.getId(), null);
                    } else {
                        db.insert(TABLE_UNITS, null, values);
                    }
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Timber.e("insertOrUpdateUnits ", e);
            } finally {
                db.endTransaction();
            }
        }
    }


    public void syncUnits(List<Unit> units, String hospitalRef) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (null != units && units.size() > 0) {
            List<Unit> existingRecords = this.getAllUnitsTypes(hospitalRef);
            db.beginTransaction();
            try {
                for (Unit unit : units) {
                    ContentValues values = SqliteDataMapper.setUnitContent(unit);
                    Unit foundUnit = getValue(existingRecords, unit);
                    existingRecords.remove(foundUnit);
                    if (null != foundUnit) {
                        db.update(TABLE_UNITS, values, Columns.KEY_ID + "=" + foundUnit.getId(), null);
                    } else {
                        db.insert(TABLE_UNITS, null, values);
                    }
                }
                if (null != existingRecords && 0 < existingRecords.size()) {
                    for (Unit unit : existingRecords) {
                        unit.setStatusCode(2);
                        ContentValues values = SqliteDataMapper.setUnitContent(unit);
                        db.update(TABLE_UNITS, values, Columns.KEY_ID + "=" + unit.getId(), null);
                    }
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Timber.e("syncUnits ", e);
            } finally {
                db.endTransaction();
            }
        }
    }


    private List<Unit> setUnits(Cursor c) {
        List<Unit> units = Collections.emptyList();
        try {
            if (c.moveToFirst()) {
                units = new ArrayList<Unit>();
                do {
                    Unit unit = SqliteDataMapper.setUnit(c);
                    units.add(unit);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Timber.e(LOG, e);
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return units;
    }

    public List<Unit> getAllUnitsTypesByStatus(String hospitalID, int statusCode) {
        String selectQuery = "SELECT  * FROM " + TABLE_UNITS + " WHERE " + Columns.KEY_HOSPITAL_ID + " = ? AND " +
                Columns.KEY_STATUS_CODE + " = ? ORDER BY " + Columns.KEY_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[]{hospitalID, String.valueOf(statusCode)});
        return setUnits(c);
    }

    public List<Unit> getAllUnitsTypes(String hospitalID) {
        List<Unit> units = new ArrayList<Unit>();
        String selectQuery = "SELECT  * FROM " + TABLE_UNITS + " WHERE " + Columns.KEY_HOSPITAL_ID + " = ? ORDER BY " +
                Columns.KEY_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[]{hospitalID});
        return setUnits(c);
    }


    /***
     * Specialty
     ***/

    public void insertOrUpdateSpecialty(List<Specialty> specialties, String hospitalRef) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (null != specialties && specialties.size() > 0) {
            List<Specialty> existingRecords = this.getAllSpecialties(hospitalRef);
            db.beginTransaction();
            try {
                for (Specialty specialty : specialties) {
                    ContentValues values = SqliteDataMapper.setSpecialtyContent(specialty);
                    Specialty existingRecord = getValue(existingRecords, specialty);
                    existingRecords.remove(specialty);
                    if (null != existingRecord) {
                        db.update(TABLE_SPECIALTY, values, Columns.KEY_ID + "=" + existingRecord.getId(), null);
                    } else {
                        db.insert(TABLE_SPECIALTY, null, values);
                    }
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Timber.e("insertOrUpdateSpecialty ", e);
            } finally {
                db.endTransaction();
            }
        }
    }

    public void syncSpecialities(List<Specialty> specialties, String hospitalRef) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (null != specialties && specialties.size() > 0) {
            List<Specialty> existingRecords = this.getAllSpecialties(hospitalRef);
            db.beginTransaction();
            try {
                for (Specialty specialty : specialties) {
                    ContentValues values = SqliteDataMapper.setSpecialtyContent(specialty);
                    Specialty exisitngRecord = getValue(existingRecords, specialty);
                    existingRecords.remove(specialty);
                    if (null != exisitngRecord) {
                        db.update(TABLE_SPECIALTY, values, Columns.KEY_ID + "=" + exisitngRecord.getId(), null);
                    } else {
                        db.insert(TABLE_SPECIALTY, null, values);
                    }
                }
                if (null != existingRecords && 0 < existingRecords.size()) {
                    for (Specialty specialty : existingRecords) {
                        specialty.setStatusCode(2);
                        ContentValues values = SqliteDataMapper.setSpecialtyContent(specialty);
                        db.update(TABLE_SPECIALTY, values, Columns.KEY_ID + "=" + specialty.getId(), null);
                    }
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Timber.e("syncSpecialities ", e);
            } finally {
                db.endTransaction();
            }
        }
    }

    private List<Specialty> setSpecialities(Cursor c) {
        List<Specialty> specialties = Collections.emptyList();
        try {
            if (c.moveToFirst()) {
                specialties = new ArrayList<Specialty>();
                do {
                    Specialty specialty = SqliteDataMapper.setSpeciality(c);
                    specialties.add(specialty);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Timber.e(LOG, e);
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return specialties;
    }

    public List<Specialty> getAllSpecialtiesByStatus(String hospitalID, int statusCode) {
        String selectQuery = "SELECT  * FROM " + TABLE_SPECIALTY + " WHERE " + Columns.KEY_HOSPITAL_ID + " = ? AND " +
                Columns.KEY_STATUS_CODE + " = ? ORDER BY " + Columns.KEY_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[]{hospitalID, String.valueOf(statusCode)});
        return setSpecialities(c);
    }

    public List<Specialty> getAllSpecialties(String hospitalID) {
        String selectQuery = "SELECT  * FROM " + TABLE_SPECIALTY + " WHERE " + Columns.KEY_HOSPITAL_ID + " = ? ORDER BY " + Columns.KEY_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[]{hospitalID});
        return setSpecialities(c);
    }


    public void syncIncidentTypes(List<IncidentType> incidentTypes, String hospitalRef) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (null != incidentTypes && incidentTypes.size() > 0) {
            List<IncidentType> existingRecords = this.getAllIncidentTypes(hospitalRef);
            db.beginTransaction();
            try {
                for (IncidentType incidentType : incidentTypes) {
                    incidentType.setHospitalUUID(hospitalRef);
                    ContentValues values = SqliteDataMapper.setIncidentTypeContent(incidentType);
                    IncidentType foundIcidentType = getValue(existingRecords, incidentType);
                    if (null != foundIcidentType) {
                        existingRecords.remove(foundIcidentType);
                        db.update(TABLE_INCIDENT_TYPE, values, Columns.KEY_ID + "=" + foundIcidentType.getId(), null);
                    } else {
                        db.insert(TABLE_INCIDENT_TYPE, null, values);
                    }
                }
                if (null != existingRecords && 0 < existingRecords.size()) {
                    for (IncidentType incidentType : existingRecords) {
                        incidentType.setStatusCode(2);
                        ContentValues values = SqliteDataMapper.setIncidentTypeContent(incidentType);
                        db.update(TABLE_INCIDENT_TYPE, values, Columns.KEY_ID + "=" + incidentType.getId(), null);
                    }
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Timber.e("syncIncidentTypes ", e);
            } finally {
                db.endTransaction();
            }
        }
    }

    private List<IncidentType> setIncidentTypes(Cursor c) {
        List<IncidentType> incidentTypes = Collections.emptyList();
        try {
            if (c.moveToFirst()) {
                incidentTypes = new ArrayList<IncidentType>();
                do {
                    IncidentType incidentType = SqliteDataMapper.setIncidentType(c);
                    incidentTypes.add(incidentType);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Timber.e(LOG, e);
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return incidentTypes;
    }

    public List<IncidentType> getAllIncidentTypesByStatus(String hospitalID, int statusCode) {
        String selectQuery = "SELECT  * FROM " + TABLE_INCIDENT_TYPE + " WHERE " + Columns.KEY_HOSPITAL_ID + " = ? AND " +
                Columns.KEY_STATUS_CODE + " = ? ORDER BY " + Columns.KEY_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[]{hospitalID, String.valueOf(statusCode)});
        return setIncidentTypes(c);
    }

    public List<IncidentType> getAllIncidentTypes(String hospitalID) {
        String selectQuery = "SELECT  * FROM " + TABLE_INCIDENT_TYPE + " WHERE " + Columns.KEY_HOSPITAL_ID + " = ? ORDER BY " + Columns.KEY_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[]{hospitalID});
        return setIncidentTypes(c);
    }

    /**
     * Incident report
     **/


    public static final class EventReportKey {

        public static final String TABLE_INCIDENT_REPORT = "tbl_incident_report";
        public static final String TABLE_MEDICATION_ERR_REPORT = "tbl_medication_error_report";
        public static final String TABLE_ADVERSE_DRUGG_REACTION_REPORT = "tbl_adverse_drug_reaction";

        public static final String TABLE_PERSON_INVOLVED = "tbl_person_involved";
        public static final String TABLE_REPORTED_BY = "tbl_reported_by";

        //Incident report
        public static final String KEY_INCIDENT_NUMBER = "incident_number";
        public static final String KEY_INCIDENT_TIME = "incidentTime";
        public static final String KEY_INCIDENT_LOCATION = "incidentLocation";
        public static final String KEY_DESCRIPTION = "description";
        public static final String KEY_CORRECTIVE_ACTION = "correctiveAction";
        public static final String KEY_REPORTED_BY_REF = "reported_by";
        public static final String KEY_PERSON_INVOLVED_REF = "person_involved";

        public static final String KEY_EVENT_REPORT_REF = "report_ref";

        //Person involved
        public static final String KEY_PERSONNEL_TYPE_CODE = "personnelTypeCode";
        public static final String KEY_PERSON_NAME = "person_name";
        public static final String KEY_DESIGNATION = "designation";
        public static final String KEY_STAFF_ID = "staff_id";
        public static final String KEY_HOSPITAL_NUMBER = "hospitalNumber";
        public static final String KEY_PERSON_DOB = "dob_individual";
        public static final String KEY_PATIENT_TYPE = "patientTypeCode";
        public static final String KEY_HEIGHT = "height";
        public static final String KEY_WEIGHT = "weight";
        public static final String KEY_CONSUTANT = "consultant_name";
        public static final String KEY_DIAGNOSIS = "diagnosis";
        public static final String KEY_GENDER = "gender_code";

        //Reproted By
        public static final String KEY_FNAME = "first_name";
        public static final String KEY_LNAME = "last_name";
        public static final String KEY_REPORTED_ON = "reported_on";
        public static final String KEY_DEPARTMENT = "department";

    }

    public static final class IncidentReportKey {
        public static final String KEY_INCIDENT_TYPE_REF = "incidentTypeRef";
        public static final String KEY_INCIDENT_LEVEL = "incident_level";
        public static final String KEY_MEDICAL_REPORT = "medical_report";
    }


    public static final String CREATE_TABLE_PERSON_INVOLVED = "CREATE TABLE IF NOT EXISTS " + EventReportKey.TABLE_PERSON_INVOLVED
            + " (" + Columns.KEY_ID + " INTEGER PRIMARY KEY, " + EventReportKey.KEY_PERSON_NAME + " TEXT NOT NULL, "
            + EventReportKey.KEY_PERSONNEL_TYPE_CODE + " INTEGER NOT NULL, " + EventReportKey.KEY_DESIGNATION + " TEXT, "
            + EventReportKey.KEY_STAFF_ID + " TEXT, " + EventReportKey.KEY_HOSPITAL_NUMBER + " TEXT, " + EventReportKey.KEY_PERSON_DOB + " DATETIME,"
            + EventReportKey.KEY_PATIENT_TYPE + " INTEGER, " + EventReportKey.KEY_HEIGHT + " REAL, " + EventReportKey.KEY_WEIGHT + " REAL, "
            + EventReportKey.KEY_CONSUTANT + " TEXT, " + EventReportKey.KEY_DIAGNOSIS + " TEXT, " + EventReportKey.KEY_EVENT_REPORT_REF
            + " INTEGER, " + EventReportKey.KEY_GENDER + " INTEGER)";


    public static final String CREATE_TABLE_REPORTED_BY = "CREATE TABLE IF NOT EXISTS " + EventReportKey.TABLE_REPORTED_BY
            + " (" + Columns.KEY_ID + " INTEGER PRIMARY KEY, " + EventReportKey.KEY_LNAME + " TEXT NOT NULL, " + EventReportKey.KEY_FNAME + " TEXT, "
            + EventReportKey.KEY_DESIGNATION + " TEXT, " + EventReportKey.KEY_DEPARTMENT + " TEXT, " + EventReportKey.KEY_REPORTED_ON + " DATETIME ,"
            + EventReportKey.KEY_EVENT_REPORT_REF + " INTEGER)";


    public static final String CREATE_TABLE_INCIDENT_REPORT = "CREATE TABLE IF NOT EXISTS " + EventReportKey.TABLE_INCIDENT_REPORT
            + " (" + Columns.KEY_ID + " INTEGER PRIMARY KEY, " + Columns.KEY_SERVER_ID + " INTEGER, "
            + Columns.KEY_HOSPITAL_ID + " TEXT NOT NULL, " + EventReportKey.KEY_INCIDENT_NUMBER + "  TEXT, "
            + Columns.KEY_STATUS_CODE + " INTEGER, " + EventReportKey.KEY_INCIDENT_LOCATION + " TEXT, "
            + IncidentReportKey.KEY_INCIDENT_TYPE_REF + " INTEGER NOT NULL , " + Columns.KEY_UNIT_REF + " INTEGER NOT NULL , "
            + EventReportKey.KEY_REPORTED_BY_REF + " INTEGER REFERENCES " + EventReportKey.TABLE_REPORTED_BY + "(" + Columns.KEY_ID + "), "
            + EventReportKey.KEY_PERSON_INVOLVED_REF + " INTEGER REFERENCES " + EventReportKey.TABLE_PERSON_INVOLVED + "(" + Columns.KEY_ID + "), "
            + EventReportKey.KEY_DESCRIPTION + " TEXT, " + EventReportKey.KEY_CORRECTIVE_ACTION + " TEXT, " + EventReportKey.KEY_INCIDENT_TIME + " DATETIME,"
            + IncidentReportKey.KEY_INCIDENT_LEVEL + " INTEGER, " + IncidentReportKey.KEY_MEDICAL_REPORT + " TEXT, "
            + Columns.KEY_CREATED_ON + " DATETIME, " + Columns.KEY_UPDATED_ON + " DATETIME )";

    public List<IncidentReport> getIncidentReportForDisplayByHospital(String hospitalID, int pageNumber) throws DataAccessException {
        List<IncidentReport> reports = new ArrayList<>();
        int start = 0;
        if (pageNumber > 0) {
            start = pageNumber * 10;
        }
        String selectQuery = "SELECT DISTINCT s.id as id, s.serverId as serverId, s.hospitalID as hospitalID, " + EventReportKey.KEY_INCIDENT_NUMBER
                + ", s.unitRef, s." + IncidentReportKey.KEY_INCIDENT_TYPE_REF + ", " + EventReportKey.KEY_INCIDENT_LOCATION + ", "
                + EventReportKey.KEY_INCIDENT_LOCATION + ", " + EventReportKey.KEY_REPORTED_BY_REF + ", " + EventReportKey.KEY_LNAME + ", "
                + EventReportKey.KEY_FNAME + ", " + EventReportKey.KEY_PERSON_INVOLVED_REF + ", "
                + EventReportKey.KEY_DESCRIPTION + ", " + EventReportKey.KEY_CORRECTIVE_ACTION + ", " + EventReportKey.KEY_INCIDENT_TIME + ", "
                + IncidentReportKey.KEY_INCIDENT_LEVEL + ", " + IncidentReportKey.KEY_MEDICAL_REPORT + ", "
                + " s.status_code as status_code, s.createdOn as createdOn, s.updatedOn as updatedOn, u.name as unitName, it.name as incidentTypeName FROM " +
                EventReportKey.TABLE_INCIDENT_REPORT + " s LEFT JOIN " + TABLE_UNITS + " u ON s." + Columns.KEY_UNIT_REF + " = u." +
                Columns.KEY_UNIT_REF + " LEFT JOIN " + TABLE_INCIDENT_TYPE + " it ON s." + Columns.KEY_INCIDENTTYPE_REF + " = it." +
                Columns.KEY_INCIDENTTYPE_REF + " LEFT JOIN " + EventReportKey.TABLE_REPORTED_BY + " rep ON rep.id = " + EventReportKey.KEY_REPORTED_BY_REF + " WHERE s." + Columns.KEY_STATUS_CODE + " IN (?, ?, ?) AND s." +
                Columns.KEY_HOSPITAL_ID + " = ? ORDER BY s." + Columns.KEY_UPDATED_ON + " DESC LIMIT ?, 10";
        Log.e(LOG, selectQuery);
        Cursor c = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            c = db.rawQuery(selectQuery, new String[]{"0", "1", "2", hospitalID, String.valueOf(start)});
            if (c.moveToFirst()) {
                do {
                    reports.add(SqliteDataMapper.setIncidentReport(c));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return reports;
    }

    public long insertIncidentReport(IncidentReport report) {
        Log.e(LOG, "insertIncidentReport - " + report.toString());
        SQLiteDatabase db = this.getWritableDatabase();
        if (null != report) {
            ContentValues values = SqliteDataMapper.setIncidentReportContent(report);
            values.put(Columns.KEY_HOSPITAL_ID, report.getHospital());
            return db.insert(EventReportKey.TABLE_INCIDENT_REPORT, null, values);
        }
        return -1;
    }

    public long insertOrUpdateIncidentReport(IncidentReport report) {
        if (null == report.getId() || 0 >= report.getId()) {
            return insertIncidentReport(report);
        } else {
            Log.e(LOG, "insertOrUpdateIncidentReport - " + report.toString());
            SQLiteDatabase db = this.getWritableDatabase();
            db.beginTransaction();
            ContentValues values = SqliteDataMapper.setIncidentReportContent(report);
            try {
                int id = db.update(EventReportKey.TABLE_INCIDENT_REPORT, values, Columns.KEY_ID + "=" + report.getId(), null);
                Log.e(LOG, "Update IncidentReport result - " + id);
                db.setTransactionSuccessful();
                return id > 0 ? report.getId() : id;
            } catch (Exception e) {
                Log.e(LOG, "insertOrUpdateIncidentReport - ", e);
                Timber.e(this.getClass() + " insertOrUpdateIncidentReport -", e);
            } finally {
                db.endTransaction();
            }
            return -1;
        }
    }

    private long insertOrUpdateIncidentReportedBy(ReportedBy reportedBy, SQLiteDatabase db) {
        Log.e(LOG, "insertIncidentReportedBy - " + reportedBy.toString());
        //SQLiteDatabase db = this.getWritableDatabase();
        if (null != reportedBy) {
            ContentValues values = SqliteDataMapper.setReportedByContent(reportedBy);
            //values.put(Columns.KEY_HOSPITAL_ID, reportedBy.getHospital());
            if (null != reportedBy.getId() && 0 < reportedBy.getId()) {
                // db.beginTransaction();
                int id = db.update(EventReportKey.TABLE_REPORTED_BY, values, Columns.KEY_ID + "=" + reportedBy.getId(), null);
                Log.e(LOG, "Update insertOrUpdateIncidentReportedBy result - " + id);
                // db.setTransactionSuccessful();
                return id > 0 ? reportedBy.getId() : id;
            } else {
                return db.insert(EventReportKey.TABLE_REPORTED_BY, null, values);
            }
        }
        return -1;
    }

    public long updateIncidentReportedBy(IncidentReport report) {
        if (null == report.getId() || 0 >= report.getId()) {
            return insertIncidentReport(report);
        } else {
            Log.e(LOG, "updateIncidentReportedBy - " + report.toString());
            SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.beginTransaction();
                long reportedById = this.insertOrUpdateIncidentReportedBy(report.getReportedBy(), db);
                if (0 < reportedById) {
                    ContentValues values = SqliteDataMapper.setIncidentReportContent(report);
                    values.put(EventReportKey.KEY_REPORTED_BY_REF, reportedById);
                    int id = db.update(EventReportKey.TABLE_INCIDENT_REPORT, values, Columns.KEY_ID + "=" + report.getId(), null);
                    Log.e(LOG, "Update IncidentReport result - " + id);
                    db.setTransactionSuccessful();
                    return id > 0 ? report.getId() : id;
                }
            } catch (Exception e) {
                Log.e(LOG, "updateIncidentReportedBy - ", e);
                Timber.e(this.getClass() + " updateIncidentReportedBy -", e);
            } finally {
                db.endTransaction();
            }
            return -1;
        }
    }

    private long insertOrUpdateIncidentPersonInvolved(PersonInvolved personInvolved, SQLiteDatabase db) {
        if (null != personInvolved) {
            ContentValues values = SqliteDataMapper.setPersonInvolvedContent(personInvolved);
            //values.put(Columns.KEY_HOSPITAL_ID, reportedBy.getHospital());
            if (null != personInvolved.getId() && 0 < personInvolved.getId()) {
                // db.beginTransaction();
                int id = db.update(EventReportKey.TABLE_PERSON_INVOLVED, values, Columns.KEY_ID + "=" + personInvolved.getId(), null);
                Log.e(LOG, "Update insertOrUpdateIncidentPersonInvolved result - " + id);
                //db.setTransactionSuccessful();
                return id > 0 ? personInvolved.getId() : id;
            } else {
                return db.insert(EventReportKey.TABLE_PERSON_INVOLVED, null, values);
            }

        }
        return -1;
    }

    public long updateIncidentPersonInvolved(IncidentReport report) {
        if (null == report.getId() || 0 >= report.getId()) {
            return insertIncidentReport(report);
        } else {
            Log.e(LOG, "updateIncidentPersonInvolved - " + report.toString());
            SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.beginTransaction();
                long personInvolvedId = this.insertOrUpdateIncidentPersonInvolved(report.getPersonInvolved(), db);
                if (0 < personInvolvedId) {
                    ContentValues values = SqliteDataMapper.setIncidentReportContent(report);
                    values.put(EventReportKey.KEY_PERSON_INVOLVED_REF, personInvolvedId);
                    int id = db.update(EventReportKey.TABLE_INCIDENT_REPORT, values, Columns.KEY_ID + "=" + report.getId(), null);
                    Log.e(LOG, "Update updateIncidentPersonInvolved result - " + id);
                    db.setTransactionSuccessful();
                    return id > 0 ? report.getId() : id;
                }
            } catch (Exception e) {
                Log.e(LOG, "updateIncidentPersonInvolved - ", e);
                Timber.e(this.getClass() + " updateIncidentPersonInvolved -", e);
            } finally {
                db.endTransaction();
            }
            return -1;
        }
    }

    public PersonInvolved getPersonInvolvedById(Long personRef) {
        if (null == personRef || 0 >= personRef) {
            return null;
        }
        String selectQuery = "SELECT * FROM " + EventReportKey.TABLE_PERSON_INVOLVED + " s WHERE s." + Columns.KEY_ID + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[]{String.valueOf(personRef)});
        // looping through all rows and adding to list
        PersonInvolved personInvolved = new PersonInvolved();
        try {
            if (c.moveToFirst()) {
                do {
                    personInvolved =  SqliteDataMapper.setPersonInvolved(c);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Timber.e(LOG, e);
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return personInvolved;
    }

    public ReportedBy getReproteeByID(Long reporteeRef) {
        if (null == reporteeRef || 0 >= reporteeRef) {
            return null;
        }
        String selectQuery = "SELECT * FROM " + EventReportKey.TABLE_REPORTED_BY + " s WHERE s." + Columns.KEY_ID + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[]{String.valueOf(reporteeRef)});
        // looping through all rows and adding to list
        try {
            if (c.moveToFirst()) {
                do {
                    return SqliteDataMapper.setReportedBy(c);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Timber.e(LOG, e);
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return null;
    }


    public IncidentReport getIncidentReportById(Long reportRef) {
        String selectQuery = "SELECT DISTINCT s.id as id, s.serverId as serverId, s.hospitalID as hospitalID, " + EventReportKey.KEY_INCIDENT_NUMBER
                + ", s.unitRef, s." + IncidentReportKey.KEY_INCIDENT_TYPE_REF + ", " + EventReportKey.KEY_INCIDENT_LOCATION + ", "
                + EventReportKey.KEY_INCIDENT_LOCATION + ", " + EventReportKey.KEY_REPORTED_BY_REF + ", " + EventReportKey.KEY_LNAME + ", "
                + EventReportKey.KEY_FNAME + ", " + EventReportKey.KEY_PERSON_INVOLVED_REF + ", "
                + EventReportKey.KEY_DESCRIPTION + ", " + EventReportKey.KEY_CORRECTIVE_ACTION + ", " + EventReportKey.KEY_INCIDENT_TIME + ", "
                + IncidentReportKey.KEY_INCIDENT_LEVEL + ", " + IncidentReportKey.KEY_MEDICAL_REPORT + ", "
                + " s.status_code as status_code, s.createdOn as createdOn, s.updatedOn as updatedOn, u.name as unitName, it.name as incidentTypeName FROM " +
                EventReportKey.TABLE_INCIDENT_REPORT + " s LEFT JOIN " + TABLE_UNITS + " u ON s." + Columns.KEY_UNIT_REF + " = u." +
                Columns.KEY_UNIT_REF + " LEFT JOIN " + TABLE_INCIDENT_TYPE + " it ON s." + Columns.KEY_INCIDENTTYPE_REF + " = it." +
                Columns.KEY_INCIDENTTYPE_REF + " LEFT JOIN " + EventReportKey.TABLE_REPORTED_BY + " rep ON rep.id = "
                + EventReportKey.KEY_REPORTED_BY_REF + " WHERE s." + Columns.KEY_ID + " = ?";


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[]{String.valueOf(reportRef)});
        // looping through all rows and adding to list
        try {
            if (c.moveToFirst()) {
                do {
                    return SqliteDataMapper.setIncidentReport(c);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Timber.e(LOG, e);
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return null;
    }

    public List<IncidentReport> getFullyLoadedIncidentReportsByStatus(Integer statusCode, int pageNumber, int size,String hospitalID) throws DataAccessException {
        StringBuilder sb = new StringBuilder("SELECT DISTINCT s.id as id, s.serverId as serverId, s.hospitalID as hospitalID, s.incident_number, "
                + "s.unitRef, s." + IncidentReportKey.KEY_INCIDENT_TYPE_REF + ", " + EventReportKey.KEY_INCIDENT_LOCATION + ", "
                + EventReportKey.KEY_INCIDENT_LOCATION + ", " + EventReportKey.KEY_REPORTED_BY_REF + ", " + EventReportKey.KEY_LNAME + ", "
                + EventReportKey.KEY_FNAME + ", " + EventReportKey.KEY_PERSON_INVOLVED_REF + ", "
                + EventReportKey.KEY_DESCRIPTION + ", " + EventReportKey.KEY_CORRECTIVE_ACTION + ", " + EventReportKey.KEY_INCIDENT_TIME + ", "
                + IncidentReportKey.KEY_INCIDENT_LEVEL + ", " + IncidentReportKey.KEY_MEDICAL_REPORT + ", "
                + " s.status_code as status_code, s.createdOn as createdOn, s.updatedOn as updatedOn, u.name as unitName, it.name as incidentTypeName FROM " +
                EventReportKey.TABLE_INCIDENT_REPORT + " s JOIN " + TABLE_UNITS + " u ON s." + Columns.KEY_UNIT_REF + " = u." +
                Columns.KEY_UNIT_REF + " JOIN " + TABLE_INCIDENT_TYPE + " it ON s." + Columns.KEY_INCIDENTTYPE_REF + " = it." +
                Columns.KEY_INCIDENTTYPE_REF + " JOIN " + EventReportKey.TABLE_REPORTED_BY + " rep ON rep.id = "
                + EventReportKey.KEY_REPORTED_BY_REF + " WHERE (s.serverId IS NULL or s.serverId <= 0) AND s." + Columns.KEY_STATUS_CODE +
                " = ? AND s."+Columns.KEY_HOSPITAL_ID+" = ? ORDER BY s." + EventReportKey.KEY_INCIDENT_TIME + " DESC");
        List<IncidentReport> reports = new ArrayList<>();
        int start = 0;
        String[] params;
        if (0 < size) {
            sb.append(" LIMIT ?, ?");
            if (pageNumber > 0) {
                start = pageNumber * size;
            }
            params = new String[]{String.valueOf(statusCode),hospitalID, String.valueOf(start), String.valueOf(size)};
        } else {
            params = new String[]{String.valueOf(statusCode),hospitalID};
        }
        Cursor c = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Log.e("QUERY", sb.toString());
            c = db.rawQuery(sb.toString(), params);
            // looping through all rows and adding to list
            if (c.moveToFirst()) {
                do {
                    IncidentReport report = SqliteDataMapper.setIncidentReport(c);
                    report.setUnit(report.getUnitRef());
                    report.setIncidentType(report.getIncidentTypeRef());
                    report.setPersonInvolved(getPersonInvolvedById(report.getPersonInvolvedRef()));
                    report.setReportedBy(getReproteeByID(report.getReportedByRef()));
                    reports.add(report);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        if (reports!=null){
            Log.d("Fetched reports",ListViewer.view(reports));

        }
        return reports;
    }


    public int completeIncidentReport(IncidentReport report) {
        ContentValues values = new ContentValues();
        values.put(Columns.KEY_STATUS_CODE, report.getStatusCode());
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            int result = -1;
            if (null != report && null != report.getId() && 0 < report.getId()) {
                // Updating profile picture url for user with that userName
                result = db.update(EventReportKey.TABLE_INCIDENT_REPORT, values, Columns.KEY_ID + " = ?",
                        new String[]{String.valueOf(report.getId())});
                if (null != report.getPersonInvolved()) {
                    //this.savePersonInvolved(report.getPersonInvolved());
                }
                if (null != report.getReportedBy()) {
                    //this.saveReportedBy(report.getReportedBy());
                }
                db.setTransactionSuccessful();
            }
            return result;
        } catch (Exception e) {
            Timber.e(this.getClass() + " completeIncidentReport -", e);
        } finally {
            db.endTransaction();
        }
        return -1;
    }

    public int updateIncidentReportStatus(Long auditId, Long serverId, int statusCode) {
        ContentValues values = new ContentValues();
        values.put(Columns.KEY_STATUS_CODE, statusCode);
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            int result = -1;
            result = db.update(EventReportKey.TABLE_INCIDENT_REPORT, values, Columns.KEY_ID + " = ? AND " + Columns.KEY_SERVER_ID + " =?",
                    new String[]{String.valueOf(auditId), String.valueOf(serverId)});
            db.setTransactionSuccessful();
            return result;
        } catch (Exception e) {
            Timber.e(this.getClass() + " updateIncidentReportStatus -", e);
        } finally {
            db.endTransaction();
        }
        return -1;
    }

    public void deleteIncidentReportById(Long reportRef) {
        IncidentReport report = getIncidentReportById(reportRef);
        if (report != null){
            String deleteQry = "DELETE FROM " + EventReportKey.TABLE_INCIDENT_REPORT + " WHERE " + Columns.KEY_ID + " =?";
            String deleteReproteeQry = "DELETE FROM " + EventReportKey.TABLE_REPORTED_BY + " WHERE " +
                    Columns.KEY_ID + " =?";
            String deletePersonInvolvedQry = "DELETE FROM " + EventReportKey.TABLE_PERSON_INVOLVED + " WHERE " +
                    Columns.KEY_ID + " =?";
            SQLiteDatabase db = this.getWritableDatabase();
            db.beginTransaction();
            try {
                Log.e(LOG, "deleteIncidentReportById - " + deleteQry);
                db.execSQL(deleteReproteeQry, new String[]{String.valueOf(report.getReportedByRef())});
                db.execSQL(deletePersonInvolvedQry, new String[]{String.valueOf(report.getPersonInvolvedRef())});
                db.execSQL(deleteQry, new String[]{String.valueOf(reportRef)});
                db.setTransactionSuccessful();
            } catch (Throwable e) {
                Log.e(LOG, "deleteIncidentReportById: ", e);
            } finally {
                db.endTransaction();
            }
        }

    }

    /**
     * Medication Error Report
     **/
    public static final String CREATE_TABLE_MEDICATION_ERR_REPORT = "CREATE TABLE IF NOT EXISTS " + EventReportKey.TABLE_MEDICATION_ERR_REPORT
            + " (" + Columns.KEY_ID + " INTEGER PRIMARY KEY, " + Columns.KEY_SERVER_ID + " INTEGER, "
            + Columns.KEY_HOSPITAL_ID + " TEXT NOT NULL, " + EventReportKey.KEY_INCIDENT_NUMBER + "  TEXT, "
            + Columns.KEY_STATUS_CODE + " INTEGER, " + EventReportKey.KEY_INCIDENT_LOCATION + " TEXT, "
            + Columns.KEY_UNIT_REF + " INTEGER NOT NULL , "
            + EventReportKey.KEY_REPORTED_BY_REF + " INTEGER REFERENCES " + EventReportKey.TABLE_REPORTED_BY + "(" + Columns.KEY_ID + "), "
            + EventReportKey.KEY_PERSON_INVOLVED_REF + " INTEGER REFERENCES " + EventReportKey.TABLE_PERSON_INVOLVED + "(" + Columns.KEY_ID + "), "
            + EventReportKey.KEY_DESCRIPTION + " TEXT, " + EventReportKey.KEY_CORRECTIVE_ACTION + " TEXT, " + EventReportKey.KEY_INCIDENT_TIME + " DATETIME,"
            + IncidentReportKey.KEY_INCIDENT_LEVEL + " INTEGER, " + IncidentReportKey.KEY_MEDICAL_REPORT + " TEXT, "
            + Columns.KEY_CREATED_ON + " DATETIME, " + Columns.KEY_UPDATED_ON + " DATETIME )";


    public List<MedicationError> getMedicationErrorForDisplayByHospital(String hospitalID, int pageNumber) throws DataAccessException {
        List<MedicationError> reports = new ArrayList<>();
        int start = 0;
        if (pageNumber > 0) {
            start = pageNumber * 10;
        }
        String selectQuery = "SELECT DISTINCT s.id as id, s.serverId as serverId, s.hospitalID as hospitalID, " + EventReportKey.KEY_INCIDENT_NUMBER
                + ", s.unitRef, s." + EventReportKey.KEY_INCIDENT_LOCATION + ", "
                + EventReportKey.KEY_INCIDENT_LOCATION + ", " + EventReportKey.KEY_REPORTED_BY_REF + ", " + EventReportKey.KEY_LNAME + ", "
                + EventReportKey.KEY_FNAME + ", " + EventReportKey.KEY_PERSON_INVOLVED_REF + ", "
                + EventReportKey.KEY_DESCRIPTION + ", " + EventReportKey.KEY_CORRECTIVE_ACTION + ", " + EventReportKey.KEY_INCIDENT_TIME + ", "
                + IncidentReportKey.KEY_INCIDENT_LEVEL + ", " + IncidentReportKey.KEY_MEDICAL_REPORT + ", "
                + " s.status_code as status_code, s.createdOn as createdOn, s.updatedOn as updatedOn, u.name as unitName FROM " +
                EventReportKey.TABLE_MEDICATION_ERR_REPORT + " s LEFT JOIN " + TABLE_UNITS + " u ON s." + Columns.KEY_UNIT_REF + " = u." +
                Columns.KEY_UNIT_REF + " LEFT JOIN " + EventReportKey.TABLE_REPORTED_BY + " rep ON rep.id = " + EventReportKey.KEY_REPORTED_BY_REF + " WHERE s." + Columns.KEY_STATUS_CODE + " IN (?, ?, ?) AND s." +
                Columns.KEY_HOSPITAL_ID + " = ? ORDER BY s." + Columns.KEY_UPDATED_ON + " DESC LIMIT ?, 10";
        Log.e(LOG, selectQuery);

        Cursor c = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            c = db.rawQuery(selectQuery, new String[]{"0", "1", "2", hospitalID, String.valueOf(start)});
            if (c.moveToFirst()) {
                do {
                    reports.add(SqliteDataMapper.setMedicationError(c));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        Log.d("Query", ListViewer.view(reports));
        return reports;
    }

    public long insertMedicationError(MedicationError report) {
        Log.e(LOG, "insertMedicationError - " + report.toString());
        SQLiteDatabase db = this.getWritableDatabase();
        if (null != report) {
            ContentValues values = SqliteDataMapper.setMedicationErrorContent(report);
            values.put(Columns.KEY_HOSPITAL_ID, report.getHospital());
            return db.insert(EventReportKey.TABLE_MEDICATION_ERR_REPORT, null, values);
        }
        return -1;
    }

    public long insertOrUpdateMedicationError(MedicationError report) {
        if (null == report.getId() || 0 >= report.getId()) {
            return insertMedicationError(report);
        } else {
            Log.e(LOG, "insertOrUpdateMedicationError - " + report.toString());
            SQLiteDatabase db = this.getWritableDatabase();
            db.beginTransaction();
            ContentValues values = SqliteDataMapper.setMedicationErrorContent(report);
            try {
                int id = db.update(EventReportKey.TABLE_MEDICATION_ERR_REPORT, values, Columns.KEY_ID + "=" + report.getId(), null);
                Log.e(LOG, "Update AdverseDrugEvent result - " + id);
                db.setTransactionSuccessful();
                return id > 0 ? report.getId() : id;
            } catch (Exception e) {
                Log.e(LOG, "insertOrUpdateMedicationError - ", e);
                Timber.e(this.getClass() + " insertOrUpdateMedicationError -", e);
            } finally {
                db.endTransaction();
            }
            return -1;
        }
    }


    public long updateMedicationErrorReportedBy(MedicationError report) {
        if (null == report.getId() || 0 >= report.getId()) {
            return insertMedicationError(report);
        } else {
            Log.e(LOG, "updateMedicationErrorReportedBy - " + report.toString());
            SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.beginTransaction();
                long reportedById = this.insertOrUpdateIncidentReportedBy(report.getReportedBy(), db);
                if (0 < reportedById) {
                    ContentValues values = SqliteDataMapper.setMedicationErrorContent(report);
                    values.put(EventReportKey.KEY_REPORTED_BY_REF, reportedById);
                    int id = db.update(EventReportKey.TABLE_MEDICATION_ERR_REPORT, values, Columns.KEY_ID + "=" + report.getId(), null);
                    Log.e(LOG, "Update AdverseDrugEvent result - " + id);
                    db.setTransactionSuccessful();
                    return id > 0 ? report.getId() : id;
                }
            } catch (Exception e) {
                Log.e(LOG, "updateMedicationErrorReportedBy - ", e);
                Timber.e(this.getClass() + " updateMedicationErrorReportedBy -", e);
            } finally {
                db.endTransaction();
            }
            return -1;
        }
    }


    public long updateMedicationErrorPersonInvolved(MedicationError report) {
        if (null == report.getId() || 0 >= report.getId()) {
            return insertMedicationError(report);
        } else {
            Log.e(LOG, "updateMedicationErrorPersonInvolved - " + report.toString());
            SQLiteDatabase db = this.getWritableDatabase();
            try {
                db.beginTransaction();
                long personInvolvedId = this.insertOrUpdateIncidentPersonInvolved(report.getPersonInvolved(), db);
                if (0 < personInvolvedId) {
                    ContentValues values = SqliteDataMapper.setMedicationErrorContent(report);
                    values.put(EventReportKey.KEY_PERSON_INVOLVED_REF, personInvolvedId);
                    int id = db.update(EventReportKey.TABLE_MEDICATION_ERR_REPORT, values, Columns.KEY_ID + "=" + report.getId(), null);
                    Log.e(LOG, "Update updateMedicationErrorPersonInvolved result - " + id);
                    db.setTransactionSuccessful();
                    return id > 0 ? report.getId() : id;
                }
            } catch (Exception e) {
                Log.e(LOG, "updateMedicationErrorPersonInvolved - ", e);
                Timber.e(this.getClass() + " updateMedicationErrorPersonInvolved -", e);
            } finally {
                db.endTransaction();
            }
            return -1;
        }
    }

    public MedicationError getMedicationErrorById(Long reportRef) {
        String selectQuery = "SELECT DISTINCT s.id as id, s.serverId as serverId, s.hospitalID as hospitalID, " + EventReportKey.KEY_INCIDENT_NUMBER
                + ", s.unitRef, " + EventReportKey.KEY_INCIDENT_LOCATION + ", " + EventReportKey.KEY_INCIDENT_LOCATION
                + ", " + EventReportKey.KEY_REPORTED_BY_REF + ", " + EventReportKey.KEY_LNAME + ", "
                + EventReportKey.KEY_FNAME + ", " + EventReportKey.KEY_PERSON_INVOLVED_REF + ", "
                + EventReportKey.KEY_DESCRIPTION + ", " + EventReportKey.KEY_CORRECTIVE_ACTION + ", " + EventReportKey.KEY_INCIDENT_TIME + ", "
                + IncidentReportKey.KEY_INCIDENT_LEVEL + ", " + IncidentReportKey.KEY_MEDICAL_REPORT + ", "
                + " s.status_code as status_code, s.createdOn as createdOn, s.updatedOn as updatedOn, u.name as unitName FROM " +
                EventReportKey.TABLE_MEDICATION_ERR_REPORT + " s JOIN " + TABLE_UNITS + " u ON s." + Columns.KEY_UNIT_REF + " = u." +
                Columns.KEY_UNIT_REF + " LEFT JOIN " + EventReportKey.TABLE_REPORTED_BY + " rep ON rep.id = "
                + EventReportKey.KEY_REPORTED_BY_REF + " WHERE s." + Columns.KEY_ID + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Log.e("QUERY",selectQuery);
        Cursor c = db.rawQuery(selectQuery, new String[]{String.valueOf(reportRef)});
        // looping through all rows and adding to list
        try {
            if (c.moveToFirst()) {
                do {
                    return SqliteDataMapper.setMedicationError(c);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Timber.e(LOG, e);
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return null;
    }

    public List<MedicationError> getFullyLoadedMedicationErrorsByStatus(Integer statusCode, int pageNumber, int size,String hospitalID) throws DataAccessException {
        StringBuilder sb = new StringBuilder("SELECT DISTINCT s.id as id, s.serverId as serverId, s.hospitalID as hospitalID, "
                + " s.incident_number, s.unitRef, " + EventReportKey.KEY_INCIDENT_LOCATION + ", "
                + EventReportKey.KEY_INCIDENT_LOCATION + ", " + EventReportKey.KEY_REPORTED_BY_REF + ", " + EventReportKey.KEY_LNAME + ", "
                + EventReportKey.KEY_FNAME + ", " + EventReportKey.KEY_PERSON_INVOLVED_REF + ", "
                + EventReportKey.KEY_DESCRIPTION + ", " + EventReportKey.KEY_CORRECTIVE_ACTION + ", " + EventReportKey.KEY_INCIDENT_TIME + ", "
                + IncidentReportKey.KEY_INCIDENT_LEVEL + ", " + IncidentReportKey.KEY_MEDICAL_REPORT + ", "
                + " s.status_code as status_code, s.createdOn as createdOn, s.updatedOn as updatedOn, u.name as unitName FROM " +
                EventReportKey.TABLE_MEDICATION_ERR_REPORT + " s JOIN " + TABLE_UNITS + " u ON s." + Columns.KEY_UNIT_REF + " = u." +
                Columns.KEY_UNIT_REF + " JOIN " + EventReportKey.TABLE_REPORTED_BY + " rep ON rep.id = "
                + EventReportKey.KEY_REPORTED_BY_REF + " WHERE (s.serverId IS NULL or s.serverId <= 0) AND s." + Columns.KEY_STATUS_CODE +
                " = ? AND s."+Columns.KEY_HOSPITAL_ID+" = ? ORDER BY s." + EventReportKey.KEY_INCIDENT_TIME + " DESC");
        List<MedicationError> reports = new ArrayList<>();
        int start = 0;
        String[] params;
        if (0 < size) {
            sb.append(" LIMIT ?, ?");
            if (pageNumber > 0) {
                start = pageNumber * size;
            }
            params = new String[]{String.valueOf(statusCode), hospitalID, String.valueOf(start), String.valueOf(size)};
        } else {
            params = new String[]{String.valueOf(statusCode),hospitalID};
        }
        Cursor c = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Log.e("QUERY", sb.toString());
            c = db.rawQuery(sb.toString(), params);
            // looping through all rows and adding to list
            if (c.moveToFirst()) {
                do {
                    MedicationError report = SqliteDataMapper.setMedicationError(c);
                    report.setUnit(report.getUnitRef());
                    report.setPersonInvolved(getPersonInvolvedById(report.getPersonInvolvedRef()));
                    report.setReportedBy(getReproteeByID(report.getReportedByRef()));
                    reports.add(report);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return reports;
    }


    public int completeMedicationError(MedicationError report) {
        ContentValues values = new ContentValues();
        values.put(Columns.KEY_STATUS_CODE, report.getStatusCode());
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            int result = -1;
            if (null != report && null != report.getId() && 0 < report.getId()) {
                // Updating profile picture url for user with that userName
                result = db.update(EventReportKey.TABLE_MEDICATION_ERR_REPORT, values, Columns.KEY_ID + " = ?",
                        new String[]{String.valueOf(report.getId())});
                if (null != report.getPersonInvolved()) {
                    //this.savePersonInvolved(report.getPersonInvolved());
                }
                if (null != report.getReportedBy()) {
                    //this.saveReportedBy(report.getReportedBy());
                }
                db.setTransactionSuccessful();
            }
            return result;
        } catch (Exception e) {
            Timber.e(this.getClass() + " completeMedicationError -", e);
        } finally {
            db.endTransaction();
        }
        return -1;
    }

    public int updateMedicationErrorStatus(Long auditId, Long serverId, int statusCode) {
        ContentValues values = new ContentValues();
        values.put(Columns.KEY_STATUS_CODE, statusCode);
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            int result = -1;
            result = db.update(EventReportKey.TABLE_MEDICATION_ERR_REPORT, values, Columns.KEY_ID + " = ? AND " + Columns.KEY_SERVER_ID + " =?",
                    new String[]{String.valueOf(auditId), String.valueOf(serverId)});
            db.setTransactionSuccessful();
            return result;
        } catch (Exception e) {
            Timber.e(this.getClass() + " updateMedicationErrorStatus -", e);
        } finally {
            db.endTransaction();
        }
        return -1;
    }

    public void deleteMedicationErrorById(Long reportRef) {
        MedicationError report = getMedicationErrorById(reportRef);
        if (report != null){
            String deleteQry = "DELETE FROM " + EventReportKey.TABLE_MEDICATION_ERR_REPORT + " WHERE " + Columns.KEY_ID + " =?";
            String deleteReproteeQry = "DELETE FROM " + EventReportKey.TABLE_REPORTED_BY + " WHERE " +
                    Columns.KEY_ID + " =?";
            String deletePersonInvolvedQry = "DELETE FROM " + EventReportKey.TABLE_PERSON_INVOLVED + " WHERE " +
                    Columns.KEY_ID + " =?";
            SQLiteDatabase db = this.getWritableDatabase();
            db.beginTransaction();
            try {
                Log.e(LOG, "deleteMedicationErrorById - " + deleteQry);
                db.execSQL(deleteReproteeQry, new String[]{String.valueOf(report.getReportedByRef())});
                db.execSQL(deletePersonInvolvedQry, new String[]{String.valueOf(report.getPersonInvolvedRef())});
                db.execSQL(deleteQry, new String[]{String.valueOf(reportRef)});
                db.setTransactionSuccessful();
            } catch (Throwable e) {
                Log.e(LOG, "deleteMedicationErrorById: ", e);
            } finally {
                db.endTransaction();
            }
        }

    }

    /**
     * Adverse Drug reaction Report
     **/

    public static final class DrugReactionKey {
        public static final String KEY_REACTION_DATE = "reactionDate";
        public static final String KEY_REACTION_OUTCOME_CODE = "actionOutcomeCode";
        public static final String KEY_DATE_RECOVERY = "dateOfRecovery";
        public static final String KEY_DATE_DEATH = "dateOfDeath";
        public static final String KEY_ADMITTED_POST_REACTION = "admittedPostReaction";
        public static final String KEY_REACTION_ADDED_CASESHEET = "reactionAddedToCasesheet";
        public static final String KEY_COMMENTS = "comments";

        //Drug Info
        public static final String TABLE_DRUG_INFO = "tbl_drug_info";

        public static final String KEY_DRUG = "drug";
        public static final String KEY_DOSE = "dose";
        public static final String KEY_FREQUENCY = "frequency";
        public static final String KEY_ROUTE = "route";
        public static final String KEY_DATE_STARTED = "dateStarted";
        public static final String KEY_DATE_CEASED = "dateCeased";
        public static final String KEY_SUSPECTED_DRUG = "suspected_drug_flag";



    }

    public static final String CREATE_TABLE_DRUG_INFO = "CREATE TABLE IF NOT EXISTS " + DrugReactionKey.TABLE_DRUG_INFO
            + " (" + Columns.KEY_ID + " INTEGER PRIMARY KEY, " + DrugReactionKey.KEY_DRUG + " TEXT, " + DrugReactionKey.KEY_DOSE + " TEXT, "
            + DrugReactionKey.KEY_FREQUENCY + " TEXT, " + DrugReactionKey.KEY_ROUTE + " TEXT, " + DrugReactionKey.KEY_DATE_STARTED + " DATETIME, "
            + DrugReactionKey.KEY_DATE_CEASED + " DATETIME, "+ DrugReactionKey.KEY_SUSPECTED_DRUG + " INTEGER, "
            + EventReportKey.KEY_EVENT_REPORT_REF + " INTEGER);";

    public static final String CREATE_TABLE_ADVERSE_DRUGG_REACTION_REPORT = "CREATE TABLE IF NOT EXISTS "
            + EventReportKey.TABLE_ADVERSE_DRUGG_REACTION_REPORT
            + " (" + Columns.KEY_ID + " INTEGER PRIMARY KEY, " + Columns.KEY_SERVER_ID + " INTEGER, "
            + Columns.KEY_HOSPITAL_ID + " TEXT NOT NULL, " + EventReportKey.KEY_INCIDENT_NUMBER + "  TEXT, "
            + Columns.KEY_STATUS_CODE + " INTEGER, " + EventReportKey.KEY_INCIDENT_LOCATION + " TEXT, "
            + Columns.KEY_UNIT_REF + " INTEGER, "
            + EventReportKey.KEY_REPORTED_BY_REF + " INTEGER REFERENCES " + EventReportKey.TABLE_REPORTED_BY + "(" + Columns.KEY_ID + "), "
            + EventReportKey.KEY_PERSON_INVOLVED_REF + " INTEGER REFERENCES " + EventReportKey.TABLE_PERSON_INVOLVED + "(" + Columns.KEY_ID + "), "

            + EventReportKey.KEY_DESCRIPTION + " TEXT, " + EventReportKey.KEY_CORRECTIVE_ACTION + " TEXT, "
            + EventReportKey.KEY_INCIDENT_TIME + " DATETIME,"  + DrugReactionKey.KEY_COMMENTS + " TEXT,"
            + DrugReactionKey.KEY_REACTION_OUTCOME_CODE + " INTEGER, " + DrugReactionKey.KEY_REACTION_DATE + " DATETIME, "
            + DrugReactionKey.KEY_DATE_RECOVERY + " DATETIME, " + DrugReactionKey.KEY_DATE_DEATH + " DATETIME, "
            + DrugReactionKey.KEY_ADMITTED_POST_REACTION + " INTEGER, " + DrugReactionKey.KEY_REACTION_ADDED_CASESHEET + " INTEGER, "
            + Columns.KEY_CREATED_ON + " DATETIME, " + Columns.KEY_UPDATED_ON + " DATETIME )";


    public long insertDrugInfo(DrugInfo drugInfo){

        SQLiteDatabase db = this.getWritableDatabase();
        if (null != drugInfo){
            ContentValues values = new ContentValues();
            values.put(EventReportKey.KEY_EVENT_REPORT_REF, drugInfo.getEventRef());
            values.put(DrugReactionKey.KEY_DRUG, drugInfo.getDrug());
            values.put(DrugReactionKey.KEY_DOSE, drugInfo.getDose());
            values.put(DrugReactionKey.KEY_FREQUENCY, drugInfo.getFrequency());
            values.put(DrugReactionKey.KEY_ROUTE, drugInfo.getRoute());

            if (null != drugInfo.getDateStarted())
                values.put(DrugReactionKey.KEY_DATE_STARTED, drugInfo.getDateStarted().getTimeInMillis());
            if (null != drugInfo.getDateCeased())
                values.put(DrugReactionKey.KEY_DATE_CEASED, drugInfo.getDateCeased().getTimeInMillis());

            values.put(KEY_SUSPECTED_DRUG, drugInfo.isSuspectedDrug()?1:0);
           return db.insert(DrugReactionKey.TABLE_DRUG_INFO,null,values);
        }

        return -1;
    }


    public long insertOrUpdateDrugInfo(DrugInfo drugInfo) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.e(LOG, "insertOrUpdateDrugInfo - " + drugInfo.toString());
        //SQLiteDatabase db = this.getWritableDatabase();
        if (null != drugInfo) {
            ContentValues values = SqliteDataMapper.setDrugInfoContent(drugInfo);
            //values.put(Columns.KEY_HOSPITAL_ID, reportedBy.getHospital());
            if (null != drugInfo.getId() && 0 < drugInfo.getId()) {
                if (!db.inTransaction()){
                    db.beginTransaction();
                }
                 try{
                     int id = db.update(DrugReactionKey.TABLE_DRUG_INFO, values, Columns.KEY_ID + "=" + drugInfo.getId(), null);
                     Log.e(LOG, "Update insertOrUpdateDrugInfo result - " + id);
                     db.setTransactionSuccessful();
                     return id > 0 ? drugInfo.getId() : id;
                 }catch (Exception e){
                     Log.e(LOG,"Update insertOrUpdateDrugInfo result - "+e.getMessage());
                 }finally {
                     db.endTransaction();
                 }
            } else {
                return insertDrugInfo(drugInfo);
            }
        }

        return -1;
    }
    public List<DrugInfo> getDrugInfoByEventID(Long eventId) {
        if (null == eventId || 0 >= eventId) {
            return null;
        }
        String selectQuery = "SELECT * FROM " + DrugReactionKey.TABLE_DRUG_INFO + " s WHERE s." + EventReportKey.KEY_EVENT_REPORT_REF + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[]{String.valueOf(eventId)});
        List<DrugInfo> drugInfos = new ArrayList<>();
        // looping through all rows and adding to list
        try {
            if (c.moveToFirst()) {
                do {
                    drugInfos.add(SqliteDataMapper.setDrugIngo(c));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Timber.e(LOG, e);
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return drugInfos;
    }

    public List<AdverseDrugEvent> getAdverseDrugEventForDisplayByHospital(String hospitalID, int pageNumber) throws DataAccessException {
        List<AdverseDrugEvent> reports = new ArrayList<>();
        int start = 0;
        if (pageNumber > 0) {
            start = pageNumber * 10;
        }
        String selectQuery = "SELECT DISTINCT s.id as id, s.serverId as serverId, s.hospitalID as hospitalID, " + EventReportKey.KEY_INCIDENT_NUMBER
                + ", s.unitRef, s." + EventReportKey.KEY_INCIDENT_LOCATION + ", "
                + EventReportKey.KEY_INCIDENT_TIME+ ", " + EventReportKey.KEY_REPORTED_BY_REF + ", " + EventReportKey.KEY_LNAME + ", "
                + EventReportKey.KEY_FNAME + ", " + EventReportKey.KEY_PERSON_INVOLVED_REF + ", "+EventReportKey.KEY_PERSON_NAME+", "
                + EventReportKey.KEY_PATIENT_TYPE+", "+EventReportKey.KEY_HOSPITAL_NUMBER+", "
                + EventReportKey.KEY_DESCRIPTION + ", " + EventReportKey.KEY_CORRECTIVE_ACTION + ", " + EventReportKey.KEY_INCIDENT_TIME + ", "
                + DrugReactionKey.KEY_REACTION_DATE+", " +DrugReactionKey.KEY_REACTION_OUTCOME_CODE+", "
                + DrugReactionKey.KEY_DATE_RECOVERY+", "+DrugReactionKey.KEY_DATE_DEATH+", " +DrugReactionKey.KEY_COMMENTS+","
                + DrugReactionKey.KEY_ADMITTED_POST_REACTION +", "+DrugReactionKey.KEY_REACTION_ADDED_CASESHEET+", "
                + " s.status_code as status_code, s.createdOn as createdOn, s.updatedOn as updatedOn, u.name as unitName FROM " +
                EventReportKey.TABLE_ADVERSE_DRUGG_REACTION_REPORT + " s JOIN "+EventReportKey.TABLE_PERSON_INVOLVED +" p on p.id = s."+EventReportKey.KEY_PERSON_INVOLVED_REF
                +" LEFT JOIN " + TABLE_UNITS + " u ON s." + Columns.KEY_UNIT_REF + " = u." +
                Columns.KEY_UNIT_REF + " LEFT JOIN " + EventReportKey.TABLE_REPORTED_BY + " rep ON rep.id = " + EventReportKey.KEY_REPORTED_BY_REF + " WHERE s." + Columns.KEY_STATUS_CODE + " IN (?, ?, ?) AND s." +
                Columns.KEY_HOSPITAL_ID + " = ? ORDER BY s." + Columns.KEY_UPDATED_ON + " DESC LIMIT ?, 10";
        Log.e(LOG, selectQuery);
        Cursor c = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            c = db.rawQuery(selectQuery, new String[]{"0", "1", "2", hospitalID, String.valueOf(start)});
            if (c.moveToFirst()) {
                do {
                    reports.add(SqliteDataMapper.setAdverseDrugEvent(c));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return reports;
    }

    public long insertAdverseDrugReaction(AdverseDrugEvent report) {
        Log.e(LOG, "insertAdverseDrugReaction - " + report.toString());
        SQLiteDatabase db = this.getWritableDatabase();
        if (null != report) {
            ContentValues values = SqliteDataMapper.setAdverseDrugEventContent(report);
            values.put(Columns.KEY_HOSPITAL_ID, report.getHospital());
            return db.insert(EventReportKey.TABLE_ADVERSE_DRUGG_REACTION_REPORT, null, values);
        }
        return -1;
    }

    public long insertOrUpdateAdverseDrugReaction(AdverseDrugEvent report) {
        if (null == report.getId() || 0 >= report.getId()) {
            return insertAdverseDrugReaction(report);
        } else {
            Log.e(LOG, "insertOrUpdateAdverseDrugReaction - " + report.toString());
            SQLiteDatabase db = this.getWritableDatabase();
            if (!db.inTransaction()) {
                db.beginTransaction();
            }
            ContentValues values = SqliteDataMapper.setAdverseDrugEventContent(report);
            try {
                int id = db.update(EventReportKey.TABLE_ADVERSE_DRUGG_REACTION_REPORT, values, Columns.KEY_ID + "=" + report.getId(), null);
                Log.e(LOG, "Update AdverseDrugEvent result - " + id);
                db.setTransactionSuccessful();
                return id > 0 ? report.getId() : id;
            } catch (Exception e) {
                Log.e(LOG, "insertOrUpdateAdverseDrugReaction - ", e);
                Timber.e(this.getClass() + " insertOrUpdateAdverseDrugReaction -", e);
            } finally {
                db.endTransaction();
            }
            return -1;
        }
    }


    public long updateAdverseDrugEventReportedBy(AdverseDrugEvent report) {
        if (null == report.getId() || 0 >= report.getId()) {
            return insertAdverseDrugReaction(report);
        } else {
            Log.e(LOG, "updateAdverseDrugEventReportedBy - " + report.toString());
            SQLiteDatabase db = this.getWritableDatabase();
            try {
                if (!db.inTransaction()) {
                    db.beginTransaction();
                }else{
                    db.endTransaction();
                    db.beginTransaction();
                }
                long reportedById = this.insertOrUpdateIncidentReportedBy(report.getReportedBy(), db);
                if (0 < reportedById) {
                    ContentValues values = SqliteDataMapper.setAdverseDrugEventContent(report);
                    values.put(EventReportKey.KEY_REPORTED_BY_REF, reportedById);
                    int id = db.update(EventReportKey.TABLE_ADVERSE_DRUGG_REACTION_REPORT, values, Columns.KEY_ID + "=" + report.getId(), null);
                    Log.e(LOG, "Update AdverseDrugEvent result - " + id);
                    db.setTransactionSuccessful();
                    return id > 0 ? report.getId() : id;
                }
            } catch (Exception e) {
                Log.e(LOG, "updateAdverseDrugEventReportedBy - ", e);
                Timber.e(this.getClass() + " updateAdverseDrugEventReportedBy -", e);
            } finally {
                db.endTransaction();
            }
            return -1;
        }
    }


    public long updateAdverseDrugEventPersonInvolved(AdverseDrugEvent report) {
        if (null == report.getId() || 0 >= report.getId()) {
            return insertAdverseDrugReaction(report);
        } else {
            Log.e(LOG, "updateAdverseDrugEventPersonInvolved - " + report.toString());
            SQLiteDatabase db = this.getWritableDatabase();
            try {
                if (db.inTransaction()){
                    db.endTransaction();
                }
                db.beginTransaction();
                long personInvolvedId = this.insertOrUpdateIncidentPersonInvolved(report.getPersonInvolved(), db);
                if (0 < personInvolvedId) {
                    ContentValues values = SqliteDataMapper.setAdverseDrugEventContent(report);
                    values.put(EventReportKey.KEY_PERSON_INVOLVED_REF, personInvolvedId);
                    int id = db.update(EventReportKey.TABLE_ADVERSE_DRUGG_REACTION_REPORT, values, Columns.KEY_ID + "=" + report.getId(), null);
                    Log.e(LOG, "Update updateMedicationErrorPersonInvolved result - " + id);
                    db.setTransactionSuccessful();
                    return id > 0 ? report.getId() : id;
                }
            } catch (Exception e) {
                Log.e(LOG, "updateAdverseDrugEventPersonInvolved - ", e);
                Timber.e(this.getClass() + " updateAdverseDrugEventPersonInvolved -", e);
            } finally {
                db.endTransaction();
            }
            return -1;
        }
    }


    public AdverseDrugEvent getAdverseDrugEventById(Long reportRef) {
        String selectQuery = "SELECT DISTINCT s.id as id, s.serverId as serverId, s.hospitalID as hospitalID, " + EventReportKey.KEY_INCIDENT_NUMBER
                + ", s.unitRef, " + EventReportKey.KEY_INCIDENT_LOCATION + ", " + EventReportKey.KEY_INCIDENT_TIME
                + ", " + EventReportKey.KEY_REPORTED_BY_REF + ", " + EventReportKey.KEY_LNAME + ", "
                + EventReportKey.KEY_FNAME + ", " + EventReportKey.KEY_PERSON_INVOLVED_REF + ", "+EventReportKey.KEY_PERSON_NAME+", "
                + EventReportKey.KEY_PATIENT_TYPE+", "+EventReportKey.KEY_HOSPITAL_NUMBER+", "
                + EventReportKey.KEY_DESCRIPTION + ", " + EventReportKey.KEY_CORRECTIVE_ACTION + ", " + EventReportKey.KEY_INCIDENT_TIME + ", "
                + DrugReactionKey.KEY_REACTION_DATE+", " +DrugReactionKey.KEY_REACTION_OUTCOME_CODE+", "
                + DrugReactionKey.KEY_DATE_RECOVERY+", "+DrugReactionKey.KEY_DATE_DEATH+", " +DrugReactionKey.KEY_COMMENTS+","
                + DrugReactionKey.KEY_ADMITTED_POST_REACTION +", "+DrugReactionKey.KEY_REACTION_ADDED_CASESHEET+", "
                + " s.status_code as status_code, s.createdOn as createdOn, s.updatedOn as updatedOn, u.name as unitName FROM " +
                EventReportKey.TABLE_ADVERSE_DRUGG_REACTION_REPORT + " s JOIN "+EventReportKey.TABLE_PERSON_INVOLVED +" p on p.id = s."+EventReportKey.KEY_PERSON_INVOLVED_REF
                +" LEFT JOIN " + TABLE_UNITS + " u ON s." + Columns.KEY_UNIT_REF + " = u." +
                Columns.KEY_UNIT_REF + " LEFT JOIN " + EventReportKey.TABLE_REPORTED_BY + " rep ON rep.id = "
                + EventReportKey.KEY_REPORTED_BY_REF + " WHERE s." + Columns.KEY_ID + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, new String[]{String.valueOf(reportRef)});
        // looping through all rows and adding to list
        AdverseDrugEvent report = new AdverseDrugEvent();
        try {
            if (c.moveToFirst()) {
                do {
                    report = SqliteDataMapper.setAdverseDrugEvent(c);
                    report.setUnit(report.getUnitRef());
                    report.setPersonInvolved(getPersonInvolvedById(report.getPersonInvolvedRef()));
                    report.setReportedBy(getReproteeByID(report.getReportedByRef()));
                    report.setOtherDrugsTaken(getDrugInfoByEventID(report.getId()));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(LOG,e.getMessage());
            Timber.e(LOG, e);
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return report;
    }

    public List<AdverseDrugEvent> getFullyLoadedAdverseDrugEventsByStatus(Integer statusCode, int pageNumber, int size,String hospitalID) throws DataAccessException {
        StringBuilder sb = new StringBuilder("SELECT DISTINCT s.id as id, s.serverId as serverId, s.hospitalID as hospitalID, "
                + " s.incident_number, s.unitRef, " + EventReportKey.KEY_INCIDENT_LOCATION + ", "
                + EventReportKey.KEY_INCIDENT_TIME + ", " + EventReportKey.KEY_REPORTED_BY_REF + ", " + EventReportKey.KEY_LNAME + ", "
                + EventReportKey.KEY_FNAME + ", " + EventReportKey.KEY_PERSON_INVOLVED_REF + ", "
                + EventReportKey.KEY_PERSON_NAME+", "+EventReportKey.KEY_PATIENT_TYPE+", "+EventReportKey.KEY_HOSPITAL_NUMBER+", "
                + EventReportKey.KEY_DESCRIPTION + ", " + EventReportKey.KEY_CORRECTIVE_ACTION + ", " + EventReportKey.KEY_INCIDENT_TIME + ", "
                + DrugReactionKey.KEY_REACTION_DATE+", " +DrugReactionKey.KEY_REACTION_OUTCOME_CODE+", "
                + DrugReactionKey.KEY_DATE_RECOVERY+", "+DrugReactionKey.KEY_DATE_DEATH+", " +DrugReactionKey.KEY_COMMENTS+","
                + DrugReactionKey.KEY_ADMITTED_POST_REACTION +", "+DrugReactionKey.KEY_REACTION_ADDED_CASESHEET+", "
                + " s.status_code as status_code, s.createdOn as createdOn, s.updatedOn as updatedOn, u.name as unitName FROM " +
                EventReportKey.TABLE_ADVERSE_DRUGG_REACTION_REPORT +" s JOIN "+EventReportKey.TABLE_PERSON_INVOLVED+" p on p.id = s."+
                EventReportKey.KEY_PERSON_INVOLVED_REF+ " LEFT JOIN " + TABLE_UNITS + " u ON s." + Columns.KEY_UNIT_REF + " = u." +
                Columns.KEY_UNIT_REF + " JOIN " + EventReportKey.TABLE_REPORTED_BY + " rep ON rep.id = "
                + EventReportKey.KEY_REPORTED_BY_REF + " WHERE (s.serverId IS NULL or s.serverId <= 0) AND s." + Columns.KEY_STATUS_CODE +
                " = ? AND s."+Columns.KEY_HOSPITAL_ID+" = ? ORDER BY s." + EventReportKey.KEY_INCIDENT_TIME + " DESC");
        List<AdverseDrugEvent> reports = new ArrayList<>();
        int start = 0;
        String[] params;
        if (0 < size) {
            sb.append(" LIMIT ?, ?");
            if (pageNumber > 0) {
                start = pageNumber * size;
            }
            params = new String[]{String.valueOf(statusCode),hospitalID, String.valueOf(start), String.valueOf(size)};
        } else {
            params = new String[]{String.valueOf(statusCode),hospitalID};
        }
        Cursor c = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Log.e("QUERY", sb.toString());
            c = db.rawQuery(sb.toString(), params);
            // looping through all rows and adding to list
            if (c.moveToFirst()) {
                do {
                    AdverseDrugEvent report = SqliteDataMapper.setAdverseDrugEvent(c);
                    report.setUnit(report.getUnitRef());
                    report.setPersonInvolved(getPersonInvolvedById(report.getPersonInvolvedRef()));
                    report.setReportedBy(getReproteeByID(report.getReportedByRef()));
                    report.setOtherDrugsTaken(getDrugInfoByEventID(report.getId()));
                    if(null != report.getOtherDrugsTaken()){
                        Iterator<DrugInfo> drugInfoIterator = report.getOtherDrugsTaken().iterator();
                        while(drugInfoIterator.hasNext()){
                            DrugInfo drugInfo = drugInfoIterator.next();
                            if(null != drugInfo && drugInfo.isSuspectedDrug()){
                                report.setSuspectedDrug(drugInfo);
                                drugInfoIterator.remove();
                                break;
                            }
                        }
                    }
                    reports.add(report);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return reports;
    }


    public int completeAdverseDrugEvent(AdverseDrugEvent report) {
        ContentValues values = new ContentValues();
        values.put(Columns.KEY_STATUS_CODE, report.getStatusCode());
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            int result = -1;
            if (null != report && null != report.getId() && 0 < report.getId()) {
                // Updating profile picture url for user with that userName
                result = db.update(EventReportKey.TABLE_ADVERSE_DRUGG_REACTION_REPORT, values, Columns.KEY_ID + " = ?",
                        new String[]{String.valueOf(report.getId())});
                if (null != report.getPersonInvolved()) {
                    //this.savePersonInvolved(report.getPersonInvolved());
                }
                if (null != report.getReportedBy()) {
                    //this.saveReportedBy(report.getReportedBy());
                }
                db.setTransactionSuccessful();
            }
            return result;
        } catch (Exception e) {
            Timber.e(this.getClass() + " completeAdverseDrugEvent -", e);
        } finally {
            db.endTransaction();
        }
        return -1;
    }

    public int updateAdverseDrugEvent(AdverseDrugEvent report) {
        ContentValues values = new ContentValues();
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            int result = -1;
            if (null != report && null != report.getId() && 0 < report.getId()) {
                values = SqliteDataMapper.setAdverseDrugEventContent(report);
                // Updating profile picture url for user with that userName
                result = db.update(EventReportKey.TABLE_ADVERSE_DRUGG_REACTION_REPORT, values, Columns.KEY_ID + " = ?",
                        new String[]{String.valueOf(report.getId())});
                db.setTransactionSuccessful();
            }
            return result;
        } catch (Exception e) {
            Timber.e(this.getClass() + " updateAdverseDrugEvent -", e);
        } finally {
            db.endTransaction();
        }
        return -1;
    }

    public int updateAdverseDrugEventStatus(Long auditId, Long serverId, int statusCode) {
        ContentValues values = new ContentValues();
        values.put(Columns.KEY_STATUS_CODE, statusCode);
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            int result = -1;
            result = db.update(EventReportKey.TABLE_ADVERSE_DRUGG_REACTION_REPORT, values, Columns.KEY_ID + " = ? AND " + Columns.KEY_SERVER_ID + " =?",
                    new String[]{String.valueOf(auditId), String.valueOf(serverId)});
            db.setTransactionSuccessful();
            return result;
        } catch (Exception e) {
            Timber.e(this.getClass() + " updateAdverseDrugEventStatus -", e);
        } finally {
            db.endTransaction();
        }
        return -1;
    }

    public void deleteAdverseDrugEventById(Long reportRef) {
        AdverseDrugEvent report = getAdverseDrugEventById(reportRef);

        if (report != null){
            String deleteQry = "DELETE FROM " + EventReportKey.TABLE_ADVERSE_DRUGG_REACTION_REPORT + " WHERE " + Columns.KEY_ID + " =?";
            String deleteReproteeQry = "DELETE FROM " + EventReportKey.TABLE_REPORTED_BY + " WHERE " +
                    Columns.KEY_ID + " =?";
            String deletePersonInvolvedQry = "DELETE FROM " + EventReportKey.TABLE_PERSON_INVOLVED + " WHERE " +
                    Columns.KEY_ID + " =?";
            String deleteDrugIngoQry = "DELETE FROM " + DrugReactionKey.TABLE_DRUG_INFO + " WHERE " +
                    Columns.KEY_ID + " =?";
            SQLiteDatabase db = this.getWritableDatabase();
            db.beginTransaction();
            try {
                Log.e(LOG, "deleteAdverseDrugEventById - " + deleteQry);
                db.execSQL(deleteReproteeQry, new String[]{String.valueOf(report.getReportedByRef())});
                db.execSQL(deletePersonInvolvedQry, new String[]{String.valueOf(report.getPersonInvolvedRef())});
                db.execSQL(deleteDrugIngoQry, new String[]{String.valueOf(reportRef)});
                db.execSQL(deleteQry, new String[]{String.valueOf(reportRef)});
                db.setTransactionSuccessful();
            } catch (Throwable e) {
                Log.e(LOG, "deleteAdverseDrugEventById: ", e);
            } finally {
                db.endTransaction();
            }


        }

    }

    private List<Long> getUniqueReportIds(StringBuilder sb,String[] params) throws DataAccessException {
        Cursor c = null;
        List<Long> selectedIds = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Log.e("QUERY", sb.toString());
            c = db.rawQuery(sb.toString(), params);
            // looping through all rows and adding to list
            if (c.moveToFirst()) {
                do {
                    Long id = c.getLong(c.getColumnIndex(Columns.KEY_ID));
                    selectedIds.add(id);
                } while (c.moveToNext());

            }
        }catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
       return selectedIds;
    }


    public boolean deleteIncidentReportsBeforeDate(Calendar date) throws DataAccessException {
        Long upperDate = date.getTimeInMillis();
        StringBuilder sb = new StringBuilder("SELECT DISTINCT "+Columns.KEY_ID+" FROM " +
                EventReportKey.TABLE_INCIDENT_REPORT + " WHERE " + Columns.KEY_UPDATED_ON +
                " <?");
        String[] params = new String[]{String.valueOf(upperDate)};
        List<Long> selectedIds = getUniqueReportIds(sb, params);
        //Delete all selected reports

        try {
            for (Long id : selectedIds) {
                deleteIncidentReportById(id);
            }
            return true;
        } catch (Exception e) {
            Log.e("deleteIncidentReports", e.getMessage());
            return false;
        }
    }

    public boolean deleteAdverseDrugReportsBeforeDate(Calendar date) throws DataAccessException {
        Long upperDate = date.getTimeInMillis();
        StringBuilder sb = new StringBuilder("SELECT DISTINCT "+Columns.KEY_ID+" FROM " +
                EventReportKey.TABLE_ADVERSE_DRUGG_REACTION_REPORT + " WHERE " + Columns.KEY_UPDATED_ON +
                " <?");
        String[] params = new String[]{String.valueOf(upperDate)};
        List<Long> selectedIds = getUniqueReportIds(sb, params);
        //Delete all selected reports

        try {
            for (Long id : selectedIds) {
                deleteAdverseDrugEventById(id);
            }
            return true;
        } catch (Exception e) {
            Log.e("deleteAdverseDgReports", e.getMessage());
            return false;
        }
    }

    public boolean deleteMedicalErrorReportsBeforeDate(Calendar date) throws DataAccessException {
        Long upperDate = date.getTimeInMillis();
        StringBuilder sb = new StringBuilder("SELECT DISTINCT "+Columns.KEY_ID+" FROM " +
                EventReportKey.TABLE_MEDICATION_ERR_REPORT + " WHERE " + Columns.KEY_UPDATED_ON +
                " <?");
        String[] params = new String[]{String.valueOf(upperDate)};
        List<Long> selectedIds = getUniqueReportIds(sb, params);
        //Delete all selected reports

        try {
            for (Long id : selectedIds) {
                deleteMedicationErrorById(id);
            }
            return true;
        } catch (Exception e) {
            Log.e("deleteMediErrReports", e.getMessage());
            return false;
        }
    }

}
