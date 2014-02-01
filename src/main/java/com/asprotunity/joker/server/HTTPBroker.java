package com.asprotunity.joker.server;

import com.asprotunity.joker.RemoteException;
import com.asprotunity.joker.internal.protocol.*;
import com.asprotunity.joker.proxy.ServiceAddress;
import com.asprotunity.joker.proxy.ServiceProxy;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

public class HTTPBroker {
    private final Server server;
    private final ServiceHandler serviceHandler;

    public HTTPBroker(int port) {
        server = new Server(port);
        serviceHandler = new ServiceHandler(this);
        server.setHandler(serviceHandler);
    }

    public void start() {
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> ServiceProxy<T> registerService(String serviceName, T service, Class<T> interfaceClass) {
        return serviceHandler.registerService(service, serviceName, interfaceClass);
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isStarted() {
        return server.isStarted();
    }

    public void join() {
        try {
            server.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public int getPort() {
        if (!isStarted()) {
            throw new BrokerNotStartedException("The broker must be started before getting the port");
        }
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    private static class ServiceHandler extends AbstractHandler {

        private final CallEncoder encoder;
        private ConcurrentHashMap<String, ServiceProxyImpl<?>> services;
        private HTTPBroker broker;

        public ServiceHandler(HTTPBroker broker) {
            this.broker = broker;
            services = new ConcurrentHashMap<>();
            encoder = new JsonCallEncoder(JsonParserBuilder.build(new
                    ServiceProxyMaker(new HTTPServiceCaller())));
        }

        @Override
        public void handle(String target, Request request, HttpServletRequest httpServletRequest,
                           HttpServletResponse response) throws IOException {

            String[] serviceNameCall = target.substring(1).split("/");

            String serviceName = serviceNameCall[0];
            String methodName = MethodName.normalize(serviceNameCall[1]);

            String jsonRequest = readRequestBody(request);
            response.setContentType(encoder.contentType());
            response.setCharacterEncoding(encoder.charset().name());
            request.setHandled(true);

            ServiceProxyImpl<?> serviceProxy = services.get(serviceName);
            Method method = findMethod(methodName, serviceProxy.interfaceClass);

            if (method != null) {
                try {
                    Object[] parameters = encoder.decode(jsonRequest, method.getGenericParameterTypes());
                    Object resultObj = method.invoke(serviceProxy.service(), parameters);
                    sendResult(resultObj, response);
                } catch (IllegalAccessException e) {
                    sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            e.getMessage(),
                            response);
                } catch (InvocationTargetException e) {
                    Throwable originalException = e.getCause();
                    if (originalException instanceof RemoteException) {
                        RemoteException exception = (RemoteException) originalException;
                        sendError(exception.getErrorCode(),
                                exception.getMessage(),
                                response);
                    } else {
                        sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                originalException.getMessage(),
                                response);
                    }
                }
            } else {
                sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Method " + methodName + " not found",
                        response);
            }

        }

        private void sendResult(Object resultObj, HttpServletResponse response) throws IOException {
            String result = encoder.encode(resultObj);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(result);
        }

        private void sendError(int errorCode, String message,
                               HttpServletResponse response) throws IOException {
            String result = encoder.encode(new ExceptionWrapper(message, ""));
            response.setStatus(errorCode);
            response.getWriter().println(result);
        }


        private Method findMethod(String methodName, Class<?> clazz) {
            for (Method method : clazz.getMethods()) {
                if (MethodName.normalize(method.getName()).equals(methodName) &&
                        Modifier.isPublic(method.getModifiers())) {
                    return method;
                }
            }
            return null;
        }


        private String readRequestBody(Request request) throws IOException {
            BufferedReader bodyReader = request.getReader();
            StringBuilder content = new StringBuilder();
            while (true) {
                String line = bodyReader.readLine();
                if (line == null) {
                    break;
                }
                content.append(line);
            }
            bodyReader.close();
            return content.toString();
        }


        public <T> ServiceProxy<T> registerService(final T service,
                                                   final String serviceName,
                                                   Class<T> interfaceClass) {

            checkNotRegistered(serviceName);

            try {
                final String address = InetAddress.getLocalHost().getHostAddress();

                ServiceProxyImpl<T> serviceProxy = new ServiceProxyImpl<>(new
                        ServiceAddress(address, broker.getPort(), serviceName),
                        service, interfaceClass);

                services.put(serviceName, serviceProxy);

                return serviceProxy;
            } catch (UnknownHostException shouldNeverHappen) {
                throw new RuntimeException(shouldNeverHappen);
            }
        }

        private void checkNotRegistered(String serviceName) {
            if (services.containsKey(serviceName)) {
                throw new ServiceAlreadyRegisteredException(String.format
                        ("A service named '%s' is already registered ",
                                serviceName));
            }
        }

    }
}
