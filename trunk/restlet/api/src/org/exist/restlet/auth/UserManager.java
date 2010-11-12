/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exist.restlet.auth;

import org.exist.security.Subject;
import org.exist.security.realm.Realm;
import org.restlet.security.Verifier;

/**
 *
 * @author alex
 */
public interface UserManager extends Verifier {
   Subject authenticate(String username, String password);
   boolean isUserAllowedDatabaseAccess(String dbname,String username);
}
