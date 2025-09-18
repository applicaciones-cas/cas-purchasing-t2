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
import ph.com.guanzongroup.cas.purchasing.t2.POQuotation;
import ph.com.guanzongroup.cas.purchasing.t2.services.QuotationControllers;
import ph.com.guanzongroup.cas.purchasing.t2.status.POQuotationRequestStatus;
/**
 *
 * @author Arsiela
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class testPOQuotation {
    
    static GRiderCAS instance;
    static POQuotation poController;
    @BeforeClass
    public static void setUpClass() {
        System.setProperty("sys.default.path.metadata", "D:/GGC_Maven_Systems/config/metadata/new/");

        instance = MiscUtil.Connect();

        poController = new QuotationControllers(instance, null).POQuotation();
    }

//    @Test
    public void testNewTransaction() {
        String branchCd = instance.getBranchCode();
        String industryId = "03";
        String companyId = "0004";
        String categoryId = "0007";
        String category2 = "0022";
        String remarks = "this is a test Class 4.";
        String sourceNo = "A00125000015";
        String sourceCode = "POQR";
        String supplierId = "V00125000003";
        String term = "0000003";
        String referenceNo = "143";

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
                poController.Master().setSourceNo(sourceNo); 
                Assert.assertEquals(poController.Master().getSourceNo(), sourceNo);
                poController.Master().setSourceCode(sourceCode); 
                Assert.assertEquals(poController.Master().getSourceCode(), sourceCode);
                poController.Master().setSupplierId(supplierId); 
                Assert.assertEquals(poController.Master().getSupplierId(), supplierId);
                poController.Master().setCompanyId(companyId); 
                Assert.assertEquals(poController.Master().getCompanyId(), companyId);
                poController.Master().setTerm(term); 
                Assert.assertEquals(poController.Master().getTerm(), term);
                poController.Master().setReferenceNo(referenceNo); 
                Assert.assertEquals(poController.Master().getReferenceNo(), referenceNo);
                poController.Master().setTransactionDate(instance.getServerDate()); 
                Assert.assertEquals(poController.Master().getTransactionDate(), instance.getServerDate());
                poController.Master().setValidityDate(instance.getServerDate()); 
                Assert.assertEquals(poController.Master().getValidityDate(), instance.getServerDate());
                poController.Master().setBranchCode(branchCd); 
                Assert.assertEquals(poController.Master().getBranchCode(), branchCd);
                poController.Master().setRemarks(remarks);
                Assert.assertEquals(poController.Master().getRemarks(), remarks);
                poController.Master().setGrossAmount(5000.00);
                poController.Master().setTransactionTotal(5000.00);

                poController.Detail(0).setStockId("P0W125000008");
                poController.Detail(0).setDescription("General Asset Model1");
                poController.Detail(0).setQuantity(4.00);
                poController.AddDetail();

                System.out.println("Industry ID : " + instance.getIndustry());
                System.out.println("Industry : " + poController.Master().Industry().getDescription());
                System.out.println("Category Code : " + poController.Master().getCategoryCode());
                System.out.println("TransNox : " + poController.Master().getTransactionNo());
                
                
                poController.addAttachment();
                poController.TransactionAttachmentList(0).getModel().setDescription("test");
                poController.TransactionAttachmentList(0).getModel().setDocumentType("0");
                poController.TransactionAttachmentList(0).getModel().setFileName("viber_image_2025-07-16_15-48-39-171.jpg");
                poController.TransactionAttachmentList(0).getModel().setSourceNo("0");
                
                
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

    @Test
    public void testUpdateTransaction() {
        JSONObject loJSON;

        try {
            loJSON = poController.InitTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poController.OpenTransaction("A00125000002");
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }

            loJSON = poController.UpdateTransaction();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
            //Update fields
            poController.Detail(0).setUnitPrice(1000.00);
            poController.Detail(0).setDiscountRate(0.10);
            poController.Detail(0).setDiscountAmount(50.00);
            poController.Master().isVatable(false);
            poController.Master().setDiscountRate(0.00);
            poController.Master().setAdditionalDiscountAmount(0.0000);
            poController.Master().setFreightAmount(100.00);
            poController.computeFields();
            
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
    
//    @Test
    public void testPOQuotationRequestList() throws SQLException {
        String lsTransNo = "";
        String industryId = "01";
        String categoryCode = "0007";
        String branch = "";
        String department = "";
        String category2 = "";
        String supplier = "";
        String company = "0001";
        Date lDate = instance.getServerDate();
        JSONObject loJSON;
        loJSON = poController.InitTransaction();
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        poController.setIndustryId(industryId);
        poController.setCategoryId(categoryCode);
        poController.setCompanyId(company);
        loJSON = poController.loadPOQuotationRequestSupplierList(branch,department,supplier,category2,lsTransNo);
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        //retreiving using column index
        for (int lnCtr = 0; lnCtr <= poController.getPOQuotationRequestSupplierCount()- 1; lnCtr++) {
            try {
                System.out.println("Row No ->> " + lnCtr);
                System.out.println("Transaction No ->> " + poController.POQuotationRequestSupplierList(lnCtr).getTransactionNo());
                System.out.println("Transaction Date ->> " + poController.POQuotationRequestSupplierList(lnCtr).POQuotationRequestMaster().getTransactionDate());
                System.out.println("Branch ->> " + poController.POQuotationRequestSupplierList(lnCtr).POQuotationRequestMaster().Branch().getBranchName());
                System.out.println("Department ->> " + poController.POQuotationRequestSupplierList(lnCtr).POQuotationRequestMaster().Department().getDescription());
                System.out.println("Category ->> " + poController.POQuotationRequestSupplierList(lnCtr).POQuotationRequestMaster().Category2().getDescription());
                System.out.println("Supplier ->> " + poController.POQuotationRequestSupplierList(lnCtr).Supplier().getCompanyName());
                System.out.println("Company ->> " + poController.POQuotationRequestSupplierList(lnCtr).Company().getCompanyName());
                System.out.println("----------------------------------------------------------------------------------");
            } catch (SQLException | GuanzonException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
//    @Test
    public void testPOQuotationList() throws SQLException {
        String lsTransNo = "";
        String industryId = "05";
        String categoryCode = "0007";
        String branch = "";
        String department = "";
        String category2 = "";
        String supplier = "";
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
        loJSON = poController.loadPOQuotationList(branch,department,supplier,category2,lsTransNo);
        if (!"success".equals((String) loJSON.get("result"))) {
            System.err.println((String) loJSON.get("message"));
            Assert.fail();
        }
        //retreiving using column index
        for (int lnCtr = 0; lnCtr <= poController.getPOQuotationCount()- 1; lnCtr++) {
            try {
                System.out.println("Row No ->> " + lnCtr);
                System.out.println("Transaction No ->> " + poController.POQuotationList(lnCtr).getTransactionNo());
                System.out.println("Transaction Date ->> " + poController.POQuotationList(lnCtr).getTransactionDate());
                System.out.println("Branch ->> " + poController.POQuotationList(lnCtr).Branch().getBranchName());
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
            }
           
            loJSON = poController.loadAttachments();
            if (!"success".equals((String) loJSON.get("result"))) {
                System.err.println((String) loJSON.get("message"));
                Assert.fail();
            }
            
//            //retreiving using column index
            for (int lnCtr = 0; lnCtr <= poController.getTransactionAttachmentCount() - 1; lnCtr++) {
                System.out.println("Row No ->> " + lnCtr);
                System.out.println("Transaction No ->> " + poController.TransactionAttachmentList(lnCtr).getModel().getTransactionNo());
                System.out.println("File Name ->> " + poController.TransactionAttachmentList(lnCtr).getModel().getFileName());
                System.out.println("----------------------------------------------------------------------------------");
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
}
