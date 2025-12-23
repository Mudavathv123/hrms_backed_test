package com.hrms.hrm.service;

import com.hrms.hrm.model.Payroll;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PayslipService {

    public byte[] generatePayslip(Payroll p) throws Exception {

        Document doc = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);

        doc.open();

        doc.add(new Paragraph("PAYSLIP"));
        doc.add(new Paragraph("Employee: " + p.getEmployee().getFirstName()));
        doc.add(new Paragraph("Month: " + p.getMonth() + "/" + p.getYear()));
        doc.add(new Paragraph("----------------------------------"));

        doc.add(new Paragraph("Basic Salary: ₹" + p.getBasicSalary()));
        doc.add(new Paragraph("Overtime: ₹" + p.getOvertimeAmount()));
        doc.add(new Paragraph("PF: ₹" + p.getPfAmount()));
        doc.add(new Paragraph("Professional Tax: ₹" + p.getProfessionalTax()));
        doc.add(new Paragraph("Unpaid Leave Deduction: ₹" + p.getUnpaidLeaveDeduction()));

        doc.add(new Paragraph("----------------------------------"));
        doc.add(new Paragraph("Net Salary: ₹" + p.getNetSalary()));

        doc.close();
        return out.toByteArray();
    }
}
