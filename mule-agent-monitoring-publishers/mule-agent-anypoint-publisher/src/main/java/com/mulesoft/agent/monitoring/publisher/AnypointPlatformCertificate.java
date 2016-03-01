package com.mulesoft.agent.monitoring.publisher;


import com.mulesoft.agent.AgentInitializationException;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class AnypointPlatformCertificate {

    private static final String MULE_AGENT_JKS = "mule-agent.jks";
    private static final String TRUSTSTORE_JKS = "truststore.jks";

    private String keyStorePassword;
    private String keyStoreAliasPassword;

    public String getKeyStorePassword()
    {
        return keyStorePassword;
    }

    public String getKeyStoreAliasPassword()
    {
        return keyStoreAliasPassword;
    }


    public SSLContext createSSLContext() throws Exception{
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        loadSSLContext(sslContext);
        return sslContext;
    }

    private void loadSSLContext(SSLContext sslContext) throws Exception
    {
        try
        {
            if (keyStorePassword == null || keyStoreAliasPassword == null ){
                throw new Exception("The Anypoint Platform Certificate is not configured correctly"); // TODO: Throw better exception
            }

            String keyStorePassword = getKeyStorePassword();
            KeyStore keyStore = KeyStore.getInstance("JKS");
            String configurationFolder = System.getProperty("mule.agent.configuration.folder");
            File keyStoreFile = new File(configurationFolder + File.separator + MULE_AGENT_JKS);

            loadKeyStore(keyStore, keyStorePassword, keyStoreFile);

            String certificatePassword = getKeyStoreAliasPassword();
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, certificatePassword.toCharArray());

            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(new FileInputStream(new File(configurationFolder + File.separator + TRUSTSTORE_JKS)), null);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(trustStore);

            KeyManager[] keyManagers = kmf.getKeyManagers();
            TrustManager[] trustManagers = tmf.getTrustManagers();
            SecureRandom secureRandom = new SecureRandom();

            sslContext.init(keyManagers, trustManagers, secureRandom);
        }
        catch (KeyStoreException e)
        {
            throw new AgentInitializationException("Invalid KeyStore format. Your key store is not valid. Agent only support JKS KeyStore.", e);
        }
        catch (CertificateException e)
        {
            throw new AgentInitializationException("Invalid certificate algorithm. Your key store is not valid. Agent only support JKS KeyStore.", e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new AgentInitializationException("Invalid KeyStore algorithm. Your key store is not valid. Agent only support JKS KeyStore.", e);
        }
        catch (FileNotFoundException e)
        {
            throw new AgentInitializationException("Unable to read KeyStore/TrustStore file for web socket ssl connection", e);
        }
        catch (KeyManagementException e)
        {
            throw new AgentInitializationException("Unable to read KeyStore/TrustStore file for web socket ssl connection", e);
        }
        catch (UnrecoverableKeyException e)
        {
            throw new AgentInitializationException("Unable to read KeyStore/TrustStore file for web socket ssl connection", e);
        }
        catch (IOException e)
        {
            throw new AgentInitializationException("Unable to read KeyStore/TrustStore file for web socket ssl connection", e);
        }
    }


    protected void loadKeyStore(KeyStore jks, String keyStorePassword, File keyStoreFile) throws IOException, NoSuchAlgorithmException, CertificateException
    {
        jks.load(new FileInputStream(keyStoreFile), keyStorePassword == null ? null : keyStorePassword.toCharArray());
    }
}
