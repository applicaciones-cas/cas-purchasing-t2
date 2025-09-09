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
import org.guanzon.cas.parameter.Brand;
import org.guanzon.cas.parameter.CategoryLevel2;
import org.guanzon.cas.parameter.Department;
import org.guanzon.cas.parameter.ModelVariant;
import org.guanzon.cas.parameter.Term;
import org.guanzon.cas.parameter.services.ParamControllers;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ph.com.guanzongroup.cas.purchasing.t2.model.Model_PO_Quotation_Request_Detail;
import ph.com.guanzongroup.cas.purchasing.t2.model.Model_PO_Quotation_Request_Master;
import ph.com.guanzongroup.cas.purchasing.t2.model.Model_PO_Quotation_Request_Supplier;
import ph.com.guanzongroup.cas.purchasing.t2.services.QuotationModels;
import ph.com.guanzongroup.cas.purchasing.t2.status.POQuotationRequestStatus;
import ph.com.guanzongroup.cas.purchasing.t2.validator.POQuotationRequestValidatorFactory;

/**
 *
 * @author Arsiela
 */
public class POQuotationRequest extends Transaction {
    private String psIndustryId = "";
    private String psCompanyId = "";
    private String psCategorCd = "";
    
    private String psSearchBranch;
    private String psSearchCategory;
    private String psSearchDepartment;
    
    List<Model_PO_Quotation_Request_Master> paMasterList;
    List<Model> paDetailRemoved;
    List<Model> paSuppliersRemoved;
    List<Model_PO_Quotation_Request_Supplier> paSuppliers;
    
    public JSONObject InitTransaction() {
        SOURCE_CODE = "PORq";

        poMaster = new QuotationModels(poGRider).POQuotationRequestMaster();
        poDetail = new QuotationModels(poGRider).POQuotationRequestDetails();

        paMasterList = new ArrayList<>();
        paDetail = new ArrayList<>();
        paDetailRemoved = new ArrayList<>();
        paSuppliers = new ArrayList<>();
        paSuppliersRemoved = new ArrayList<>();

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

        String lsStatus = POQuotationRequestStatus.CONFIRMED;
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
    
    public JSONObject ApproveTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = POQuotationRequestStatus.APPROVED;
        boolean lbApprove = true;

        if (getEditMode() != EditMode.READY) {
            poJSON.put("result", "error");
            poJSON.put("message", "No transacton was loaded.");
            return poJSON;
        }

        if (lsStatus.equals((String) poMaster.getValue("cTranStat"))) {
            poJSON.put("result", "error");
            poJSON.put("message", "Transaction was already approved.");
            return poJSON;
        }

        //validator
        poJSON = isEntryOkay(POQuotationRequestStatus.APPROVED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (POQuotationRequestStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
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
        poJSON = statusChange(poMaster.getTable(), (String) poMaster.getValue("sTransNox"), remarks, lsStatus, !lbApprove);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }
        
        //Update Process
        

        poJSON = new JSONObject();
        poJSON.put("result", "success");
        if (lbApprove) {
            poJSON.put("message", "Transaction approved successfully.");
        } else {
            poJSON.put("message", "Transaction approving request submitted successfully.");
        }

        return poJSON;
    }
    
    public JSONObject PostTransaction(String remarks)
            throws ParseException,
            SQLException,
            GuanzonException,
            CloneNotSupportedException {
        poJSON = new JSONObject();

        String lsStatus = POQuotationRequestStatus.POSTED;
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
        poJSON = isEntryOkay(POQuotationRequestStatus.POSTED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (POQuotationRequestStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
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

        String lsStatus = POQuotationRequestStatus.VOID;
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
        poJSON = isEntryOkay(POQuotationRequestStatus.VOID);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (POQuotationRequestStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
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

        String lsStatus = POQuotationRequestStatus.CANCELLED;
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
        poJSON = isEntryOkay(POQuotationRequestStatus.CANCELLED);
        if (!"success".equals((String) poJSON.get("result"))) {
            return poJSON;
        }

        if (POQuotationRequestStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
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
    
    public JSONObject searchTransaction(String fsBranch, String fsDepartment, String fsCateogry, String fdTransactionDate,String fsTransactionNo)
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
        
        String lsCategory = fsCateogry != null && !"".equals(fsCateogry) 
                                                    ? " AND a.sDescript LIKE " + SQLUtil.toSQL("%"+fsCateogry)
                                                    : "";
        
        String lsTransactionDate = fdTransactionDate != null && !"".equals(fdTransactionDate) && !"1900-01-01".equals(fdTransactionDate) 
                                                    ? " AND a.dTransact = " + SQLUtil.toSQL(fdTransactionDate)
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
                 + lsCategory
                 + lsTransactionDate
                 + lsTransactionNo
                );
        
        if (lsTransStat != null && !"".equals(lsTransStat)) {
            lsSQL = lsSQL + lsTransStat;
        }

        System.out.println("Executing SQL: " + lsSQL);
        poJSON = ShowDialogFX.Browse(poGRider,
                lsSQL,
                "",
                "Transaction Date»Transaction No»Branch»Department»Category",
                "dTransact»sTransNox»Branch»Department»Category", // TODO
                "a.dTransact»a.sTransNox»Branch»Department»Category", //TODO
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
    
    public JSONObject SearchDestination(String value, boolean byCode) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            Master().setDestination(object.getModel().getBranchCode());
        }
        return poJSON;
    }
     
    public JSONObject SearchBranch(String value, boolean byCode, boolean isSearch) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Branch object = new ParamControllers(poGRider, logwrapr).Branch();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            if(isSearch){
                setSearchBranch(object.getModel().getDescription());
            } else {
                Master().setBranchCode(object.getModel().getBranchCode());
            }
        }

        return poJSON;
    }
    
    public JSONObject SearchDepartment(String value, boolean byCode, boolean isSearch) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Department object = new ParamControllers(poGRider, logwrapr).Department();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            if(isSearch){
                setSearchDepartment(object.getModel().getDescription());
            } else {
                Master().setDepartmentId(object.getModel().getDepartmentId());
            }
        }
        return poJSON;
    }
    
    public JSONObject SearchCategory(String value, boolean byCode, boolean isSearch) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        
        CategoryLevel2 object = new ParamControllers(poGRider, logwrapr).CategoryLevel2();
        String lsSQL = MiscUtil.addCondition(object.getSQ_Browse(), "cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                                            + " AND sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryId()));
        
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
                if(isSearch){
                    setSearchCategory(object.getModel().getDescription());
                } else {
                    Master().setCategoryLevel2(object.getModel().getCategoryId());
                }
            }
        } else {
            poJSON = new JSONObject();
            poJSON.put("result", "error");
            poJSON.put("message", "No record loaded.");
        }
        
        return poJSON;
    }
    
    public JSONObject SearchBrand(String value, boolean byCode, int row)
            throws ExceptionInInitializerError,
            SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        //TODO
//        if(Master().getCategory() == null || "".equals(Master().getCategory())){
//            poJSON.put("result", "error");
//            poJSON.put("message", "Category is not set.");
//            return poJSON;
//        }
        
        Brand object = new ParamControllers(poGRider, logwrapr).Brand();
        object.setRecordStatus(RecordStatus.ACTIVE);

        poJSON = object.searchRecord(value, byCode, Master().getIndustryId());
        if ("success".equals((String) poJSON.get("result"))) {
            if (!object.getModel().getBrandId().equals(Detail(row).getBrandId())) {
                Detail(row).setModelId("");
                Detail(row).setStockId("");
                Detail(row).setDescription("");
            }

            Detail(row).setBrandId(object.getModel().getBrandId());
        }
        return poJSON;
    }

    public JSONObject SearchModel(String value, boolean byCode, int row)
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        poJSON.put("row", row);

        if(Detail(row).getBrandId() == null || "".equals(Detail(row).getBrandId())){
            poJSON.put("result", "error");
            poJSON.put("message", "Brand is not set.");
            return poJSON;
        }
        
        ModelVariant object = new ParamControllers(poGRider, logwrapr).ModelVariant();
        object.setRecordStatus(RecordStatus.ACTIVE);
        System.out.println("Brand ID : "  + Detail(row).getBrandId());
        poJSON = object.searchRecordByModel(value, byCode, Detail(row).getBrandId());
        poJSON.put("row", row);
        if ("success".equals((String) poJSON.get("result"))) {
            if (!object.getModel().getModelId().equals(Detail(row).getModelId())) {
                Detail(row).setStockId("");
                Detail(row).setDescription("");
            }
            
            Detail(row).setModelId(object.getModel().getModelId());
        }

        return poJSON;
    }
    
    public JSONObject SearchInventory(String value, boolean byCode, int row) throws SQLException, GuanzonException {
        poJSON = new JSONObject();
        poJSON.put("row", row);
        
//        if(Master().getCategory()== null || "".equals(Detail(row).getCategory())){
//            poJSON.put("result", "error");
//            poJSON.put("message", "Category is not set.");
//            return poJSON;
//        }
        String lsBrand = Detail(row).getBrandId() != null && !"".equals(Detail(row).getBrandId()) 
                                                    ? " AND a.sBrandIDx = " + SQLUtil.toSQL(Detail(row).getBrandId())
                                                    : "";
        String lsModel = Detail(row).getModelId()!= null && !"".equals(Detail(row).getModelId()) 
                                                    ? " AND a.sModelIDx = " + SQLUtil.toSQL(Detail(row).getModelId())
                                                    : "";
        Inventory object = new InvControllers(poGRider, logwrapr).Inventory();
        String lsSQL = MiscUtil.addCondition(object.getSQ_Browse(), 
                                            // " a.cRecdStat = " + SQLUtil.toSQL(RecordStatus.ACTIVE)
                                            " a.sCategCd1 = " + SQLUtil.toSQL(Master().getCategoryCode())
                                            + " AND a.sIndstCdx = " + SQLUtil.toSQL(Master().getIndustryId())
//                                            + " AND a.sCategCd2 = " + SQLUtil.toSQL(Master().getCategory())
                                            + lsBrand
                                            + lsModel
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
                poJSON = checkExistingDetail(row,
                        object.getModel().getStockId(),
                        object.getModel().getDescription()
                        );
                if ("error".equals((String) poJSON.get("result"))) {
                    return poJSON;
                }

                Detail(row).setStockId(object.getModel().getStockId());
                Detail(row).setDescription(object.getModel().getDescription());
                Detail(row).setBrandId(object.getModel().getBrandId());
                Detail(row).setModelId(object.getModel().getModelId());
                   
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
        
        poJSON.put("row", row);
        return poJSON;
    }
    
    public JSONObject SearchSupplier(String value, boolean byCode, int row) throws SQLException, GuanzonException {
        Client object = new ClientControllers(poGRider, logwrapr).Client();
        object.Master().setRecordStatus(RecordStatus.ACTIVE);
        object.Master().setClientType("1");
        poJSON = object.Master().searchRecord(value, byCode);

        if ("success".equals((String) poJSON.get("result"))) {
            poJSON = checkExistingSupplier(row,object.Master().getModel().getClientId());
            if ("success".equals((String) poJSON.get("result"))) {
                return poJSON;
            }
            POQuotationRequestSupplierList(row).setSupplierId(object.Master().getModel().getClientId());
            POQuotationRequestSupplierList(row).setAddressId(object.ClientAddress().getModel().getAddressId());
            POQuotationRequestSupplierList(row).setContactId(object.ClientInstitutionContact().getModel().getClientId());
        }

        poJSON.put("row", row);
        return poJSON;
    }

    public JSONObject SearchTerm(String value, boolean byCode, int row) throws ExceptionInInitializerError, SQLException, GuanzonException {
        Term object = new ParamControllers(poGRider, logwrapr).Term();
        object.setRecordStatus("1");

        poJSON = object.searchRecord(value, byCode);
        if ("success".equals((String) poJSON.get("result"))) {
            POQuotationRequestSupplierList(row).setTerm(object.getModel().getTermId());
        }

        return poJSON;
    }
    
    /*Validate*/
    public JSONObject checkExistingDetail(int row, String stockId, String description){
        poJSON = new JSONObject();
        poJSON.put("row", row);
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
                    if(stockId.equals(Detail(lnCtr).getStockId()) || description.equals(Detail(lnCtr).getDescription())){
                        if(Detail(lnCtr).isReverse()){
                            poJSON.put("result", "error");
                            poJSON.put("message", "Item Description already exists in the transaction detail at row "+lnRow+".");
                            poJSON.put("row", lnCtr);
                            return poJSON;
                        }
                    }
                }    
            }
        }
        
        poJSON.put("result", "success");
        poJSON.put("message", "success");
        return poJSON;
    }
    
    private JSONObject checkExistingSupplier(int row, String supplierId){
        poJSON.put("row", row);
        int lnRow = 1;
        for(int lnCtr = 0; lnRow <= getPOQuotationRequestSupplierCount()- 1; lnRow++){
            if(Detail(lnCtr).isReverse()){
                lnRow++;
            }
            if (POQuotationRequestSupplierList(lnCtr).getSupplierId().equals(supplierId)) {
                if(POQuotationRequestSupplierList(lnCtr).isReverse()){
                    poJSON.put("result", "error");
                    poJSON.put("message", "Supplier already exists in the table at row "+lnRow+".");
                    poJSON.put("row", lnRow);
                    return poJSON;
                } else {
                    POQuotationRequestSupplierList(lnCtr).isReverse(true);
                    break;
                }
            } 
        }
        
        poJSON.put("row", row);
        return poJSON;
    }
    
    /*Load*/
    public JSONObject loadPOQuotationRequestList(String branch, String department, String category2, Date date, String referenceNo) {
        try {

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
                    + " AND c.sBranchNm LIKE " + SQLUtil.toSQL("%" + branch)
                    + " AND f.sDescript LIKE " + SQLUtil.toSQL("%" + category2) //TODO
                    + " AND d.sDeptName LIKE " + SQLUtil.toSQL("%" + department) //TODO
                    + " AND a.sTransNox LIKE " + SQLUtil.toSQL("%" + referenceNo)
//                    + " AND a.cProcessd = '0' "
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
                    System.out.println("------------------------------------------------------------------------------");

                    paMasterList.add(POQuotationRequestMaster());
                    paMasterList.get(paMasterList.size() - 1).openRecord(loRS.getString("sTransNox"));
                    lnctr++;
                }

                System.out.println("Records found: " + lnctr);
                poJSON.put("result", "success");
                poJSON.put("message", "Record loaded successfully.");
            } else {
                paMasterList = new ArrayList<>();
                paMasterList.add(POQuotationRequestMaster());
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
    
    public JSONObject loadPOQuotationRequestSupplierList()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        paSuppliers = new ArrayList<>();

        List loList = getPOQuotationRequestSupplier();
        for (int lnCtr = 0; lnCtr <= loList.size() - 1; lnCtr++) {
            paSuppliers.add(POQuotationRequestSupplier());
            poJSON = paSuppliers.get(getPOQuotationRequestSupplierCount()- 1).openRecord((String) loList.get(lnCtr), lnCtr+1);
            if ("success".equals((String) poJSON.get("result"))) {
                if(Master().getEditMode() == EditMode.UPDATE){
                   poJSON = paSuppliers.get(getPOQuotationRequestSupplierCount() - 1).updateRecord();
                }
            }
            
        }
        return poJSON;
    }
    
    
    public void ReloadDetail() throws CloneNotSupportedException{
        int lnCtr = getDetailCount() - 1;
        while (lnCtr >= 0) {
            if ((Detail(lnCtr).getStockId() == null || "".equals(Detail(lnCtr).getStockId()))
                    && (Detail(lnCtr).getDescription()== null || "".equals(Detail(lnCtr).getDescription()))) {
                
                if(Detail(lnCtr).getEditMode() == EditMode.ADDNEW){
                    deleteDetail(lnCtr); 
                    //Detail().remove(lnCtr);
                } else {
                    Detail(lnCtr).isReverse(false);
                    removeDetail(Detail(lnCtr));
                }
            }
            lnCtr--;
        }

        if ((getDetailCount() - 1) >= 0) {
            if ((Detail(getDetailCount() - 1).getStockId() != null
                    && !"".equals(Detail(getDetailCount() - 1).getStockId()))
                || (Detail(getDetailCount() - 1).getDescription()!= null
                    && !"".equals(Detail(getDetailCount() - 1).getDescription()))) {
                AddDetail();
            }
        }

        if ((getDetailCount() - 1) < 0) {
            AddDetail();
        }
    }
    
    public void ReloadSupplier() 
            throws CloneNotSupportedException, 
            SQLException, 
            GuanzonException{     
        
        if(getEditMode() == EditMode.ADDNEW || getEditMode() == EditMode.UPDATE){
        } else {
            return;
        }
        int lnRow = getPOQuotationRequestSupplierCount() - 1;
        while (lnRow >= 0) {
            if (paSuppliers.get(lnRow).getSupplierId()== null || "".equals(paSuppliers.get(lnRow).getSupplierId())) {
                if(paSuppliers.get(lnRow).getEditMode() == EditMode.ADDNEW){
                    paSuppliers.remove(lnRow);
                } else {
                    paSuppliers.get(lnRow).isReverse(false);
                }
            }
            lnRow--;
        }

        if ((getPOQuotationRequestSupplierCount()- 1) >= 0) {
            if (paSuppliers.get(getPOQuotationRequestSupplierCount() - 1).getSupplierId()!= null
                    && !"".equals(paSuppliers.get(getPOQuotationRequestSupplierCount() - 1).getSupplierId())) {
                AddPOQuotationRequestSupplier();
            }
        }

        if ((getPOQuotationRequestSupplierCount() - 1) < 0) {
            AddPOQuotationRequestSupplier();
        }
    }
    
    private Model_PO_Quotation_Request_Master POQuotationRequestMaster() {
        return new QuotationModels(poGRider).POQuotationRequestMaster();
    }
    
    private Model_PO_Quotation_Request_Supplier POQuotationRequestSupplier() {
        return new QuotationModels(poGRider).POQuotationRequestSupplier();
    }

    @Override
    public Model_PO_Quotation_Request_Master Master() {
        return (Model_PO_Quotation_Request_Master) poMaster;
    }

    public Model_PO_Quotation_Request_Detail getDetail() {
        return (Model_PO_Quotation_Request_Detail) poDetail;
    }
    
    public List<Model_PO_Quotation_Request_Supplier> POQuotationRequestSupplierList() {
        return paSuppliers;
    }
    
    public Model_PO_Quotation_Request_Master POQuotationRequestList(int row) {
        return (Model_PO_Quotation_Request_Master) paMasterList.get(row);
    }

    @Override
    public Model_PO_Quotation_Request_Detail Detail(int row) {
        return (Model_PO_Quotation_Request_Detail) paDetail.get(row);
    }
    
    public Model_PO_Quotation_Request_Supplier POQuotationRequestSupplierList(int row) {
        return (Model_PO_Quotation_Request_Supplier) paSuppliers.get(row);
    }

    private Model_PO_Quotation_Request_Detail DetailRemoved(int row) {
        return (Model_PO_Quotation_Request_Detail) paDetailRemoved.get(row);
    }
    
    @Override
    public int getDetailCount() {
        if (paDetail == null) {
            paDetail = new ArrayList<>();
        }

        return paDetail.size();
    }
    
    public int getPOQuotationRequestCount() {
        if (paMasterList == null) {
            paMasterList = new ArrayList<>();
        }

        return paMasterList.size();
    }
    
    public int getPOQuotationRequestSupplierCount() {
        if (paSuppliers == null) {
            paSuppliers = new ArrayList<>();
        }

        return paSuppliers.size();
    }
    
    private int getDetailRemovedCount() {
        if (paDetailRemoved == null) {
            paDetailRemoved = new ArrayList<>();
        }

        return paDetailRemoved.size();
    }

    private List getPOQuotationRequestSupplier() throws SQLException, GuanzonException {
        String lsSQL = MiscUtil.makeSelect(POQuotationRequestSupplier());
        lsSQL = MiscUtil.addCondition(lsSQL, " sTransNox = " + SQLUtil.toSQL(Master().getTransactionNo()));
        System.out.println("Executing SQL: " + lsSQL);
        ResultSet loRS = poGRider.executeQuery(lsSQL);
        List<String> loList = new ArrayList();
        while (loRS.next()) {
             loList.add(loRS.getString("sTransNox")); 
        }
        return loList;
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
    
    public JSONObject AddPOQuotationRequestSupplier()
            throws SQLException,
            GuanzonException {
        poJSON = new JSONObject();
        
        if (paSuppliers == null) {
            paSuppliers = new ArrayList<>();
        }

        if (paSuppliers.isEmpty()) {
            paSuppliers.add(POQuotationRequestSupplier());
            poJSON = paSuppliers.get(getPOQuotationRequestSupplierCount()- 1).newRecord();
        } else {
            if (paSuppliers.get(paSuppliers.size() - 1).getSupplierId() != null && !"".equals(paSuppliers.get(paSuppliers.size() - 1).getSupplierId())) {
                paSuppliers.add(POQuotationRequestSupplier());
                poJSON = paSuppliers.get(getPOQuotationRequestSupplierCount()- 1).newRecord();
            } else {
                poJSON.put("result", "error");
                poJSON.put("message", "Unable to add supplier.");
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

    private void removeDetail(Model_PO_Quotation_Request_Detail item) {
        if (paDetailRemoved == null) {
            paDetailRemoved = new ArrayList<>();
        }
        
        paDetailRemoved.add(item);
    }

    public void resetMaster() {
        poMaster = new QuotationModels(poGRider).POQuotationRequestMaster();
    }
    
    public void resetOthers() {
        paSuppliers = new ArrayList<>();
        paDetailRemoved = new ArrayList<>();
        paSuppliersRemoved = new ArrayList<>();
        setSearchBranch("");
        setSearchCategory("");
        setSearchDepartment("");
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
        
        if (POQuotationRequestStatus.CONFIRMED.equals(Master().getTransactionStatus())) {
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
            Master().setPrepared(poGRider.Encrypt(poGRider.getUserID()));
            Master().setPreparedDate(poGRider.getServerDate());
        }
        
        Master().setModifyingId(poGRider.Encrypt(poGRider.getUserID()));
        Master().setModifiedDate(poGRider.getServerDate());

        Iterator<Model> detail = Detail().iterator();
        while (detail.hasNext()) {
            Model item = detail.next();
            if ((item.getValue("sDescript") == null || "".equals(item.getValue("sDescript")))
                  &&  (item.getValue("sStockIDx") == null || "".equals(item.getValue("sStockIDx")))) {
                detail.remove();

                if (item.getEditMode() == EditMode.UPDATE) {
                    paDetailRemoved.add(item);
                }

            }
        }
        
        //remove supplier without details
        Iterator<Model_PO_Quotation_Request_Supplier> others = paSuppliers.iterator();
        while (others.hasNext()) {
            Model_PO_Quotation_Request_Supplier item = others.next();
            if (item.getSupplierId() == null || "".equals(item.getSupplierId())){
                others.remove();
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
        
        for (int lnCtr = 0; lnCtr <= getPOQuotationRequestSupplierCount()- 1; lnCtr++) {
            POQuotationRequestSupplierList(lnCtr).setTransactionNo(Master().getTransactionNo());
            POQuotationRequestSupplierList(lnCtr).setEntryNo(lnCtr+1);
//            POQuotationRequestSupplierList(lnCtr).setIndustryId(Master().getIndustryId());
//            POQuotationRequestSupplierList(lnCtr).setCategoryCode(Master().getCategoryCode());
        }
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    @Override
    public JSONObject save() {
        /*Put saving business rules here*/
        return isEntryOkay(POQuotationRequestStatus.OPEN);
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
            //Save PO Quotation Request Supplier
            for (int lnCtr = 0; lnCtr <= getPOQuotationRequestSupplierCount()- 1; lnCtr++) {
                if (paSuppliers.get(lnCtr).getEditMode() == EditMode.ADDNEW || paSuppliers.get(lnCtr).getEditMode() == EditMode.UPDATE) {
                    paSuppliers.get(lnCtr).setModifiedDate(poGRider.getServerDate());
                    poJSON = paSuppliers.get(lnCtr).saveRecord();
                    if ("error".equals((String) poJSON.get("result"))) {
                        System.out.println("Save Sales Inquiry Requirements " + (String) poJSON.get("message"));
                        poJSON.put("result", "error");
                        poJSON.put("message", (String) poJSON.get("message"));
                        return poJSON;
                    }
                }
            }
        } catch (SQLException | GuanzonException ex) {
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
            
            Master().setBranchCode(poGRider.getBranchCode());
            Master().setIndustryId(psIndustryId);
            Master().setCategoryCode(psCategorCd);
            Master().setTransactionDate(poGRider.getServerDate());
            Master().setTransactionStatus(POQuotationRequestStatus.OPEN);
            
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
        GValidator loValidator = POQuotationRequestValidatorFactory.make(Master().getIndustryId());

        loValidator.setApplicationDriver(poGRider);
        loValidator.setTransactionStatus(status);
        loValidator.setMaster(poMaster);
//        loValidator.setDetail(paDetail);

        poJSON = loValidator.validate();

        return poJSON;
    }

    @Override
    public void initSQL() {
        SQL_BROWSE =  " SELECT         "
                    + "    a.sTransNox "
                    + "  , a.sIndstCdx "
                    + "  , a.sBranchCd "
                    + "  , a.sDeptIDxx "
                    + "  , a.sCategrCd "
                    + "  , a.dTransact "
                    + "  , a.sCategCd2 "
                    + "  , a.sDestinat "
                    + "  , a.sReferNox "
                    + "  , a.cTranStat "
                    + "  , b.sDescript AS Industry   "
                    + "  , c.sBranchNm AS Branch     "
                    + "  , d.sDeptName AS Department "
                    + "  , e.sDescript AS Category   "
                    + "  , f.sDescript AS Category2  "
                    + " FROM po_quotation_request_master a "
                    + " LEFT JOIN industry b ON b.sIndstCdx = a.sIndstCdx        "
                    + " LEFT JOIN branch c ON c.sBranchCd = a.sBranchCd          "
                    + " LEFT JOIN department d ON d.sDeptIDxx = a.sDeptIDxx      "
                    + " LEFT JOIN category e ON e.sCategrCd = a.sCategrCd        "
                    + " LEFT JOIN category_level2 f ON f.sCategrCd = a.sCategCd2 " ;
        
    }
}
