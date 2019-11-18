package net.tospay.transaction.configs;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.tospay.transaction.enums.ResponseCode;
import okhttp3.Authenticator;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

import static net.tospay.transaction.models.response.ResponseCode.BANK_FAILURE;

/**
 * @author : Clifford Owino
 * @Email : owinoclifford@gmail.com
 * @since : 9/5/2019, Thu
 **/
@Configuration
public class RetrofitConfig
{
    private static final Logger LOG = LoggerFactory.getLogger(RetrofitConfig.class);

    @Autowired
    ResourceLoader resourceLoader;

    @Value("${server.ssl.cer}")
    Resource cerFile;

    @Value("${server.ssl.key-store}")
    Resource jksFile;

    String accessToken = null;

    boolean justAuthed;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${coop.baseurl}")
    private String baseURL;

    //  @Value("${server.ssl.key-store}")
    //  String key;

    @Value("${coop.consumer.key}")
    private String consumerkey;

    @Value("${coop.consumer.secret}")
    private String consumersecret;

    @Value("${server.ssl.key-store-password}")
    private String keystorePassword;

    @Value("${server.ssl.key-alias}")
    private String keystoreAlias;

    @Value("${server.ssl.key-store-type}")
    private String keyStoreType;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public Retrofit retrofit() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException
    {
        String usernamePassword = consumerkey + ":" + consumersecret;
        String authorization = Base64.getEncoder().encodeToString(usernamePassword.getBytes());

        // loading CAs from an InputStream
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream cert = cerFile.getInputStream();
        Certificate ca = null;
        try {
            ca = cf.generateCertificate(cert);
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            cert.close();
        }

        // creating a KeyStore containing our trusted CAs
        //String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(jksFile.getInputStream(), keystorePassword.toCharArray());
        keyStore.setCertificateEntry(keystoreAlias, ca);

        // creating a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // creating an SSLSocketFactory that uses our TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) tmf.getTrustManagers()[0])
                .addInterceptor(chain -> {
                    Request request = chain.request();

                    Request.Builder requestBuilder = request.newBuilder();

                    if (request.headers("Authorization").isEmpty()) {
                        requestBuilder.addHeader("Content-Type", "Application/json");
                        requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
                    }
                    requestBuilder.method(request.method(), request.body());
                    request = requestBuilder.build();
                    Response response = chain.proceed(request);

                    //retry coz now session is active if intercepter kicked in
                    if (justAuthed && (response.code() == HttpStatus.UNAUTHORIZED.value()
                            || response.code() == HttpStatus.FORBIDDEN.value()))
                    {
                        LOG.debug("retrying request for access token", accessToken);
                        justAuthed = false;
                        requestBuilder.removeHeader("Authorization");
                        requestBuilder.addHeader("Authorization", "Bearer " + accessToken);
                        requestBuilder.method(request.method(), request.body());
                        request = requestBuilder.build();
                        response = chain.proceed(request);
                    }

                    return response;
                }).authenticator(new Authenticator()
                {
                    @Nullable
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException
                    {
                        if ((response.code() == HttpStatus.UNAUTHORIZED.value()
                                || response.code() == HttpStatus.FORBIDDEN.value()) && !response.request()
                                .header("Authorization").equals("Bearer " + accessToken))
                        {
                            return null;
                        }

                        // request new key
                        LOG.debug("request for new access token ", accessToken);
                        try {
                            AuthenticationInterface authService = retrofit().create(AuthenticationInterface.class);
                            Map<String, String> header = new HashMap<>();
                            header.put("Content-Type", "application/x-www-form-urlencoded");
                            header.put("Authorization", "Basic " + authorization);
                            Map<String, String> ftRequest = new HashMap<>();
                            ftRequest.put("grant_type", "client_credentials");

                            retrofit2.Response<Map<Object, Object>> result =
                                    authService.token(header, ftRequest).execute();
                            Map<Object, Object> tokenMap = result.body();
                            LOG.debug("access token ", tokenMap);

                            if (tokenMap != null) {
                                accessToken = tokenMap.get("access_token").toString();
                                justAuthed = true;
                            }
                        } catch (Exception ex) {
                            LOG.debug("ERROR", ex);
                        }
                        if (accessToken != null)
                        // retry the failed 401 request with new access token
                        {
                            return response.request().newBuilder()
                                    .header("Authorization", accessToken) // use the new access token
                                    .build();
                        } else {
                            return null;
                        }
                    }
                }).addInterceptor(
                        chain -> {
                            Request request = chain.request();
                            Response response = chain.proceed(request);
                            LOG.debug("response ", response);
                            MediaType contentType = response.body().contentType();

                            if (org.apache.http.HttpStatus.SC_OK != response.code() && (
                                    response.code() != HttpStatus.UNAUTHORIZED.value()
                                            || response.code() != HttpStatus.FORBIDDEN.value()))
                            {//map error codes if not an auth error
                                String jsonData = response.body().string();
                                Map<String, Object> map = JsonParserFactory.getJsonParser().parseMap(jsonData);

                                if (map.get("fault") != null) {
                                    Map<String, Object> fault = (Map<String, Object>) map
                                            .get("fault");//JsonParserFactory.getJsonParser().parseMap(.toString());
                                    map.put("MessageCode", fault.get("code"));
                                    map.put("MessageDescription", jsonData);//fault.get("message"));
                                } else if (org.apache.http.HttpStatus.SC_PROCESSING == response
                                        .code())
                                {//102  -= PROCESSING3

                                    map.put("MessageCode", ResponseCode.GENERAL_ERROR.code);
                                    map.put("MessageDescription", jsonData);//fault.get("message"));
                                } else if (org.apache.http.HttpStatus.SC_FORBIDDEN == response
                                        .code())
                                {//403  -9=CURRENCY INVALID/NOT ALLOWED -10=invalid bank code /not pesalink

                                    map.put("MessageCode", ResponseCode.INVALID_CURRENCY_ERROR.code);
                                    map.put("MessageDescription", jsonData);//fault.get("message"));
                                } else if (org.apache.http.HttpStatus.SC_REQUEST_TIMEOUT == response
                                        .code())
                                {//403  -4=SC_REQUEST_TIMEOUT

                                    map.put("MessageCode", ResponseCode.REQUEST_TIMEOUT_ERROR.code);
                                    map.put("MessageDescription", jsonData);//fault.get("message"));
                                } else if (org.apache.http.HttpStatus.SC_CONFLICT == response
                                        .code())
                                {//409  -1=SC_CONFLICT

                                    map.put("MessageCode", ResponseCode.DUPLICATE_MESSAGE_ERROR.code);
                                    map.put("MessageDescription", jsonData);//fault.get("message"));
                                }
                                //defaults
                                if (map.get("MessageCode") == null) {
                                    map.put("MessageCode", BANK_FAILURE.responseCode);
                                    map.put("MessageDescription", jsonData);
                                }
                                String json = new ObjectMapper().writeValueAsString(map);
                                ResponseBody body = ResponseBody.create(contentType, json);
                                return response.newBuilder().code(org.apache.http.HttpStatus.SC_OK).body(body).build();
                            }

                            return response;
                        }
                ).build();

        return new Retrofit.Builder().baseUrl(baseURL)
                .addConverterFactory(JacksonConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    public interface AuthenticationInterface
    {
        /**
         * @param request
         * @return
         */
        @POST("/token")
        @FormUrlEncoded
        retrofit2.Call<Map<Object, Object>> token(@HeaderMap Map<String, String> header,
                @FieldMap Map<String, String> request);
    }

//    private KeyStore readKeyStore() {
//
//        KeyStore ks = null;
//        try {
//            ks = KeyStore.getInstance(KeyStore.getDefaultType());
//            Resource resource = applicationContext.getResource(keystorePath);
//            InputStream inputStream = new ClassPathResource(keystorePath).getInputStream();
//            Resource resource = resourceLoader.getResource(key);
//            ks.load(resource.getInputStream(), keystorePassword.toCharArray());
//            ks.load(new ClassPathResource(keystorePath).getInputStream(), keystorePassword.toCharArray());
//            ks.load(new ClassPathResource("coop_tsp.jks").getInputStream(), keystorePassword.toCharArray());
//            ks.load(jksFile.getInputStream(), keystorePassword.toCharArray());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return ks;
//    }
}
