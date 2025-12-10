package com.serbriss.filehash.service;

import com.serbriss.filehash.configuration.FileHashConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@Slf4j
public class FileHashService {

    private static final int BUFFER_SIZE = 8192;
    private static final String FILE_CONTENT = "Your file content here";
    private static final String HASH_TYPE = "SHA-256";

    private final FileHashConfig config;

    public FileHashService(FileHashConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        try {
            File file = writeFile();
            String checksum = getChecksum(file);
            verifyCheckSumOnDisk(file.getName(), checksum);
        } catch (Exception e) {
            log.error("Error during file hash operations", e);
        }
    }

    public File writeFile() throws IOException {
        File location = new File(config.getLocation());
        if (!location.exists() && !location.mkdirs()) {
            throw new RuntimeException("Failed to create directory: " + location.getAbsolutePath());
        }
        File newFile = new File(location, UUID.randomUUID().toString());
        try (FileWriter writer = new FileWriter(newFile)) {
            writer.write(FILE_CONTENT);
            log.info("File created with content at: " + newFile.getAbsolutePath());
        }
        return newFile;
    }

    private String getChecksum(File file) throws IOException, NoSuchAlgorithmException {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(HASH_TYPE);
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while ((n = fis.read(buffer)) > 0) {
                digest.update(buffer, 0, n);
            }
            return Hex.encodeHexString(digest.digest());
        }
    }

    public void verifyCheckSumOnDisk(String fileName, String checksum) throws IOException {
        Process process = Runtime.getRuntime().exec(new String[]{"sha256sum",
                config.getLocation() + File.separator + fileName});
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String output = reader.readLine();
            if (output == null) {
                log.error("No output from sha256sum command");
                return;
            }
            String resultChecksum = output.split(" ")[0];
            log.info("File hash: " + checksum);
            log.info("SHA256 hash: " + resultChecksum);
            if (!resultChecksum.equals(checksum)) {
                log.info("Checksums do not match!");
            } else {
                log.info("Checksums match.");
            }
        }
    }
}
