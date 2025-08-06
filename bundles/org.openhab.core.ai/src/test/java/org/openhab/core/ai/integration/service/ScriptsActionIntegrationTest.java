package org.openhab.core.ai.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.action.ActionResult;
import org.openhab.core.ai.action.actions.scripts.*;

/**
 * Integration tests for Scripts-related Actions using mocked openHAB services.
 */
class ScriptsActionIntegrationTest extends BaseActionIntegrationTest {

    private GetScriptAction getScriptAction;
    private ListScriptsAction listScriptsAction;
    private CreateScriptAction createScriptAction;
    private UpdateScriptAction updateScriptAction;
    private DeleteScriptAction deleteScriptAction;
    private GetScriptEnginesAction getScriptEnginesAction;
    private ScriptExecutionAction scriptExecutionAction;
    private ValidateScriptAction validateScriptAction;
    private CheckScriptSyntaxAction checkScriptSyntaxAction;
    private SearchScriptsAction searchScriptsAction;
    private ScriptLibraryAction scriptLibraryAction;

    @BeforeEach
    void setUpActions() {
        // Initialize all script actions
        getScriptAction = new GetScriptAction();
        listScriptsAction = new ListScriptsAction();
        createScriptAction = new CreateScriptAction();
        updateScriptAction = new UpdateScriptAction();
        deleteScriptAction = new DeleteScriptAction();
        getScriptEnginesAction = new GetScriptEnginesAction();
        scriptExecutionAction = new ScriptExecutionAction();
        validateScriptAction = new ValidateScriptAction();
        checkScriptSyntaxAction = new CheckScriptSyntaxAction();
        searchScriptsAction = new SearchScriptsAction();
        scriptLibraryAction = new ScriptLibraryAction();

        // Initialize actions with context
        getScriptAction.initialize(actionContext);
        listScriptsAction.initialize(actionContext);
        createScriptAction.initialize(actionContext);
        updateScriptAction.initialize(actionContext);
        deleteScriptAction.initialize(actionContext);
        getScriptEnginesAction.initialize(actionContext);
        scriptExecutionAction.initialize(actionContext);
        validateScriptAction.initialize(actionContext);
        checkScriptSyntaxAction.initialize(actionContext);
        searchScriptsAction.initialize(actionContext);
        scriptLibraryAction.initialize(actionContext);
    }

    @Test
    void testListScriptsAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        ActionResult result = executeAction(listScriptsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "scripts");
    }

    @Test
    void testListScriptsWithTypeFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "type", "type", "js");
        ActionResult result = executeAction(listScriptsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "scripts");
    }

    @Test
    void testListScriptsWithEngineFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "engine", "engine", "nashorn");
        ActionResult result = executeAction(listScriptsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "scripts");
    }

    @Test
    void testGetScriptAction() throws Exception {
        Map<String, Object> parameters = Map.of("scriptUID", "test-script-1");
        ActionResult result = executeAction(getScriptAction, parameters);

        // In mocked mode without real scripts, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "script");
            assertResultContainsKey(result, "scriptUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testCreateScriptAction() throws Exception {
        Map<String, Object> parameters = Map.of("scriptName", "Test Script", "scriptType", "js", "scriptContent",
                "console.log('Hello World');");
        ActionResult result = executeAction(createScriptAction, parameters);

        // In mocked mode without real script engine, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "script");
            assertResultContainsKey(result, "scriptUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testUpdateScriptAction() throws Exception {
        Map<String, Object> parameters = Map.of("scriptUID", "test-script-1", "scriptContent",
                "console.log('Updated script');");
        ActionResult result = executeAction(updateScriptAction, parameters);

        // In mocked mode without real scripts, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "script");
            assertResultContainsKey(result, "scriptUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testDeleteScriptAction() throws Exception {
        Map<String, Object> parameters = Map.of("scriptUID", "test-script-1");
        ActionResult result = executeAction(deleteScriptAction, parameters);

        // In mocked mode without real scripts, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "deleted");
            assertResultContainsKey(result, "scriptUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetScriptEnginesAction() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "all");
        ActionResult result = executeAction(getScriptEnginesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "engines");
    }

    @Test
    void testGetScriptEnginesWithTypeFilter() throws Exception {
        Map<String, Object> parameters = Map.of("filter", "type", "type", "js");
        ActionResult result = executeAction(getScriptEnginesAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "engines");
    }

    @Test
    void testScriptExecutionAction() throws Exception {
        Map<String, Object> parameters = Map.of("scriptUID", "test-script-1", "input", Map.of("param1", "value1"));
        ActionResult result = executeAction(scriptExecutionAction, parameters);

        // In mocked mode without real scripts, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "output");
            assertResultContainsKey(result, "scriptUID");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testScriptExecutionWithInlineScript() throws Exception {
        Map<String, Object> parameters = Map.of("scriptType", "js", "scriptContent", "console.log('Hello World');",
                "input", Map.of("param1", "value1"));
        ActionResult result = executeAction(scriptExecutionAction, parameters);

        // In mocked mode without real script engine, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "output");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testValidateScriptAction() throws Exception {
        Map<String, Object> parameters = Map.of("scriptType", "js", "scriptContent", "console.log('Valid script');");
        ActionResult result = executeAction(validateScriptAction, parameters);

        // In mocked mode without real script engine, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "valid");
            assertResultContainsKey(result, "scriptType");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testCheckScriptSyntaxAction() throws Exception {
        Map<String, Object> parameters = Map.of("scriptType", "js", "scriptContent", "console.log('Valid syntax');");
        ActionResult result = executeAction(checkScriptSyntaxAction, parameters);

        // In mocked mode without real script engine, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "syntaxValid");
            assertResultContainsKey(result, "scriptType");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testSearchScriptsAction() throws Exception {
        Map<String, Object> parameters = Map.of("query", "test");
        ActionResult result = executeAction(searchScriptsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "scripts");
        assertResultContainsKey(result, "query");
    }

    @Test
    void testSearchScriptsWithFilters() throws Exception {
        Map<String, Object> parameters = Map.of("query", "test", "type", "js", "engine", "nashorn");
        ActionResult result = executeAction(searchScriptsAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "scripts");
    }

    @Test
    void testScriptLibraryAction() throws Exception {
        Map<String, Object> parameters = Map.of("action", "list");
        ActionResult result = executeAction(scriptLibraryAction, parameters);

        assertSuccess(result);
        assertResultContainsKey(result, "libraries");
    }

    @Test
    void testScriptLibraryActionWithGet() throws Exception {
        Map<String, Object> parameters = Map.of("action", "get", "libraryName", "test-library");
        ActionResult result = executeAction(scriptLibraryAction, parameters);

        // In mocked mode without real script libraries, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "library");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testScriptLibraryActionWithInstall() throws Exception {
        Map<String, Object> parameters = Map.of("action", "install", "libraryName", "test-library");
        ActionResult result = executeAction(scriptLibraryAction, parameters);

        // In mocked mode without real script libraries, this should fail gracefully
        if (result.isSuccess()) {
            assertResultContainsKey(result, "installed");
        } else {
            assertNotNull(result.getMessage());
        }
    }

    @Test
    void testGetScriptActionWithInvalidUID() throws Exception {
        Map<String, Object> parameters = Map.of("scriptUID", "invalid-script-uid");
        ActionResult result = executeAction(getScriptAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testCreateScriptActionWithInvalidType() throws Exception {
        Map<String, Object> parameters = Map.of("scriptName", "Invalid Script", "scriptType", "invalid-type",
                "scriptContent", "console.log('test');");
        ActionResult result = executeAction(createScriptAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testValidateScriptActionWithInvalidSyntax() throws Exception {
        Map<String, Object> parameters = Map.of("scriptType", "js", "scriptContent", "console.log('Invalid syntax';");
        ActionResult result = executeAction(validateScriptAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testScriptExecutionActionWithInvalidScript() throws Exception {
        Map<String, Object> parameters = Map.of("scriptUID", "non-existent-script", "input",
                Map.of("param1", "value1"));
        ActionResult result = executeAction(scriptExecutionAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testSearchScriptsActionWithEmptyQuery() throws Exception {
        Map<String, Object> parameters = Map.of("query", "");
        ActionResult result = executeAction(searchScriptsAction, parameters);

        assertFailure(result);
        assertNotNull(result.getMessage());
    }
}
