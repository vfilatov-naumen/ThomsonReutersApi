package ru.naumen.thomsonreuters.client.main;

import ru.naumen.thomsonreuters.client.auth.*;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.naumen.thomsonreuters.client.main.ConfigHolder.cfg;

public abstract class Auth {

    private final WOKMWSAuthenticate service;
    private Map<String, List<String>> requestHeaders;

    public Auth() {
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(cfg.getString("auth.login"), cfg.getString("auth.password").toCharArray());
            }
        };
        Authenticator.setDefault(authenticator);
        service = new WOKMWSAuthenticateService().getWOKMWSAuthenticatePort();
        extractCookies();
        withAuth((BindingProvider)service);
    }

    public void closeSession() {
        try {
            service.closeSession();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected  <PORT extends BindingProvider> void withAuth(PORT port) {
        port.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, requestHeaders);
    }

    @SuppressWarnings("unchecked")
    private void extractCookies() {
        try {
            service.authenticate();
            Map<String, List<String>> headers = (Map<String, List<String>>)
                    ((BindingProvider) service).getResponseContext().get(MessageContext.HTTP_RESPONSE_HEADERS);
            List<String> cookies = headers.get("Set-Cookie");
            requestHeaders = new HashMap<>(1);
            requestHeaders.put("Cookie", cookies);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
