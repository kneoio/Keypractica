package com.semantyca.oidc;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import org.junit.jupiter.api.Test;

public class OIDCTest {


    @Test
    public void testLoginWithZitadel() throws Exception {
        try (final WebClient webClient = new WebClient()) {
            // Construct the authorization URL
            String authUrl = "http://localhost:8080/oauth/v2/authorize?response_type=code&client_id=232318575426207747@semantyca&redirect_uri=http://localhost:8100/workspace&scope=openid";

            // Use HtmlUnit to simulate navigating to the Zitadel login page
            final HtmlPage page = webClient.getPage(authUrl);

            // Simulate filling in the login form and submitting it
            // This will depend on the structure of your Zitadel login page
            final HtmlForm form = page.getFormByName("login_form");
            final HtmlSubmitInput button = form.getInputByName("submit_button");
            final HtmlTextInput usernameField = form.getInputByName("username");
            final HtmlPasswordInput passwordField = form.getInputByName("password");

            // Replace these with your test user's username and password
            usernameField.setValueAttribute("test_user");
            passwordField.setValueAttribute("test_password");

            // Submit the login form
            final HtmlPage resultPage = button.click();

            // Extract the authorization code from the redirect URL
            // This will depend on your redirect URI
            String redirectUrl = resultPage.getUrl().toString();
            String authorizationCode = extractAuthorizationCode(redirectUrl);

            // Use the authorization code to obtain an access token
            // This will depend on your application setup
            String accessToken = obtainAccessToken(authorizationCode);

            // Use the access token to make authenticated requests to your Quarkus server
            // This will depend on your testing setup
            makeAuthenticatedRequest(accessToken);
        }
    }

    private void makeAuthenticatedRequest(String accessToken) {
    }

    private String obtainAccessToken(String authorizationCode) {
        return null;
    }

    private String extractAuthorizationCode(String redirectUrl) {
        return null;
    }

}
