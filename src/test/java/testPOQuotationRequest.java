/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.guanzon.appdriver.base.GRiderCAS;
import org.guanzon.appdriver.base.GuanzonException;
import org.guanzon.appdriver.base.MiscUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ph.com.guanzongroup.cas.purchasing.t2.POQuotationRequest;
import ph.com.guanzongroup.cas.purchasing.t2.services.QuotationControllers;
import ph.com.guanzongroup.cas.purchasing.t2.status.POQuotationRequestStatus;
/**
 *
 * @author Arsiela
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testPOQuotationRequest {
    
    static GRiderCAS instance;
    static POQuotationRequest poController;
    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        instance = MiscUtil.Connect();

        poController = new QuotationControllers(instance, null).POQuotationRequest();
    }

//    @Test
    public void testNewTransaction() {
        String branchCd = instance.getBranchCode();
        String industryId = "05";
        String companyId = "";
        String categoryId = "0007";
        String category2 = "0022";
        String remarks = "this is a test Class 4.";

        JSONObject loJSON;

        try {

            loJSON = poController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poController.NewTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            try {
                poController.setIndustryId(industryId);
                poController.setCompanyId(companyId);
                poController.setCategoryId(categoryId);

                poController.initFields();
                poController.Master().setIndustryId(industryId); 
                Assert.assertEquals(poController.Master().getIndustryId(), industryId);
                poController.Master().setCategoryCode(categoryId); 
                Assert.assertEquals(poController.Master().getCategoryCode(), categoryId);
                poController.Master().setCategoryLevel2(category2); 
                Assert.assertEquals(poController.Master().getCategoryLevel2(), category2);
                poController.Master().setDestination("01"); 
                Assert.assertEquals(poController.Master().getDestination(), "01");
                poController.Master().setDepartmentId("0002"); 
                Assert.assertEquals(poController.Master().getDepartmentId(), "0002");
                poController.Master().setTransactionDate(instance.getServerDate()); 
                Assert.assertEquals(poController.Master().getTransactionDate(), instance.getServerDate());
                poController.Master().setExpectedPurchaseDate(instance.getServerDate()); 
                Assert.assertEquals(poController.Master().getExpectedPurchaseDate(), instance.getServerDate());
                poController.Master().setBranchCode(branchCd); 
                Assert.assertEquals(poController.Master().getBranchCode(), branchCd);
                poController.Master().setRemarks(remarks);
                Assert.assertEquals(poController.Master().getRemarks(), remarks);

                poController.Detail(0).setStockId("M00125000017");
                poController.Detail(0).setDescription("General Asset Model1");
                poController.AddDetail();
                poController.Detail(1).setStockId("M00125000018");
                poController.Detail(1).setDescription("General Asset Model2");

                System.out.println("Industry ID : " + instance.getIndustry());
                System.out.println("Industry : " + poController.Master().Industry().getDescription());
                System.out.println("Category Code : " + poController.Master().getCategoryCode());
                System.out.println("TransNox : " + poController.Master().getTransactionNo());
                
                loJSON = poController.SaveTransaction();
                if (!"success".equals((String) loJSON.get("result"))) {
                    System.err.println((String) loJSON.get("message"));
                    Assert.fail();
                }

            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ExceptionInInitializerError e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

//    @Test
    public void testUpdateTransaction() {
        JSONObject loJSON;

        try {
            loJSON = poController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poController.OpenTransaction("A00125000001");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poController.loadPOQuotationRequestSupplierList();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poController.UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
//            poController.AddPOQuotationRequestSupplier();
            poController.POQuotationRequestSupplierList(0).setSupplierId("M00125000001");
            poController.POQuotationRequestSupplierList(0).setCompanyId("0004");
            poController.POQuotationRequestSupplierList(0).isSent(true);
            poController.POQuotationRequestSupplierList(0).setTerm("0000001");
            
            loJSON = poController.SaveTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
        } catch (CloneNotSupportedException | SQLException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, MiscUtil.getException(ex), ex);
        }

    }
    
    @Test
    public void testPOQuotationRequestList() throws SQLException {
        String lsTransNo = "";
        String industryId = "05";
        String categoryCode = "0007";
        String branch = "";
        String department = "";
        String category2 = "";
        Date lDate = instance.getServerDate();
        JSONObject loJSON;
        loJSON = poController.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        poController.setIndustryId(industryId);
        poController.setCategoryId(categoryCode);
        poController.setTransactionStatus(POQuotationRequestStatus.CONFIRMED+POQuotationRequestStatus.OPEN);
        loJSON = poController.loadPOQuotationRequestList(branch,department,category2,lDate,lsTransNo, true);
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        //retreiving using column index
        for (int lnCtr = 0; lnCtr <= poController.getPOQuotationRequestCount()- 1; lnCtr++) {
            try {
                System.out.println("Row No ->> " + lnCtr);
                System.out.println("Transaction No ->> " + poController.POQuotationRequestList(lnCtr).getTransactionNo());
                System.out.println("Transaction Date ->> " + poController.POQuotationRequestList(lnCtr).getTransactionDate());
                System.out.println("Branch ->> " + poController.POQuotationRequestList(lnCtr).Branch().getBranchName());
                System.out.println("Department ->> " + poController.POQuotationRequestList(lnCtr).Department().getDescription());
                System.out.println("Category ->> " + poController.POQuotationRequestList(lnCtr).Category2().getDescription());
                System.out.println("----------------------------------------------------------------------------------");
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
//    @Test
    public void testOpenTransaction() {
        JSONObject loJSON;
        
        try {
            loJSON = poController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            loJSON = poController.OpenTransaction("A00125000001");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poController.Master().getColumnCount(); lnCol++){
                System.out.println(poController.Master().getColumn(lnCol) + " ->> " + poController.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poController.Master().Branch().getBranchName());
            System.out.println(poController.Master().Industry().getDescription());

            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poController.Detail().size() - 1; lnCtr++){
                for (int lnCol = 1; lnCol <= poController.Detail(lnCtr).getColumnCount(); lnCol++){
                    System.out.println(poController.Detail(lnCtr).getColumn(lnCol) + " ->> " + poController.Detail(lnCtr).getValue(lnCol));
                }
                System.out.println("Brand Description " +  poController.Detail(lnCtr).Brand().getDescription());
                System.out.println("Model Description " +  poController.Detail(lnCtr).Model().getDescription());
            }
           
            loJSON = poController.loadPOQuotationRequestSupplierList();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poController.getPOQuotationRequestSupplierCount() - 1; lnCtr++) {
                try {
                    System.out.println("Row No ->> " + lnCtr);
                    System.out.println("Transaction No ->> " + poController.POQuotationRequestSupplierList(lnCtr).getTransactionNo());
                    System.out.println("Company ->> " + poController.POQuotationRequestSupplierList(lnCtr).Company().getCompanyName());
                    System.out.println("Supplier ->> " + poController.POQuotationRequestSupplierList(lnCtr).Supplier().getCompanyName());
                    System.out.println("Term ->> " + poController.POQuotationRequestSupplierList(lnCtr).Term().getDescription());
                    System.out.println("----------------------------------------------------------------------------------");
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        } catch (CloneNotSupportedException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }      
    

//    @Test
    public void testConfirmTransaction() {
        JSONObject loJSON;
        
        try {
            loJSON = poController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            loJSON = poController.OpenTransaction("A00125000001");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poController.Master().getColumnCount(); lnCol++){
                System.out.println(poController.Master().getColumn(lnCol) + " ->> " + poController.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poController.Master().Branch().getBranchName());
            System.out.println(poController.Master().Industry().getDescription());

            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poController.Detail().size() - 1; lnCtr++){
                for (int lnCol = 1; lnCol <= poController.Detail(lnCtr).getColumnCount(); lnCol++){
                    System.out.println(poController.Detail(lnCtr).getColumn(lnCol) + " ->> " + poController.Detail(lnCtr).getValue(lnCol));
                }
            }
            
            loJSON = poController.ConfirmTransaction("test confirm");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            
            System.out.println((String) loJSON.get("message"));
        } catch (CloneNotSupportedException |ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }   
    
//    @Test
    public void testVoidTransaction() {
        JSONObject loJSON;
        
        try {
            loJSON = poController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            loJSON = poController.OpenTransaction("A00125000001");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poController.Master().getColumnCount(); lnCol++){
                System.out.println(poController.Master().getColumn(lnCol) + " ->> " + poController.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poController.Master().Branch().getBranchName());
            System.out.println(poController.Master().Industry().getDescription());

            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poController.Detail().size() - 1; lnCtr++){
                for (int lnCol = 1; lnCol <= poController.Detail(lnCtr).getColumnCount(); lnCol++){
                    System.out.println(poController.Detail(lnCtr).getColumn(lnCol) + " ->> " + poController.Detail(lnCtr).getValue(lnCol));
                }
            }
            
            loJSON = poController.VoidTransaction("test void");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            
            System.out.println((String) loJSON.get("message"));
        } catch (CloneNotSupportedException |ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    } 
    
//    @Test
    public void testCancelTransaction() {
        JSONObject loJSON;
        
        try {
            loJSON = poController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            loJSON = poController.OpenTransaction("A00125000001");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poController.Master().getColumnCount(); lnCol++){
                System.out.println(poController.Master().getColumn(lnCol) + " ->> " + poController.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poController.Master().Branch().getBranchName());
            System.out.println(poController.Master().Industry().getDescription());

            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poController.Detail().size() - 1; lnCtr++){
                for (int lnCol = 1; lnCol <= poController.Detail(lnCtr).getColumnCount(); lnCol++){
                    System.out.println(poController.Detail(lnCtr).getColumn(lnCol) + " ->> " + poController.Detail(lnCtr).getValue(lnCol));
                }
            }
            
            loJSON = poController.CancelTransaction("test cancel");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            
            System.out.println((String) loJSON.get("message"));
        } catch (CloneNotSupportedException |ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    } 
    
//    @Test
    public void testApproveTransaction() {
        JSONObject loJSON;
        
        try {
            loJSON = poController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            loJSON = poController.OpenTransaction("A00125000001");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poController.Master().getColumnCount(); lnCol++){
                System.out.println(poController.Master().getColumn(lnCol) + " ->> " + poController.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poController.Master().Branch().getBranchName());
            System.out.println(poController.Master().Industry().getDescription());

            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poController.Detail().size() - 1; lnCtr++){
                for (int lnCol = 1; lnCol <= poController.Detail(lnCtr).getColumnCount(); lnCol++){
                    System.out.println(poController.Detail(lnCtr).getColumn(lnCol) + " ->> " + poController.Detail(lnCtr).getValue(lnCol));
                }
            }
            
            loJSON = poController.PostTransaction("test post");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 
            
            System.out.println((String) loJSON.get("message"));
        } catch (CloneNotSupportedException |ParseException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    } 
    
//    @Test
    public void testExportTransaction() {
        JSONObject loJSON;
        
        try {
            loJSON = poController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            loJSON = poController.OpenTransaction("A00125000002");
            if (!"success".equals((String) loJSON.get("result"))){
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            } 

            //retreiving using column index
            for (int lnCol = 1; lnCol <= poController.Master().getColumnCount(); lnCol++){
                System.out.println(poController.Master().getColumn(lnCol) + " ->> " + poController.Master().getValue(lnCol));
            }
            //retreiving using field descriptions
            System.out.println(poController.Master().Branch().getBranchName());
            System.out.println(poController.Master().Industry().getDescription());

            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poController.Detail().size() - 1; lnCtr++){
                for (int lnCol = 1; lnCol <= poController.Detail(lnCtr).getColumnCount(); lnCol++){
                    System.out.println(poController.Detail(lnCtr).getColumn(lnCol) + " ->> " + poController.Detail(lnCtr).getValue(lnCol));
                }
                System.out.println("Brand Description " +  poController.Detail(lnCtr).Brand().getDescription());
                System.out.println("Model Description " +  poController.Detail(lnCtr).Model().getDescription());
            }
           
            loJSON = poController.loadPOQuotationRequestSupplierList();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poController.getPOQuotationRequestSupplierCount() - 1; lnCtr++) {
                try {
                    System.out.println("Row No ->> " + lnCtr);
                    System.out.println("Transaction No ->> " + poController.POQuotationRequestSupplierList(lnCtr).getTransactionNo());
                    System.out.println("Company ->> " + poController.POQuotationRequestSupplierList(lnCtr).Company().getCompanyName());
                    System.out.println("Supplier ->> " + poController.POQuotationRequestSupplierList(lnCtr).Supplier().getCompanyName());
                    System.out.println("Term ->> " + poController.POQuotationRequestSupplierList(lnCtr).Term().getDescription());
                    System.out.println("----------------------------------------------------------------------------------");
                    System.out.println("-----------------------------------EXPORT-------------------------------------");
                    poController.exportFile(lnCtr);
                    
                } catch (SQLException | GuanzonException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            
        } catch (CloneNotSupportedException e) {
            System.err.println(MiscUtil.getException(e));
            Assert.fail();
        } catch (SQLException | GuanzonException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }      
    
}
