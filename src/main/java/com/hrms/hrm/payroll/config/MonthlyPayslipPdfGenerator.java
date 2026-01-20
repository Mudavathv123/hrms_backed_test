package com.hrms.hrm.payroll.config;

import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.payroll.model.EmployeeBankDetails;
import com.hrms.hrm.payroll.model.Payroll;
import com.hrms.hrm.payroll.model.PayrollDeduction;
import com.hrms.hrm.payroll.model.SalaryStructure;
import com.hrms.hrm.repository.EmployeeRepository;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MonthlyPayslipPdfGenerator {

        private final EmployeeRepository employeeRepository;

        private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        // ================= MAIN METHOD =================
        public String generatePayslip(
                        Payroll payroll,
                        SalaryStructure salary,
                        List<PayrollDeduction> deductions) throws Exception {

                String fileName = "payslip_" + payroll.getEmployeeId() + "_"
                                + payroll.getMonth() + "_" + payroll.getYear() + ".pdf";

                Path path = Paths.get("uploads/payslips", fileName);
                Files.createDirectories(path.getParent());

                PdfWriter writer = new PdfWriter(path.toString());
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);
                document.setMargins(25, 25, 35, 25);

                pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterHandler());

                Employee emp = employeeRepository.findById(payroll.getEmployeeId())
                                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

                Image logo = loadLogo();

                addHeader(document, logo);
                addPaySummary(document, emp, payroll);
                addBankSection(document, emp);
                addSalarySection(document, salary, deductions);

                document.close();
                return path.toString();
        }

        // ================= HEADER =================
        private void addHeader(Document document, Image logo) {

                Table table = new Table(new float[] { 80, 20 });
                table.setWidth(UnitValue.createPercentValue(100));

                Cell companyCell = new Cell()
                                .add(new Paragraph("Raynx Systems Private Limited")
                                                .setBold()
                                                .setFontSize(14))
                                .add(new Paragraph("Telangana, India").setFontSize(9))
                                .setBorder(Border.NO_BORDER)
                                .setTextAlignment(TextAlignment.LEFT);

                Cell logoCell = new Cell()
                                .add(logo)
                                .setBorder(Border.NO_BORDER)
                                .setTextAlignment(TextAlignment.RIGHT);

                table.addCell(companyCell);
                table.addCell(logoCell);

                document.add(table);

                document.add(new Paragraph("MONTHLY PAYSLIP")
                                .setBold()
                                .setFontSize(11)
                                .setTextAlignment(TextAlignment.CENTER));

                document.add(new LineSeparator(new SolidLine()));
        }

        // ================= PAY SUMMARY =================
        private void addPaySummary(Document document, Employee emp, Payroll payroll) {

                document.add(new Paragraph("PAY SUMMARY")
                                .setBold()
                                .setFontSize(10));

                Table table = new Table(new float[] { 25, 25, 25, 25 });
                table.setWidth(UnitValue.createPercentValue(100));

                table.addCell(info("Employee Name", emp.getFirstName() + " " + emp.getLastName()));
                table.addCell(info("Employee ID", emp.getEmployeeId()));

                table.addCell(info("Designation", emp.getDesignation()));
                table.addCell(info("Pay Period", payroll.getMonth() + " " + payroll.getYear()));

                table.addCell(info("Date of Joining",
                                emp.getJoiningDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
                table.addCell(info("Paid Days", String.valueOf(payroll.getPaidDays())));

                table.addCell(info("UAN", payroll.getUanNumber() == null ? "-" : payroll.getUanNumber()));
                table.addCell(info("LOP Days", String.valueOf(payroll.getUnpaidLeaveDays())));

                document.add(table);
                document.add(space());
        }

        // ================= BANK DETAILS =================
        private void addBankSection(Document document, Employee emp) {

                EmployeeBankDetails bank = emp.getBankDetails();
                if (bank == null)
                        return;

                document.add(new Paragraph("BANK DETAILS")
                                .setBold().setFontSize(10));

                Table table = new Table(new float[] { 25, 25, 25, 25 });
                table.setWidth(UnitValue.createPercentValue(100));

                table.addCell(info("Bank Name", bank.getBankName()));
                table.addCell(info("Account No", bank.getAccountNumber()));
                table.addCell(info("IFSC Code", bank.getIfscCode()));
                table.addCell(info("Branch", bank.getBranch()));

                document.add(table);
                document.add(space());
        }

        // ================= SALARY DETAILS =================
        private void addSalarySection(
                        Document document,
                        SalaryStructure salary,
                        List<PayrollDeduction> deductions) {

                Table table = new Table(new float[] { 30, 15, 15, 25, 15 });
                table.setWidth(UnitValue.createPercentValue(100));

                table.addHeaderCell(header("EARNINGS"));
                table.addHeaderCell(header("AMOUNT"));
                table.addHeaderCell(header("YTD"));
                table.addHeaderCell(header("DEDUCTIONS"));
                table.addHeaderCell(header("AMOUNT"));

                BigDecimal totalEarnings = BigDecimal.ZERO;
                BigDecimal totalDeductions = BigDecimal.ZERO;

                totalEarnings = addEarningRow(table, "Basic",
                                salary.getBasic(), salary.getBasic(), totalEarnings);

                totalEarnings = addEarningRow(table, "House Rent Allowance",
                                salary.getHra(), salary.getHra(), totalEarnings);

                totalEarnings = addEarningRow(table, "Fixed Allowance",
                                salary.getAllowance(), salary.getAllowance(), totalEarnings);

                for (PayrollDeduction d : deductions) {

                        if ("LOSS_OF_PAY".equals(d.getDeductionType())) {
                                continue; 
                        }

                        table.addCell(value(""));
                        table.addCell(value(""));
                        table.addCell(value(""));
                        table.addCell(value(d.getDeductionType()));
                        table.addCell(value(currency.format(d.getAmount())));
                }

                table.addCell(totalCell("Gross Earnings"));
                table.addCell(totalCell(currency.format(totalEarnings)));
                table.addCell(totalCell(""));
                table.addCell(totalCell("Total Deductions"));
                table.addCell(totalCell(currency.format(totalDeductions)));

                document.add(table);

                document.add(new LineSeparator(new SolidLine()));

                BigDecimal netPay = totalEarnings.subtract(totalDeductions);

                document.add(new Paragraph("Total Net Payable : " +
                                currency.format(netPay))
                                .setBold()
                                .setFontSize(11)
                                .setTextAlignment(TextAlignment.RIGHT));

                document.add(new Paragraph("(" +
                                amountInWords(netPay) + ")")
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.RIGHT));

                addNoteAndDeclaration(document);

        }

        // ================= HELPERS =================
        private Image loadLogo() throws Exception {
                URL url = getClass().getClassLoader()
                                .getResource("static/logo/company-logo.png");
                if (url == null)
                        throw new RuntimeException("Logo not found");
                ImageData data = ImageDataFactory.create(url);
                return new Image(data).scaleToFit(100, 50);
        }

        private Cell info(String label, String value) {
                Paragraph p = new Paragraph()
                                .add(new Text(label + " : ").setBold())
                                .add(value)
                                .setFontSize(9);

                return new Cell()
                                .add(p)
                                .setBorder(Border.NO_BORDER);
        }

        private Cell header(String text) {
                return new Cell()
                                .add(new Paragraph(text).setBold().setFontSize(9))
                                .setTextAlignment(TextAlignment.CENTER);
        }

        private Cell value(String text) {
                return new Cell().add(new Paragraph(text).setFontSize(9));
        }

        private Cell totalCell(String text) {
                return new Cell()
                                .add(new Paragraph(text).setBold().setFontSize(9));
        }

        private Paragraph space() {
                return new Paragraph("\n");
        }

        private BigDecimal addEarningRow(
                        Table table,
                        String label,
                        BigDecimal amount,
                        BigDecimal ytd,
                        BigDecimal total) {

                if (amount == null)
                        amount = BigDecimal.ZERO;
                if (ytd == null)
                        ytd = BigDecimal.ZERO;

                table.addCell(value(label));
                table.addCell(value(currency.format(amount)));
                table.addCell(value(currency.format(ytd)));
                table.addCell(value(""));
                table.addCell(value(""));

                return total.add(amount);
        }

        private String amountInWords(BigDecimal amount) {
                return currency.format(amount).replace("₹", "Indian Rupee ")
                                + " Only";
        }

        // ================= FOOTER =================
        static class FooterHandler implements IEventHandler {
                @Override
                public void handleEvent(Event event) {
                        PdfDocumentEvent e = (PdfDocumentEvent) event;
                        PdfCanvas canvas = new PdfCanvas(e.getPage());
                        try {
                                canvas.beginText();
                                canvas.setFontAndSize(PdfFontFactory.createFont(), 9);
                                canvas.moveText(520, 20);
                                canvas.showText("Page " +
                                                e.getDocument().getPageNumber(e.getPage()));
                                canvas.endText();
                        } catch (Exception ignored) {
                        }
                }
        }

        private void addNoteAndDeclaration(Document document) {

                document.add(space());

                // Note
                document.add(new Paragraph("Note:")
                                .setBold()
                                .setFontSize(9));

                document.add(new Paragraph(
                                "**Total Net Payable = (Gross Earnings - Total Deductions)")
                                .setFontSize(8)
                                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY));

                document.add(space());

                // Prepared / Checked / Authorised
                Table signTable = new Table(new float[] { 33, 33, 34 });
                signTable.setWidth(UnitValue.createPercentValue(100));

                signTable.addCell(signatureCell("Prepared By:"));
                signTable.addCell(signatureCell("Checked By:"));
                signTable.addCell(signatureCell("Authorised By:"));

                document.add(signTable);
                document.add(space());

                // Declaration
                document.add(new Paragraph("Declaration By the Receiver")
                                .setBold()
                                .setFontSize(9));

                document.add(new Paragraph(
                                "I, the undersigned, hereby confirm that the above amount has been "
                                                + "paid to me as per the salary structure applicable for this pay period. "
                                                + "I acknowledge that the payment details mentioned above are correct "
                                                + "to the best of my knowledge.")
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.JUSTIFIED));

                document.add(space());

                // Employee Signature
                Table empSign = new Table(new float[] { 70, 30 });
                empSign.setWidth(UnitValue.createPercentValue(100));

                empSign.addCell(new Cell()
                                .setBorder(Border.NO_BORDER));

                empSign.addCell(new Cell()
                                .add(new Paragraph("Employee's Signature:")
                                                .setFontSize(9))
                                .setBorder(Border.NO_BORDER));

                document.add(empSign);
        }

        private Cell signatureCell(String text) {
                return new Cell()
                                .add(new Paragraph(text).setFontSize(9))
                                .setMinHeight(40)
                                .setBorderTop(new com.itextpdf.layout.borders.SolidBorder(0.5f))
                                .setBorderLeft(Border.NO_BORDER)
                                .setBorderRight(Border.NO_BORDER)
                                .setBorderBottom(Border.NO_BORDER);
        }

}
