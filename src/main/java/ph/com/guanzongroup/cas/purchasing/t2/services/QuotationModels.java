/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.purchasing.t2.services;

import org.guanzon.appdriver.base.GRiderCAS;
import ph.com.guanzongroup.cas.purchasing.t2.model.Model_PO_Quotation_Detail;
import ph.com.guanzongroup.cas.purchasing.t2.model.Model_PO_Quotation_Master;
import ph.com.guanzongroup.cas.purchasing.t2.model.Model_PO_Quotation_Request_Detail;
import ph.com.guanzongroup.cas.purchasing.t2.model.Model_PO_Quotation_Request_Master;
import ph.com.guanzongroup.cas.purchasing.t2.model.Model_PO_Quotation_Request_Supplier;

/**
 *
 * @author Arsiela 03-12-2025
 */
public class QuotationModels {
    
    public QuotationModels(GRiderCAS applicationDriver){
        poGRider = applicationDriver;
    }
    
    public Model_PO_Quotation_Request_Master POQuotationRequestMaster(){
        if (poGRider == null){
            System.err.println("POQuotationRequestModels.POQuotationRequestMaster: Application driver is not set.");
            return null;
        }
        
        if (POQuotatioRequestMaster == null){
            POQuotatioRequestMaster = new Model_PO_Quotation_Request_Master();
            POQuotatioRequestMaster.setApplicationDriver(poGRider);
            POQuotatioRequestMaster.setXML("Model_PO_Quotation_Request_Master");
            POQuotatioRequestMaster.setTableName("PO_Quotation_Request_Master");
            POQuotatioRequestMaster.initialize();
        }

        return POQuotatioRequestMaster;
    }
    
    public Model_PO_Quotation_Request_Detail POQuotationRequestDetails(){
        if (poGRider == null){
            System.err.println("POQuotationRequestModels.POQuotationRequestDetails: Application driver is not set.");
            return null;
        }
        
        if (POQuotatioRequestDetail == null){
            POQuotatioRequestDetail = new Model_PO_Quotation_Request_Detail();
            POQuotatioRequestDetail.setApplicationDriver(poGRider);
            POQuotatioRequestDetail.setXML("Model_PO_Quotation_Request_Detail");
            POQuotatioRequestDetail.setTableName("PO_Quotation_Request_Detail");
            POQuotatioRequestDetail.initialize();
        }

        return POQuotatioRequestDetail;
    }
    
    public Model_PO_Quotation_Request_Supplier POQuotationRequestSupplier(){
        if (poGRider == null){
            System.err.println("POQuotationRequestModels.POQuotationRequestSupplier: Application driver is not set.");
            return null;
        }
        
        if (POQuotatioRequestSupplier == null){
            POQuotatioRequestSupplier = new Model_PO_Quotation_Request_Supplier();
            POQuotatioRequestSupplier.setApplicationDriver(poGRider);
            POQuotatioRequestSupplier.setXML("Model_PO_Quotation_Request_Supplier");
            POQuotatioRequestSupplier.setTableName("PO_Quotation_Request_Supplier");
            POQuotatioRequestSupplier.initialize();
        }

        return POQuotatioRequestSupplier;
    }
    
    public Model_PO_Quotation_Master POQuotationMaster(){
        if (poGRider == null){
            System.err.println("POQuotationModels.POQuotationMaster: Application driver is not set.");
            return null;
        }
        
        if (POQuotatioMaster == null){
            POQuotatioMaster = new Model_PO_Quotation_Master();
            POQuotatioMaster.setApplicationDriver(poGRider);
            POQuotatioMaster.setXML("Model_PO_Quotation_Master");
            POQuotatioMaster.setTableName("PO_Quotation_Master");
            POQuotatioMaster.initialize();
        }

        return POQuotatioMaster;
    }
    
    public Model_PO_Quotation_Detail POQuotationDetails(){
        if (poGRider == null){
            System.err.println("POQuotationModels.POQuotationDetails: Application driver is not set.");
            return null;
        }
        
        if (POQuotatioDetail == null){
            POQuotatioDetail = new Model_PO_Quotation_Detail();
            POQuotatioDetail.setApplicationDriver(poGRider);
            POQuotatioDetail.setXML("Model_PO_Quotation_Detail");
            POQuotatioDetail.setTableName("PO_Quotation_Detail");
            POQuotatioDetail.initialize();
        }

        return POQuotatioDetail;
    }
    
    private final GRiderCAS poGRider;
    private Model_PO_Quotation_Request_Master POQuotatioRequestMaster;
    private Model_PO_Quotation_Request_Detail POQuotatioRequestDetail;
    private Model_PO_Quotation_Request_Supplier POQuotatioRequestSupplier;
    private Model_PO_Quotation_Master POQuotatioMaster;
    private Model_PO_Quotation_Detail POQuotatioDetail;
    
}
