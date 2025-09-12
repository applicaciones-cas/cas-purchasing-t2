/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.purchasing.t2.model;

import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Category;
import org.guanzon.cas.parameter.model.Model_Category_Level2;
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.model.Model_Department;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.purchasing.t2.status.POQuotationRequestStatus;

/**
 *
 * @author Arsiela
 */
public class Model_PO_Quotation_Request_Master extends Model {
    String psSearchDepartment = "";
    String psSearchBranch = "";
    String psSearchCategory = "";
    
    Model_Branch poBranch;
    Model_Branch poDestination;
    Model_Industry poIndustry;
    Model_Company poCompany;
    Model_Department poDepartment;
    Model_Category poCategory;
    Model_Category_Level2 poCategory2;
    
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

//            poEntity.updateObject("dModified", SQLUtil.toDate("1900-01-01", SQLUtil.FORMAT_SHORT_DATE));

            //assign default values
            poEntity.updateNull("dTransact");
            poEntity.updateNull("dExpPurch");
            poEntity.updateNull("dPrepared");
            poEntity.updateNull("dModified");
            poEntity.updateObject("nEntryNox", 0);
//            poEntity.updateString("cProcessd", "0");
            poEntity.updateString("cTranStat", POQuotationRequestStatus.OPEN);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sTransNox";

            //initialize reference objects
            ParamModels model = new ParamModels(poGRider);
            poBranch = model.Branch();
            poDestination = model.Branch(); 
            poIndustry = model.Industry();
            poCompany = model.Company();
            poDepartment = model.Department();
            poCategory2 = model.Category2();
            poCategory = model.Category(); 
//            end - initialize reference objects

            pnEditMode = EditMode.UNKNOWN;
        } catch (SQLException e) {
            logwrapr.severe(e.getMessage());
            System.exit(1);
        }
    }

    public JSONObject setTransactionNo(String transactionNo) {
        return setValue("sTransNox", transactionNo);
    }

    public String getTransactionNo() {
        return (String) getValue("sTransNox");
    }

    public JSONObject setIndustryId(String industryId) {
        return setValue("sIndstCdx", industryId);
    }

    public String getIndustryId() {
        return (String) getValue("sIndstCdx");
    }

    public JSONObject setCategoryCode(String categoryCode) {
        return setValue("sCategrCd", categoryCode);
    }

    public String getCategoryCode() {
        return (String) getValue("sCategrCd");
    }

    public JSONObject setCategoryLevel2(String categoryCode) {
        return setValue("sCategCd2", categoryCode);
    }

    public String getCategoryLevel2() {
        return (String) getValue("sCategCd2");
    }

    public JSONObject setBranchCode(String branchCode) {
        return setValue("sBranchCd", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCd");
    }

    public JSONObject setDepartmentId(String departmentId) {
        return setValue("sDeptIDxx", departmentId);
    }

    public String getDepartmentId() {
        return (String) getValue("sDeptIDxx");
    }
    
    public JSONObject setTransactionDate(Date transactionDate) {
        return setValue("dTransact", transactionDate);
    }

    public Date getTransactionDate() {
        return (Date) getValue("dTransact");
    }

    public JSONObject setDestination(String destination) {
        return setValue("sDestinat", destination);
    }

    public String getDestination() {
        return (String) getValue("sDestinat");
    }

    public JSONObject setReferenceNo(String referenceNo) {
        return setValue("sReferNox", referenceNo);
    }

    public String getReferenceNo() {
        return (String) getValue("sReferNox");
    }

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }
    
    public JSONObject setExpectedPurchaseDate(Date expectedPurchaseDate) {
        if(expectedPurchaseDate == null){
            try {
                poEntity.updateNull("dExpPurch");
                return null;
            } catch (SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
        return setValue("dExpPurch", expectedPurchaseDate);
    }

    public Date getExpectedPurchaseDate() {
        return (Date) getValue("dExpPurch");
    }

    public JSONObject setEntryNo(Number entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public Number getEntryNo() {
        return (Number) getValue("nEntryNox");
    }

    public JSONObject setTransactionStatus(String transactionStatus) {
        return setValue("cTranStat", transactionStatus);
    }

    public String getTransactionStatus() {
        return (String) getValue("cTranStat");
    }
    
    public JSONObject isProcessed(boolean isProcessed) {
        return setValue("cProcessd", isProcessed ? "1" : "0");
    }

    public boolean isProcessed() {
        return ((String) getValue("cProcessd")).equals("1");
    }

    public JSONObject setPrepared(String preparedBy) {
        return setValue("sPrepared", preparedBy);
    }

    public String getPrepared() {
        return (String) getValue("sPrepared");
    }

    public JSONObject setPreparedDate(Date preparedDate) {
        return setValue("dPrepared", preparedDate);
    }

    public Date setPreparedDate() {
        return (Date) getValue("dPrepared");
    }

    public JSONObject setModifyingId(String modifiedBy) {
        return setValue("sModified", modifiedBy);
    }

    public String getModifyingId() {
        return (String) getValue("sModified");
    }

    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    @Override
    public String getNextCode() {
//        return "";
        return MiscUtil.getNextCode(this.getTable(), ID, true, poGRider.getGConnection().getConnection(), poGRider.getBranchCode());
    }

    //reference object models
    public Model_Branch Branch() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sBranchCd"))) {
            if (poBranch.getEditMode() == EditMode.READY
                    && poBranch.getBranchCode().equals((String) getValue("sBranchCd"))) {
                return poBranch;
            } else {
                poJSON = poBranch.openRecord((String) getValue("sBranchCd"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poBranch;
                } else {
                    poBranch.initialize();
                    return poBranch;
                }
            }
        } else {
            poBranch.initialize();
            return poBranch;
        }
    }
    
    public Model_Branch Destination() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sDestinat"))) {
            if (poDestination.getEditMode() == EditMode.READY
                    && poDestination.getBranchCode().equals((String) getValue("sDestinat"))) {
                return poDestination;
            } else {
                poJSON = poDestination.openRecord((String) getValue("sDestinat"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poDestination;
                } else {
                    poDestination.initialize();
                    return poDestination;
                }
            }
        } else {
            poDestination.initialize();
            return poDestination;
        }
    }

    public Model_Industry Industry() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sIndstCdx"))) {
            if (poIndustry.getEditMode() == EditMode.READY
                    && poIndustry.getIndustryId().equals((String) getValue("sIndstCdx"))) {
                return poIndustry;
            } else {
                poJSON = poIndustry.openRecord((String) getValue("sIndstCdx"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poIndustry;
                } else {
                    poIndustry.initialize();
                    return poIndustry;
                }
            }
        } else {
            poIndustry.initialize();
            return poIndustry;
        }
    }
    
    public Model_Category Category() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sCategrCd"))) {
            if (poCategory.getEditMode() == EditMode.READY
                    && poCategory.getCategoryId().equals((String) getValue("sCategrCd"))) {
                return poCategory;
            } else {
                poJSON = poCategory.openRecord((String) getValue("sCategrCd"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poCategory;
                } else {
                    poCategory.initialize();
                    return poCategory;
                }
            }
        } else {
            poCategory.initialize();
            return poCategory;
        }
    }
    
    public Model_Category_Level2 Category2() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sCategCd2"))) {
            if (poCategory2.getEditMode() == EditMode.READY
                    && poCategory2.getCategoryId().equals((String) getValue("sCategCd2"))) {
                return poCategory2;
            } else {
                poJSON = poCategory2.openRecord((String) getValue("sCategCd2"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poCategory2;
                } else {
                    poCategory2.initialize();
                    return poCategory2;
                }
            }
        } else {
            poCategory2.initialize();
            return poCategory2;
        }
    }

    public Model_Department Department() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sDeptIDxx"))) {
            if (poDepartment.getEditMode() == EditMode.READY
                    && poDepartment.getDepartmentId().equals((String) getValue("sDeptIDxx"))) {
                return poDepartment;
            } else {
                poJSON = poDepartment.openRecord((String) getValue("sDeptIDxx"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poDepartment;
                } else {
                    poDepartment.initialize();
                    return poDepartment;
                }
            }
        } else {
            poDepartment.initialize();
            return poDepartment;
        }
    }
//    
//    /* For Searching method*/
//    public Model_Branch SearchBranch() throws SQLException, GuanzonException {
//        if (!"".equals(psSearchBranch)) {
//            if (poBranch.getEditMode() == EditMode.READY
//                    && poBranch.getBranchCode().equals(psSearchBranch)) {
//                return poBranch;
//            } else {
//                poJSON = poBranch.openRecord(psSearchBranch);
//
//                if ("success".equals((String) poJSON.get("result"))) {
//                    return poBranch;
//                } else {
//                    poBranch.initialize();
//                    return poBranch;
//                }
//            }
//        } else {
//            poBranch.initialize();
//            return poBranch;
//        }
//    }
//    
//    public Model_Category_Level2 SearchCategory2() throws GuanzonException, SQLException {
//        if (!"".equals(psSearchCategory)) {
//            if (poCategory2.getEditMode() == EditMode.READY
//                    && poCategory2.getCategoryId().equals(psSearchCategory)) {
//                return poCategory2;
//            } else {
//                poJSON = poCategory2.openRecord(psSearchCategory);
//
//                if ("success".equals((String) poJSON.get("result"))) {
//                    return poCategory2;
//                } else {
//                    poCategory2.initialize();
//                    return poCategory2;
//                }
//            }
//        } else {
//            poCategory2.initialize();
//            return poCategory2;
//        }
//    }
}
