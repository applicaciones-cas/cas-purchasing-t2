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
import org.guanzon.cas.parameter.model.Model_Category_Level2;
import org.guanzon.cas.parameter.model.Model_Company;
import org.guanzon.cas.parameter.model.Model_Industry;
import org.guanzon.cas.parameter.model.Model_Term;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;
import ph.com.guanzongroup.cas.purchasing.t2.services.QuotationModels;

/**
 *
 * @author Arsiela
 */
public class Model_PO_Quotation_Request_Supplier extends Model {
    
    String psBrandId = "";
    String psModelId = "";
    
    //reference objects
    Model_Industry poIndustry;
    Model_Company poCompany;
    Model_Term poTerm;
    Model_Client_Master poSupplier;
    Model_Client_Address poSupplierAddress;
    Model_Client_Mobile poSupplierMobile;
    Model_Category_Level2 poCategory;
    
    Model_PO_Quotation_Request_Master poPOQuotationRequest;
    
    @Override
    public void initialize() {
        try {
            poEntity = MiscUtil.xml2ResultSet(System.getProperty("sys.default.path.metadata") + XML, getTable());

            poEntity.last();
            poEntity.moveToInsertRow();

            MiscUtil.initRowSet(poEntity);

            //assign default values
            poEntity.updateNull("dModified");
            poEntity.updateObject("nEntryNox", 0);
            poEntity.updateObject("cReversex", "1");
            poEntity.updateObject("cSendStat", "0");
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sTransNox";
            ID2 = "nEntryNox";

            //initialize reference objects
            ParamModels model = new ParamModels(poGRider);
            poIndustry = model.Industry();
            poCompany = model.Company();
            poCategory = model.Category2();
            poTerm = model.Term();
            
            ClientModels clientModel = new ClientModels(poGRider);
            poSupplier = clientModel.ClientMaster();
            poSupplierAddress = clientModel.ClientAddress();
            poSupplierMobile = clientModel.ClientMobile();
            
            
            QuotationModels quotationRequestModel = new QuotationModels(poGRider);
            poPOQuotationRequest = quotationRequestModel.POQuotationRequestMaster();
            
            //end - initialize reference objects

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
    
    public JSONObject setEntryNo(int entryNo) {
        return setValue("nEntryNox", entryNo);
    }

    public int getEntryNo() {
        if (getValue("nEntryNox") == null || "".equals(getValue("nEntryNox"))) {
            return 0;
        }
        return (int) getValue("nEntryNox");
    }

    public JSONObject setCompanyId(String companyId) {
        return setValue("sCompnyID", companyId);
    }

    public String getCompanyId() {
        return (String) getValue("sCompnyID");
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

    public JSONObject setTerm(String termCode) {
        return setValue("sTermCode", termCode);
    }

    public String getTerm() {
        return (String) getValue("sTermCode");
    }
    
    public JSONObject isReverse(boolean isReverse) {
        return setValue("cReversex", isReverse ? "1" : "0");
    }

    public boolean isReverse() {
        return ((String) getValue("cReversex")).equals("1");
    }
    
    public JSONObject isSent(boolean sent) {
        return setValue("cSendStat", sent ? "1" : "0");
    }

    public boolean isSent() {
        return ((String) getValue("cSendStat")).equals("1");
    }

    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }

    @Override
    public String getNextCode() {
        return "";
    }

    //reference object models
//    public Model_Industry Industry() throws SQLException, GuanzonException {
//        if (!"".equals((String) getValue("sIndstCdx"))) {
//            if (poIndustry.getEditMode() == EditMode.READY
//                    && poIndustry.getIndustryId().equals((String) getValue("sIndstCdx"))) {
//                return poIndustry;
//            } else {
//                poJSON = poIndustry.openRecord((String) getValue("sIndstCdx"));
//
//                if ("success".equals((String) poJSON.get("result"))) {
//                    return poIndustry;
//                } else {
//                    poIndustry.initialize();
//                    return poIndustry;
//                }
//            }
//        } else {
//            poIndustry.initialize();
//            return poIndustry;
//        }
//    }

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
    
//    public Model_Category_Level2 Category2() throws GuanzonException, SQLException {
//        if (!"".equals((String) getValue("sCategrCd"))) {
//            if (poCategory.getEditMode() == EditMode.READY
//                    && poCategory.getCategoryId().equals((String) getValue("sCategrCd"))) {
//                return poCategory;
//            } else {
//                poJSON = poCategory.openRecord((String) getValue("sCategrCd"));
//
//                if ("success".equals((String) poJSON.get("result"))) {
//                    return poCategory;
//                } else {
//                    poCategory.initialize();
//                    return poCategory;
//                }
//            }
//        } else {
//            poCategory.initialize();
//            return poCategory;
//        }
//    }
    
    public Model_Term Term() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sTermCode"))) {
            if (poTerm.getEditMode() == EditMode.READY
                    && poTerm.getTermId().equals((String) getValue("sTermCode"))) {
                return poTerm;
            } else {
                poJSON = poTerm.openRecord((String) getValue("sTermCode"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poTerm;
                } else {
                    poTerm.initialize();
                    return poTerm;
                }
            }
        } else {
            poTerm.initialize();
            return poTerm;
        }
    }
    
    public Model_Client_Master Supplier() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sSupplier"))) {
            if (poSupplier.getEditMode() == EditMode.READY
                    && poSupplier.getClientId().equals((String) getValue("sSupplier"))) {
                return poSupplier;
            } else {
                poJSON = poSupplier.openRecord((String) getValue("sSupplier"));

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
        if (!"".equals((String) getValue("sSupplier"))) {
            if (poSupplierAddress.getEditMode() == EditMode.READY
                    && poSupplierAddress.getClientId().equals((String) getValue("sSupplier"))) {
                return poSupplierAddress;
            } else {
                poJSON = poSupplierAddress.openRecord((String) getValue("sSupplier")); //sAddrssID

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
    
    public Model_PO_Quotation_Request_Master POQuotationRequestMaster() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sTransNox"))) {
            if (poPOQuotationRequest.getEditMode() == EditMode.READY
                    && poPOQuotationRequest.getTransactionNo().equals((String) getValue("sTransNox"))) {
                return poPOQuotationRequest;
            } else {
                poJSON = poPOQuotationRequest.openRecord((String) getValue("sTransNox"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poPOQuotationRequest;
                } else {
                    poPOQuotationRequest.initialize();
                    return poPOQuotationRequest;
                }
            }
        } else {
            poPOQuotationRequest.initialize();
            return poPOQuotationRequest;
        }
    }
    //end reference object models
}
