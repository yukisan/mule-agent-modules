package com.mulesoft.agent.monitoring.publisher.ingest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

/**
 * Created by sebastianvinci on 5/26/16.
 */
public class AuthProxyClient
{

    private static final Logger LOGGER = LoggerFactory.getLogger(AnypointMonitoringIngestAPIClient.class);

    private final String baseUrl;
    private final Client client;

    private AuthProxyClient(String baseUrl, Client client)
    {
        this.baseUrl = baseUrl;
        this.client = client;
    }

    public static AuthProxyClient create(String baseUrl, String keyStorePassword, String certificatePassword)
    {
        try
        {

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

            KeyStore keyStore = getKeyStore("mule-agent.jks", keyStorePassword.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, certificatePassword.toCharArray());

            KeyStore trustStore = getKeyStore("truststore.jks", null);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
            return new AuthProxyClient(baseUrl, ClientBuilder.newBuilder().sslContext(sslContext).build());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static KeyStore getKeyStore(String keyStoreFileName, char[] password) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException
    {
        File keyStoreFile = new File(getConfigFilePath(keyStoreFileName));
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(keyStoreFile), password);
        return keyStore;
    }

    private static String getConfigFilePath(String configFileName)
    {
        String configurationFolder = System.getProperty("mule.agent.configuration.folder");
        if (configurationFolder == null)
        {
            throw new RuntimeException("Configuration folder was not set.");
        }
        return configurationFolder + File.separator +  configFileName;
    }

    public <T> void post(String path, Entity<T> json)
    {
        LOGGER.info("Doing POST request to auth proxy at " + path);
        final Response response = this.client
                .target(this.baseUrl + path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(json);
        LOGGER.info("Auth Proxy response code: " + String.valueOf(response.getStatus()));
        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL)
        {
            Response.StatusType statusInfo = response.getStatusInfo();
            throw new RuntimeException("(" + statusInfo.getFamily()+ ") " + statusInfo.getStatusCode() + " " + statusInfo.getReasonPhrase());
        }
    }

}
