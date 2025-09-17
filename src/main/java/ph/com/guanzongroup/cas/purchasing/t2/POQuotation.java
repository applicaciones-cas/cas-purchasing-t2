/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.purchasing.t2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.agent.ShowDialogFX;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.agent.services.Transaction;
import org.guanzon.appdriver.agent.systables.SysTableContollers;
import org.guanzon.appdriver.agent.systables.TransactionAttachment;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.base.SQLUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.appdriver.constant.RecordStatus;
import org.guanzon.appdriver.constant.UserRight;
import org.guanzon.appdriver.iface.GValidator;
import org.guanzon.cas.client.Client;
import org.guanzon.cas.client.services.ClientControllers;
import org.guanzon.cas.inv.Inventory;
import org.guanzon.cas.inv.services.InvControllers;
import org.guanzon.cas.parameter.Branch;
import org.guanzon.cas.parameter.CategoryLevel2;
import org.guanzon.cas.parameter.Company;
import org.guanzon.cas.parameter.Department;
import org.guanzon.cas.parameter.Term;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.purchasing.t2.model.Model_PO_Quotation_Detail;
import ph.com.guanzongroup.cas.purchasing.t2.model.Model_PO_Quotation_Master;
import ph.com.guanzongroup.cas.purchasing.t2.model.Model_PO_Quotation_Request_Supplier;
import ph.com.guanzongroup.cas.purchasing.t2.services.QuotationControllers;
import ph.com.guanzongroup.cas.purchasing.t2.services.QuotationModels;
import ph.com.guanzongroup.cas.purchasing.t2.status.POQuotationRequestStatus;
import ph.com.guanzongroup.cas.purchasing.t2.status.POQuotationStatus;
import ph.com.guanzongroup.cas.purchasing.t2.validator.POQuotationValidatorFactory;

/**
 *
 * @author Arsiela
 */
public class POQuotation extends Transaction {
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategorCd = "";
    
    private String psSearchBranch;
    private String psSearchDepartment;
    private String psSearchSupplier;
    private String psSearchCategory;
    
    List<Model_PO_Quotation_Request_Supplier> paPORequestSupplier;
    List<Model_PO_Quotation_Master> paMasterList;
    List<Model> paDetailRemoved;
    List<TransactionAttachment> paAttachments;
    
    POQuotationRequest poQuoationRequest;
    
    public JSONObject InitTransaction() {
        SOURCE_CODE = "POQT";

        poMaster = new QuotationModels(poGRider).POQuotationMaster();
        poDetail = new QuotationModels(poGRider).POQuotationDetails();

        paMasterList = new ArrayList<>();
        paDetail = new ArrayList<>();
        paDetailRemoved = new ArrayList<>();
        paPORequestSupplier = new ArrayList<>();
        paAttachments = new ArrayList<>();

        return initialize();
    }

    public JSONObject NewTransaction()
            throws CloneNotSupportedException {
        return newTransaction();
    }

    public JSONObject SaveTransaction()
            throws SQLException,
            GuanzonException,
            CloneNotSupportedException {
        return saveTransaction();
    }

    public JSONObject OpenTransaction(String transactionNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        //Clear data
        resetMaster();
        resetOthers();
        Detail().clear();
        return openTransaction(transactionNo);
    }

    public JSONObject UpdateTransaction() {
        return updateTransaction();
    }
    
    public JSONObject ConfirmTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = POQuotationStatus.CONFIRMED;
        boolean lbConfirm = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already confirmed.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(lsStatus);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
                
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            poJSON = ShowDialogFX.getUserApproval(poGRider);
            if (!"success".equals((String) poJSON.get("result"))) {
                return poJSON;
            } else {
                if(Integer.parseInt(poJSON.get("nUserLevl").toString())<= UserRight.ENCODER){
                    poJSON.put("result", "error");
                    poJSON.put("message", "User is not an authorized approving officer.");
                    return poJSON;
                }
            }
        }
        
        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbConfirm);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbConfirm) {
            poJSON.put("message", "Transaction confirmed successfully.");
        } else {
            poJSON.put("message", "Transaction confirmation request submitted successfully.");
        }

        return poJSON;
    }
    
    public JSONObject PostTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = POQuotationStatus.POSTED;
        boolean lbPosted = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already posted.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(POQuotationStatus.POSTED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (POQuotationStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                } else {
                    if(Integer.parseInt(poJSON.get("nUserLevl").toString())<= UserRight.ENCODER){
                        poJSON.put("result", "error");
                        poJSON.put("message", "User is not an authorized approving officer.");
                        return poJSON;
                    }
                }
            }
        }
        
        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbPosted);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbPosted) {
            poJSON.put("message", "Transaction posted successfully.");
        } else {
            poJSON.put("message", "Transaction posting request submitted successfully.");
        }

        return poJSON;
    }
    
    public JSONObject VoidTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = POQuotationStatus.VOID;
        boolean lbVoid = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already voided.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(POQuotationStatus.VOID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (POQuotationStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                } else {
                    if(Integer.parseInt(poJSON.get("nUserLevl").toString())<= UserRight.ENCODER){
                        poJSON.put("result", "error");
                        poJSON.put("message", "User is not an authorized approving officer.");
                        return poJSON;
                    }
                }
            }
        }
        
        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbVoid);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbVoid) {
            poJSON.put("message", "Transaction voided successfully.");
        } else {
            poJSON.put("message", "Transaction voiding request submitted successfully.");
        }

        return poJSON;
    }
    
    public JSONObject CancelTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = POQuotationStatus.CANCELLED;
        boolean lbCancel = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already cancelled.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(POQuotationStatus.CANCELLED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (POQuotationStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                } else {
                    if(Integer.parseInt(poJSON.get("nUserLevl").toString())<= UserRight.ENCODER){
                        poJSON.put("result", "error");
                        poJSON.put("message", "User is not an authorized approving officer.");
                        return poJSON;
                    }
                }
            }
        }
        
        //change status
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbCancel);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbCancel) {
            poJSON.put("message", "Transaction cancelled successfully.");
        } else {
            poJSON.put("message", "Transaction cancellation request submitted successfully.");
        }

        return poJSON;
    }
    
    /*Search References*/
    public JSONObject searchTransaction()
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        if (psTranStat != null) {
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                    lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                }
                lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
            } else {
                lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
            }
        }

        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                                            + " AND a.sCategrCd = " + SQLUtil.toSQL(psCategorCd)
                                            + " AND a.sBranchCd = " + SQLUtil.toSQL(poGRider.getBranchCode()));
        //If current user is an ordinary user load only its inquiries
        if (poGRider.getUserLevel() <= UserRight.ENCODER) {
            lsSQL = MiscUtil.addCondition(lsSQL, 
                    " a.sSalesman = " + SQLUtil.toSQL(poGRider.getUserID()));
        }
        
        if (lsTransStat != null && !"".equals(lsTransStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction Date»Transaction No»Client»Sales Person",
                "dTransact»sTransNox»sClientNm»sSalePrsn",
                "a.dTransact»a.sTransNox»b.sCompnyNm»concat(c.sLastName,', ',c.sFrstName, ' ',c.sMiddName)",
                1);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    public JSONObject searchTransaction(String fsBranch, String fsDepartment, String fsSupplier, String fsCateogry, String fsTransactionNo)
            throws CloneNotSupportedException,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        String lsTransStat = "";
        if (psTranStat != null) {
            if (psTranStat.length() > 1) {
                for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                    lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                }
                lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
            } else {
                lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
            }
        }
        
        String lsBranch = fsBranch != null && !"".equals(fsBranch) 
                                                    ? " AND a.sDescript LIKE " + SQLUtil.toSQL("%"+fsBranch)
                                                    : "";
        
        String lsDepartment = fsDepartment != null && !"".equals(fsDepartment) 
                                                    ? " AND a.sDescript LIKE " + SQLUtil.toSQL("%"+fsDepartment)
                                                    : "";                                           

        String lsSupplier = fsSupplier != null && !"".equals(fsSupplier) 
                                                    ? " AND a.sDescript LIKE " + SQLUtil.toSQL("%"+fsSupplier)
                                                    : "";    
        
        String lsCategory = fsCateogry != null && !"".equals(fsCateogry) 
                                                    ? " AND a.sDescript LIKE " + SQLUtil.toSQL("%"+fsCateogry)
                                                    : "";  
        
        String lsTransactionNo = fsTransactionNo != null && !"".equals(fsTransactionNo) 
                                                    ? " AND a.sTransNox LIKE " + SQLUtil.toSQL("%"+fsTransactionNo)
                                                    : "";

        initSQL();
        String lsSQL = MiscUtil.addCondition(SQL_BROWSE, 
                   " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                 + " AND a.sCategrCd = " + SQLUtil.toSQL(psCategorCd)
                 + lsBranch
                 + lsDepartment
                 + lsSupplier
                 + lsCategory
                 + lsTransactionNo
                );
        
        if (lsTransStat != null && !"".equals(lsTransStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction Date»Transaction No»Supplier»Branch»Department»Category",
                "dTransact»sTransNox»Supplier»Branch»Department»Category", // TODO
                "a.dTransact»a.sTransNox»Supplier»Branch»Department»Category", //TODO
                1);

        if (poJSON != null) {
            return OpenTransaction((String) poJSON.get("sTransNox"));
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
            return poJSON;
        }
    }
    
    public JSONObject SearchInventory(String value, boolean byCode, int row) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        poJSON.put("row", row);
        Inventory object = new InvControllers(poGRider, logwrapr).Inventory();
        String lsSQL = MiscUtil.addCondition(object.getSQ_Browse(), 
                                            // " a.cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                                            " a.sCategCd1 = " + SQLUtil.toSQL(Master().getCategoryCode())
                                            + " AND a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryId())
//                                            + " AND a.sCategCd2 = " + SQLUtil.toSQL(Master().getCategory())
                                            );
        
        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Barcode»Description»Brand»Model»Variant»UOM",
                "sBarCodex»sDescript»xBrandNme»xModelNme»xVrntName»xMeasurNm",
                "a.sBarCodex»a.sDescript»IFNULL(b.sDescript, '')»IFNULL(c.sDescript, '')»IFNULL(f.sDescript, '')»IFNULL(e.sDescript, '')",
                byCode ? 0 : 1);

        if (poJSON != null) {
            poJSON = object.getModel().openRecord((String) poJSON.get("sStockIDx"));
            if ("success".equals((String) poJSON.get("result"))) {
                JSONObject loJSON = checkExistingDetail(row,
                        object.getModel().getStockId(),
                        object.getModel().getDescription(),
                        true
                        );
                if ("error".equals((String) loJSON.get("result"))) {
                    if((boolean) loJSON.get("reverse")){
                        return loJSON;
                    } else {
                        row = (int) loJSON.get("row");
                        Detail(row).isReverse(true);
                    }
                }

                Detail(row).setReplaceId(object.getModel().getStockId());
                Detail(row).setReplaceDescription(object.getModel().getDescription());
                   
//                if(object.getModel().Variant().getSellingPrice() != 0.0000){  //Error TODO
//                    Detail(row).setSellPrice(object.getModel().Variant().getSellingPrice());
//                } else {
                    if(object.getModel().getSellingPrice() != null){
                        Detail(row).setUnitPrice(object.getModel().getSellingPrice().doubleValue());
                    } else {
                        Detail(row).setUnitPrice(0.0000);
                    }
//                }
            }
            
            System.out.println("Barcode : " + Detail(row).Inventory().getBarCode());
            System.out.println("Description : " + Detail(row).Inventory().getDescription());
            System.out.println("Category : " + Master().Category2().getDescription());
            System.out.println("Brand : " + Detail(row).Brand().getDescription());
            System.out.println("Model : " + Detail(row).Model().getDescription());
            
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
        }
        if ("error".equals((String) poJSON.get("result"))) {
            if(!"".equals(value)){
                poJSON = checkExistingDetail(row,
                        "",
                        value,
                        false
                        );
                if ("error".equals((String) poJSON.get("result"))) {
                    if((boolean) poJSON.get("reverse")){
                        return poJSON;
                    } else {
                        row = (int) poJSON.get("row");
                        Detail(row).isReverse(true);
                    }
                }
            }

            Detail(row).setReplaceId("");
            Detail(row).setReplaceDescription(value);
        }
        
        
        poJSON.put("row", row);
        return poJSON;
    }
    
    public JSONObject SearchCompany(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Company object = new ParamControllers(poGRider, logwrapr).Company();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Master().setCompanyId(object.getModel().getCompanyId());
        }

        return poJSON;
    }
    
    public JSONObject SearchBranch(String value, boolean byCode, boolean isSearch) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            if(isSearch){
                setSearchBranch(object.getModel().getBranchName());
            } else {
                Master().setBranchCode(object.getModel().getBranchCode());
            }
        }

        return poJSON;
    }
    
    public JSONObject SearchDepartment(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Department object = new ParamControllers(poGRider, logwrapr).Department();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            setSearchDepartment(object.getModel().getDescription());
        }
        return poJSON;
    }

    public JSONObject SearchSupplier(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Client object = new ClientControllers(poGRider, logwrapr).Client();
        object.Master().setRecordStatus(RecordStatus.ACTIVE);
        object.Master().setClientType("1");

        poJSON = object.Master().searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Master().setSupplierId(object.Master().getModel().getClientId());
        }

        return poJSON;
    }
    
    public JSONObject SearchCategory(String value, boolean byCode) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        
//        CategoryLevel2 object = new ParamControllers(poGRider, logwrapr).CategoryLevel2();
//        object.setRecordStatus(RecordStatus.ACTIVE);
//
//        poJSON = object.searchRecord(value, byCode);
//        if ("success".equals((String) poJSON.get("result"))) {
//            if(isSearch){
//                setSearchCategory(object.getModel().getDescription());
//            } else {
//                System.out.println("Category ID: " + object.getModel().getCategoryId());
//                System.out.println("Description " + object.getModel().getDescription());
//                Master().setCategoryLevel2(object.getModel().getCategoryId());
//            }
//        }
        
        
        CategoryLevel2 object = new ParamControllers(poGRider, logwrapr).CategoryLevel2();
        String lsSQL = MiscUtil.addCondition(object.getSQ_Browse(), "cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                                            + " AND (sIndstCdx = '' OR ISNULL(sIndstCdx))"); //+ SQLUtil.toSQL(Master().getIndustryId()));
        
        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                value,
                "Category ID»Description",
                "sCategrCd»sDescript",
                "sCategrCd»sDescript",
                byCode ? 0 : 1);

        if (poJSON != null) {
            poJSON = object.getModel().openRecord((String) poJSON.get("sCategrCd"));
            if ("success".equals((String) poJSON.get("result"))) {
                setSearchCategory(object.getModel().getDescription());
            }
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
        }
        
        return poJSON;
    }
    
    public JSONObject SearchTerm(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Term object = new ParamControllers(poGRider, logwrapr).Term();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            Master().setTerm(object.getModel().getTermId());
        }

        return poJSON;
    }
    
    /*Validate*/
    public JSONObject checkExistingDetail(int row, String stockId, String description, boolean isSearch){
        JSONObject loJSON = new JSONObject();
        loJSON.put("row", row);
        if(stockId == null){
            stockId = "";
        }
        if(description == null){
            description = "";
        }
        int lnRow = 1;
        for (int lnCtr = 0; lnCtr <= getDetailCount()- 1; lnCtr++) {
            if(Detail(lnCtr).isReverse()){
                lnRow++;
            }
            if (lnCtr != row) {
                //Check Existing Stock ID and Description
                if(!"".equals(stockId) || !"".equals(description)){
                    if(((stockId.equals(Detail(lnCtr).getStockId())   && isSearch )
                        || description.equals(Detail(lnCtr).getDescription()))
                            || ((stockId.equals(Detail(lnCtr).getReplaceId()) && isSearch )
                            || description.equals(Detail(lnCtr).getReplaceDescription()))
                            ){
                        if(Detail(lnCtr).isReverse()){
                            loJSON.put("result", "error");
                            loJSON.put("message", "Item Description already exists in the transaction detail at row "+lnRow+".");
                            loJSON.put("reverse", true);
                            loJSON.put("row", lnCtr);
                            return loJSON;
                        } else {
                            loJSON.put("result", "error");
                            loJSON.put("reverse", false);
                            loJSON.put("row", lnCtr);
                            return loJSON;
                        }
                    }
                }    
            }
        }
        
        loJSON.put("result", "success");
        loJSON.put("message", "success");
        return loJSON;
    }
    
    public JSONObject computeFields()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        //Compute Transaction Total
        Double ldblTotal = 0.0000;
        Double ldblDiscount = Master().getAdditionalDiscountAmount();
        Double ldblDiscountRate = Master().getDiscountRate();
        Double ldblDetailDiscount = 0.0000;
        Double ldblDetailDiscountRate = 0.00;
        Double ldblDetailTotal = 0.0000;
        
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            ldblDetailTotal = 
            
            
            ldblTotal += (Detail(lnCtr).getUnitPrice() * Detail(lnCtr).getQuantity());
            
            ldblDetailTotal = 0.0000;
        }
        poJSON = Master().setTransactionTotal(ldblTotal); //Sum of purchase amount
        if(ldblDiscountRate > 0){
            ldblDiscountRate = ldblTotal * (ldblDiscountRate / 100);
        }
        
        /*Compute VAT Amount*/
        double ldblVatSales = 0.0000;
        double ldblVatAmount = 0.0000;
        double ldblTransactionTotal = 0.0000;
        double ldblVatExempt = 0.00;
        double ldblVatableTotal = 0.00;
            
        //VAT Sales : (Vatable Total + Freight Amount) - Discount Amount
        ldblVatSales = (ldblTotal + Master().getFreightAmount()) - (ldblDiscount + ldblDiscountRate);

        if(Master().isVatable()){
            //VAT Amount : VAT Sales - (VAT Sales / 1.12)
            ldblVatAmount = ldblVatSales - ( ldblVatSales / 1.12);
            //Net VAT Sales : VAT Sales - VAT Amount
            ldblTransactionTotal = ldblVatSales - ldblVatAmount;
        } 
        //else {
//            //VAT Amount : VAT Sales - (VAT Sales / 1.12)
//            ldblVatAmount = ldblVatSales - ( ldblVatSales / 1.12);
//            //Net : VAT Sales - VAT Amount
//            ldblTransactionTotal = ldblVatSales + ldblVatAmount;
        //}

        System.out.println("Vat Sales " + ldblTransactionTotal);
        System.out.println("Vat Amount " + ldblVatAmount);
        System.out.println("Vat Exempt " + ldblVatExempt);

        poJSON = Master().setGrossAmount(ldblTotal);
        poJSON = Master().setTransactionTotal(ldblTransactionTotal);
        poJSON = Master().setVatAmount(ldblVatAmount);
        if(Master().getVatRate() == 0.00){
            if(getEditMode() == EditMode.UNKNOWN || Master().getEditMode() == EditMode.UNKNOWN){
                poJSON = Master().setVatRate(0.00); //Set default value
            } else {
                poJSON = Master().setVatRate(12.00); //Set default value
            }
        }
        return poJSON;
    }
    
    public JSONObject computeDiscountRate(double discount) {
        poJSON = new JSONObject();
        Double ldblTotal = 0.00;
        Double ldblDiscRate = 0.00;

        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            ldblTotal += (Detail(lnCtr).getUnitPrice()  * Detail(lnCtr).getQuantity());
        }
        
        if (discount < 0 || discount > ldblTotal) {
            Master().setAdditionalDiscountAmount(0.00);
            computeDiscountRate(0.00);
            poJSON.put("result", "error");
            poJSON.put("message", "Discount amount cannot be negative or exceed the transaction total.");
            return poJSON;
        } else {
//            ldblDiscRate = (discount / ldblTotal) * 100;
//            ldblDiscRate = (discount / ldblTotal);
            //nettotal = total - discount - rate
//            Master().setDiscountRate(ldblDiscRate);

            ldblTotal = ldblTotal - (discount + ((Master().getDiscountRate() / 100.00) * ldblTotal));
            if(ldblTotal < 0 ){
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid transaction net total.");
                return poJSON;
            }
        }
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }

    public JSONObject computeDiscount(double discountRate) {
        poJSON = new JSONObject();
        Double ldblTotal = 0.00;
        Double ldblDiscount = 0.00;

        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            ldblTotal += (Detail(lnCtr).getUnitPrice() * Detail(lnCtr).getQuantity());
        }

        if (discountRate < 0 || discountRate > 100.00) {
//        if (discountRate < 0 || discountRate > 1.00) {
            Master().setDiscountRate(0.00);
            computeDiscount(0.00);
            poJSON.put("result", "error");
            poJSON.put("message", "Discount rate cannot be negative or exceed 100.00");
            return poJSON;
        } else {
//            ldblDiscount = ldblTotal * (discountRate / 100.00);
//            ldblDiscount = ldblTotal * discountRate;
            //nettotal = total - discount - rate
//            Master().setDiscount(ldblDiscount);

            ldblTotal = ldblTotal - (Master().getAdditionalDiscountAmount() + ((discountRate / 100.00) * ldblTotal));
            if(ldblTotal < 0 ){
                poJSON.put("result", "error");
                poJSON.put("message", "Invalid transaction net total.");
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    /*Load*/
    public JSONObject loadPOQuotationList(String fsBranch, String fsDepartment ,String fsSupplier, String fsCateogry, String fsTransactionNo) {
        try {
            String lsBranch = fsBranch != null && !"".equals(fsBranch) 
                                                        ? " AND c.sBranchNm LIKE " + SQLUtil.toSQL("%"+fsBranch)
                                                        : "";

            String lsDepartment = fsDepartment != null && !"".equals(fsDepartment) 
                                                        ? " AND d.sDeptName LIKE " + SQLUtil.toSQL("%"+fsDepartment)
                                                        : "";

            String lsCategory = fsCateogry != null && !"".equals(fsCateogry) 
                                                        ? " AND f.sDescript LIKE " + SQLUtil.toSQL("%"+fsCateogry)
                                                        : "";

            String lsSupplier = fsSupplier != null && !"".equals(fsSupplier) 
                                                        ? " AND f.sCompnyNm LIKE " + SQLUtil.toSQL("%"+fsSupplier)
                                                        : "";

            String lsTransactionNo = fsTransactionNo != null && !"".equals(fsTransactionNo) 
                                                        ? " AND a.sTransNox LIKE " + SQLUtil.toSQL("%"+fsTransactionNo)
                                                        : "";

            String lsTransStat = "";
            if (psTranStat != null) {
                if (psTranStat.length() > 1) {
                    for (int lnCtr = 0; lnCtr <= psTranStat.length() - 1; lnCtr++) {
                        lsTransStat += ", " + SQLUtil.toSQL(Character.toString(psTranStat.charAt(lnCtr)));
                    }
                    lsTransStat = " AND a.cTranStat IN (" + lsTransStat.substring(2) + ")";
                } else {
                    lsTransStat = " AND a.cTranStat = " + SQLUtil.toSQL(psTranStat);
                }
            }

            initSQL();
            String lsSQL = MiscUtil.addCondition(SQL_BROWSE, " a.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                    + " AND a.sCategrCd = " + SQLUtil.toSQL(psCategorCd)
                    + lsBranch
                    + lsDepartment
                    + lsCategory
                    + lsSupplier
                    + lsTransactionNo
            );

            if (lsTransStat != null && !"".equals(lsTransStat)) {
                lsSQL = lsSQL + lsTransStat;
            }

            lsSQL = lsSQL + " ORDER BY a.dTransact DESC ";

            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();

            int lnctr = 0;

            if (MiscUtil.RecordCount(loRS) >= 0) {
                paMasterList = new ArrayList<>();
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("dTransact: " + loRS.getDate("dTransact"));
                    System.out.println("sCompnyNm: " + loRS.getString("SpplierNm"));
                    System.out.println("------------------------------------------------------------------------------");

                    paMasterList.add(POQuotationMaster());
                    paMasterList.get(paMasterList.size() - 1).openRecord(loRS.getString("sTransNox"));
                    lnctr++;
                }

                System.out.println("Records found: " + lnctr);
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } else {
                paMasterList = new ArrayList<>();
                paMasterList.add(POQuotationMaster());
                poJSON.put("result", "error");
                poJSON.put("continue", true);
                poJSON.put("message", "No record found.");
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        } catch (GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
        }
        return poJSON;
    }
    
    public JSONObject loadAttachments()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        paAttachments = new ArrayList<>();

        TransactionAttachment loAttachment = new SysTableContollers(poGRider, null).TransactionAttachment();
        List loList = loAttachment.getAttachments(SOURCE_CODE, Master().getTransactionNo());
        for (int lnCtr = 0; lnCtr <= loList.size() - 1; lnCtr++) {
            paAttachments.add(TransactionAttachment());
            poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).openRecord((String) loList.get(lnCtr));
            if ("success".equals((String) poJSON.get("result"))) {
                if(Master().getEditMode() == EditMode.UPDATE){
                   poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).updateRecord();
                }
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getTransactionNo());
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getSourceNo());
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getSourceCode());
                System.out.println(paAttachments.get(getTransactionAttachmentCount() - 1).getModel().getFileName());
            }
        }
        return poJSON;
    }
    
    public JSONObject loadPOQuotationRequestSupplierList(String company, String branch, String department, String supplier, String category2) {
        try {
            if (company == null) {
                company = "";
            }
            if (branch == null) {
                branch = "";
            }
            if (department == null) {
                department = "";
            }
            if (supplier == null) {
                supplier = "";
            }
            if (category2 == null) {
                category2 = "";
            }
            String lsSQL = MiscUtil.addCondition(getSupplierSQL(), 
                      " b.sIndstCdx = " + SQLUtil.toSQL(psIndustryId)
                    + " AND b.sCategrCd = " + SQLUtil.toSQL(psCategorCd)
                    + " AND a.sCompnyID LIKE " + SQLUtil.toSQL("%" + company)
                    + " AND d.sBranchNm LIKE " + SQLUtil.toSQL("%" + branch)
                    + " AND e.sDeptName LIKE " + SQLUtil.toSQL("%" + department) 
                    + " AND h.sCompnyNm LIKE " + SQLUtil.toSQL("%" + supplier) 
                    + " AND g.sDescript LIKE " + SQLUtil.toSQL("%" + category2) 
                    + " AND b.cTranStat = " + SQLUtil.toSQL(POQuotationRequestStatus.APPROVED)
            );

            lsSQL = lsSQL + " ORDER BY b.dTransact DESC ";

            System.out.println("Executing SQL: " + lsSQL);
            ResultSet loRS = poGRider.executeQuery(lsSQL);
            poJSON = new JSONObject();

            int lnctr = 0;

            if (MiscUtil.RecordCount(loRS) >= 0) {
                paMasterList = new ArrayList<>();
                while (loRS.next()) {
                    // Print the result set
                    System.out.println("sTransNox: " + loRS.getString("sTransNox"));
                    System.out.println("dTransact: " + loRS.getDate("dTransact"));
                    System.out.println("sCompnyNm: " + loRS.getString("SpplierNm"));
                    System.out.println("------------------------------------------------------------------------------");

                    paPORequestSupplier.add(POQuotationRequestSupplier());
                    paPORequestSupplier.get(paPORequestSupplier.size() - 1).openRecord(loRS.getString("sTransNox"));
                    lnctr++;
                }

                System.out.println("Records found: " + lnctr);
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } else {
                paPORequestSupplier = new ArrayList<>();
                paPORequestSupplier.add(POQuotationRequestSupplier());
                poJSON.put("result", "error");
                poJSON.put("continue", true);
                poJSON.put("message", "No record found.");
            }
            MiscUtil.close(loRS);
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        } catch (GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, getClass().getName(), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
        }
        return poJSON;
    }
    
    public JSONObject populatePOQuotation(int row) {
        poJSON = new JSONObject();
        try {
            
            POQuotationRequest object = new QuotationControllers(poGRider, logwrapr).POQuotationRequest();
            object.InitTransaction();
            poJSON = object.OpenTransaction(POQuotationRequestSupplierList(row).getTransactionNo());
            if ("error".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            
            Master().setSupplierId(POQuotationRequestSupplierList(row).getSupplierId());
            Master().setAddressId(POQuotationRequestSupplierList(row).getAddressId());
            Master().setContactId(POQuotationRequestSupplierList(row).getContactId());
            Master().setTerm(POQuotationRequestSupplierList(row).getTerm());
            Master().setSourceNo(POQuotationRequestSupplierList(row).getTransactionNo());
            Master().setSourceCode(object.getSourceCode());
            Master().setCompanyId(POQuotationRequestSupplierList(row).getCompanyId());
            Master().setBranchCode(object.Master().getBranchCode());
            Master().setCategoryCode(object.Master().getCategoryLevel2());
            
            for(int lnCtr = 0; lnCtr <= object.getDetailCount()-1; lnCtr++){
                if(object.Detail(lnCtr).isReverse()){
                    Detail(getDetailCount()-1).setStockId(object.Detail(lnCtr).getStockId());
                    Detail(getDetailCount()-1).setDescription(object.Detail(lnCtr).getDescription());
                    Detail(getDetailCount()-1).setQuantity(object.Detail(lnCtr).getQuantity());
                    Detail(getDetailCount()-1).setUnitPrice(object.Detail(lnCtr).getUnitPrice());
                    AddDetail();
                }
            }
            
        } catch (SQLException e) {
            poJSON.put("result", "error");
            poJSON.put("message", e.getMessage());
        } catch (GuanzonException | CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, getClass().getName(), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
        }
        return poJSON;
    }
    
//    public void ReloadDetail() throws CloneNotSupportedException{
//        int lnCtr = getDetailCount() - 1;
//        while (lnCtr >= 0) {
//            if ((Detail(lnCtr).getStockId() == null || "".equals(Detail(lnCtr).getStockId()))
//                    && (Detail(lnCtr).getDescription()== null || "".equals(Detail(lnCtr).getDescription()))) {
//                
//                if(Detail(lnCtr).getEditMode() == EditMode.ADDNEW){
//                    deleteDetail(lnCtr); 
//                    //Detail().remove(lnCtr);
//                } else {
//                    Detail(lnCtr).isReverse(false);
//                    removeDetail(Detail(lnCtr));
//                }
//            }
//            lnCtr--;
//        }
//
//        if ((getDetailCount() - 1) >= 0) {
//            if ((Detail(getDetailCount() - 1).getStockId() != null
//                    && !"".equals(Detail(getDetailCount() - 1).getStockId()))
//                || (Detail(getDetailCount() - 1).getDescription()!= null
//                    && !"".equals(Detail(getDetailCount() - 1).getDescription()))) {
//                AddDetail();
//            }
//        }
//
//        if ((getDetailCount() - 1) < 0) {
//            AddDetail();
//        }
//    }
    
    private Model_PO_Quotation_Master POQuotationMaster() {
        return new QuotationModels(poGRider).POQuotationMaster();
    }
    
    private Model_PO_Quotation_Request_Supplier POQuotationRequestSupplier() {
        return new QuotationModels(poGRider).POQuotationRequestSupplier();
    }
    
    private TransactionAttachment TransactionAttachment()
            throws SQLException,
            GuanzonException {
        return new SysTableContollers(poGRider, null).TransactionAttachment();
    }

    @Override
    public Model_PO_Quotation_Master Master() {
        return (Model_PO_Quotation_Master) poMaster;
    }

    public Model_PO_Quotation_Detail getDetail() {
        return (Model_PO_Quotation_Detail) poDetail;
    }
    
    public List<Model_PO_Quotation_Request_Supplier> POQuotationRequestSupplierList() {
        return paPORequestSupplier;
    }
    
    private List<TransactionAttachment> TransactionAttachmentList() {
        return paAttachments;
    }

    public List<Model_PO_Quotation_Master> POQuotationList() {
        return paMasterList;
    }
    
    public Model_PO_Quotation_Master POQuotationList(int row) {
        return (Model_PO_Quotation_Master) paMasterList.get(row);
    }

    @Override
    public Model_PO_Quotation_Detail Detail(int row) {
        return (Model_PO_Quotation_Detail) paDetail.get(row);
    }
    
    public Model_PO_Quotation_Request_Supplier POQuotationRequestSupplierList(int row) {
        return (Model_PO_Quotation_Request_Supplier) paPORequestSupplier.get(row);
    }
    
    public TransactionAttachment TransactionAttachmentList(int row) {
        return (TransactionAttachment) paAttachments.get(row);
    }

    private Model_PO_Quotation_Detail DetailRemoved(int row) {
        return (Model_PO_Quotation_Detail) paDetailRemoved.get(row);
    }
    
    @Override
    public int getDetailCount() {
        if (paDetail == null) {
            paDetail = new ArrayList<>();
        }

        return paDetail.size();
    }
    
    public int getPOQuotationCount() {
        if (paMasterList == null) {
            paMasterList = new ArrayList<>();
        }

        return paMasterList.size();
    }
    
    public int getPOQuotationRequestSupplierCount() {
        if (paPORequestSupplier == null) {
            paPORequestSupplier = new ArrayList<>();
        }

        return paPORequestSupplier.size();
    }
    
    public int getTransactionAttachmentCount() {
        if (paAttachments == null) {
            paAttachments = new ArrayList<>();
        }

        return paAttachments.size();
    }
    
    private int getDetailRemovedCount() {
        if (paDetailRemoved == null) {
            paDetailRemoved = new ArrayList<>();
        }

        return paDetailRemoved.size();
    }
    
    public JSONObject AddDetail()
            throws CloneNotSupportedException {
        poJSON = new JSONObject();

        if (getDetailCount() > 0) {
            if (Detail(getDetailCount() - 1).getStockId()!= null || Detail(getDetailCount() - 1).getDescription() != null) {
                if (Detail(getDetailCount() - 1).getStockId().isEmpty() && Detail(getDetailCount() - 1).getDescription().isEmpty()) {
                    poJSON.put("result", "error");
                    poJSON.put("message", "Last row has empty item.");
                    return poJSON;
                }
            }
        }

        return addDetail();
    }
    
    public JSONObject addAttachment()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();

        if (paAttachments.isEmpty()) {
            paAttachments.add(TransactionAttachment());
            poJSON = paAttachments.get(getTransactionAttachmentCount() - 1).newRecord();
        } else {
            if (!paAttachments.get(paAttachments.size() - 1).getModel().getTransactionNo().isEmpty()) {
                paAttachments.add(TransactionAttachment());
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to add transaction attachment.");
                return poJSON;
            }
        }

        poJSON.put("result", "success");
        return poJSON;

    }
    
    public JSONObject removeDetails() {
        poJSON = new JSONObject();
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next();
            if (item.getEditMode() == EditMode.UPDATE) {
                paDetailRemoved.add(item);
            }
            
            detail.remove();
        }

        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }

    private void removeDetail(Model_PO_Quotation_Detail item) {
        if (paDetailRemoved == null) {
            paDetailRemoved = new ArrayList<>();
        }
        
        paDetailRemoved.add(item);
    }

    public void resetMaster() {
        poMaster = new QuotationModels(poGRider).POQuotationMaster();
    }
    
    public void resetOthers() {
        paAttachments = new ArrayList<>();
        paDetailRemoved = new ArrayList<>();
        setSearchBranch("");
        setSearchDepartment("");
        setSearchSupplier("");
        setSearchCategory("");
    }
    
    /*Convert Date to String*/
    private static String xsDateShort(Date fdValue) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(fdValue);
        return date;
    }

    private LocalDate strToDate(String val) {
        DateTimeFormatter date_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(val, date_formatter);
        return localDate;
    }
    
    public void setSearchBranch(String searchBranch) {
        psSearchBranch = searchBranch;
    }
    
    public String getSearchBranch() {
        return psSearchBranch;
    }

    public void setSearchDepartment(String searchDepartment) {
        psSearchDepartment = searchDepartment;
    }
    
    public String getSearchDepartment() {
        return psSearchDepartment;
    }
    
    public void setSearchSupplier(String searchSupplier) {
        psSearchSupplier = searchSupplier;
    }
    
    public String getSearchSupplier() {
        return psSearchSupplier;
    }

    public void setSearchCategory(String searchCategory) {
        psSearchCategory = searchCategory;
    }
    
    public String getSearchCategory() {
        return psSearchCategory;
    }
    
    public void setIndustryId(String industryId) {
        psIndustryId = industryId;
    }

    public void setCompanyId(String companyId) {
        psCompanyId = companyId;
    }

    public void setCategoryId(String categoryId) {
        psCategorCd = categoryId;
    }

    @Override
    public String getSourceCode() {
        return SOURCE_CODE;
    }
    
    @Override
    public JSONObject willSave()
            throws SQLException,
            GuanzonException,
            CloneNotSupportedException {
        /*Put system validations and other assignments here*/
        poJSON = new JSONObject();
        
        if (POQuotationStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
            if (poGRider.getUserLevel() <= UserRight.ENCODER) {
                poJSON = ShowDialogFX.getUserApproval(poGRider);
                if (!"success".equals((String) poJSON.get("result"))) {
                    return poJSON;
                } else {
                    if(Integer.parseInt(poJSON.get("nUserLevl").toString())<= UserRight.ENCODER){
                        poJSON.put("result", "error");
                        poJSON.put("message", "User is not an authorized approving officer.");
                        return poJSON;
                    }
                }
            }
        }

        if (paDetailRemoved == null) {
            paDetailRemoved = new ArrayList<>();
        }
        
        if(Master().getEditMode() == EditMode.ADDNEW){
            System.out.println("Will Save : " + Master().getNextCode());
            Master().setTransactionNo(Master().getNextCode());
            Master().setPrepared(poGRider.getUserID());
        }
        
        Master().setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        Master().setModifiedDate(poGRider.getServerDate());
        Master().setPreparedDate(poGRider.getServerDate()); //Re-updated prepared date when user edited the transaction conflict in null when updating transaction
        
        //Check detail
        boolean lbWillDelete = true;
        for(int lnCtr = 0; lnCtr <= getDetailCount()-1; lnCtr++){
            if ((Detail(lnCtr).getQuantity() > 0.00) && Detail(lnCtr).isReverse() ) {
                lbWillDelete = false;
            }
        }
        
        if(lbWillDelete){
            poJSON.put("result", "error");
            poJSON.put("message", "No transaction quantity to be save.");
            return poJSON;
        }
        
        String lsQuantity = "0.00";
        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next();
            if(item.getValue("nQuantity") != null && !"".equals(item.getValue("nQuantity"))){
                lsQuantity = item.getValue("nQuantity").toString();
            }
            if (((item.getValue("sDescript") == null || "".equals(item.getValue("sDescript")))
                  &&  (item.getValue("sStockIDx") == null || "".equals(item.getValue("sStockIDx"))))
                  || (Double.valueOf(lsQuantity) <= 0.00)) {

                if (item.getEditMode() == EditMode.ADDNEW) {
                    detail.remove();
                } else {
                    paDetailRemoved.add(item);
                    item.setValue("cReversex", "0");
                }

            }
        }

        //Validate detail after removing all zero qty and empty stock Id
        if (getDetailCount() <= 0) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transaction detail to be save.");
            return poJSON;
        }

        //assign other info on detail
        for (int lnCtr = 0; lnCtr <= getDetailCount() - 1; lnCtr++) {
            Detail(lnCtr).setTransactionNo(Master().getTransactionNo());
            Detail(lnCtr).setEntryNo(lnCtr+1);
        }

        //assign other info on attachment
        for (int lnCtr = 0; lnCtr <= getTransactionAttachmentCount()- 1; lnCtr++) {
            TransactionAttachmentList(lnCtr).getModel().setSourceNo(Master().getTransactionNo());
            TransactionAttachmentList(lnCtr).getModel().setSourceCode(getSourceCode());
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public JSONObject save() {
        /*Put saving business rules here*/
        return isEntryOkay(POQuotationStatus.OPEN);
    }
    
    @Override
    public void saveComplete() {
        /*This procedure was called when saving was complete*/
        System.out.println("Transaction saved successfully.");
    }
    
    @Override
    public JSONObject saveOthers() {
        /*Only modify this if there are other tables to modify except the master and detail tables*/
        poJSON = new JSONObject();
        try {
            //Save Attachments
            for (int lnCtr = 0; lnCtr <= getTransactionAttachmentCount() - 1; lnCtr++) {
                if (paAttachments.get(lnCtr).getEditMode() == EditMode.ADDNEW || paAttachments.get(lnCtr).getEditMode() == EditMode.UPDATE) {
                    paAttachments.get(lnCtr).getModel().setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
                    paAttachments.get(lnCtr).getModel().setModifiedDate(poGRider.getServerDate());
                    paAttachments.get(lnCtr).setWithParentClass(true);
                    poJSON = paAttachments.get(lnCtr).saveRecord();
                    if ("error".equals((String) poJSON.get("result"))) {
                        return poJSON;
                    }
                }
            }
            
//            POQuotationRequest object = new QuotationControllers(poGRider, logwrapr).POQuotationRequest();
//            object.InitTransaction();
//            poJSON = object.OpenTransaction(Master().getSourceNo());
//            if ("error".equals((String) poJSON.get("result"))) {
//                return poJSON;
//            }
//            object.setWithParent(true);
//            object.setWithUI(true);
//            switch(Master().getTransactionStatus()){
//                case POQuotationStatus.CONFIRMED:
//                    object.PostTransaction("Post Transaction");
//                    if ("error".equals((String) poJSON.get("result"))) {
//                        return poJSON;
//                    }
//                    break;
//            
//            } 
            
        } catch (SQLException | GuanzonException | CloneNotSupportedException  ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }
        poJSON.put("result", "success");
        return poJSON;
    }
    

    @Override
    public JSONObject initFields() {
        try {
            /*Put initial model values here*/
            poJSON = new JSONObject();
            System.out.println("Dept ID : " + poGRider.getDepartment());
            System.out.println("Current User : " + poGRider.getUserID());
            
            Master().setIndustryId(psIndustryId);
            Master().setCompanyId(psCompanyId);
            Master().setCategoryCode(psCategorCd);
            Master().setTransactionDate(poGRider.getServerDate());
            Master().setTransactionStatus(POQuotationStatus.OPEN);
            
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
            poJSON.put("result", "error");
            poJSON.put("message", MiscUtil.getException(ex));
            return poJSON;
        }

        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    protected JSONObject isEntryOkay(String status) {
        GValidator loValidator = POQuotationValidatorFactory.make(Master().getIndustryId());

        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(poMaster);
//        loValidator.setDetail(paDetail);

        poJSON = loValidator.validate();

        return poJSON;
    }

    @Override
    public void initSQL() {
        SQL_BROWSE =  " SELECT  "       
                    + "     a.sTransNox  "
                    + "   , a.sIndstCdx  "
                    + "   , a.sBranchCd  "
                    + "   , a.sCategrCd  "
                    + "   , a.dTransact  "
                    + "   , a.sReferNox  "
                    + "   , a.cTranStat  "
                    + "   , a.sCompnyID  "
                    + "   , b.sDescript AS Industry      "
                    + "   , c.sBranchNm AS Branch        "
                    + "   , e.sDescript AS Category      "
                    + "   , h.sDeptName AS Department    "
                    + "   , i.sCompnyNm AS SpplierNm  "
                    + "  FROM po_quotation_master a      "
                    + "  LEFT JOIN industry b ON b.sIndstCdx = a.sIndstCdx          "
                    + "  LEFT JOIN branch c ON c.sBranchCd = a.sBranchCd            "
                    + "  LEFT JOIN company d ON d.sCompnyID = a.sCompnyID           "
                    + "  LEFT JOIN category e ON e.sCategrCd = a.sCategrCd          "
                    + "  LEFT JOIN category_level2 f ON f.sCategrCd = a.sCategrCd   "
                    + "  LEFT JOIN po_quotation_request_master g ON g.sTransNox = a.sSourceNo "
                    + "  LEFT JOIN department h ON h.sDeptIDxx = g.sDeptIDxx        "
                    + "  LEFT JOIN client_master i ON i.sClientID = a.sSupplier     ";
        
    }
    
    private String getSupplierSQL(){
        return   " SELECT "
                + "   b.sTransNox "
                + " , b.sIndstCdx "
                + " , b.sBranchCd "
                + " , b.sDeptIDxx "
                + " , b.sCategrCd "
                + " , b.dTransact "
                + " , b.sCategCd2 "
                + " , b.sDestinat "
                + " , b.sReferNox "
                + " , b.cTranStat "
                + " , a.sSupplier "
                + " , c.sDescript AS Industry   "
                + " , d.sBranchNm AS Branch     "
                + " , e.sDeptName AS Department "
                + " , f.sDescript AS Category   "
                + " , g.sDescript AS Category2  "
                + " , h.sCompnyNm AS  SpplierNm       "
                + " FROM po_quotation_request_supplier a "
                + " LEFT JOIN po_quotation_request_master b ON b.sTransNox = a.sTransNox "
                + " LEFT JOIN industry c ON c.sIndstCdx = b.sIndstCdx                    "
                + " LEFT JOIN branch d ON d.sBranchCd = b.sBranchCd                      "
                + " LEFT JOIN department e ON e.sDeptIDxx = b.sDeptIDxx                  "
                + " LEFT JOIN category f ON f.sCategrCd = b.sCategrCd                    "
                + " LEFT JOIN category_level2 g ON g.sCategrCd = b.sCategCd2             "
                + " LEFT JOIN client_master h ON h.sClientID = a.sSupplier               ";
    
    }
}
