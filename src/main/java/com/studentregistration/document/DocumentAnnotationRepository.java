
package com.studentregistration.document;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentAnnotationRepository
        extends JpaRepository<DocumentAnnotation, Long> {

    // Get annotations for a particular document version
    List<DocumentAnnotation>
    findByDocumentIdAndVersionNumberOrderByCreatedAtDesc(
            Long documentId,
            Integer versionNumber
    );

    // Count all annotations belonging to a document
    long countByDocumentId(Long documentId);
}
