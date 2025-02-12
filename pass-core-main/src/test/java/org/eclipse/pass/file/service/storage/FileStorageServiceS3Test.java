package org.eclipse.pass.file.service.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import edu.wisc.library.ocfl.api.exception.NotFoundException;
import io.findify.s3mock.S3Mock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;

class FileStorageServiceS3Test {
    private final static String ROOT_DIR = System.getProperty("java.io.tmpdir") + "/pass-s3-test";

    private FileStorageService fileStorageService;
    private final StorageProperties properties = new StorageProperties();
    private S3Mock s3MockApi;

    /**
     * Setup the test environment. Uses custom endpoint for the in-memory S3 mock.
     */
    @BeforeEach
    void setUp() {
        s3MockApi = new S3Mock.Builder().withPort(8001).withInMemoryBackend().build();
        s3MockApi.start();
        properties.setStorageType(StorageServiceType.S3);
        properties.setRootDir(ROOT_DIR);
        properties.setS3Endpoint("http://localhost:8001");
        properties.setS3BucketName("bucket-test-name");
        properties.setS3RepoPrefix("s3-repo-prefix");
        StorageConfiguration storageConfiguration = new StorageConfiguration(properties);

        // Set properties to make the credentials provider happy
        System.setProperty("aws.accessKeyId", "A B C");
        System.setProperty("aws.secretAccessKey", "D E F");

        try {
            fileStorageService = new FileStorageService(storageConfiguration, "us-east-1");
        } catch (IOException e) {
            assertEquals("Exception during setup", e.getMessage());
        }
    }

    /**
     * Tear down the test environment. Deletes the temporary directory.
     */
    @AfterEach
    void tearDown() {
        s3MockApi.stop();
        FileSystemUtils.deleteRecursively(Paths.get(ROOT_DIR).toFile());
    }

    /**
     * Test that the file is stored in the S3 mock.
     */
    @Test
    void storeFileToS3ThatExists() {
        try {
            StorageFile storageFile = fileStorageService.storeFile(new MockMultipartFile("test", "test.txt",
                    MediaType.TEXT_PLAIN_VALUE, "Test S3 Pass-core".getBytes()));
            assertFalse(fileStorageService.getResourceFileRelativePath(storageFile.getId()).isEmpty());
        } catch (Exception e) {
            assertEquals("An exception was thrown in storeFileThatExists.", e.getMessage());
        }
    }

    /**
     * Should get the file from the S3 bucket and return it.
     */
    @Test
    void getFileFromS3ShouldReturnFile() {
        try {
            StorageFile storageFile = fileStorageService.storeFile(new MockMultipartFile("test", "test.txt",
                    MediaType.TEXT_PLAIN_VALUE, "Test S3 Pass-core".getBytes()));
            ByteArrayResource file = fileStorageService.getFile(storageFile.getId());
            assertTrue(file.contentLength() > 0);
        } catch (IOException e) {
            assertEquals("Exception during getFileShouldReturnFile", e.getMessage());
        }
    }

    /**
     * Should throw an exception because the file ID does not exist.
     */
    @Test
    void getFileShouldThrowException() {
        Exception exception = assertThrows(IOException.class,
                () -> fileStorageService.getFile("12345")
        );
        String expectedExceptionText = "File Service: The file could not be loaded";
        String actualExceptionText = exception.getMessage();
        assertTrue(actualExceptionText.contains(expectedExceptionText));
    }

    @Test
    void storeFileWithDifferentLangFilesNames() {
        //generate long string of all english characters
        String engFileName = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*()_+{}|:\"<>?`~[]\\;',./.txt";
        String frFileName = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZÀÁÂÃÄÅÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖÙÚÛÜÝßàáâãäå" +
                "çèéêëìíîïñòóôõöùúûüýÿœŒæÆ.txt";
        String spFileName = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZÁÉÍÑÓÚÜáéíñóúü¡¿.txt";
        String arFileName = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZءآأؤإئابةتثجحخدذرزسشصضطظعغ" +
                "ـفقكلمنهوي.txt";
        String chFileName = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ我 爱 你 我爱你 家庭 家人 我想你 我想你 " +
                "我喜欢你 的 shì yí de 一个人 - yí gè rén 是 shì wǒ 我 .txt";
        String ruFileName = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZАа Бб Вв Гг Дд Ее Ёё Жж Зз Ии Йй Кк " +
                "Лл Мм Нн Оо Пп Рр Сс Тт Уу Фф Хх Цц Чч Шш Щщ Ъъ Ыы Ьь Ээ Юю Яя .txt";

        Map<String, String> allCharSets = new HashMap<>();
        allCharSets.put("eng", engFileName);
        allCharSets.put("fr", frFileName);
        allCharSets.put("sp", spFileName);
        allCharSets.put("ar", arFileName);
        allCharSets.put("ch", chFileName);
        allCharSets.put("ru", ruFileName);

        //test each character set
        allCharSets.forEach((k,v) -> {
            try {
                StorageFile storageFile = fileStorageService.storeFile(new MockMultipartFile("test", v,
                        MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()));
                assertFalse(fileStorageService.getResourceFileRelativePath(storageFile.getId()).isEmpty());
            } catch (IOException e) {
                assertEquals("An exception was thrown in storeFileWithDifferentLangFilesNames. On charset=" + k,
                        e.getMessage());
            }
        });
    }

    /**
     * Stores file, then deletes it. Should throw an exception because the file does not exist.
     */
    @Test
    void deleteShouldThrowExceptionFileNotExist() {
        try {
            StorageFile storageFile = fileStorageService.storeFile(new MockMultipartFile("test", "test.txt",
                    MediaType.TEXT_PLAIN_VALUE, "Test Pass-core".getBytes()));
            fileStorageService.deleteFile(storageFile.getId());
            Exception exception = assertThrows(NotFoundException.class,
                    () -> fileStorageService.getResourceFileRelativePath(storageFile.getId()));
            String exceptionText = exception.getMessage();
            assertTrue(exceptionText.matches("(.)+(was not found){1}(.)+"));
        } catch (IOException e) {
            assertEquals("Exception during deleteShouldThrowExceptionFileNotExist", e.getMessage());
        }
    }
}