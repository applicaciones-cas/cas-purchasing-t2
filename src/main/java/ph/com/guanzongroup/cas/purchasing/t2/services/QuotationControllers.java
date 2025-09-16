/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.purchasing.t2.services;

import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.LogWrapper;
import ph.com.guanzongroup.cas.purchasing.t2.POQuotation;
import ph.com.guanzongroup.cas.purchasing.t2.POQuotationRequest;

/**
 *
 * @author Arsiela 04-28-2025
 */
public class QuotationControllers {
    
    public QuotationControllers(GRiderCAS applicationDriver, LogWrapper logWrapper){
        poGRider = applicationDriver;
        poLogWrapper = logWrapper;
    }
    
    public POQuotationRequest POQuotationRequest(){
        if (poGRider == null){
            poLogWrapper.severe("QuotationControllers.POQuotationRequest: Application driver is not set.");
            return null;
        }
        
        if (POQuotationRequest != null) return POQuotationRequest;
        
        POQuotationRequest = new POQuotationRequest();
        POQuotationRequest.setApplicationDriver(poGRider);
        POQuotationRequest.setBranchCode(poGRider.getBranchCode());
        POQuotationRequest.setVerifyEntryNo(true);
        POQuotationRequest.setWithParent(false);
        POQuotationRequest.setLogWrapper(poLogWrapper);
        return POQuotationRequest;        
    }
    
    public POQuotation POQuotation(){
        if (poGRider == null){
            poLogWrapper.severe("QuotationControllers.POQuotation: Application driver is not set.");
            return null;
        }
        
        if (POQuotation != null) return POQuotation;
        
        POQuotation = new POQuotation();
        POQuotation.setApplicationDriver(poGRider);
        POQuotation.setBranchCode(poGRider.getBranchCode());
        POQuotation.setVerifyEntryNo(true);
        POQuotation.setWithParent(false);
        POQuotation.setLogWrapper(poLogWrapper);
        return POQuotation;        
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {
            POQuotationRequest = null;
                    
            poLogWrapper = null;
            poGRider = null;
        } finally {
            super.finalize();
        }
    }
    
    private GRiderCAS poGRider;
    private LogWrapper poLogWrapper;
    private POQuotationRequest POQuotationRequest;
    private POQuotation POQuotation;
}
