// SPDX-License-Identifier: Apache-2.0

package org.hiero.mirror.restjava.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

import com.hedera.mirror.rest.model.Error;
import com.hedera.mirror.rest.model.ErrorStatusMessagesInner;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.ThrowableAssert;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.hiero.mirror.restjava.RestJavaIntegrationTest;
import org.hiero.mirror.restjava.RestJavaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;

@RequiredArgsConstructor
@RunWith(SpringRunner.class)
@EnableConfigurationProperties(value = RestJavaProperties.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class ControllerTest extends RestJavaIntegrationTest {

    private static final String BASE_PATH = "/api/v1/";
    protected RestClient.Builder restClientBuilder;

    @Autowired
    private RestJavaProperties properties;

    private String baseUrl;

    @LocalServerPort
    private int port;

    @BeforeEach
    final void setup() {
        baseUrl = "http://localhost:%d%s".formatted(port, BASE_PATH);
        restClientBuilder = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Access-Control-Request-Method", "GET")
                .defaultHeader("Origin", "http://example.com");
    }

    @SuppressWarnings({"unchecked", "java:S6103"})
    protected final void validateError(
            ThrowableAssert.ThrowingCallable callable,
            Class<? extends HttpClientErrorException> clazz,
            String... detail) {
        AtomicReference<HttpClientErrorException> e = new AtomicReference<>();
        assertThatThrownBy(callable)
                .isInstanceOf(clazz)
                .asInstanceOf(InstanceOfAssertFactories.type(clazz))
                .satisfies(e::set)
                .extracting(
                        r -> r.getResponseBodyAs(Error.class).getStatus().getMessages(),
                        list(ErrorStatusMessagesInner.class))
                .hasSize(detail.length)
                .allSatisfy(error -> assertThat(error.getData()).isNull())
                .allSatisfy(error -> assertThat(error.getMessage())
                        .isEqualTo(HttpStatus.resolve(e.get().getStatusCode().value())
                                .getReasonPhrase()))
                .extracting(ErrorStatusMessagesInner::getDetail)
                .asInstanceOf(InstanceOfAssertFactories.list(String.class))
                .contains(detail);
    }

    protected abstract class EndpointTest {

        protected RestClient restClient;

        protected abstract String getUrl();

        @BeforeEach
        void setup() {
            var suffix = getUrl().replace("/api/v1/", "");
            restClient = restClientBuilder.baseUrl(baseUrl + suffix).build();
        }

        /*
         * This method allows subclasses to do any setup work like entity persistence and provide a default URI and
         * parameters for the parent class to run its common set of tests.
         */
        protected abstract RequestHeadersSpec<?> defaultRequest(RequestHeadersUriSpec<?> uriSpec);

        @Test
        void headers() {
            // When
            var headers = defaultRequest(restClient.get())
                    .retrieve()
                    .toBodilessEntity()
                    .getHeaders();

            // Then
            var headersConfig = properties.getResponse().getHeaders();
            var headersPathKey = "%s%s".formatted(BASE_PATH, getUrl());
            var headersExpected = headersConfig.getPath().getOrDefault(headersPathKey, headersConfig.getDefaults());

            headersExpected.forEach((expectedName, expectedValue) -> {
                var headerValues = headers.get(expectedName);
                assertThat(headerValues).isNotNull();
                assertThat(headerValues).contains(expectedValue);
            });
        }

        @Test
        void methodNotAllowed() {
            // When
            ThrowingCallable callable =
                    () -> defaultRequest(restClient.post()).retrieve().toBodilessEntity();

            // Then
            validateError(
                    callable,
                    HttpClientErrorException.MethodNotAllowed.class,
                    "Request method 'POST' is not supported");
        }
    }
}
