package com.qbe.aistarter.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.qbe.aistarter.dto.ActivityDto;
import com.qbe.aistarter.dto.TravelDayDto;
import com.qbe.aistarter.dto.TravelResponseDto;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PdfTravelServiceImpl {

    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
    private static final Font SECTION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 11);

    public byte[] generatePdf(TravelResponseDto travel) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(document, baos);
            document.open();
            addHeader(document, travel);
            addBudget(document, travel);
            addDays(document, travel);
            addTravelTips(document, travel);
            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new IllegalStateException("Unable to generate PDF", e);
        }
    }

    private void addHeader(Document document, TravelResponseDto travel) throws DocumentException {
        document.add(new Paragraph(travel.summary().destination(), TITLE));
        document.add(new Paragraph(
                travel.summary().durationInDays() + " days - "
                        + travel.summary().durationInDays(),
                NORMAL));
        document.add(Chunk.NEWLINE);
    }

    private void addBudget(Document document, TravelResponseDto travel) throws DocumentException {
        var budget = travel.budgetBreakdown();
        document.add(new Paragraph("Budget Breakdown", SECTION));
        document.add(new Paragraph("Accommodation : %.2f €".formatted(budget.accommodation()), NORMAL));
        document.add(new Paragraph("Transport : %.2f €".formatted(budget.transportation()), NORMAL));
        document.add(new Paragraph("Food : %.2f €".formatted(budget.food()), NORMAL));
        document.add(new Paragraph("Activities : %.2f €".formatted(budget.activities()), NORMAL));
        document.add(new Paragraph("Total : %.2f €".formatted(budget.total()), NORMAL));
        document.add(Chunk.NEWLINE);
    }

    private void addDays(Document document, TravelResponseDto travel) throws DocumentException {
        for (TravelDayDto day : travel.days()) {
            document.add(new Paragraph("Day %d - %s".formatted(day.dayNumber(), day.theme()), SECTION));
            document.add(new Paragraph("Estimated cost : %.2f €".formatted(day.estimatedDailyCost()), NORMAL));
            document.add(Chunk.NEWLINE);
            addActivities(document, "Morning", day.morning());
            addActivities(document, "Afternoon", day.afternoon());
            addActivities(document, "Evening", day.evening());
            document.add(Chunk.NEWLINE);
        }
    }

    private void addActivities(Document document, String label, List<ActivityDto> activities) throws DocumentException {
        if (activities == null || activities.isEmpty()) {
            return;
        }

        document.add(new Paragraph(label, SECTION));

        for (ActivityDto activity : activities) {
            document.add(new Paragraph("• " + activity.name(), NORMAL));
            document.add(new Paragraph(activity.description(), NORMAL));
            document.add(new Paragraph("Location: %s".formatted(activity.location()), NORMAL));
            document.add(new Paragraph("Duration: %d minutes".formatted(activity.durationMinutes()), NORMAL));
            document.add(new Paragraph("Cost: %.2f €".formatted(activity.estimatedCost()), NORMAL));
            document.add(new Paragraph("Reason: " + activity.reason(), NORMAL));
            document.add(Chunk.NEWLINE);
        }
    }

    private void addTravelTips(Document document, TravelResponseDto travel) throws DocumentException {
        if (travel.travelTips() == null || travel.travelTips().isEmpty()) {
            return;
        }

        document.add(new Paragraph("Travel Tips", SECTION));
        for (String tip : travel.travelTips()) {
            document.add(new Paragraph("• " + tip, NORMAL));
        }
    }
}
