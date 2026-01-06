package com.hrms.hrm.payroll.config;

import com.hrms.hrm.error.ResourceNotFoundException;
import com.hrms.hrm.model.Employee;
import com.hrms.hrm.payroll.model.Payroll;
import com.hrms.hrm.payroll.model.PayrollDeduction;
import com.hrms.hrm.payroll.model.SalaryStructure;
import com.hrms.hrm.repository.EmployeeRepository;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;

import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayslipPdfGenerator {

    private final EmployeeRepository employeeRepository;

    public String generatePayslip(Payroll payroll, SalaryStructure salary, List<PayrollDeduction> deductions,
            String employeeName)
            throws Exception {

        String fileName = "payslip_" + payroll.getId() + ".pdf";
        Path filePath = Paths.get("uploads/payslips", fileName);
        Files.createDirectories(filePath.getParent());

        PdfWriter writer = new PdfWriter(filePath.toString());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        pdf.addEventHandler(
                PdfDocumentEvent.START_PAGE,
                new ImageWatermarkHandler("static/logo/company-logo.png"));

        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new FooterHandler());

        URL logoUrl = PayslipPdfGenerator.class
                .getClassLoader()
                .getResource("static/logo/company-logo.png");

        if (logoUrl == null) {
            throw new ResourceNotFoundException("Company logo not found in resources");
        }

        ImageData logoData = ImageDataFactory.create(logoUrl);
        Image logo = new Image(logoData)
                .scaleToFit(100, 60);

        document.add(logo);
        document.add(new Paragraph("Hyderabad, India"));
        document.add(new LineSeparator(new SolidLine(1f)));

        Employee employee = employeeRepository.findById(payroll.getEmployeeId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Employee not found with id:" + payroll.getEmployeeId()));

        document.add(
                new Paragraph("Employee Payslip").setBold().setFontSize(14).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Employee Name: " + employee.getFirstName() +" " +employee.getLastName()));
        document.add(new Paragraph("Employee ID: " + employee.getEmployeeId()));
           document.add(new Paragraph("Designation: " + employee.getDesignation()));
        document.add(new Paragraph("Department: " + employee.getDepartment().getName()));
        document.add(new Paragraph("Month / Year: " + payroll.getMonth() + " / " + payroll.getYear()));
        document.add(new Paragraph(
                "Generated On: " + payroll.getGeneratedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))));
        document.add(new LineSeparator(new SolidLine(1f)).setMarginTop(10).setMarginBottom(10));

        document.add(new Paragraph("Earnings").setBold());
        Table earnings = new Table(UnitValue.createPercentArray(new float[] { 70, 30 })).useAllAvailableWidth();
        earnings.addCell(new Cell().add(new Paragraph("Basic Salary")));
        earnings.addCell(new Cell().add(new Paragraph(currency.format(salary.getBasic()))));
        earnings.addCell(new Cell().add(new Paragraph("HRA")));
        earnings.addCell(new Cell()
                .add(new Paragraph(currency.format(salary.getHra() != null ? salary.getHra() : BigDecimal.ZERO))));
        earnings.addCell(new Cell().add(new Paragraph("Allowance")));
        earnings.addCell(new Cell().add(new Paragraph(
                currency.format(salary.getAllowance() != null ? salary.getAllowance() : BigDecimal.ZERO))));
        document.add(earnings);

        Table detailsTable = new Table(UnitValue.createPercentArray(new float[] { 50, 50 })).useAllAvailableWidth();
        detailsTable.addCell(new Cell().add(new Paragraph("Total Working Days").setBold()));
        detailsTable.addCell(new Cell().add(new Paragraph(String.valueOf(payroll.getTotalWorkingDays()))));
        detailsTable.addCell(new Cell().add(new Paragraph("Present Days").setBold()));
        detailsTable.addCell(new Cell().add(new Paragraph(String.valueOf(payroll.getPresentDays()))));
        detailsTable.addCell(new Cell().add(new Paragraph("Paid Leave Days").setBold()));
        detailsTable.addCell(new Cell().add(new Paragraph(String.valueOf(payroll.getPaidLeaveDays()))));
        detailsTable.addCell(new Cell().add(new Paragraph("Unpaid Leave Days").setBold()));
        detailsTable.addCell(new Cell().add(new Paragraph(String.valueOf(payroll.getUnpaidLeaveDays()))));
        detailsTable.addCell(new Cell().add(new Paragraph("Status").setBold()));
        detailsTable.addCell(new Cell().add(new Paragraph(payroll.getStatus().name())));
        document.add(detailsTable);

        document.add(new Paragraph("Deductions").setBold());
        Table deductionTable = new Table(UnitValue.createPercentArray(new float[] { 70, 30 })).useAllAvailableWidth();
        deductionTable.addHeaderCell(new Cell().add(new Paragraph("Type").setBold()));
        deductionTable.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold()));

        BigDecimal totalDeductions = BigDecimal.ZERO;
        for (PayrollDeduction d : deductions) {
            deductionTable.addCell(new Cell().add(new Paragraph(d.getDeductionType())));
            deductionTable.addCell(new Cell().add(new Paragraph(currency.format(d.getAmount()))));
            totalDeductions = totalDeductions.add(d.getAmount());
        }
        deductionTable.addCell(new Cell().add(new Paragraph("Total").setBold()));
        deductionTable.addCell(new Cell().add(new Paragraph(currency.format(totalDeductions)).setBold()));
        document.add(deductionTable);

        BigDecimal totalEarnings = salary.getBasic().add(salary.getHra() != null ? salary.getHra() : BigDecimal.ZERO)
                .add(salary.getAllowance() != null ? salary.getAllowance() : BigDecimal.ZERO);
        BigDecimal netSalary = totalEarnings.subtract(totalDeductions);
        if (netSalary.compareTo(BigDecimal.ZERO) < 0)
            netSalary = BigDecimal.ZERO;

        document.add(new LineSeparator(new SolidLine(1f)));
        document.add(new Paragraph("Net Salary: " + currency.format(netSalary))
                .setBold().setFontSize(14).setTextAlignment(TextAlignment.RIGHT));

        document.close();
        return filePath.toString();
    }

    // Footer handler
    private static class FooterHandler implements IEventHandler {
        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event; // cast to PdfDocumentEvent
            PdfCanvas canvas = new PdfCanvas(docEvent.getPage());
            canvas.saveState();
            try {
                PdfFont font = PdfFontFactory.createFont();
                canvas.beginText();
                canvas.setFontAndSize(font, 10);
                int pageNumber = docEvent.getDocument().getPageNumber(docEvent.getPage());
                canvas.moveText(520, 20);
                canvas.showText("Page " + pageNumber);
                canvas.endText();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                canvas.restoreState();
                canvas.release();
            }
        }
    }

    private static class ImageWatermarkHandler implements IEventHandler {

        private final String resourcePath;

        public ImageWatermarkHandler(String resourcePath) {
            this.resourcePath = resourcePath;
        }

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;

            try {
                URL imageUrl = PayslipPdfGenerator.class
                        .getClassLoader()
                        .getResource(resourcePath);

                if (imageUrl == null) {
                    throw new RuntimeException("Watermark image not found");
                }

                ImageData imageData = ImageDataFactory.create(imageUrl);
                Image image = new Image(imageData);

                Rectangle pageSize = docEvent.getPage().getPageSize();

                float x = (pageSize.getWidth() - image.getImageScaledWidth()) / 2;
                float y = (pageSize.getHeight() - image.getImageScaledHeight()) / 2;

                image.setFixedPosition(x, y);
                image.setOpacity(0.15f);

                Canvas canvas = new Canvas(
                        new PdfCanvas(docEvent.getPage()),
                        docEvent.getPage().getPageSize());

                canvas.add(image);
                canvas.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
