package nl.thehyve.podium.common.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.UserAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Collection;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractAccessPolicyIntTest {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private OAuth2TokenMockUtil tokenUtil;

    private ObjectMapper mapper = new ObjectMapper();

    /**
     * The implementing class should create a mock mvc object in an initialisation
     * methods annotated with @Setup.
     * @return the mock mvc object.
     */
    protected abstract MockMvc getMockMvc();

    private RequestPostProcessor token(AuthenticatedUser user) {
        if (user == null) {
            return SecurityMockMvcRequestPostProcessors.anonymous();
        }
        return tokenUtil.oauth2Authentication(new UserAuthenticationToken(user));
    }

    private MockHttpServletRequestBuilder getRequest(Action action) {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(action.method, action.url);
        if (action.body != null) {
            try {
                request = request
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsBytes(action.body));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON serialisation error", e);
            }
        }
        for(Map.Entry<String, String> entry: action.parameters.entrySet()) {
            request = request.param(entry.getKey(), entry.getValue());
        }
        return request;
    }

    protected void expectSuccess(Action action, AuthenticatedUser user) throws Exception {
        getMockMvc().perform(
            getRequest(action)
                .with(token(user))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    protected void expectFail(Action action, AuthenticatedUser user) throws Exception {
        getMockMvc().perform(
            getRequest(action)
                .with(token(user))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(result -> log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString()))
            .andExpect(status().is4xxClientError());
    }

    protected void expectStatus(Action action, HttpStatus status, AuthenticatedUser user) throws Exception {
        getMockMvc().perform(
            getRequest(action)
                .with(token(user))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(result -> log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString()))
            .andExpect(status().is(status.value()));
    }

    /**
     * Run all actions with all of the specified users.
     * @param actions the actions to be performed
     * @param allUsers the list of all users (both allowed and not allowed to perform the actions)
     */
    public void runAll(Collection<Action> actions, Collection<AuthenticatedUser> allUsers) throws Exception {
        for (Action action: actions) {
            for (AuthenticatedUser user: allUsers) {
                String login = user == null ? "anonymous" : user.getName();
                log.info("Testing action {} {} for user {}", action.method, action.url, login);
                if (user == null) {
                    log.info("Expect failure for anonymous...");
                    expectFail(action, user);
                } else if (action.expectedStatus != null) {
                    log.info("Expect {}...", action.expectedStatus);
                    expectStatus(action, action.expectedStatus, user);
                } else {
                    if (action.allowedUsers.contains(user)) {

                        if (action.successStatus != null) {
                            log.info("Expect success status {}...", action.successStatus);
                            expectStatus(action, action.successStatus, user);
                        } else {
                            log.info("Expect success...");
                            expectSuccess(action, user);
                        }
                    } else {
                        log.info("Expect failure...");
                        expectFail(action, user);
                    }
                }
            }
        }
    }

}
