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
import org.guanzon.cas.inv.model.Model_Inv_Master;
import org.guanzon.cas.inv.model.Model_Inventory;
import org.guanzon.cas.inv.services.InvModels;
import org.guanzon.cas.parameter.model.Model_Brand;
import org.guanzon.cas.parameter.model.Model_Category_Level2;
import org.guanzon.cas.parameter.model.Model_Color;
import org.guanzon.cas.parameter.model.Model_Model;
import org.guanzon.cas.parameter.model.Model_Model_Variant;
import org.guanzon.cas.parameter.services.ParamModels;
import org.json.simple.JSONObject;

/**
 *
 * @author Arsiela
 */
public class Model_PO_Quotation_Request_Detail extends Model {
    
    String psBrandId = "";
    String psModelId = "";
    
    //reference objects
    Model_Brand poBrand;
    Model_Model poModel;
    Model_Model_Variant poModelVariant;
    Model_Color poColor;
    Model_Inventory poInventory;
    Model_Inv_Master poInvMaster;
    Model_Category_Level2 poCategory;
    
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
            poEntity.updateObject("nQuantity", 0);
            poEntity.updateObject("nUnitPrce", 0.0000);
            poEntity.updateObject("cReversex", "1");
            //end - assign default values

            poEntity.insertRow();
            poEntity.moveToCurrentRow();
            poEntity.absolute(1);

            ID = "sTransNox";
            ID2 = "nEntryNox";

            //initialize reference objects
            ParamModels model = new ParamModels(poGRider);
            poBrand = model.Brand();
            poModel = model.Model();
            poColor = model.Color();
            poModelVariant = model.ModelVariant();
            poCategory = model.Category2();
            
            InvModels invModel = new InvModels(poGRider); 
            poInventory = invModel.Inventory();
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

    public JSONObject setQuantity(Double quantity) {
        return setValue("nQuantity", quantity);
    }

    public Double getQuantity() {
        if (getValue("nQuantity") == null || "".equals(getValue("nQuantity"))) {
            return 0.00;
        }
        return Double.valueOf(getValue("nQuantity").toString());
    }

    public JSONObject setStockId(String stockId) {
        return setValue("sStockIDx", stockId);
    }

    public String getStockId() {
        return (String) getValue("sStockIDx");
    }

    public JSONObject setDescription(String description) {
        return setValue("sDescript", description);
    }

    public String getDescription() {
        return (String) getValue("sDescript");
    }
    
    public JSONObject setUnitPrice(Double unitPrice) {
        return setValue("nUnitPrce", unitPrice);
    }

    public Double getUnitPrice() {
        if (getValue("nUnitPrce") == null || "".equals(getValue("nUnitPrce"))) {
            return 0.0000;
        }
        return Double.valueOf(getValue("nUnitPrce").toString());
    }
    
    public JSONObject isReverse(boolean isReverse) {
        return setValue("cReversex", isReverse ? "1" : "0");
    }

    public boolean isReverse() {
        return ((String) getValue("cReversex")).equals("1");
    }

    public JSONObject setModifiedDate(Date modifiedDate) {
        return setValue("dModified", modifiedDate);
    }

    public Date getModifiedDate() {
        return (Date) getValue("dModified");
    }
    
    public void setBrandId(String brandId){
        psBrandId = brandId;
    }
    
    public String getBrandId(){
        return psBrandId;
    }
    
    public void setModelId(String modelId){
        psModelId = modelId;
    }
    
    public String getModelId(){
        return psModelId;
    }

    @Override
    public String getNextCode() {
        return "";
    }

    //reference object models
    public Model_Brand Brand() throws GuanzonException, SQLException {
        if (!"".equals((String) getValue("sStockIDx")) && (String) getValue("sStockIDx") != null) {
            psBrandId = Inventory().getBrandId();
            setBrandId(Inventory().getBrandId());
        }
        
        if (!"".equals(getBrandId())) {
            if (poBrand.getEditMode() == EditMode.READY
                    && poBrand.getBrandId().equals(getBrandId())) {
                return poBrand;
            } else {
                poJSON = poBrand.openRecord(getBrandId());
                if ("success".equals((String) poJSON.get("result"))) {
                    return poBrand;
                } else {
                    poBrand.initialize();
                    return poBrand;
                }
            }
        } else {
            poBrand.initialize();
            return poBrand;
        }
    }
    
    public Model_Model Model() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sStockIDx")) && (String) getValue("sStockIDx") != null) {
            psModelId = Inventory().getModelId();
            setModelId(Inventory().getModelId());
        }
        
        if (!"".equals(getModelId())) {
            if (poModel.getEditMode() == EditMode.READY
                    && poModel.getModelId().equals(getModelId())) {
                return poModel;
            } else {
                poJSON = poModel.openRecord(getModelId());
                if ("success".equals((String) poJSON.get("result"))) {
                    return poModel;
                } else {
                    poModel.initialize();
                    return poModel;
                }
            }
        } else {
            poModel.initialize();
            return poModel;
        }
    }
    
    public Model_Inventory Inventory() throws SQLException, GuanzonException {
        if (!"".equals((String) getValue("sStockIDx"))) {
            if (poInventory.getEditMode() == EditMode.READY
                    && poInventory.getStockId().equals((String) getValue("sStockIDx"))) {
                return poInventory;
            } else {
                poJSON = poInventory.openRecord((String) getValue("sStockIDx"));

                if ("success".equals((String) poJSON.get("result"))) {
                    return poInventory;
                } else {
                    poInventory.initialize();
                    return poInventory;
                }
            }
        } else {
            poInventory.initialize();
            return poInventory;
        }
    }
    
    //end reference object models
}
