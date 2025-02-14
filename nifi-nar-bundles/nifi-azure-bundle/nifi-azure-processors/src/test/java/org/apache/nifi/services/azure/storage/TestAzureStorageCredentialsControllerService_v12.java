/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.services.azure.storage;

import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.NoOpProcessor;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.nifi.processors.azure.AzureServiceEndpoints.DEFAULT_BLOB_ENDPOINT_SUFFIX;
import static org.apache.nifi.services.azure.storage.AzureStorageCredentialsControllerService_v12.ACCOUNT_NAME;
import static org.apache.nifi.services.azure.storage.AzureStorageCredentialsControllerService_v12.CREDENTIALS_TYPE;
import static org.apache.nifi.services.azure.storage.AzureStorageCredentialsControllerService_v12.ENDPOINT_SUFFIX;
import static org.apache.nifi.services.azure.storage.AzureStorageCredentialsControllerService_v12.SERVICE_PRINCIPAL_CLIENT_ID;
import static org.apache.nifi.services.azure.storage.AzureStorageCredentialsControllerService_v12.SERVICE_PRINCIPAL_CLIENT_SECRET;
import static org.apache.nifi.services.azure.storage.AzureStorageCredentialsControllerService_v12.SERVICE_PRINCIPAL_TENANT_ID;
import static org.apache.nifi.services.azure.storage.AzureStorageCredentialsType.ACCOUNT_KEY;
import static org.apache.nifi.services.azure.storage.AzureStorageCredentialsType.MANAGED_IDENTITY;
import static org.apache.nifi.services.azure.storage.AzureStorageCredentialsType.SAS_TOKEN;
import static org.apache.nifi.services.azure.storage.AzureStorageCredentialsType.SERVICE_PRINCIPAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestAzureStorageCredentialsControllerService_v12 {

    public static final String CREDENTIALS_SERVICE_IDENTIFIER = "credentials-service";

    private static final String ACCOUNT_NAME_VALUE = "AccountName";
    private static final String ACCOUNT_KEY_VALUE = "AccountKey";
    private static final String SAS_TOKEN_VALUE = "SasToken";
    private static final String ENDPOINT_SUFFIX_VALUE = "endpoint.suffix";
    private static final String SERVICE_PRINCIPAL_TENANT_ID_VALUE = "ServicePrincipalTenantID";
    private static final String SERVICE_PRINCIPAL_CLIENT_ID_VALUE = "ServicePrincipalClientID";
    private static final String SERVICE_PRINCIPAL_CLIENT_SECRET_VALUE = "ServicePrincipalClientSecret";

    private TestRunner runner;
    private AzureStorageCredentialsControllerService_v12 credentialsService;

    @BeforeEach
    public void setUp() throws InitializationException {
        runner = TestRunners.newTestRunner(NoOpProcessor.class);
        credentialsService = new AzureStorageCredentialsControllerService_v12();
        runner.addControllerService(CREDENTIALS_SERVICE_IDENTIFIER, credentialsService);
    }

    @Test
    public void testNotValidBecauseAccountNameMissing() {
        configureCredentialsType(ACCOUNT_KEY);
        configureAccountKey();

        runner.assertNotValid(credentialsService);
    }

    @Test
    public void testAccountKeyCredentialsTypeValid() {
        configureAccountName();
        configureCredentialsType(ACCOUNT_KEY);
        configureAccountKey();

        runner.assertValid(credentialsService);
    }

    @Test
    public void testAccountKeyCredentialsTypeNotValidBecauseAccountKeyMissing() {
        configureAccountName();
        configureCredentialsType(ACCOUNT_KEY);

        runner.assertNotValid(credentialsService);
    }

    @Test
    public void testSasTokenCredentialsTypeValid() {
        configureAccountName();
        configureCredentialsType(SAS_TOKEN);
        configureSasToken();

        runner.assertValid(credentialsService);
    }

    @Test
    public void testSasTokenCredentialsTypeNotValidBecauseSasTokenMissing() {
        configureAccountName();
        configureCredentialsType(SAS_TOKEN);

        runner.assertNotValid(credentialsService);
    }

    @Test
    public void testManagedIdentityCredentialsTypeValid() {
        configureAccountName();
        configureCredentialsType(MANAGED_IDENTITY);

        runner.assertValid(credentialsService);
    }

    @Test
    public void testServicePrincipalCredentialsTypeValid() {
        configureAccountName();
        configureCredentialsType(SERVICE_PRINCIPAL);
        configureServicePrincipalTenantId();
        configureServicePrincipalClientId();
        configureServicePrincipalClientSecret();

        runner.assertValid(credentialsService);
    }

    @Test
    public void testServicePrincipalCredentialsTypeNotValidBecauseTenantIdMissing() {
        configureAccountName();
        configureCredentialsType(SERVICE_PRINCIPAL);
        configureServicePrincipalClientId();
        configureServicePrincipalClientSecret();

        runner.assertNotValid(credentialsService);
    }

    @Test
    public void testServicePrincipalCredentialsTypeNotValidBecauseClientIdMissing() {
        configureAccountName();
        configureCredentialsType(SERVICE_PRINCIPAL);
        configureServicePrincipalTenantId();
        configureServicePrincipalClientSecret();

        runner.assertNotValid(credentialsService);
    }

    @Test
    public void testServicePrincipalCredentialsTypeNotValidBecauseClientSecretMissing() {
        configureAccountName();
        configureCredentialsType(SERVICE_PRINCIPAL);
        configureServicePrincipalTenantId();
        configureServicePrincipalClientId();

        runner.assertNotValid(credentialsService);
    }

    @Test
    public void testGetCredentialsDetailsWithAccountKey() {
        configureAccountName();
        configureCredentialsType(ACCOUNT_KEY);
        configureAccountKey();

        runner.enableControllerService(credentialsService);

        AzureStorageCredentialsDetails_v12 actual = credentialsService.getCredentialsDetails();

        assertEquals(ACCOUNT_NAME_VALUE, actual.getAccountName());
        assertEquals(DEFAULT_BLOB_ENDPOINT_SUFFIX, actual.getEndpointSuffix());
        assertEquals(ACCOUNT_KEY, actual.getCredentialsType());
        assertEquals(ACCOUNT_KEY_VALUE, actual.getAccountKey());
        assertNull(actual.getSasToken());
        assertNull(actual.getTenantId());
        assertNull(actual.getClientId());
        assertNull(actual.getClientSecret());
    }

    @Test
    public void testGetCredentialsDetailsWithSasToken() {
        configureAccountName();
        configureCredentialsType(SAS_TOKEN);
        configureSasToken();

        runner.enableControllerService(credentialsService);

        AzureStorageCredentialsDetails_v12 actual = credentialsService.getCredentialsDetails();

        assertEquals(ACCOUNT_NAME_VALUE, actual.getAccountName());
        assertEquals(DEFAULT_BLOB_ENDPOINT_SUFFIX, actual.getEndpointSuffix());
        assertEquals(SAS_TOKEN, actual.getCredentialsType());
        assertNull(actual.getAccountKey());
        assertEquals(SAS_TOKEN_VALUE, actual.getSasToken());
        assertNull(actual.getTenantId());
        assertNull(actual.getClientId());
        assertNull(actual.getClientSecret());
    }

    @Test
    public void testGetCredentialsDetailsWithManagedIdentity() {
        configureAccountName();
        configureCredentialsType(MANAGED_IDENTITY);

        runner.enableControllerService(credentialsService);

        AzureStorageCredentialsDetails_v12 actual = credentialsService.getCredentialsDetails();

        assertEquals(ACCOUNT_NAME_VALUE, actual.getAccountName());
        assertEquals(DEFAULT_BLOB_ENDPOINT_SUFFIX, actual.getEndpointSuffix());
        assertEquals(MANAGED_IDENTITY, actual.getCredentialsType());
        assertNull(actual.getAccountKey());
        assertNull(actual.getSasToken());
        assertNull(actual.getTenantId());
        assertNull(actual.getClientId());
        assertNull(actual.getClientSecret());
    }

    @Test
    public void testGetCredentialsDetailsWithServicePrincipal() {
        configureAccountName();
        configureCredentialsType(SERVICE_PRINCIPAL);
        configureServicePrincipalTenantId();
        configureServicePrincipalClientId();
        configureServicePrincipalClientSecret();

        runner.enableControllerService(credentialsService);

        AzureStorageCredentialsDetails_v12 actual = credentialsService.getCredentialsDetails();

        assertEquals(ACCOUNT_NAME_VALUE, actual.getAccountName());
        assertEquals(DEFAULT_BLOB_ENDPOINT_SUFFIX, actual.getEndpointSuffix());
        assertEquals(SERVICE_PRINCIPAL, actual.getCredentialsType());
        assertNull(actual.getAccountKey());
        assertNull(actual.getSasToken());
        assertEquals(SERVICE_PRINCIPAL_TENANT_ID_VALUE, actual.getTenantId());
        assertEquals(SERVICE_PRINCIPAL_CLIENT_ID_VALUE, actual.getClientId());
        assertEquals(SERVICE_PRINCIPAL_CLIENT_SECRET_VALUE, actual.getClientSecret());
    }

    @Test
    public void testGetCredentialsDetailsWithCustomEndpointSuffix() {
        configureAccountName();
        configureEndpointSuffix();
        configureCredentialsType(ACCOUNT_KEY);
        configureAccountKey();

        runner.enableControllerService(credentialsService);

        AzureStorageCredentialsDetails_v12 actual = credentialsService.getCredentialsDetails();

        assertEquals(ENDPOINT_SUFFIX_VALUE, actual.getEndpointSuffix());
    }

    private void configureAccountName() {
        runner.setProperty(credentialsService, ACCOUNT_NAME, ACCOUNT_NAME_VALUE);
    }

    private void configureEndpointSuffix() {
        runner.setProperty(credentialsService, ENDPOINT_SUFFIX, ENDPOINT_SUFFIX_VALUE);
    }

    private void configureCredentialsType(AzureStorageCredentialsType credentialsType) {
        runner.setProperty(credentialsService, CREDENTIALS_TYPE, credentialsType.getAllowableValue());
    }

    private void configureAccountKey() {
        runner.setProperty(credentialsService, AzureStorageCredentialsControllerService_v12.ACCOUNT_KEY, ACCOUNT_KEY_VALUE);
    }

    private void configureSasToken() {
        runner.setProperty(credentialsService, AzureStorageCredentialsControllerService_v12.SAS_TOKEN, SAS_TOKEN_VALUE);
    }

    private void configureServicePrincipalTenantId() {
        runner.setProperty(credentialsService, SERVICE_PRINCIPAL_TENANT_ID, SERVICE_PRINCIPAL_TENANT_ID_VALUE);
    }

    private void configureServicePrincipalClientId() {
        runner.setProperty(credentialsService, SERVICE_PRINCIPAL_CLIENT_ID, SERVICE_PRINCIPAL_CLIENT_ID_VALUE);
    }

    private void configureServicePrincipalClientSecret() {
        runner.setProperty(credentialsService, SERVICE_PRINCIPAL_CLIENT_SECRET, SERVICE_PRINCIPAL_CLIENT_SECRET_VALUE);
    }
}
