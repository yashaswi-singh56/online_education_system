package com.example.student.controller;

import com.example.student.model.Course;
import com.example.student.model.User;
import com.example.student.repository.CourseRepository;
import com.example.student.repository.UserRepository;
import com.example.student.service.CourseStatusService;
import com.example.student.service.CourseStatusService.CourseStatus;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
public class CertificateController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseStatusService statusService;

    public CertificateController(UserRepository userRepository,
                                 CourseRepository courseRepository,
                                 CourseStatusService statusService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.statusService = statusService;
    }

    @GetMapping("/api/certificate/{courseId}")
    public ResponseEntity<?> certificate(@PathVariable Long courseId, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }

        CourseStatus status = statusService.getStatus(user.getId(), courseId);
        if (!status.certificateReady()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Complete all lessons and pass the quiz to get the certificate");
        }

        byte[] pdf = buildPdf(user.getName(), course.getTitle(), status.bestPercent());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate.pdf")
                .body(pdf);
    }

    private byte[] buildPdf(String studentName, String courseTitle, int percent) {
        try {
            Rectangle size = PageSize.A4.rotate();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(size, 60, 60, 80, 60);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            PdfContentByte canvas = writer.getDirectContent();
            canvas.setColorStroke(new Color(91, 75, 219));
            canvas.setLineWidth(5);
            canvas.rectangle(25, 25, size.getWidth() - 50, size.getHeight() - 50);
            canvas.stroke();
            canvas.setColorStroke(new Color(244, 183, 64));
            canvas.setLineWidth(1.5f);
            canvas.rectangle(36, 36, size.getWidth() - 72, size.getHeight() - 72);
            canvas.stroke();

            Color navy = new Color(20, 18, 58);
            Color violet = new Color(91, 75, 219);

            addCentered(document, "CERTIFICATE OF COMPLETION",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 34, navy), 0);
            addCentered(document, "This is to certify that",
                    FontFactory.getFont(FontFactory.HELVETICA, 16, Color.GRAY), 36);
            addCentered(document, studentName,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30, violet), 16);
            addCentered(document, "has successfully completed the course",
                    FontFactory.getFont(FontFactory.HELVETICA, 16, Color.GRAY), 16);
            addCentered(document, courseTitle,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, navy), 16);
            addCentered(document, "with a quiz score of " + percent + "%",
                    FontFactory.getFont(FontFactory.HELVETICA, 16, Color.GRAY), 18);

            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            addCentered(document, "Issued on " + today,
                    FontFactory.getFont(FontFactory.HELVETICA, 14, Color.GRAY), 40);
            addCentered(document, "Course Portal",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, violet), 30);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Could not generate certificate", e);
        }
    }

    private void addCentered(Document document, String text, Font font, float spacingBefore)
            throws Exception {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingBefore(spacingBefore);
        document.add(paragraph);
    }
}