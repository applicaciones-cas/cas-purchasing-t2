/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.purchasing.t2.validator;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.iface.GValidator;
import ph.com.guanzongroup.cas.purchasing.t2.model.Model_PO_Quotation_Detail;
import ph.com.guanzongroup.cas.purchasing.t2.model.Model_PO_Quotation_Master;
import ph.com.guanzongroup.cas.purchasing.t2.status.POQuotationStatus;
import org.json.simple.JSONObject;

/**
 *
 * @author Arsiela
 */
public class POQuotation_Appliances implements GValidator{
    GRiderCAS poGRider;
    String psTranStat;
    JSONObject poJSON;
    
    Model_PO_Quotation_Master poMaster;
    ArrayList<Model_PO_Quotation_Detail> poDetail;

    @Override
    public void setApplicationDriver(Object applicationDriver) {
        poGRider = (GRiderCAS) applicationDriver;
    }

    @Override
    public void setTransactionStatus(String transactionStatus) {
        psTranStat = transactionStatus;
    }

    @Override
    public void setMaster(Object value) {
        poMaster = (Model_PO_Quotation_Master) value;
    }

    @Override
    public void setDetail(ArrayList<Object> value) {
        poDetail.clear();
        for(int lnCtr = 0; lnCtr <= value.size() - 1; lnCtr++){
            poDetail.add((Model_PO_Quotation_Detail) value.get(lnCtr));
        }
    }

    @Override
    public void setOthers(ArrayList<Object> value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JSONObject validate() {
        try {
            switch (psTranStat){
                case POQuotationStatus.OPEN:
                    return validateNew();
                case POQuotationStatus.CONFIRMED:
                    return validateConfirmed();
                case POQuotationStatus.DISAPPROVED:
                    return validateDisApproved();
                case POQuotationStatus.POSTED:
                    return validatePosted();
                case POQuotationStatus.CANCELLED:
                    return validateCancelled();
                case POQuotationStatus.VOID:
                    return validateVoid();
                default:
                    poJSON = new JSONObject();
                    poJSON.put("result", "success");
            }
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        
        return poJSON;
    }
    
    private JSONObject validateNew() throws SQLException{
        poJSON = new JSONObject();
        Date loTransactionDate = poMaster.getTransactionDate();
        Date loValidityDate = poMaster.getValidityDate();
        Date loReferenceDate = poMaster.getValidityDate();
        LocalDate serverDate = strToDate(xsDateShort(poGRider.getServerDate()));
        LocalDate oneYearAgo = serverDate.minusYears(1);
        
        if (loTransactionDate == null) {
            poJSON.put("message", "Invalid Transaction Date.");
            return poJSON;
        }

        if ("1900-01-01".equals(xsDateShort(loTransactionDate))) {
            poJSON.put("message", "Invalid Transaction Date.");
            return poJSON;
        }
        
        if (loValidityDate == null) {
            poJSON.put("message", "Invalid Validity Date.");
            return poJSON;
        }

        if ("1900-01-01".equals(xsDateShort(loReferenceDate))) {
            poJSON.put("message", "Invalid Reference Date.");
            return poJSON;
        }
        
        if (loReferenceDate == null) {
            poJSON.put("message", "Invalid Reference Date.");
            return poJSON;
        }
        
        if (poMaster.getIndustryId() == null) {
            poJSON.put("message", "Industry is not set.");
            return poJSON;
        }
        if (poMaster.getCompanyId() == null || "".equals(poMaster.getCompanyId())) {
            poJSON.put("message", "Company is not set.");
            return poJSON;
        }
        if (poMaster.getCategoryCode()== null || "".equals(poMaster.getCategoryCode())) {
            poJSON.put("message", "Category Code is not set.");
            return poJSON;
        }
        if (poMaster.getBranchCode()== null || "".equals(poMaster.getBranchCode())) {
            poJSON.put("message", "Branch is not set.");
            return poJSON;
        }
        if (poMaster.getSourceNo()== null || "".equals(poMaster.getSourceNo())) {
            poJSON.put("message", "Source No is not set.");
            return poJSON;
        }
        if (poMaster.getSourceCode()== null || "".equals(poMaster.getSourceCode())) {
            poJSON.put("message", "Source Code is not set.");
            return poJSON;
        }
        if (poMaster.getSupplierId()== null || "".equals(poMaster.getSupplierId())) {
            poJSON.put("message", "Supplier is not set.");
            return poJSON;
        }
//        if (poMaster.getAddressId()== null || "".equals(poMaster.getAddressId())) {
//            poJSON.put("message", "Address is not set.");
//            return poJSON;
//        }
//        if (poMaster.getContactId()== null || "".equals(poMaster.getContactId())) {
//            poJSON.put("message", "Contact Number is not set.");
//            return poJSON;
//        }
        if (poMaster.getTerm()== null || "".equals(poMaster.getTerm())) {
            poJSON.put("message", "Term is not set.");
            return poJSON;
        }
        
        if (poMaster.getReferenceNo()== null || "".equals(poMaster.getReferenceNo())) {
            poJSON.put("message", "Reference No is not set.");
            return poJSON;
        }
        if(poMaster.getGrossAmount() <= 0.0000){
            poJSON.put("result","error"); 
            poJSON.put("message", "Invalid gross amount.");
            return poJSON;
        }
        if(poMaster.getTransactionTotal()<= 0.0000){
            poJSON.put("result","error"); 
            poJSON.put("message", "Invalid transaction total.");
            return poJSON;
        }
        if (poMaster.getPrepared()== null || "".equals(poMaster.getPrepared())) {
            poJSON.put("message", "Prepared by is not set.");
            return poJSON;
        }
        if (poMaster.getModifyingId()== null || "".equals(poMaster.getModifyingId())) {
            poJSON.put("message", "Modified by is not set.");
            return poJSON;
        }
        if (poMaster.getTransactionStatus()== null || "".equals(poMaster.getTransactionStatus())) {
            poJSON.put("message", "Transaction Status is not set.");
            return poJSON;
        }
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject validateConfirmed()throws SQLException{
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject validateDisApproved(){
        poJSON = new JSONObject();
                
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject validatePosted(){
        poJSON = new JSONObject();
                
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject validateCancelled() throws SQLException{
        poJSON = new JSONObject();
        
        poJSON.put("result", "success");
        return poJSON;
    }
    
    private JSONObject validateVoid() throws SQLException{
        poJSON = new JSONObject();
        poJSON.put("result", "success");
        return poJSON;
    }
    
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
    
    
}
