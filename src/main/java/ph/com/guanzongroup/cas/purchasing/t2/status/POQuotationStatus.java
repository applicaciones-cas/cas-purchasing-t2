/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ph.com.guanzongroup.cas.purchasing.t2.status;

/**
 *
 * @author Arsiela
 */
public class POQuotationStatus {
    public static final String OPEN = "0";
    public static final  String CONFIRMED = "1";
    public static final  String APPROVED = "2"; 
    public static final  String CANCELLED = "3";
    public static final  String VOID = "4";
    
    //0 as new entry, 1 = confirmed, 2 = approved, 3 = cancelled, 4 = Void
    
    public static class Reverse  {
        public static final  String INCLUDE = "+"; 
        public static final  String EXCLUDE = "-"; 
    }
    
}
