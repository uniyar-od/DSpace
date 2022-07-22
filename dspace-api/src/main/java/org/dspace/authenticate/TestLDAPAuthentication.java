package org.dspace.authenticate;

import java.io.IOException;
import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsResponse;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class TestLDAPAuthentication
{
    public static void main(String[] args) throws ParseException
    {
        CommandLineParser parser = new PosixParser();

        Options options = new Options();
        options.addOption("u", "user", true,
                "the full DN");
        options.addOption("p", "password", true,
                "the password");
        options.addOption("l", "ldapurl", true,
                "ldap provider url");

        CommandLine line = parser.parse(options, args);
        String netid = line.getOptionValue("u");
        String password = line.getOptionValue("p");
        String ldapurl = line.getOptionValue("l");

        
        System.out.println("DN - " + netid );
        System.out.println("PASSWORD - " + password);
        System.out.println("LDAPURL - " + ldapurl);
        
        LdapContext ctx = null;
        StartTlsResponse startTLSResponse = null;
        
        
        // Set up environment for creating initial context
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(javax.naming.Context.PROVIDER_URL, ldapurl);

        try
        {
                // Authenticate
                env.put(javax.naming.Context.SECURITY_AUTHENTICATION, "Simple");
                env.put(javax.naming.Context.SECURITY_PRINCIPAL, netid);
                env.put(javax.naming.Context.SECURITY_CREDENTIALS, password);
                env.put(javax.naming.Context.AUTHORITATIVE, "true");
                env.put(javax.naming.Context.REFERRAL, "follow");

                // Try to bind
                ctx = new InitialLdapContext(env, null);
                System.out.println("OK - ldap_authentication type=success");
        }
        catch (NamingException e)
        {
            // something went wrong (like wrong password) so return false
            System.out.println("ERROR - ldap_authentication type=failed_auth " + e);
        } finally 
        {
            // Close the context when we're done
            try {
                if (startTLSResponse != null)
                {
                    startTLSResponse.close();
                }
                if (ctx != null)
                {
                    ctx.close();
                }
            } catch (NamingException | IOException e) {
            }
        }
        
    }

}
