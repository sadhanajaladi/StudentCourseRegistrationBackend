package com.studentregistration.document;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documents")
public class DocumentAnnotationController {

    private static final Logger log = LoggerFactory.getLogger(DocumentAnnotationController.class);

    private final DocumentAnnotationService annotationService;

    public DocumentAnnotationController(DocumentAnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    // Create annotation - document owner only
    @PostMapping("/{documentId}/versions/{versionNumber}/annotations")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentAnnotation createAnnotation(
            @PathVariable Long documentId,
            @PathVariable Integer versionNumber,
            @RequestBody AnnotationRequest request,
            Authentication authentication) {

        log.info("[DocumentAnnotationController] Creating annotation on doc ID: {}, version: {}, type: {}",
                documentId, versionNumber, request.getType());

        return annotationService.createAnnotation(
                documentId,
                versionNumber,
                request.getType(),
                request.getPageNumber(),
                request.getX(),
                request.getY(),
                request.getWidth(),
                request.getHeight(),
                request.getComment(),
                authentication
        );
    }

    // Get annotations - document owner or Admin
    @GetMapping("/{documentId}/versions/{versionNumber}/annotations")
    public List<DocumentAnnotation> getAnnotations(
            @PathVariable Long documentId,
            @PathVariable Integer versionNumber,
            Authentication authentication) {

        log.info("[DocumentAnnotationController] Fetching annotations for doc ID: {}, version: {}",
                documentId, versionNumber);

        return annotationService.getAnnotations(
                documentId,
                versionNumber,
                authentication
        );
    }

    // Count annotations - document owner or Admin
    @GetMapping("/{documentId}/annotations/count")
    public long countAnnotations(
            @PathVariable Long documentId,
            Authentication authentication) {

        log.info("[DocumentAnnotationController] Counting annotations for doc ID: {}", documentId);

        return annotationService.countAnnotations(
                documentId,
                authentication
        );
    }
}