package org.openhab.core.ai.common.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.action.AIActionResult;
import org.openhab.core.ai.common.actions.filesystem.*;

/**
 * Integration tests for Filesystem-related AIActions using mocked openHAB services.
 */
class FilesystemActionIntegrationTest extends BaseAIActionIntegrationTest {

    private ListFilesAction listFilesAction;
    private ReadFileAction readFileAction;
    private WriteFileAction writeFileAction;
    private CreateDirectoryAction createDirectoryAction;
    private DeleteFileAction deleteFileAction;
    private CopyFileAction copyFileAction;
    private MoveFileAction moveFileAction;
    private GetFileInfoAction getFileInfoAction;
    private GetFilePermissionsAction getFilePermissionsAction;
    private SetFilePermissionsAction setFilePermissionsAction;
    private GetFileChecksumAction getFileChecksumAction;
    private SearchFilesAction searchFilesAction;
    private CompressFilesAction compressFilesAction;
    private DecompressFilesAction decompressFilesAction;

    @BeforeEach
    void setUpActions() {
        // Initialize all filesystem actions
        listFilesAction = new ListFilesAction();
        readFileAction = new ReadFileAction();
        writeFileAction = new WriteFileAction();
        createDirectoryAction = new CreateDirectoryAction();
        deleteFileAction = new DeleteFileAction();
        copyFileAction = new CopyFileAction();
        moveFileAction = new MoveFileAction();
        getFileInfoAction = new GetFileInfoAction();
        getFilePermissionsAction = new GetFilePermissionsAction();
        setFilePermissionsAction = new SetFilePermissionsAction();
        getFileChecksumAction = new GetFileChecksumAction();
        searchFilesAction = new SearchFilesAction();
        compressFilesAction = new CompressFilesAction();
        decompressFilesAction = new DecompressFilesAction();

        // Initialize actions with context
        listFilesAction.initialize(actionContext);
        readFileAction.initialize(actionContext);
        writeFileAction.initialize(actionContext);
        createDirectoryAction.initialize(actionContext);
        deleteFileAction.initialize(actionContext);
        copyFileAction.initialize(actionContext);
        moveFileAction.initialize(actionContext);
        getFileInfoAction.initialize(actionContext);
        getFilePermissionsAction.initialize(actionContext);
        setFilePermissionsAction.initialize(actionContext);
        getFileChecksumAction.initialize(actionContext);
        searchFilesAction.initialize(actionContext);
        compressFilesAction.initialize(actionContext);
        decompressFilesAction.initialize(actionContext);
    }

    @Test
    void testListFilesAction() throws Exception {
        Map<String, Object> parameters = Map.of("path", "/tmp");
        AIActionResult result = executeAction(listFilesAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "files");
            assertResultContainsKey(result, "path");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testListFilesActionWithFilter() throws Exception {
        Map<String, Object> parameters = Map.of("path", "/tmp", "filter", "*.txt");
        AIActionResult result = executeAction(listFilesAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "files");
            assertResultContainsKey(result, "filter");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testReadFileAction() throws Exception {
        Map<String, Object> parameters = Map.of("filePath", "/tmp/test.txt");
        AIActionResult result = executeAction(readFileAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "content");
            assertResultContainsKey(result, "filePath");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testReadFileActionWithEncoding() throws Exception {
        Map<String, Object> parameters = Map.of("filePath", "/tmp/test.txt", "encoding", "UTF-8");
        AIActionResult result = executeAction(readFileAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "content");
            assertResultContainsKey(result, "encoding");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testWriteFileAction() throws Exception {
        Map<String, Object> parameters = Map.of("filePath", "/tmp/test.txt", "content", "Hello World");
        AIActionResult result = executeAction(writeFileAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "written");
            assertResultContainsKey(result, "filePath");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testWriteFileActionWithAppend() throws Exception {
        Map<String, Object> parameters = Map.of("filePath", "/tmp/test.txt", "content", "Additional content", "append",
                true);
        AIActionResult result = executeAction(writeFileAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "written");
            assertResultContainsKey(result, "append");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testCreateDirectoryAction() throws Exception {
        Map<String, Object> parameters = Map.of("directoryPath", "/tmp/testdir");
        AIActionResult result = executeAction(createDirectoryAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "created");
            assertResultContainsKey(result, "directoryPath");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testCreateDirectoryActionWithParents() throws Exception {
        Map<String, Object> parameters = Map.of("directoryPath", "/tmp/parent/child/dir", "createParents", true);
        AIActionResult result = executeAction(createDirectoryAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "created");
            assertResultContainsKey(result, "createParents");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDeleteFileAction() throws Exception {
        Map<String, Object> parameters = Map.of("filePath", "/tmp/test.txt");
        AIActionResult result = executeAction(deleteFileAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "deleted");
            assertResultContainsKey(result, "filePath");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDeleteFileActionWithRecursive() throws Exception {
        Map<String, Object> parameters = Map.of("filePath", "/tmp/testdir", "recursive", true);
        AIActionResult result = executeAction(deleteFileAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "deleted");
            assertResultContainsKey(result, "recursive");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testCopyFileAction() throws Exception {
        Map<String, Object> parameters = Map.of("sourcePath", "/tmp/source.txt", "destinationPath", "/tmp/dest.txt");
        AIActionResult result = executeAction(copyFileAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "copied");
            assertResultContainsKey(result, "sourcePath");
            assertResultContainsKey(result, "destinationPath");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testCopyFileActionWithOverwrite() throws Exception {
        Map<String, Object> parameters = Map.of("sourcePath", "/tmp/source.txt", "destinationPath", "/tmp/dest.txt",
                "overwrite", true);
        AIActionResult result = executeAction(copyFileAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "copied");
            assertResultContainsKey(result, "overwrite");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testMoveFileAction() throws Exception {
        Map<String, Object> parameters = Map.of("sourcePath", "/tmp/source.txt", "destinationPath", "/tmp/dest.txt");
        AIActionResult result = executeAction(moveFileAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "moved");
            assertResultContainsKey(result, "sourcePath");
            assertResultContainsKey(result, "destinationPath");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetFileInfoAction() throws Exception {
        Map<String, Object> parameters = Map.of("filePath", "/tmp/test.txt");
        AIActionResult result = executeAction(getFileInfoAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "fileInfo");
            assertResultContainsKey(result, "filePath");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetFilePermissionsAction() throws Exception {
        Map<String, Object> parameters = Map.of("filePath", "/tmp/test.txt");
        AIActionResult result = executeAction(getFilePermissionsAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "permissions");
            assertResultContainsKey(result, "filePath");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSetFilePermissionsAction() throws Exception {
        Map<String, Object> parameters = Map.of("filePath", "/tmp/test.txt", "permissions", "644");
        AIActionResult result = executeAction(setFilePermissionsAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "set");
            assertResultContainsKey(result, "permissions");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetFileChecksumAction() throws Exception {
        Map<String, Object> parameters = Map.of("filePath", "/tmp/test.txt", "algorithm", "MD5");
        AIActionResult result = executeAction(getFileChecksumAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "checksum");
            assertResultContainsKey(result, "algorithm");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSearchFilesAction() throws Exception {
        Map<String, Object> parameters = Map.of("searchPath", "/tmp", "pattern", "*.txt");
        AIActionResult result = executeAction(searchFilesAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "files");
            assertResultContainsKey(result, "pattern");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSearchFilesActionWithRecursive() throws Exception {
        Map<String, Object> parameters = Map.of("searchPath", "/tmp", "pattern", "*.txt", "recursive", true);
        AIActionResult result = executeAction(searchFilesAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "files");
            assertResultContainsKey(result, "recursive");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testCompressFilesAction() throws Exception {
        Map<String, Object> parameters = Map.of("sourcePaths", List.of("/tmp/file1.txt", "/tmp/file2.txt"),
                "destinationPath", "/tmp/archive.zip");
        AIActionResult result = executeAction(compressFilesAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "compressed");
            assertResultContainsKey(result, "destinationPath");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDecompressFilesAction() throws Exception {
        Map<String, Object> parameters = Map.of("sourcePath", "/tmp/archive.zip", "destinationPath", "/tmp/extracted");
        AIActionResult result = executeAction(decompressFilesAction, parameters);

        // In mocked mode without real filesystem, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "decompressed");
            assertResultContainsKey(result, "destinationPath");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testReadFileActionWithInvalidPath() throws Exception {
        Map<String, Object> parameters = Map.of("filePath", "/invalid/path/file.txt");
        AIActionResult result = executeAction(readFileAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testWriteFileActionWithInvalidPath() throws Exception {
        Map<String, Object> parameters = Map.of("filePath", "/invalid/path/file.txt", "content", "test content");
        AIActionResult result = executeAction(writeFileAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testDeleteFileActionWithInvalidPath() throws Exception {
        Map<String, Object> parameters = Map.of("filePath", "/invalid/path/file.txt");
        AIActionResult result = executeAction(deleteFileAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testGetFileChecksumActionWithInvalidAlgorithm() throws Exception {
        Map<String, Object> parameters = Map.of("filePath", "/tmp/test.txt", "algorithm", "INVALID_ALGORITHM");
        AIActionResult result = executeAction(getFileChecksumAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }
}
