// SPDX-License-Identifier: Apache-2.0

package org.hiero.mirror.importer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.hiero.mirror.importer.ImporterProperties.HederaNetwork;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ImporterPropertiesTest {

    @ParameterizedTest(name = "Network {2} is canonical network {0}, with prefix {1}")
    @CsvSource({
        "testnet, , testnet",
        "testnet, , testnet-",
        "testnet, 2023-01, teSTnet-2023-01",
        "testnet, someprefix, testnet-someprefix",
        "mainnet, , mainnet",
        "mainnet, 2023-01, mainnet-2023-01",
        "mainnet, someprefix, maiNNet-someprefix",
        "previewnet, , previewnet",
        "previewnet, 2025-04, Previewnet-2025-04",
        "previewnet, abcdef, previewnet-abcdef",
        "demo, , deMo",
        "demo, 2023-01, demo-2023-01",
        "demo, someprefix, demo-someprefix",
        "other, , other",
        "other, 2050-02, other-2050-02",
        "other, world, othER-world"
    })
    void verifyCanonicalNetworkWithPrefix(String expectedHederaNetwork, String expectedPrefix, String networkName) {

        var properties = new ImporterProperties();
        properties.setNetwork(networkName);
        assertThat(properties.getNetwork()).isEqualTo(expectedHederaNetwork);
        assertThat(properties.getNetworkPrefix()).isEqualTo(expectedPrefix);
    }

    @ParameterizedTest(name = "Network {2} is non-canonical network {0}, with prefix {1}")
    @CsvSource({
        "integration, , integration",
        "integration, 2023-01, integration-2023-01",
        "dev, , dev",
        "dev, 2025-02, dev-2025-02"
    })
    void verifyNonCanonicalNetworkWithPrefix(String expectedNetwork, String expectedPrefix, String networkName) {

        var properties = new ImporterProperties();
        properties.setNetwork(networkName);
        assertThat(properties.getNetwork()).isEqualTo(expectedNetwork);
        assertThat(properties.getNetworkPrefix()).isEqualTo(expectedPrefix);
    }

    @Test
    void verifySetNetworkPropertyValidation() {
        var properties = new ImporterProperties();
        assertThat(properties.getNetwork()).isEqualTo(HederaNetwork.DEMO); // Default
        assertThat(properties.getNetworkPrefix()).isNull();

        assertThrows(NullPointerException.class, () -> HederaNetwork.getBucketName(null));
        assertThrows(NullPointerException.class, () -> HederaNetwork.isAllowAnonymousAccess(null));
    }
}
