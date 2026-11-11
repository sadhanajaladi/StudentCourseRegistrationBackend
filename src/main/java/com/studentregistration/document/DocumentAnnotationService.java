package com.studentregistration.document;



import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DocumentAnnotationService {

    private static final Logger log = LoggerFactory.getLogger(DocumentAnnotationService.class);

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentAnnotationRepository annotationRepository;

    public DocumentAnnotationService(
            DocumentRepository documentRepository,
            DocumentVersionRepository documentVersionRepository,
            DocumentAnnotationRepository annotationRepository) {

        this.documentRepository = documentRepository;
        this.documentVersionRepository = documentVersionRepository;
        this.annotationRepository = annotationRepository;
    }

    // Create annotation - document owner only
    @Transactional
    public DocumentAnnotation createAnnotation(
            Long documentId,
            Integer versionNumber,
            AnnotationType type,
            Integer pageNumber,
            Double x,
            Double y,
            Double width,
            Double height,
            String comment,
            Authentication authentication) {

        String userEmail = authentication.getName();

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Document not found: " + documentId));

        // Only document owner can create annotations
        if (!document.getOwnerEmail().equals(userEmail)) {
            throw new AccessDeniedException(
                    "Only the document owner can create annotations.");
        }

        // Validate document version
        if (versionNumber == null || versionNumber < 1 ||
                !documentVersionRepository.existsByDocumentIdAndVersionNumber(
                        documentId, versionNumber)) {

            throw new IllegalArgumentException(
                    "Document version not found.");
        }

        // Validate annotation type
        if (type == null) {
            throw new IllegalArgumentException(
                    "Annotation type is required.");
        }

        // Validate page number
        if (pageNumber == null || pageNumber < 1) {
            throw new IllegalArgumentException(
                    "Page number must be at least 1.");
        }

        // Validate coordinates
        if (x == null || y == null || width == null || height == null ||
                !Double.isFinite(x) || !Double.isFinite(y) ||
                !Double.isFinite(width) || !Double.isFinite(height) ||
                x < 0 || y < 0 || width <= 0 || height <= 0 ||
                x + width > 1 || y + height > 1) {

            throw new IllegalArgumentException(
                    "Annotation coordinates must fit within the page, using values from 0 to 1.");
        }

        // Create annotation
        DocumentAnnotation annotation = new DocumentAnnotation();

        annotation.setDocumentId(documentId);
        annotation.setVersionNumber(versionNumber);
        annotation.setType(type);
        annotation.setPageNumber(pageNumber);
        annotation.setX(x);
        annotation.setY(y);
        annotation.setWidth(width);
        annotation.setHeight(height);
        annotation.setComment(comment);
        annotation.setCreatedBy(userEmail);

        DocumentAnnotation saved = annotationRepository.save(annotation);
        log.info("Annotation {} created for document {} version {} by {}",
                saved.getId(), documentId, versionNumber, userEmail);
        return saved;
    }

    // Get annotations - document owner or Admin
    @Transactional(readOnly = true)
    public List<DocumentAnnotation> getAnnotations(
            Long documentId,
            Integer versionNumber,
            Authentication authentication) {

        verifyReadAccess(documentId, authentication);

        if (versionNumber == null || versionNumber < 1 ||
                !documentVersionRepository.existsByDocumentIdAndVersionNumber(
                        documentId, versionNumber)) {

            throw new IllegalArgumentException(
                    "Document version not found.");
        }

        List<DocumentAnnotation> annotations =
                annotationRepository.findByDocumentIdAndVersionNumberOrderByCreatedAtDesc(
                        documentId, versionNumber);
        log.debug("Retrieved {} annotations for document {} version {} by {}",
                annotations.size(), documentId, versionNumber, authentication.getName());
        return annotations;
    }

    // Count annotations - document owner or Admin
    @Transactional(readOnly = true)
    public long countAnnotations(
            Long documentId,
            Authentication authentication) {

        verifyReadAccess(documentId, authentication);

        return annotationRepository.countByDocumentId(documentId);
    }

    // Verify document read permission
    private void verifyReadAccess(
            Long documentId,
            Authentication authentication) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Document not found: " + documentId));

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority()));

        boolean isOwner = document.getOwnerEmail()
                .equals(authentication.getName());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                    "You are not allowed to access these annotations.");
        }
    }
}
