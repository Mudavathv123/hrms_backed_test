package com.hrms.hrm.payroll.config;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.hrms.hrm.payroll.model.Payroll;
import com.hrms.hrm.payroll.model.PayrollDeduction;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class PayslipPdfGenerator {


    public static String generatePayslip(
        Payroll payroll,
        List<PayrollDeduction> deductions,
        String employeeName) throws  Exception {

            String directory =  "payslips";

            Files.createDirectories(Path.of(directory));

            String fileName = directory + "/payslip_"
            + payroll.getEmployeeId() +"_"
            + payroll.getMonth() +"_"
            + payroll.getYear() + ". pdf";

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normaFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            document.add(new Paragraph("Raynx Systems Pvt Ltd", titleFont));
            document.add(new Paragraph("Payslip", headerFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Employee: " +employeeName, normaFont));
            document.add(new Paragraph("Employee ID: " +payroll.getEmployeeId(), normaFont));
            document.add(new Paragraph(
                "Salary Month: " +payroll.getMonth() +"/" + payroll.getYear(), normaFont
            ));

            document.add(new Paragraph(" "));

            PdfPTable deductionTable = new PdfPTable(2);

            deductionTable.setWidthPercentage(100);

            for(PayrollDeduction d : deductions) {
                addRow(deductionTable, d.getDeductionType(), d.getAmount());
            }

            document.add(deductionTable);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                "Generated On: " +
                payroll.getGeneratedAt().format(DateTimeFormatter.ISO_DATE),normaFont
            ));

            document.close();
            return fileName;
        }

        private static void addRow(PdfPTable table, String label, BigDecimal value) {
            table.addCell(new Phrase(label));
            table.addCell(new Phrase(value.toString()));
        }

}
