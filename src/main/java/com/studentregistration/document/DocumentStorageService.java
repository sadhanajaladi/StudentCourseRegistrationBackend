
package com.studentregistration.document;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class DocumentStorageService {

    private final Path storageDirectory =
            Paths.get("document-storage").toAbsolutePath().normalize();

    public String storeFile(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file.");
        }

        Files.createDirectories(storageDirectory);

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            originalFileName = "uploaded-file";
        }

        String safeFileName = Paths.get(originalFileName)
                .getFileName()
                .toString();

        String storedFileName = UUID.randomUUID()
                + "_"
                + safeFileName;

        Path destination = storageDirectory.resolve(storedFileName)
                .normalize();

        if (!destination.startsWith(storageDirectory)) {
            throw new IllegalArgumentException("Invalid file name.");
        }

        Files.copy(
                file.getInputStream(),
                destination,
                StandardCopyOption.REPLACE_EXISTING);

        return destination.toString();
    }
}
