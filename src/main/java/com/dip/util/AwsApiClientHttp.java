package com.dip.util;

import static org.joda.time.DateTimeZone.UTC;

import com.amazonaws.AmazonClientException;
import com.dip.constant.CommonConstant;
import com.dip.handler.AwsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropsUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class AwsApiClientHttp implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6005348714492302145L;

    /** The Constant ACCEPT. */
    private static final String ACCEPT = "accept";

    /** The Constant AMAZON_AWS. */
    private static final String AMAZON_AWS = "amazonaws.com";

    /** The Constant API_CONNECTION_TIMEOUT. */
    private static final String API_CONNECTION_TIMEOUT = "api.connection.timeout";

    /** The Constant API_ID. */
    private static final String API_ID = "api.id";

    /** The Constant API_REGION. */
    private static final String API_REGION = "api.region";

    /** The Constant API_STAGE. */
    private static final String API_STAGE = "api.stage";

    /** The Constant AUTHORIZATION. */
    private static final String AUTHORIZATION = "authorization";

    /** The Constant AWS4. */
    private static final String AWS4 = "AWS4";

    /** The Constant AWS4_HMAC_SHA256. */
    private static final String AWS4_HMAC_SHA256 = "AWS4-HMAC-SHA256";

    /** The Constant AWS4_REQUEST. */
    private static final String AWS4_REQUEST = "aws4_request";

    /** The Constant CONTENT_LENGTH. */
    private static final String CONTENT_LENGTH = "content-length";

    /** The Constant CONTENT_TYPE. */
    private static final String CONTENT_TYPE = "content-type";

    /** The Constant CREDENTIAL. */
    private static final String CREDENTIAL = "Credential";

    /** The Constant DATE_PATTERN. */
    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormat.forPattern("yyyyMMdd");

    /** The Constant DATE_PATTERN_AMZ. */
    private static final DateTimeFormatter DATE_PATTERN_AMZ = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss'Z'");

    /** The Constant EXECUTE_API. */
    private static final String EXECUTE_API = "execute-api";

    /** The Constant HMAC_SHA256. */
    private static final String HMAC_SHA256 = "HmacSHA256";

    /** The Constant HOST. */
    private static final String HOST = "host";

    /** The Constant HTTPS. */
    private static final String HTTPS = "https://";

    /** The Constant JSON_TYPE. */
    private static final String JSON_TYPE = "application/json";

    /** The Constant LOGGER. */
    /*private static final Log LOGGER = LogFactoryUtil.getLog(AwsApiClientHttp.class);*/
    private static final Log logger = LogFactoryUtil.getLog(AwsApiClientHttp.class);

    /** The Constant MAPPER. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** The Constant POST. */
    private static final String POST = "POST";

    /** The Constant SHA_256. */
    private static final String SHA_256 = "SHA-256";

    /** The Constant SIGNATURE. */
    private static final String SIGNATURE = "Signature";

    /** The Constant SIGNED_HEADERS. */
    private static final String SIGNED_HEADERS = "SignedHeaders";

    /** The Constant X_AMZ_DATE. */
    private static final String X_AMZ_DATE = "x-amz-date";

    /** The aws access key id. */
    private final String awsAccessKeyId;

    /** The aws secret key. */
    private final String awsSecretKey;

    /** The connection time out. */
    private int connectionTimeOut = 10;

    /** The id. */
    private String id;

    /** The region. */
    private String region;

    /** The staged api. */
    private String stagedApi;
    private String stagedApi1;
    /**
     * Instantiates a new aws api client http.
     */
    public AwsApiClientHttp() {

        /*AWSCredentialsProviderChain credentialsProvider = new DefaultAWSCredentialsProviderChain();

        final AWSCredentials credentials = credentialsProvider.getCredentials();
*/      awsAccessKeyId = PropsUtil.get("stationery.accesskey");//credentials.getAWSAccessKeyId();
        awsSecretKey = PropsUtil.get("stationery.secretkey");//credentials.getAWSSecretKey();
        id = PropsUtil.get(API_ID);
        region = PropsUtil.get(API_REGION);
        stagedApi = PropsUtil.get(API_STAGE);
        try {
            connectionTimeOut = Integer.parseInt(PropsUtil.get(API_CONNECTION_TIMEOUT));
        } catch (final Exception e) {
            logger.warn("Unable to get connection.timeOut value. Using: " + connectionTimeOut + ". Error message: "+ e.getMessage());
        }
    }

    /**
     * Execute.
     *
     * @param request the request
     * @return the AwsResponse
     * @throws Exception the exception
     */
   /* public String execute(String xmDipIdentity,String callid,String channel) throws Exception {
        return execute(xmDipIdentity, callid,channel);
    }*/

    /**
     * Execute.
     *
     * @param request the request
     * @param proxy the proxy
     * @return the AwsResponse
     * @throws Exception the exception
     */
    public String executePost(String awsRequest,String xmDipIdentity,String callid,String channel) throws Exception {
        final String host = CommonUtil.concatenateWithSymbol(Symbols.PERIOD, id, EXECUTE_API, region, AMAZON_AWS);
        final URL url = new URL(HTTPS + host + stagedApi);
        HttpURLConnection conn = null;
        try {
            //final String input = MAPPER.writeValueAsString(awsRequest);
            final String inputLength = String.valueOf(awsRequest.length());
            final DateTime date = new DateTime(UTC);
            final String amzdate = date.toString(DATE_PATTERN_AMZ);
            final String canonicalQueryString = getCanonicalQueryString(inputLength);
            final String hashedPayload = hexSha256(inputLength);
           // final String hashedPayload = hexSha256("");
            final String canonicalHeaders = getCanonicalHeader(amzdate,host,xmDipIdentity,callid,channel);
           /* final String signedHeaders = CommonUtil.concatenateWithSymbol(Symbols.SEMI_COLON, CONTENT_LENGTH,CONTENT_TYPE, HOST, X_AMZ_DATE);*/
            final String signedHeaders = CommonUtil.concatenateWithSymbol(Symbols.SEMI_COLON,"X-MyDip-Identity","Call-Id","Channel",
            		CONTENT_LENGTH,CONTENT_TYPE, HOST, X_AMZ_DATE);
            final String datestamp = date.toString(DATE_PATTERN);
            final String scope = CommonUtil.concatenateWithSymbol(Symbols.FORWARD_SLASH, datestamp, region,EXECUTE_API, AWS4_REQUEST);
            final String canonicalRequest = CommonUtil.concatenateWithSymbol(Symbols.NEW_LINE, POST,
                stagedApi + Symbols.NEW_LINE, canonicalQueryString, canonicalHeaders, signedHeaders, hashedPayload);
            
            System.out.println("canonicalRequest:"+canonicalRequest);
            final String stringToSign = CommonUtil.concatenateWithSymbol(Symbols.NEW_LINE, AWS4_HMAC_SHA256,
                amzdate, scope, hexSha256(canonicalRequest));
            
            System.out.println("stringToSign:"+stringToSign);
            final String signature = hex(
                hmacSha256(stringToSign, getSignatureKey(awsSecretKey, datestamp, region, EXECUTE_API)));
            final String credentialsAuthorizationHeader = getCredentialsAuthorizationHeader(scope);
            final String signedHeadersAuthorizationHeader = CommonUtil.concatenateWithSymbol(Symbols.EQUALS,
                SIGNED_HEADERS, signedHeaders);
            final String signatureAuthorizationHeader = CommonUtil.concatenateWithSymbol(Symbols.EQUALS,
                SIGNATURE, signature);
            final String authorization = getAuthorization(credentialsAuthorizationHeader,
                signedHeadersAuthorizationHeader, signatureAuthorizationHeader);
            //if (proxy == null) {
                conn = (HttpURLConnection) url.openConnection();
            /*} else {
                conn = (HttpURLConnection) url.openConnection(proxy);
            }*/
            conn.setConnectTimeout(connectionTimeOut);
            /*if(Validator.isNotNull("productId")){
            conn.setRequestProperty("ProductId", "72281");
            }*/
            conn.setRequestProperty("X-MyDip-Identity", xmDipIdentity);
            conn.setRequestProperty("Call-Id", callid);
            conn.setRequestProperty("Channel", channel);
            conn.setRequestProperty(CONTENT_TYPE, JSON_TYPE);
            conn.setRequestProperty(HOST, host);
            conn.setRequestProperty(CONTENT_LENGTH, inputLength);
            conn.setRequestProperty(X_AMZ_DATE, amzdate);
            conn.setRequestProperty(AUTHORIZATION, authorization);
            conn.setRequestProperty(ACCEPT, JSON_TYPE);
            conn.setRequestMethod(POST);
            conn.setDoInput(true);
            writeInput(conn, awsRequest);
            return getAPResponse(conn);
        } catch (final Exception e) {
            throw e;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
    public String execute(String xmDipIdentity,String callid,String channel,String orderId) throws Exception {
        final String host = CommonUtil.concatenateWithSymbol(Symbols.PERIOD, id, EXECUTE_API, region, AMAZON_AWS);
        URL url = new URL(HTTPS + host + stagedApi);
        
        logger.info("host and url of elb of Statinoery API::: "+host+"\n"+url);
        
        HttpURLConnection conn = null;
        UriComponentsBuilder builder = null;
        try {
        	/*builder = UriComponentsBuilder.fromUriString(url.toString());
        	builder.pathSegment(orderId);
        	builder.build().toUriString();*/
           //final String input = MAPPER.writeValueAsString(request);
            //final String inputLength = String.valueOf(input.length());
            final DateTime date = new DateTime(UTC);
            final String amzdate = date.toString(DATE_PATTERN_AMZ);
            final String canonicalQueryString ="";     //"ProductID="+productId; // getCanonicalQueryString(inputLength);
            //final String hashedPayload = hexSha256(input);
            final String hashedPayload = hexSha256("");
            final String canonicalHeaders = getCanonicalHeader(amzdate,host,xmDipIdentity,callid,channel);
           /* final String signedHeaders = CommonUtil.concatenateWithSymbol(Symbols.SEMI_COLON, CONTENT_LENGTH,CONTENT_TYPE, HOST, X_AMZ_DATE);*/
            final String signedHeaders = CommonUtil.concatenateWithSymbol(Symbols.SEMI_COLON, "X-MyDip-Identity","Call-Id","Channel",
                    CONTENT_TYPE, HOST, X_AMZ_DATE);
            final String datestamp = date.toString(DATE_PATTERN);
            final String scope = CommonUtil.concatenateWithSymbol(Symbols.FORWARD_SLASH, datestamp, region,
                EXECUTE_API, AWS4_REQUEST);
            final String canonicalRequest = CommonUtil.concatenateWithSymbol(Symbols.NEW_LINE, "GET",
                stagedApi + Symbols.NEW_LINE, canonicalQueryString, canonicalHeaders, signedHeaders, hashedPayload);
            final String stringToSign = CommonUtil.concatenateWithSymbol(Symbols.NEW_LINE, AWS4_HMAC_SHA256,
                amzdate, scope, hexSha256(canonicalRequest));
            final String signature = hex(
                hmacSha256(stringToSign, getSignatureKey(awsSecretKey, datestamp, region, EXECUTE_API)));
            final String credentialsAuthorizationHeader = getCredentialsAuthorizationHeader(scope);
            final String signedHeadersAuthorizationHeader = CommonUtil.concatenateWithSymbol(Symbols.EQUALS,
                SIGNED_HEADERS, signedHeaders);
            final String signatureAuthorizationHeader = CommonUtil.concatenateWithSymbol(Symbols.EQUALS,
                SIGNATURE, signature);
            final String authorization = getAuthorization(credentialsAuthorizationHeader,
                signedHeadersAuthorizationHeader, signatureAuthorizationHeader);
            //if (proxy == null) {
                conn = (HttpURLConnection) url.openConnection();
            /*} else {
                conn = (HttpURLConnection) url.openConnection(proxy);
            }*/
            conn.setConnectTimeout(connectionTimeOut);
            /*if(Validator.isNotNull(orderId)){
            conn.setRequestProperty("StationeryOrderId", orderId);
            }*/
            conn.setRequestProperty("X-MyDip-Identity", xmDipIdentity);
            conn.setRequestProperty("Call-Id", callid);
            conn.setRequestProperty("Channel", channel);
            conn.setRequestProperty(CONTENT_TYPE, JSON_TYPE);
            conn.setRequestProperty(HOST, host);
            //conn.setRequestProperty(CONTENT_LENGTH, inputLength);
            conn.setRequestProperty(X_AMZ_DATE, amzdate);
            conn.setRequestProperty(AUTHORIZATION, authorization);
            conn.setRequestProperty(ACCEPT, JSON_TYPE);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            //writeInput(conn, input);
            return getAPResponse(conn);
        } catch (final Exception e) {
            throw e;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Gets the authorization.
     *
     * @param credentialsAuthorizationHeader the credentials authorization
     *        header
     * @param signedHeadersAuthorizationHeader the signed headers authorization
     *        header
     * @param signatureAuthorizationHeader the signature authorization header
     * @return the authorization
     */
    private String getAuthorization(final String credentialsAuthorizationHeader,
        final String signedHeadersAuthorizationHeader, final String signatureAuthorizationHeader) {
        final StringBuilder authorizationBuilder = new StringBuilder();
        authorizationBuilder.append(AWS4_HMAC_SHA256);
        authorizationBuilder.append(Symbols.SPACE);
        authorizationBuilder.append(credentialsAuthorizationHeader);
        authorizationBuilder.append(Symbols.COMMA);
        authorizationBuilder.append(Symbols.SPACE);
        authorizationBuilder.append(signedHeadersAuthorizationHeader);
        authorizationBuilder.append(Symbols.COMMA);
        authorizationBuilder.append(Symbols.SPACE);
        authorizationBuilder.append(signatureAuthorizationHeader);
        final String authorization = authorizationBuilder.toString();
        return authorization;
    }

    
    
    
    /**
     * Gets the canonical header.
     *
     * @param amzdate the amzdate
     * @param payloadHash the payload hash
     * @param host the host
     * @return the canonical header
     */
    private String getCanonicalHeader(final String amzdate,
    		final String host,String xmyDipidentity,String channel,String callid) {
        final StringBuilder canonicalHeaderBuilder = new StringBuilder();
        canonicalHeaderBuilder.append("X-MyDip-Identity");
        canonicalHeaderBuilder.append(Symbols.COLON);
        canonicalHeaderBuilder.append(xmyDipidentity);
        

        canonicalHeaderBuilder.append(Symbols.NEW_LINE);
        canonicalHeaderBuilder.append("Call-Id");
        canonicalHeaderBuilder.append(Symbols.COLON);
        canonicalHeaderBuilder.append(callid);

        canonicalHeaderBuilder.append(Symbols.NEW_LINE);
        canonicalHeaderBuilder.append("Channel");
        canonicalHeaderBuilder.append(Symbols.COLON);        
        canonicalHeaderBuilder.append(channel);      
        canonicalHeaderBuilder.append(Symbols.NEW_LINE);
        canonicalHeaderBuilder.append(CONTENT_TYPE);
        canonicalHeaderBuilder.append(Symbols.COLON);
        canonicalHeaderBuilder.append(JSON_TYPE);
        
        canonicalHeaderBuilder.append(Symbols.NEW_LINE);
        canonicalHeaderBuilder.append(HOST);
        canonicalHeaderBuilder.append(Symbols.COLON);
        canonicalHeaderBuilder.append(host);
        canonicalHeaderBuilder.append(Symbols.NEW_LINE);
        canonicalHeaderBuilder.append(X_AMZ_DATE);
        canonicalHeaderBuilder.append(Symbols.COLON);
        canonicalHeaderBuilder.append(amzdate);
        canonicalHeaderBuilder.append(Symbols.NEW_LINE);
        
        
        
        
       /* canonicalHeaderBuilder.append(CommonConstant.CES_TAG); 
        canonicalHeaderBuilder.append(Symbols.COLON);
        canonicalHeaderBuilder.append(CommonUtil.getCESTag(resrequest));
        canonicalHeaderBuilder.append(Symbols.NEW_LINE);        
        canonicalHeaderBuilder.append(CommonConstant.UPS_HEADER_CALLID);
        canonicalHeaderBuilder.append(Symbols.COLON);
        canonicalHeaderBuilder.append(CommonUtil.generateUuidForCallId());
        canonicalHeaderBuilder.append(Symbols.NEW_LINE);
        canonicalHeaderBuilder.append(CommonConstant.UPS_HEADER_MYTOLL_IDENTITY);        
        canonicalHeaderBuilder.append(Symbols.COLON);
        canonicalHeaderBuilder.append(CommonUtil.getUserId(resrequest));
        canonicalHeaderBuilder.append(Symbols.NEW_LINE);
        canonicalHeaderBuilder.append(CommonConstant.CONTENT_TYPE); 
        canonicalHeaderBuilder.append(Symbols.COLON);
        canonicalHeaderBuilder.append(ContentTypes.APPLICATION_JSON);
        canonicalHeaderBuilder.append(Symbols.NEW_LINE);       
        canonicalHeaderBuilder.append(CommonConstant.ACCEPT);
        canonicalHeaderBuilder.append(Symbols.COLON);
        canonicalHeaderBuilder.append(ContentTypes.APPLICATION_JSON); 
        canonicalHeaderBuilder.append(Symbols.NEW_LINE);*/
        return canonicalHeaderBuilder.toString();
    }

    /**
     * Gets the canonical query string.
     *
     * @param payloadLength the payload length
     * @return the canonical query string
     */
    private String getCanonicalQueryString(final String payloadLength) {
        final StringBuilder requestParametersBuilder = new StringBuilder();
        requestParametersBuilder.append(CONTENT_LENGTH);
        requestParametersBuilder.append(Symbols.COLON);
        requestParametersBuilder.append(payloadLength);
        requestParametersBuilder.append(Symbols.NEW_LINE);
        requestParametersBuilder.append(CONTENT_TYPE);
        requestParametersBuilder.append(Symbols.COLON);
        requestParametersBuilder.append(JSON_TYPE);
        return requestParametersBuilder.toString();
    }

    /**
     * Gets the credentials authorization header.
     *
     * @param scope the scope
     * @return the credentials authorization header
     */
    private String getCredentialsAuthorizationHeader(final String scope) {
        final StringBuilder credentialsAuthorizationHeaderBuilder = new StringBuilder();
        credentialsAuthorizationHeaderBuilder.append(CREDENTIAL);
        credentialsAuthorizationHeaderBuilder.append(Symbols.EQUALS);
        credentialsAuthorizationHeaderBuilder.append(awsAccessKeyId);
        credentialsAuthorizationHeaderBuilder.append(Symbols.FORWARD_SLASH);
        credentialsAuthorizationHeaderBuilder.append(scope);
        final String credentialsAuthorizationHeader = credentialsAuthorizationHeaderBuilder.toString();
        return credentialsAuthorizationHeader;
    }

    /**
     * Gets the response.
     *
     * @param conn the conn
     * @return the response
     */
    private AwsResponse getResponse(final HttpURLConnection conn) {
        AwsResponse response = null;
        InputStream in = null;
        try {
            in = new BufferedInputStream(conn.getInputStream());
            response = MAPPER.readValue(IOUtils.toString(in, "UTF-8"), AwsResponse.class);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {
                }
            }
        }
        return response;
    }
    
    private String getAPResponse(final HttpURLConnection conn) {
        InputStream in = null;
        String data="";
        try {
            in = new BufferedInputStream(conn.getInputStream());
            data=IOUtils.toString(in, "UTF-8");
            logger.info("data::: getAPResponse() "+data);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {
                }
            }
        }
        return data;
    }

    /**
     * Gets the signature key.
     *
     * @param key the key
     * @param dateStamp the date stamp
     * @param regionName the region name
     * @param serviceName the service name
     * @return the signature key
     * @throws UnsupportedEncodingException the unsupported encoding exception
     * @throws InvalidKeyException the invalid key exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws IllegalStateException the illegal state exception
     */
    private byte[] getSignatureKey(final String key, final String dateStamp, final String regionName,
        final String serviceName)
        throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, IllegalStateException {
        final byte[] kSecret = (AWS4 + key).getBytes(CommonConstant.UTF8);
        final byte[] kDate = hmacSha256(dateStamp, kSecret);
        final byte[] kRegion = hmacSha256(regionName, kDate);
        final byte[] kService = hmacSha256(serviceName, kRegion);
        final byte[] kSigning = hmacSha256(AWS4_REQUEST, kService);
        return kSigning;
    }

    /**
     * Hex.
     *
     * @param bytes the bytes
     * @return the String
     */
    private String hex(final byte[] bytes) {
        final StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            final String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Hex sha 256.
     *
     * @param payload the payload
     * @return the String
     */
    private String hexSha256(final String payload) {
        return hex(sha256(payload));
    }

    /**
     * Hmac sha 256.
     *
     * @param data the data
     * @param key the key
     * @return the byte[]
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws InvalidKeyException the invalid key exception
     * @throws IllegalStateException the illegal state exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private byte[] hmacSha256(final String data, final byte[] key)
        throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException {
        final Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(new SecretKeySpec(key, HMAC_SHA256));
        return mac.doFinal(data.getBytes(CommonConstant.UTF8));
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Sets the region.
     *
     * @param region the new region
     */
    public void setRegion(final String region) {
        this.region = region;
    }

    /**
     * Sets the staged api.
     *
     * @param stagedApi the new staged api
     */
    public void setStagedApi(final String stagedApi) {
        this.stagedApi = stagedApi;
    }

    /**
     * Sha 256.
     *
     * @param input the input
     * @return the byte[]
     */
    private byte[] sha256(final String input) {
        try {
            final MessageDigest md = MessageDigest.getInstance(SHA_256);
            md.update(input.getBytes(CommonConstant.UTF8));
            return md.digest();
        } catch (final Exception e) {
            throw new AmazonClientException("Unable to compute hash while signing request: " + e.getMessage(), e);
        }
    }

    /**
     * Write input.
     *
     * @param conn the conn
     * @param input the input
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    private void writeInput(final HttpURLConnection conn, final String input)
        throws IOException, UnsupportedEncodingException {
        if (input != null) {
            conn.setDoOutput(true);
            final OutputStream os = conn.getOutputStream();
            os.write(input.getBytes(CommonConstant.UTF8));
            os.close();
        }
    }
}
