/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.purchasing.t2.model;

import java.sql.SQLException;
import java.util.Date;
import org.guanzon.appdriver.agent.services.Model;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.guanzon.appdriver.constant.EditMode;
import org.guanzon.cas.client.model.Model_Client_Address;
import org.guanzon.cas.client.model.Model_Client_Master;
import org.guanzon.cas.client.model.Model_Client_Mobile;
import org.guanzon.cas.client.services.ClientModels;
import org.guanzon.cas.parameter.model.Model_Branch;
import org.guanzon.cas.parameter.model.Model_Category_Level2;
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.model.Model_Department;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.model.Model_Term;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.purchasing.t2.services.QuotationModels;
import ph.com.guanzongroup.cas.purchasing.t2.status.POQuotationStatus;

/**
 *
 * @author Arsiela
 */
public class Model_PO_Quotation_Master extends Model {
    
    Model_Industry poIndustry;
    Model_Company poCompany;
    Model_Branch poBranch;
    Model_Department poDepartment;
    Model_Category_Level2 poCategory;
    Model_Term poTerm;
    Model_Client_Master poSupplier;
    Model_Client_Address poSupplierAddress;
    Model_Client_Mobile poSupplierMobile;
    
    Model_PO_Quotation_Request_Master poQuotationRequest;
    
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
            poEntity.updateNull("dReferDte");
            poEntity.updateNull("dValidity");
            poEntity.updateNull("dModified");
            poEntity.updateNull("dLastMail");
            poEntity.updateObject("nGrossAmt", 0.0000);
            poEntity.updateObject("nDiscount", 0.00);
            poEntity.updateObject("nAddDiscx", 0.0000);
            poEntity.updateObject("nVATRatex", 0.00);
            poEntity.updateObject("nVATAmtxx", 0.0000);
            poEntity.updateObject("nFreightx", 0.00);
            poEntity.updateObject("nTWithHld", 0.00);
            poEntity.updateObject("nTranTotl", 0.0000);
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateObject("nMailSent", 0);
//            poEntity.updateString("cProcessd", "0");
            poEntity.updateString("cTranStat", POQuotationStatus.OPEN);
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sTransNox";

            //initialize reference objects
            ParamModels model = new ParamModels(poGRider);
            poBranch = model.Branch();
            poIndustry = model.Industry();
            poCompany = model.Company();
            poDepartment = model.Department();
            poCategory = model.Category2();
            poTerm = model.Term();
            
            ClientModels clientModel = new ClientModels(poGRider);
            poSupplier = clientModel.ClientMaster();
            poSupplierAddress = clientModel.ClientAddress();
            poSupplierMobile = clientModel.ClientMobile();
            
            QuotationModels quotationModel = new QuotationModels(poGRider);
            poQuotationRequest = quotationModel.POQuotationRequestMaster();
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
    
    public JSONObject setTransactionDate(Date transactionDate) {
        return setValue("dTransact", transactionDate);
    }

    public Date getTransactionDate() {
        return (Date) getValue("dTransact");
    }

    public JSONObject setIndustryId(String industryId) {
        return setValue("sIndstCdx", industryId);
    }

    public String getIndustryId() {
        return (String) getValue("sIndstCdx");
    }

    public JSONObject setCompanyId(String companyId) {
        return setValue("sCompnyID", companyId);
    }

    public String getCompanyId() {
        return (String) getValue("sCompnyID");
    }

    public JSONObject setCategoryCode(String categoryCode) {
        return setValue("sCategrCd", categoryCode);
    }

    public String getCategoryCode() {
        return (String) getValue("sCategrCd");
    }

    public JSONObject setBranchCode(String branchCode) {
        return setValue("sBranchCd", branchCode);
    }

    public String getBranchCode() {
        return (String) getValue("sBranchCd");
    }

    public JSONObject setSupplierId(String supplier) {
        return setValue("sSupplier", supplier);
    }
    
    public String getSupplierId() {
        return (String) getValue("sSupplier");
    }

    public JSONObject setAddressId(String addressID) {
        return setValue("sAddrssID", addressID);
    }

    public String getAddressId() {
        return (String) getValue("sAddrssID");
    }

    public JSONObject setContactId(String contactID) {
        return setValue("sContctID", contactID);
    }

    public String getContactId() {
        return (String) getValue("sContctID");
    }

    public JSONObject setReferenceNo(String referenceNo) {
        return setValue("sReferNox", referenceNo);
    }

    public String getReferenceNo() {
        return (String) getValue("sReferNox");
    }
    
    public JSONObject setReferenceDate(Date referenceDate) {
        return setValue("dReferDte", referenceDate);
    }

    public Date getReferenceDate() {
        return (Date) getValue("dReferDte");
    }
    
    public JSONObject setValidityDate(Date validityDate) {
        return setValue("dValidity", validityDate);
    }

    public Date getValidityDate() {
        return (Date) getValue("dValidity");
    }

    public JSONObject setTerm(String term) {
        return setValue("sTermCode", term);
    }

    public String getTerm() {
        return (String) getValue("sTermCode");
    }
    
    public JSONObject setGrossAmount(Double grossAmount) {
        return setValue("nGrossAmt", grossAmount);
    }

    public Double getGrossAmount() {
        if (getValue("nGrossAmt") == null || "".equals(getValue("nGrossAmt"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nGrossAmt").toString());
    }
    
    public JSONObject setDiscountRate(Double grossAmount) {
        return setValue("nDiscount", grossAmount);
    }

    public Double getDiscountRate() {
        if (getValue("nDiscount") == null || "".equals(getValue("nDiscount"))) {
            return 0.00;
        }
        return Double.valueOf(getValue("nDiscount").toString());
    }
    
    public JSONObject setAdditionalDiscountAmount(Double additionalDiscountAmount) {
        return setValue("nAddDiscx", additionalDiscountAmount);
    }

    public Double getAdditionalDiscountAmount() {
        if (getValue("nAddDiscx") == null || "".equals(getValue("nAddDiscx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nAddDiscx").toString());
    }
    
    public JSONObject setVatRate(Double vatRate) {
        return setValue("nVATRatex", vatRate);
    }

    public Double getVatRate() {
        if (getValue("nVATRatex") == null || "".equals(getValue("nVATRatex"))) {
            return 0.00;
        }
        return Double.valueOf(getValue("nVATRatex").toString());
    }
    
    public JSONObject setVatAmount(Double vatAmount) {
        return setValue("nAddDiscx", vatAmount);
    }

    public Double getVatAmount() {
        if (getValue("nVATAmtxx") == null || "".equals(getValue("nVATAmtxx"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nVATAmtxx").toString());
    }
    
    public JSONObject isVatable(boolean isVatable) {
        return setValue("cVATAdded", isVatable ? "1" : "0");
    }

    public boolean isVatable() {
        return ((String) getValue("cVATAdded")).equals("1");
    }
    
    
    public JSONObject setTaxAmount(Double taxAmount) {
        return setValue("nTWithHld", taxAmount);
    }

    public Double getTaxAmount() {
        if (getValue("nTWithHld") == null || "".equals(getValue("nTWithHld"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nTWithHld").toString());
    }
    
    public JSONObject setFreightAmount(Double freighAmount) {
        return setValue("nFreightx", freighAmount);
    }

    public Double getFreightAmount() {
        if (getValue("nFreightx") == null || "".equals(getValue("nFreightx"))) {
            return 0.00;
        }
        return Double.valueOf(getValue("nTWithHld").toString());
    }
    
    public JSONObject setTransactionTotal(Double transactionTotal) {
        return setValue("nTranTotl", transactionTotal);
    }

    public Double getTransactionTotal() {
        if (getValue("nTranTotl") == null || "".equals(getValue("nTranTotl"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nTranTotl").toString());
    }
    
    public JSONObject setWitholdingTax(Double transactionTotal) {
        return setValue("nTWithHld", transactionTotal);
    }

    public Double getWitholdingTax() {
        if (getValue("nTWithHld") == null || "".equals(getValue("nTWithHld"))) {
            return 0.00;
        }
        return Double.valueOf(getValue("nTWithHld").toString());
    }

    public JSONObject setRemarks(String remarks) {
        return setValue("sRemarksx", remarks);
    }

    public String getRemarks() {
        return (String) getValue("sRemarksx");
    }

    public JSONObject setSourceNo(String sourceNo) {
        return setValue("sSourceNo", sourceNo);
    }

    public String getSourceNo() {
        return (String) getValue("sSourceNo");
    }

    public JSONObject setSourceCode(String sourceCode) {
        return setValue("sSourceCd", sourceCode);
    }

    public String getSourceCode() {
        return (String) getValue("sSourceCd");
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
    
    public JSONObject isMailSent(boolean isMailSent) {
        return setValue("cMailSent", isMailSent ? "1" : "0");
    }

    public boolean isMailSent() {
        return ((String) getValue("cMailSent")).equals("1");
    }
    
    public JSONObject setNumberMailSent(Number numberMailSent) {
        return setValue("nMailSent", numberMailSent);
    }

    public Number getNumberMailSent() {
        return (Number) getValue("nMailSent");
    }

    public JSONObject setLastMail(Date lastMail) {
        return setValue("dLastMail", lastMail);
    }

    public Date getLastMail() {
        return (Date) getValue("dLastMail");
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

    public Model_Company Company() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sCompnyID"))) {
            if (poCompany.getEditMode() == EditMode.READY
                    && poCompany.getCompanyId().equals((String) getValue("sCompnyID"))) {
                return poCompany;
            } else {
                poJSON = poCompany.openRecord((String) getValue("sCompnyID"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poCompany;
                } else {
                    poCompany.initialize();
                    return poCompany;
                }
            }
        } else {
            poCompany.initialize();
            return poCompany;
        }
    }
    
    public Model_Category_Level2 Category2() throws GuanzonException, SQLException {
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
    
    public Model_Client_Master Client() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sClientID"))) {
            if (poSupplier.getEditMode() == EditMode.READY
                    && poSupplier.getClientId().equals((String) getValue("sClientID"))) {
                return poSupplier;
            } else {
                poJSON = poSupplier.openRecord((String) getValue("sClientID"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poSupplier;
                } else {
                    poSupplier.initialize();
                    return poSupplier;
                }
            }
        } else {
            poSupplier.initialize();
            return poSupplier;
        }
    }
    
    public Model_Client_Address ClientAddress() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sClientID"))) {
            if (poSupplierAddress.getEditMode() == EditMode.READY
                    && poSupplierAddress.getClientId().equals((String) getValue("sClientID"))) {
                return poSupplierAddress;
            } else {
                poJSON = poSupplierAddress.openRecord((String) getValue("sClientID")); //sAddrssID

                if ("success".equals((String) poJSON.get("result"))) {
                    return poSupplierAddress;
                } else {
                    poSupplierAddress.initialize();
                    return poSupplierAddress;
                }
            }
        } else {
            poSupplierAddress.initialize();
            return poSupplierAddress;
        }
    }
    
    public Model_Client_Mobile ClientMobile() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sContctID"))) {
            if (poSupplierMobile.getEditMode() == EditMode.READY
                    && poSupplierMobile.getClientId().equals((String) getValue("sContctID"))) {
                return poSupplierMobile;
            } else {
                poJSON = poSupplierMobile.openRecord((String) getValue("sContctID"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poSupplierMobile;
                } else {
                    poSupplierMobile.initialize();
                    return poSupplierMobile;
                }
            }
        } else {
            poSupplierMobile.initialize();
            return poSupplierMobile;
        }
    }
    
    public Model_PO_Quotation_Request_Master POQuotationRequest() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sSourceNo"))) {
            if (poQuotationRequest.getEditMode() == EditMode.READY
                    && poQuotationRequest.getIndustryId().equals((String) getValue("sSourceNo"))) {
                return poQuotationRequest;
            } else {
                poJSON = poQuotationRequest.openRecord((String) getValue("sSourceNo"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poQuotationRequest;
                } else {
                    poQuotationRequest.initialize();
                    return poQuotationRequest;
                }
            }
        } else {
            poQuotationRequest.initialize();
            return poQuotationRequest;
        }
    }
    //end reference object models
}
