package com.pip.server.billing.security;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;


public class DefaultAuthenticator extends Authenticator
{

	private PasswordAuthentication authentication;


    public DefaultAuthenticator(String userName, String password)
    {
        this.authentication = new PasswordAuthentication(userName, password);
    }

    @Override
	protected PasswordAuthentication getPasswordAuthentication()
    {
        return this.authentication;
    }
}
