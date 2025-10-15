/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.purchasing.t2.validator;

import org.guanzon.appdriver.iface.GValidator;

/**
 *
 * @author Arsiela 
 */
public class POQuotationRequestValidatorFactory {
    public static GValidator make(String industryId){
        switch (industryId) {
            case "01": //Mobile Phone
                return new POQuotationRequest_MP();
            case "02": //Motorcycle
                return new POQuotationRequest_MC();
            case "03": //Vehicle
                return new POQuotationRequest_Vehicle();
            case "04": //Monarch 
                return new POQuotationRequest_Monarch();
            case "05": //Los Pedritos
            case "09": //General
                return new POQuotationRequest_LP();
            case "07": //Appliances
                return new POQuotationRequest_Appliances();
            default:
                return null;
        }
    }
    
}
