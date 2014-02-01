package com.asprotunity.joker.internal.protocol;

import com.asprotunity.joker.BadRequestException;
import com.asprotunity.joker.InternalServerErrorException;
import com.asprotunity.joker.NotFoundException;
import com.asprotunity.joker.RemoteException;
import com.asprotunity.joker.proxy.ServiceAddress;
import org.eclipse.jetty.http.HttpStatus;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPServiceCaller implements ServiceCaller {

    private final CallEncoder encoder;

    private static class CallResultInternal {
        public static final int NO_ERROR = -1;
        public final int errorCode;
        public final String encodedResult;

        private CallResultInternal(int errorCode, String encodedResult) {
            this.errorCode = errorCode;
            this.encodedResult = encodedResult;
        }
    }

    public HTTPServiceCaller() {
        encoder = new JsonCallEncoder(JsonParserBuilder.build(new
                ServiceProxyMaker(this)));
    }

    @Override
    public Object call(Object[] params, String methodName, ServiceAddress serviceAddress, Class<?> returnType) {
        String encodedCall = encoder.encode(params);
        String url = "http://" + serviceAddress.hostName + ":" +
                serviceAddress.port + "/" + serviceAddress.serviceName + "/" + methodName;
        try {
            CallResultInternal encodedResult = sendPost(new URL(url),
                    encodedCall);

            if (encodedResult.errorCode == CallResultInternal.NO_ERROR) {
                if (encodedResult.encodedResult.isEmpty()) {
                    return null;
                }
                return encoder.decode(encodedResult.encodedResult, returnType);
            } else {
                ExceptionWrapper result = encoder.decode(encodedResult.encodedResult,
                        ExceptionWrapper.class);
                throw makeException(encodedResult.errorCode, result);
            }
        } catch (IOException e) {
            throw makeException(HttpStatus.BAD_REQUEST_400,
                    new ExceptionWrapper("Malformed URL: " + url, ""));
        }
    }

    public static RemoteException makeException(int errorCode,
                                                ExceptionWrapper ew) {
        switch (errorCode) {
            case HttpStatus.BAD_REQUEST_400:
                return new BadRequestException(ew.message);
            case HttpStatus.NOT_FOUND_404:
                return new NotFoundException(ew.message);
            default:
                return new InternalServerErrorException(ew.message);
        }
    }

    private CallResultInternal sendPost(URL url, String encodedCall) throws
            IOException {

        HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();

        httpcon.setDoOutput(true);
        httpcon.setRequestProperty("Content-Type", encoder.contentType());
        httpcon.setRequestProperty("Accept", encoder.contentType());
        httpcon.setRequestMethod("POST");

        final OutputStreamWriter osw = new OutputStreamWriter(httpcon
                .getOutputStream(), encoder.charset());
        osw.write(encodedCall);
        osw.close();

        int responseCode = httpcon.getResponseCode();

        if (HttpStatus.isClientError(responseCode) ||
                HttpStatus.isServerError(responseCode)) {
            return new CallResultInternal(responseCode,
                    readString(httpcon.getErrorStream()));
        }
        return new CallResultInternal(CallResultInternal.NO_ERROR,
                readString(httpcon.getInputStream()));
    }


    private String readString(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(inputStream,
                        encoder.charset()));

        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }
}
