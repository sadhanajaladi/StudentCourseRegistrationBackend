
package com.studentregistration.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentVersionRepository
        extends JpaRepository<DocumentVersion, Long> {

    List<DocumentVersion> findByDocumentIdOrderByVersionNumberDesc(
            Long documentId);
    
    boolean existsByDocumentIdAndVersionNumber(
            Long documentId, Integer versionNumber);
}
