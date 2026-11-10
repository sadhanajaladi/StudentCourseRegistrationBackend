
package com.studentregistration.document;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
public class DocumentAnnotationController {

    private final DocumentAnnotationService annotationService;

    public DocumentAnnotationController(
            DocumentAnnotationService annotationService) {
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

        return annotationService.countAnnotations(
                documentId,
                authentication
        );
    }
}
