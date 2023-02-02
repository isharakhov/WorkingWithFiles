import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.Files.lines;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class WorkingWithFilesTests {

    private static List<FileHeader> headersList = new ArrayList<>();
    private static final String EXPECTED_CONTENT = "Многие слышали про Selenium WebDriver — один из самых популярных инструментов для" +
            " написания приёмочных/интеграционных тестов.Используя Selenium, мы очень быстро заметили, что нам раз " +
            "от раза приходится писать один и тот же код, чтобы инициализировать браузер вначале, закрыть его в конце," +
            " делать скриншоты после каждого упавшего теста и т.д. (пруфлинк).Поэтому мы решили выделить этот повторяющийся " +
            "код в отдельную библиотеку. Так на свет появился Selenide.Что такое Selenide? Selenide — это обёртка вокруг Selenium WebDriver," +
            " позволяющая быстро и просто его использовать при написании тестов, сосредоточившись на логике, а не суете с браузером.",
            PATH_FOR_ZIP_FILES = "src/test/resources/",
            PATH_FOR_UNZIP_FOLDER = "src/test/resources/forUnzipFiles",
            PATH_TEXT = "src/test/resources/AboutSelenide";

    @Test
    public void workingWithTxtFormatTest() {
        try {
            String content = lines(Path.of(PATH_TEXT + ".txt"))
                    .reduce("", String::concat);
            assertSoftly(
                    softAssertions -> {
                        softAssertions.assertThat(content)
                                .withFailMessage("Контент в файле отличается от ожидаемого")
                                .isEqualTo(EXPECTED_CONTENT);
                        softAssertions.assertThat(content.length())
                                .withFailMessage("Количество символов в файле отличается от ожидаемого")
                                .isEqualTo(643);
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void workingWithPDFFormatTest() {
        try {
            PDF parsPdf = new PDF(new File(PATH_TEXT + ".pdf"));

            assertSoftly(
                    softAssertions -> {
                        softAssertions.assertThat(parsPdf.author)
                                .withFailMessage("Автор файла отличается от ожидаемого")
                                .isEqualTo("user");
                        softAssertions.assertThat(parsPdf.text
                                        .toCharArray().length)
                                .withFailMessage("Количество символов не совпадает с ожидаемым")
                                .isEqualTo(699);
                        softAssertions.assertThat(parsPdf.content)
                                .withFailMessage("Контент в файле отличается от ожидаемого1")
                                .contains(EXPECTED_CONTENT.getBytes());
                        softAssertions.assertThat(parsPdf.content)
                                .withFailMessage("Контент в файле отличается от ожидаемого1")
                                .contains("Selenide".getBytes());
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void workingWithXLSFormatTest() throws IOException {
        try (InputStream stream = getClass().getClassLoader()
                .getResourceAsStream("картотека педкадров_2020-2021_Судинская СОШ (для сайта).xls")) {
            assert stream != null;
            XLS parsedXls = new XLS(stream);

            assertSoftly(
                    softAssertions -> {
                        softAssertions.assertThat(parsedXls.excel
                                        .getSheetAt(0)
                                        .getRow(0)
                                        .getCell(0)
                                        .getStringCellValue())
                                .withFailMessage("Title неверный")
                                .isEqualTo("Картотека педагогических работников МБОУ \"Судинская СОШ\"    на 07.11.2022 года ");
                        softAssertions.assertThat(parsedXls.excel
                                        .getSheetAt(0)
                                        .getRow(31)
                                        .getCell(1)
                                        .getStringCellValue())
                                .withFailMessage("ФИО преподавателя неверна")
                                .contains("Сайтгаряева Каусария\n" +
                                        "Шаймиевна");
                    });
        }
    }

    @Test
    void workingWithRarWithOutPasswordTest() throws IOException {
        unZip(PATH_FOR_ZIP_FILES + "zip_no_password.zip", PATH_FOR_UNZIP_FOLDER, null);

        assertSoftly(
                softAssertions -> {
                    softAssertions.assertThat(headersList.get(0).toString())
                            .withFailMessage("Название папки в zip архиве неверно")
                            .isEqualTo("zip_10MB/");
                    softAssertions.assertThat(headersList.get(1).toString())
                            .withFailMessage("Название 1 файла неверно")
                            .isEqualTo("zip_10MB/file_example_ODS_5000.ods");
                    softAssertions.assertThat(headersList.get(2).toString())
                            .withFailMessage("Название 2 файла неверно")
                            .isEqualTo("zip_10MB/file_example_PPT_1MB.ppt");
                    softAssertions.assertThat(headersList.get(3).toString())
                            .withFailMessage("Название 3 файла неверно")
                            .isEqualTo("zip_10MB/file-sample_1MB.doc");
                });

        FileUtils.deleteDirectory(new File(PATH_FOR_UNZIP_FOLDER));
    }

    @Test
    void workingWithRarWithPasswordTest() throws IOException {
        unZip(PATH_FOR_ZIP_FILES + "zip_password.zip", PATH_FOR_UNZIP_FOLDER, "153456");

        assertSoftly(
                softAssertions -> {
                    softAssertions.assertThat(headersList.get(0).toString())
                            .withFailMessage("Название папки в zip архиве неверно")
                            .isEqualTo("zip_10MB_password/");
                    softAssertions.assertThat(headersList.get(1).toString())
                            .withFailMessage("Название 1 файла неверно")
                            .isEqualTo("zip_10MB_password/file-sample_1MB_password.doc");
                    softAssertions.assertThat(headersList.get(2).toString())
                            .withFailMessage("Название 2 файла неверно")
                            .isEqualTo("zip_10MB_password/file_example_ODS_5000.ods");
                    softAssertions.assertThat(headersList.get(3).toString())
                            .withFailMessage("Название 3 файла неверно")
                            .isEqualTo("zip_10MB_password/file_example_PPT_1MB.ppt");
                });

        FileUtils.deleteDirectory(new File(PATH_FOR_UNZIP_FOLDER));
    }

    public static void unZip(String from, String toFolder, String password) {
        try {
            ZipFile zipFile = new ZipFile(from);
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password.toCharArray());
            }
            zipFile.extractAll(toFolder);
            headersList = zipFile.getFileHeaders();
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    public static void parsDocFile() throws IOException {
        try (XWPFDocument doc = new XWPFDocument(Files.newInputStream(Paths.get(PATH_FOR_UNZIP_FOLDER + "/zip_10MB/file-sample_1MB.doc")))) {
            XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(doc);
            String docText = xwpfWordExtractor.getText();
            System.out.println(docText);
            long count = Arrays.stream(docText.split("\\s+")).count();
            System.out.println("Total words: " + count);
        }
    }
}
